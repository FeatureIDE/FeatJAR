/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j.slice;

import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanClause;
import de.featjar.formula.assignment.conversion.BooleanAssignmentListToVariables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Removes features from a model while retaining dependencies of all other
 * feature.
 *
 * @author Sebastian Krieter
 */
public class CNFSlicer extends AComputation<BooleanAssignmentList> {
    protected static final Dependency<BooleanAssignmentList> CNF =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<BooleanAssignment> VARIABLES_TO_KEEP =
            Dependency.newDependency(BooleanAssignment.class);
    public static final Dependency<BooleanAssignment> VARIABLES_TO_REMOVE =
            Dependency.newDependency(BooleanAssignment.class);

    protected static final Comparator<BooleanAssignment> lengthComparator =
            Comparator.comparing(BooleanAssignment::size);

    protected BooleanAssignmentList orgCNF;
    protected BooleanAssignmentList cnfCopy;

    protected final List<DirtyClause> newDirtyClauseList = new ArrayList<>();
    protected final List<DirtyClause> newCleanClauseList = new ArrayList<>();
    protected final List<DirtyClause> dirtyClauseList = new ArrayList<>();
    protected final ArrayList<BooleanClause> cleanLiteralListIndexList = new ArrayList<>();
    protected final Set<DirtyClause> dirtyClauseSet = new HashSet<>();
    protected final Set<DirtyClause> cleanClauseSet = new HashSet<>();

    protected BooleanAssignment dirtyVariables;
    private int numberOfDirtyFeatures = 0;

    protected int[] helper;
    protected DirtyFeature[] map;
    protected MinimumClauseHeuristic heuristic;
    private SAT4JSolutionSolver newSolver;

    private boolean first = false;

    protected int globalMixedClauseCount = 0;

    protected int dirtyListPosIndex = 0;
    protected int dirtyListNegIndex = 0;
    protected int newDirtyListDelIndex = 0;

    public CNFSlicer(IComputation<BooleanAssignmentList> clauseList) {
        super(
                clauseList,
                new BooleanAssignmentListToVariables(clauseList),
                new ComputeConstant<>(new BooleanAssignment()));
    }

    int cr = 0, cnr = 0, dr = 0, dnr = 0;

    @Override
    public Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        orgCNF = CNF.get(dependencyList);
        BooleanAssignment inlcude = VARIABLES_TO_KEEP.get(dependencyList);
        BooleanAssignment exclude = VARIABLES_TO_REMOVE.get(dependencyList);

        dirtyVariables = orgCNF.getVariableMap()
                .getVariables()
                .removeAll(
                        inlcude.removeAllVariables(exclude.getAbsoluteValues()).getAbsoluteValues());

        cnfCopy = new BooleanAssignmentList(orgCNF.getVariableMap());

        map = new DirtyFeature[orgCNF.getVariableMap().size() + 1];
        numberOfDirtyFeatures = 0;
        for (final int curFeature : dirtyVariables.get()) {
            map[curFeature] = new DirtyFeature(curFeature);
            numberOfDirtyFeatures++;
        }
        helper = new int[map.length];

        // Initialize lists and sets
        createClauseLists();

        if (!prepareHeuristics()) {
            return Result.of(new BooleanAssignmentList(orgCNF));
        }

        progress.setTotalSteps(heuristic.size());

        while (heuristic.hasNext()) {
            final DirtyFeature nextFeature = heuristic.next();
            if (nextFeature == null) {
                break;
            }

            // Remove redundant dirty clauses
            firstRedundancyCheck(nextFeature);

            // Partition dirty list into clauses that contain the current variable and
            // clauses that don't
            partitionDirtyList(nextFeature);

            // Remove variable & create transitive clauses
            resolution(nextFeature);

            // Remove redundant clauses
            detectRedundancy(nextFeature);

            // Merge new dirty list into the old list
            updateLists();

            progress.incrementCurrentStep();

            // If ALL dirty clauses exclusively consists of dirty features, they can just be
            // removed without applying resolution
            if (globalMixedClauseCount == 0) {
                break;
            }
        }

        addCleanClauses();

