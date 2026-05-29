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
package de.featjar.formula.combination;

import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.Variables;

public class IndependentVariablesFilter implements ICombinationFilter {

    private final VariableMap variableMap;
    private final Variables independentVariables;

    public IndependentVariablesFilter(VariableMap variableMap, Variables independentVariables) {
        this.variableMap = variableMap;
        this.independentVariables = independentVariables;
    }

    private IndependentVariablesFilter(IndependentVariablesFilter filter, VariableMap variableMap) {
        this.variableMap = variableMap;
        this.independentVariables = filter.independentVariables.remap(filter.variableMap, variableMap);
    }

    @Override
    public boolean test(int... literals) {
        return independentVariables.containsAllLiterals(literals);
    }

    @Override
    public IndependentVariablesFilter remap(VariableMap variableMap) {
        return new IndependentVariablesFilter(this, variableMap);
    }
}
