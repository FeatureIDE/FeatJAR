/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.configuration.computation;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.feature.configuration.Configuration;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts a {@link BooleanAssignmentList} to a list of {@link Configuration configurations}.
 *
 * @author Sebastian Krieter
 */
public class ComputeConfigurationFromAssignment extends AComputation<List<Configuration>> {

    public static final Dependency<BooleanAssignmentList> ASSIGNMENTS =
            Dependency.newDependency(BooleanAssignmentList.class);

    public ComputeConfigurationFromAssignment(IComputation<BooleanAssignmentList> assignments) {
        super(assignments);
    }

    protected ComputeConfigurationFromAssignment(ComputeConfigurationFromAssignment other) {
        super(other);
    }

    @Override
    public Result<List<Configuration>> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList assignments = ASSIGNMENTS.get(dependencyList);
        VariableMap variableMap = assignments.getVariableMap();
        return Result.of(
                assignments.stream().map(a -> new Configuration(a, variableMap)).collect(Collectors.toList()));
    }
}
