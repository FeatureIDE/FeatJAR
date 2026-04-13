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
package de.featjar.analysis.sat4j.computation;

import de.featjar.analysis.RuntimeTimeoutException;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.ExpandableIntegerList;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * Finds atomic sets.
 *
 * @author Sebastian Krieter
 */
public class ComputeAtomicSetsSAT4J extends ASAT4JAnalysis.Solution<BooleanAssignmentList> {

    public static final Dependency<BooleanAssignment> VARIABLES_OF_INTEREST =
            Dependency.newDependency(BooleanAssignment.class);
    public static final Dependency<Boolean> OMIT_SINGLE_SETS = Dependency.newDependency(Boolean.class);
    public static final Dependency<Boolean> OMIT_CORE = Dependency.newDependency(Boolean.class);
    public static final Dependency<Boolean> OMIT_COMPLEMENTS = Dependency.newDependency(Boolean.class);

    private List<BitSet> solutions;
    private int variableCount, bitSetSize;

    private Random random;

    public ComputeAtomicSetsSAT4J(IComputation<BooleanAssignmentList> clauseList) {
        super(
                clauseList,
                Computations.of(new BooleanAssignment()),
                Computations.of(Boolean.FALSE),
                Computations.of(Boolean.FALSE),
                Computations.of(Boolean.FALSE));
    }

    protected ComputeAtomicSetsSAT4J(ComputeAtomicSetsSAT4J other) {
        super(other);
    }

    @Override
    protected SAT4JSolutionSolver newSolver(BooleanAssignmentList clauseList) {
        return new SAT4JSolutionSolver(clauseList, true);
    }

    @Override
    public Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        SAT4JSolutionSolver solver = createSolver(dependencyList);
        random = new Random(RANDOM_SEED.get(dependencyList));
        VariableMap variableMap = BOOLEAN_CLAUSE_LIST.get(dependencyList).getVariableMap();

        boolean omitCore = OMIT_CORE.get(dependencyList);
        boolean omitSingles = OMIT_SINGLE_SETS.get(dependencyList);
        boolean omitComplements = OMIT_COMPLEMENTS.get(dependencyList);

        final BooleanAssignmentList atomicSets = new BooleanAssignmentList(variableMap);
        variableCount = variableMap.size();
        bitSetSize = 2 * variableCount;
        solutions = new ArrayList<>();

        BooleanAssignment variables = VARIABLES_OF_INTEREST.get(dependencyList);
        final BitSet undecided = new BitSet(bitSetSize);
        if (variables.isEmpty()) {
            undecided.flip(0, bitSetSize);
        } else {
            for (int var : variables.get()) {
                undecided.set(var - 1);
                undecided.set((var - 1) + variableCount);
            }
        }
        checkCancel();
        progress.setTotalSteps(2 * variableCount + 2);

        solver.setSelectionStrategy(ISelectionStrategy.positive());
        Result<Boolean> hasSolution = solver.hasSolution();
        if (hasSolution.isEmpty()) {
            return hasSolution.nullify();
        }
        progress.incrementCurrentStep();
        checkCancel();

        BitSet commonLiterals = new BitSet(bitSetSize);
        commonLiterals.xor(addSolution(solver.getInternalSolution(), 0));

        solver.setSelectionStrategy(
                ISelectionStrategy.inverse(Arrays.copyOf(solver.getInternalSolution(), variableCount)));
        if (solver.hasSolution().valueEquals(Boolean.TRUE)) {
            commonLiterals.and(addSolution(solver.getInternalSolution(), 0));
            solver.shuffleOrder(random);
        } else {
            throw new RuntimeTimeoutException();
        }

        progress.incrementCurrentStep();
        checkCancel();

        solver.setSelectionStrategy(ISelectionStrategy.random(random));
        int log = (8 * Integer.BYTES) - Integer.numberOfLeadingZeros(variableCount);
        for (int i = 0; i < log; i++) {
            if (solver.hasSolution().valueEquals(Boolean.TRUE)) {
                commonLiterals.and(addSolution(solver.getInternalSolution(), 0));
                solver.shuffleOrder(random);
            } else {
                throw new RuntimeTimeoutException();
            }
        }

