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
package de.featjar.formula.assignment.conversion;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.Variables;
import java.util.List;

/**
 * Transforms a {@link BooleanAssignmentList} into a {@link VariableMap}.
 *
 * @author Sebastian Krieter
 */
public class VariableMapToVariables extends AComputation<Variables> {

    public static final Dependency<VariableMap> VARIABLE_MAP = Dependency.newDependency(VariableMap.class);

    public static final Dependency<IVariableNameFilter> INCLUDE = Dependency.newDependency(IVariableNameFilter.class);
    public static final Dependency<IVariableNameFilter> EXCLUDE = Dependency.newDependency(IVariableNameFilter.class);

    public VariableMapToVariables(IComputation<VariableMap> variableMap) {
        super(
                variableMap,
                Computations.of(IVariableNameFilter.of(true)),
                Computations.of(IVariableNameFilter.of(false)));
    }

    protected VariableMapToVariables(VariableMapToVariables other) {
        super(other);
    }

    @Override
    public Result<Variables> compute(List<Object> dependencyList, Progress progress) {
        final IVariableNameFilter include = INCLUDE.get(dependencyList);
        final IVariableNameFilter exclude = EXCLUDE.get(dependencyList);
        return Result.of(new Variables(VARIABLE_MAP.get(dependencyList).stream()
                .filter(p -> include.test(p.getValue()))
                .filter(p -> !exclude.test(p.getValue()))
                .mapToInt(p -> p.getKey())
                .toArray()));
    }
}
