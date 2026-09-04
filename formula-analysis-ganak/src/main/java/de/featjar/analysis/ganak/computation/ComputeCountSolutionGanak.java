/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ganak.
 *
 * formula-analysis-ganak is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ganak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ganak. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-ganak> for further information.
 */
package de.featjar.analysis.ganak.computation;

import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.math.BigInteger;
import java.util.List;

/**
 * An analysis for computing the number of satisfying assignments of a formula, optionally with
 * projection or slicing, using the Ganak Solver. It uses the default probabilistic count of the
 * Ganak Solver.
 */
public class ComputeCountSolutionGanak extends AGanakAnalysis<BigInteger> {
    /**
     * Literals to be removed
     */
    public static final Dependency<BooleanAssignment> VARIABLES_TO_REMOVE =
            Dependency.newDependency(BooleanAssignment.class);

    /**
     * Literals to be kept
     */
    public static final Dependency<BooleanAssignment> VARIABLES_TO_KEEP =
            Dependency.newDependency(BooleanAssignment.class);

    public ComputeCountSolutionGanak(IComputation<BooleanAssignmentList> cnfFormula) {
        super(
                cnfFormula,
                new ComputeConstant<>(new BooleanAssignment()),
                new ComputeConstant<>(new BooleanAssignment()));
    }

    public ComputeCountSolutionGanak(ComputeCountSolutionGanak other) {
        super(other);
    }

    @Override
    public Result<BigInteger> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignment variablesToRemove = VARIABLES_TO_REMOVE.get(dependencyList);
        BooleanAssignment variablesToKeep = VARIABLES_TO_KEEP.get(dependencyList);

        // no variables specified -> normal model counting
        if (variablesToKeep.isEmpty() && variablesToRemove.isEmpty()) {
            return initializeSolver(dependencyList).countSolution();
        }

        // keep variablesToKeep without variablesToRemove
        BooleanAssignment include = variablesToKeep.isEmpty()
                ? FORMULA.get(dependencyList).getVariableMap().getVariables().toAssignment()
                : variablesToKeep;
        include = include.removeAll(variablesToRemove);

        return initializeSolver(dependencyList).countSolution(include);
    }
}
