/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.computation;

import de.featjar.base.computation.*;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Removes variables from a sample.
 *
 * @author Sebastian Krieter
 */
public class ComputeProjectedSample extends AComputation<BooleanAssignmentList> {

    public static final Dependency<BooleanAssignmentList> SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<BooleanAssignment> INCLUDE_VARIABLES =
            Dependency.newDependency(BooleanAssignment.class);
    public static final Dependency<BooleanAssignment> EXCLUDE_VARIABLES =
            Dependency.newDependency(BooleanAssignment.class);
    /**
     * Whether to change the index in the variable map. Default: {@code false}
     * <ul>
     * <li>If {@code true} the indices of the variables may change, such that the variable map only contains the remaining variables with no gaps.</li>
     * <li>If {@code false} all indices of the variables stay as they are.</li>
     * </ul>
     */
    public static final Dependency<Boolean> REMAP_VARIABLES = Dependency.newDependency(Boolean.class);

    public ComputeProjectedSample(IComputation<BooleanAssignmentList> sample) {
        super(
                sample,
                sample.map(BooleanAssignment.VariablesComputation::new),
                Computations.of(new BooleanAssignment()),
                Computations.of(Boolean.FALSE));
    }

    @Override
    public final Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList sample = SAMPLE.get(dependencyList);
        BooleanAssignment includeVariables = INCLUDE_VARIABLES.get(dependencyList);
        BooleanAssignment excludeVariables = EXCLUDE_VARIABLES.get(dependencyList);
        BooleanAssignment projectedVariables = includeVariables.removeAllVariables(excludeVariables);

        Stream<BooleanAssignment> projectedSample =
                sample.stream().map(assignment -> assignment.retainAllVariables(projectedVariables));

        VariableMap newVariableMap = sample.getVariableMap().clone();
        if (REMAP_VARIABLES.get(dependencyList)) {
            BooleanAssignment removalVariables = newVariableMap.getVariables().removeAllVariables(projectedVariables);
            for (int variable : removalVariables.get()) {
                newVariableMap.remove(variable);
            }
            newVariableMap.normalize();
        }
        return Result.of(new BooleanAssignmentList(newVariableMap, projectedSample));
    }
}