        release();
        //        final HashSet<String> names = new HashSet<>(orgCNF.getVariableNames());
        //        for (final int literal : dirtyVariables.getIntegers()) {
        //            names.remove(
        //                    orgCNF.getVariableMap().getVariableName(Math.abs(literal)).get());
        //        }
        //        final TermMap slicedTermMap = new TermMap(names);
        final List<BooleanClause> slicedLiteralListIndexList = cleanLiteralListIndexList.stream()
                //                .map(clause ->
                //                        clause.adapt(orgCNF.getVariableMap(), slicedTermMap).get())
                .collect(Collectors.toList());

        return Result.of(new BooleanAssignmentList(orgCNF.getVariableMap(), slicedLiteralListIndexList));
    }

    private void addNewClause(final DirtyClause curClause) {
        if (curClause != null) {
            if (curClause.computeRelevance(map)) {
                globalMixedClauseCount++;
            }
            if (curClause.getRelevance() == 0) {
                if (cleanClauseSet.add(curClause)) {
                    newCleanClauseList.add(curClause);
                } else {
                    deleteClause(curClause);
                }
            } else {
                if (dirtyClauseSet.add(curClause)) {
                    newDirtyClauseList.add(curClause);
                } else {
                    deleteClause(curClause);
                }
            }
        }
    }

    private void createClauseLists() {
        for (final BooleanAssignment sortedIntegerList : orgCNF) {
            addNewClause(DirtyClause.createClause(sortedIntegerList.get()));
        }

        cleanLiteralListIndexList.ensureCapacity(cleanLiteralListIndexList.size() + newCleanClauseList.size());
        for (final DirtyClause dirtyClause : newCleanClauseList) {
            cleanLiteralListIndexList.add(new BooleanClause(dirtyClause));
        }
        dirtyClauseList.addAll(newDirtyClauseList);
        newDirtyClauseList.clear();
        newCleanClauseList.clear();

        dirtyListPosIndex = dirtyClauseList.size();
        dirtyListNegIndex = dirtyClauseList.size();
    }

    protected final void deleteClause(final DirtyClause curClause) {
        if (curClause.delete(map)) {
            globalMixedClauseCount--;
        }
    }

    protected final void deleteOldDirtyClauses() {
        if (dirtyListPosIndex < dirtyClauseList.size()) {
            final List<DirtyClause> subList = dirtyClauseList.subList(dirtyListPosIndex, dirtyClauseList.size());
            dirtyClauseSet.removeAll(subList);
            for (final DirtyClause dirtyClause : subList) {
                deleteClause(dirtyClause);
            }
            subList.clear();
        }
    }

    protected final void deleteNewDirtyClauses() {
        if (newDirtyListDelIndex < newDirtyClauseList.size()) {
            final List<DirtyClause> subList =
                    newDirtyClauseList.subList(newDirtyListDelIndex, newDirtyClauseList.size());
            dirtyClauseSet.removeAll(subList);
            for (final DirtyClause dirtyClause : subList) {
                deleteClause(dirtyClause);
            }
        }
    }

    private void resolution(DirtyFeature nextFeature) {
        final int curFeatureID = nextFeature.getId();
        for (int i = dirtyListPosIndex; i < dirtyListNegIndex; i++) {
            final int[] posOrChildren = dirtyClauseList.get(i).get();
            for (int j = dirtyListNegIndex; j < dirtyClauseList.size(); j++) {
                final int[] negOrChildren = dirtyClauseList.get(j).get();
                final int[] newChildren = new int[posOrChildren.length + negOrChildren.length];

                System.arraycopy(posOrChildren, 0, newChildren, 0, posOrChildren.length);
                System.arraycopy(negOrChildren, 0, newChildren, posOrChildren.length, negOrChildren.length);

                addNewClause(DirtyClause.createClause(newChildren, curFeatureID, helper));
            }
        }
        newDirtyListDelIndex = newDirtyClauseList.size();
    }

    private void partitionDirtyList(DirtyFeature nextFeature) {
        final int curFeatureID = nextFeature.getId();
        for (int i = 0; i < dirtyListNegIndex; i++) {
            final BooleanClause sortedIntegerList = dirtyClauseList.get(i);
            for (final int literal : sortedIntegerList.get()) {
                if (literal == -curFeatureID) {
                    Collections.swap(dirtyClauseList, i--, --dirtyListNegIndex);
                    break;
                }
            }
        }
        dirtyListPosIndex = dirtyListNegIndex;
        for (int i = 0; i < dirtyListPosIndex; i++) {
            final BooleanClause sortedIntegerList = dirtyClauseList.get(i);
            for (final int literal : sortedIntegerList.get()) {
                if (literal == curFeatureID) {
                    Collections.swap(dirtyClauseList, i--, --dirtyListPosIndex);
                    break;
                }
            }
        }
    }

    private void updateLists() {
        // delete old & redundant dirty clauses
        deleteOldDirtyClauses();

        // delete new & redundant dirty clauses
        deleteNewDirtyClauses();

        dirtyClauseList.addAll(newDirtyClauseList.subList(0, newDirtyListDelIndex));
        newDirtyClauseList.clear();

        dirtyListPosIndex = dirtyClauseList.size();
        dirtyListNegIndex = dirtyClauseList.size();
        newDirtyListDelIndex = 0;
    }

    protected final boolean isRedundant(SAT4JSolutionSolver solver, BooleanClause clause) {
        return solver.hasSolution(clause.negateInts()).valueEquals(Boolean.FALSE);
    }

    protected void detectRedundancy(DirtyFeature nextFeature) {
        if (nextFeature.getClauseCount() > 0) {
            addCleanClauses();

            final SAT4JSolutionSolver solver = new SAT4JSolutionSolver(cnfCopy);
            solver.getClauseList().addAll(cleanLiteralListIndexList);
            solver.getClauseList().addAll(dirtyClauseList.subList(0, dirtyListPosIndex));

            newDirtyClauseList.subList(0, newDirtyListDelIndex).sort(lengthComparator);
            for (int i = newDirtyListDelIndex - 1; i >= 0; --i) {
                final DirtyClause curClause = newDirtyClauseList.get(i);
                if (isRedundant(solver, curClause)) {
                    dr++;
                    Collections.swap(newDirtyClauseList, i, --newDirtyListDelIndex);
                } else {
                    dnr++;
                    solver.getClauseList().add(curClause);
                }
            }
        }
    }

    protected void addCleanClauses() {
        newCleanClauseList.sort(lengthComparator);

        for (int i = newCleanClauseList.size() - 1; i >= 0; --i) {
            final DirtyClause clause = newCleanClauseList.get(i);

            if (isRedundant(newSolver, clause)) {
                cr++;
                deleteClause(clause);
            } else {
                cnr++;
                newSolver.getClauseList().add(clause);
                cleanLiteralListIndexList.add(new BooleanClause(clause));
            }
        }
        newCleanClauseList.clear();
    }

    protected void firstRedundancyCheck(DirtyFeature nextFeature) {
        if (first && (nextFeature.getClauseCount() > 0)) {
            first = false;
            Collections.sort(dirtyClauseList.subList(0, dirtyListPosIndex), lengthComparator);

            addCleanClauses();

            final SAT4JSolutionSolver solver = new SAT4JSolutionSolver(cnfCopy);
            solver.getClauseList().addAll(cleanLiteralListIndexList);

            // SAT Relevant
            for (int i = dirtyListPosIndex - 1; i >= 0; --i) {
                final DirtyClause mainClause = dirtyClauseList.get(i);
                if (isRedundant(solver, mainClause)) {
                    dr++;
                    Collections.swap(dirtyClauseList, i, --dirtyListPosIndex);
                } else {
                    dnr++;
                    solver.getClauseList().add(mainClause);
                }
            }
            deleteOldDirtyClauses();

            dirtyListPosIndex = dirtyClauseList.size();
            dirtyListNegIndex = dirtyClauseList.size();
            cr = 0;
            cnr = 0;
            dr = 0;
            dnr = 0;
        }
    }

    protected boolean prepareHeuristics() {
        heuristic = new MinimumClauseHeuristic(map, numberOfDirtyFeatures);
        first = true;
        newSolver = new SAT4JSolutionSolver(cnfCopy);
        return newSolver.hasSolution().valueEquals(Boolean.TRUE);
    }

    protected void release() {
        newDirtyClauseList.clear();
        newCleanClauseList.clear();
        dirtyClauseSet.clear();
        cleanClauseSet.clear();
        dirtyClauseList.clear();

        //        if (newSolver != null) {
        //            newSolver.reset();
        //        }
    }
}
