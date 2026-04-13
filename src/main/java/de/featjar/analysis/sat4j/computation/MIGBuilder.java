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

import de.featjar.analysis.RuntimeContradictionException;
import de.featjar.analysis.sat4j.solver.ModalImplicationGraph;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.ExpandableIntegerList;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanClause;
import de.featjar.formula.assignment.conversion.BooleanAssignmentListToBooleanAssignment;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Adjacency matrix implementation for a feature graph.
 *
 * @author Sebastian Krieter
 */
public class MIGBuilder extends AComputation<ModalImplicationGraph> {

    public static final Dependency<BooleanAssignmentList> CNF_CLAUSES =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<BooleanAssignment> CORE = Dependency.newDependency(BooleanAssignment.class);

    public MIGBuilder(IComputation<BooleanAssignmentList> cnfFormula) {
        super(cnfFormula, new ComputeCoreSAT4J(cnfFormula).map(BooleanAssignmentListToBooleanAssignment::new));
    }

    protected MIGBuilder(MIGBuilder other) {
        super(other);
    }

    @Override
    public Result<ModalImplicationGraph> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList cnfFormula = CNF_CLAUSES.get(dependencyList);
        BooleanAssignment coreLiterals = CORE.get(dependencyList);

        progress.setTotalSteps(8);

        if (coreLiterals == null) {
            throw new RuntimeContradictionException("CNF is not satisfiable!");
        }

        final int size = cnfFormula.getVariableMap().size();

        progress.incrementCurrentStep();

        ExpandableIntegerList[] tempStrong = new ExpandableIntegerList[2 * size];
        List<BooleanClause> cleanedClausesList = new ArrayList<>(cnfFormula.size());
        cnfFormula.stream()
                .map(c -> cleanClause(c, coreLiterals))
                .filter(Objects::nonNull)
                .forEach(cleanedClausesList::add);

        final int[][] strong = new int[2 * size][];
        final int[] weakCount = new int[2 * size];
        final int[][] clauseIndices = new int[2 * size][];
        final int[][] clauseLengthIndices = new int[2 * size][];
        final int[] core = Arrays.copyOf(coreLiterals.get(), coreLiterals.get().length);

        int clausesSize = 0;
        int clauseLengthSize = 0;
        for (BooleanClause clause : cleanedClausesList) {
            final int clauseSize = clause.size();
            if (clauseSize > 2) {
                clausesSize += clauseSize;
                clauseLengthSize++;
                for (int l : clause.get()) {
                    weakCount[ModalImplicationGraph.getVertexIndex(-l)]++;
                }
            }
        }

        final int[] clauses = new int[clausesSize];
        final int[] clauseLengths = new int[clauseLengthSize];

        for (int i = 0; i < weakCount.length; i++) {
            final int c = weakCount[i];
            clauseIndices[i] = new int[c];
            clauseLengthIndices[i] = new int[c];
            tempStrong[i] = new ExpandableIntegerList();
        }

        int clausesI = 0;
        int clauseCountI = 0;
        for (BooleanClause clause : cleanedClausesList) {
            if (clause.size() == 2) {
                final int i = clause.get()[0];
                final int j = clause.get()[1];
                tempStrong[ModalImplicationGraph.getVertexIndex(-i)].add(j);
                tempStrong[ModalImplicationGraph.getVertexIndex(-j)].add(i);
            } else if (clause.size() > 2) {
                int startClausesI = clausesI;
                int[] literals = clause.get();
                for (int i = 0; i < literals.length; i++) {
                    final int l = literals[i];
                    clauses[clausesI++] = l;
                    int vertexIndex = ModalImplicationGraph.getVertexIndex(-l);
                    int[] clauseCountIndexList = clauseLengthIndices[vertexIndex];
                    int k = 0;
                    for (; k < clauseCountIndexList.length; k++) {
                        if (clauseCountIndexList[k] == 0) {
                            clauseCountIndexList[k] = clauseCountI;
                            break;
                        }
                    }
                    try {
                        clauseIndices[vertexIndex][k] = startClausesI;
                    } catch (RuntimeException e) {
                        throw new RuntimeException(clause.toString(), e);
                    }
                }
                clauseLengths[clauseCountI++] = clause.size();
            }
        }

        progress.incrementCurrentStep();

        for (int i = 1; i <= size; i++) {
            bfsStrong(i, size, strong, tempStrong);
            bfsStrong(-i, size, strong, tempStrong);
        }
        progress.incrementCurrentStep();

        ModalImplicationGraph migVisitorProvider = new ModalImplicationGraph(
                size, core, strong, clauseIndices, clauses, clauseLengthIndices, clauseLengths);
        return Result.of(migVisitorProvider);
    }

    private void bfsStrong(int literal, int size, int[][] strong, ExpandableIntegerList[] tempStrong) {
        int vertexIndex = ModalImplicationGraph.getVertexIndex(literal);
        ExpandableIntegerList temp = tempStrong[vertexIndex];
        final ArrayDeque<Integer> queue = new ArrayDeque<>();
        final boolean[] mark = new boolean[size + 1];
        mark[Math.abs(literal)] = true;
        temp.toIntStream().forEach(v -> {
            mark[Math.abs(v)] = true;
            queue.add(v);
        });
        while (!queue.isEmpty()) {
            final Integer curVertex = queue.removeFirst();
            tempStrong[ModalImplicationGraph.getVertexIndex(curVertex)]
                    .toIntStream()
                    .forEach(v -> {
                        final int index = Math.abs(v);
                        if (!mark[index]) {
                            mark[index] = true;
                            queue.add(v);
                            temp.add(v);
                        }
                    });
        }
        strong[vertexIndex] = temp.toArray();
    }

    private BooleanClause cleanClause(BooleanAssignment clause, BooleanAssignment core) {
        final int[] literals = clause.get();
        final LinkedHashSet<Integer> literalSet = new LinkedHashSet<>(literals.length << 1);

        for (int var : literals) {
            if (core.indexOf(var) >= 0) {
                return null;
            } else if (core.indexOf(-var) < 0) {
                if (literalSet.contains(-var)) {
                    return null;
                } else {
                    literalSet.add(var);
                }
            }
        }
        switch (literalSet.size()) {
            case 0:
                throw new RuntimeContradictionException();
            case 1:
                return null;
            default:
                final int[] literalArray = new int[literalSet.size()];
                int i = 0;
                for (final int lit : literalSet) {
                    literalArray[i++] = lit;
                }
                return new BooleanClause(literalArray);
        }
    }
}
