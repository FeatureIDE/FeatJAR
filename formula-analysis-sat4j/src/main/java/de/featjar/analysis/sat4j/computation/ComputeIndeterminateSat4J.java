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

import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.ExpandableIntegerList;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanClause;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Finds indeterminate features.
 *
 * @author Sebastian Krieter
 */
public class ComputeIndeterminateSat4J extends ASAT4JAnalysis.Solution<BooleanAssignment> {

    protected static final Dependency<BooleanAssignment> VARIABLES_OF_INTEREST =
            Dependency.newDependency(BooleanAssignment.class);

    public ComputeIndeterminateSat4J(IComputation<BooleanAssignmentList> clauseList) {
        super(clauseList, new ComputeConstant<>(new BooleanAssignment()));
    }

    protected ComputeIndeterminateSat4J(ComputeIndeterminateSat4J other) {
        super(other);
    }

    @Override
    public Result<BooleanAssignment> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList clauseList = BOOLEAN_CLAUSE_LIST.get(dependencyList);
        BooleanAssignment variablesOfInterest = VARIABLES_OF_INTEREST.get(dependencyList);

        BooleanAssignment variables = variablesOfInterest.isEmpty()
                ? new BooleanAssignment(
                        IntStream.rangeClosed(1, clauseList.getVariableMap().size())
                                .toArray())
                : variablesOfInterest;

        final ExpandableIntegerList resultList = new ExpandableIntegerList();
        variableLoop:
        for (final int variable : variables.get()) {
            BooleanAssignmentList modClauseList = new BooleanAssignmentList(clauseList.getVariableMap());
            for (final BooleanAssignment clause : clauseList) {
                final int[] newLiterals = clause.removeAllVariablesInts(variable);
                if (newLiterals.length > 0) {
                    modClauseList.add(new BooleanClause(newLiterals));
                } else {
                    continue variableLoop;
                }
            }
            final SAT4JSolutionSolver modSolver = new SAT4JSolutionSolver(modClauseList);

            final Result<Boolean> hasSolution = modSolver.hasSolution();
            if (hasSolution.valueEquals(Boolean.FALSE)) {
            } else if (hasSolution.isEmpty()) {
                // reportTimeout();
            } else if (hasSolution.valueEquals(Boolean.TRUE)) {
                resultList.add(variable);
            } else {
                throw new AssertionError(hasSolution);
            }
        }
        return Result.of(new BooleanAssignment(resultList.toIntStream().toArray()));
    }
}