        commonLiterals.and(undecided);

        ExpandableIntegerList core = new ExpandableIntegerList();
        for (int i = 0; i < bitSetSize; i += 2) {
            progress.incrementCurrentStep();
            checkCancel();
            final int potentialCoreLiteral;
            if (commonLiterals.get(i)) {
                potentialCoreLiteral = (i >> 1) + 1;
            } else if (commonLiterals.get(i + 1)) {
                potentialCoreLiteral = -((i >> 1) + 1);
            } else {
                continue;
            }
            solver.getAssignment().add(-potentialCoreLiteral);
            hasSolution = solver.hasSolution();
            if (hasSolution.isEmpty()) {
                throw new RuntimeTimeoutException();
            } else if (hasSolution.valueEquals(Boolean.FALSE)) {
                undecided.clear(i);
                undecided.clear(i + 1);
                core.add(potentialCoreLiteral);
                solver.getClauseList().add(potentialCoreLiteral);
            } else if (hasSolution.valueEquals(Boolean.TRUE)) {
                commonLiterals.and(addSolution(solver.getInternalSolution(), 0));
                solver.shuffleOrder(random);
            }
            solver.getAssignment().remove();
        }
        if (!omitCore) {
            atomicSets.add(new BooleanAssignment(core.toArray()));
        }
        for (int vi = 0; vi < bitSetSize; vi += 2) {
            progress.incrementCurrentStep();
            checkCancel();
            if (undecided.get(vi)) {
                int v = (vi >> 1) + 1;

                ExpandableIntegerList atomicSet = new ExpandableIntegerList();
                atomicSet.add(v);
                undecided.clear(vi);
                undecided.clear(vi + 1);

                commonLiterals = new BitSet(bitSetSize);
                commonLiterals.xor(undecided);

                for (BitSet solution : solutions) {
                    if (solution.get(vi)) {
                        commonLiterals.and(solution);
                    } else {
                        commonLiterals.andNot(solution);
                    }
                    if (commonLiterals.isEmpty()) {
                        break;
                    }
                }

                int ui = vi;
                while ((ui = commonLiterals.nextSetBit(ui + 2)) >= 0) {
                    final int u;
                    if (((ui & 1) == 0)) {
                        u = (ui >> 1) + 1;
                    } else {
                        if (omitComplements) {
                            ui--;
                            continue;
                        } else {
                            u = -((ui >> 1) + 1);
                            ui--;
                        }
                    }
                    if (unsat(solver, -v, u) && unsat(solver, v, -u)) {
                        atomicSet.add(u);
                        undecided.clear(ui);
                        undecided.clear(ui + 1);
                    }
                }

                if (!omitSingles || atomicSet.size() > 1) {
                    atomicSets.add(new BooleanAssignment(atomicSet.toArray()));
                }
            }
        }

        solutions = null;
        random = null;
        return Result.of(atomicSets);
    }

    private boolean unsat(SAT4JSolutionSolver solver, final int v, int u) {
        solver.getAssignment().add(v);
        solver.getAssignment().add(u);
        try {
            Result<Boolean> hasSolution = solver.hasSolution();
            if (hasSolution.isEmpty()) {
                return false;
            } else if (hasSolution.valueEquals(Boolean.TRUE)) {
                addSolution(solver.getInternalSolution(), Math.abs(v));
                solver.shuffleOrder(random);
                return false;
            }
            return true;
        } finally {
            solver.getAssignment().remove();
            solver.getAssignment().remove();
        }
    }

    private BitSet addSolution(final int[] solution, int min) {
        BitSet bitSetSolution = new BitSet(bitSetSize);
        solutions.add(bitSetSolution);
        for (int i = min; i < variableCount; i++) {
            boolean b = solution[i] > 0;
            bitSetSolution.set(i << 1, b);
            bitSetSolution.set((i << 1) + 1, !b);
        }
        return bitSetSolution;
    }
}
