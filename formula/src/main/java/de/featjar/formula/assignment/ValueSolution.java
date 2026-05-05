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
package de.featjar.formula.assignment;

import de.featjar.analysis.ISolver;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import java.util.LinkedHashMap;

/**
 * A (partial) value solution; that is, a conjunction of equalities.
 * Often holds output of an SMT {@link ISolver}.
 *
 * @author Elias Kuiter
 */
public class ValueSolution extends ValueAssignment implements ISolution<Integer, Object> {
    public ValueSolution() {}

    public ValueSolution(LinkedHashMap<Integer, Object> variableValuePairs) {
        super(variableValuePairs);
    }

    public ValueSolution(ValueClause predicateClause) {
        this(new LinkedHashMap<>(predicateClause.variableValuePairs));
    }

    public ValueSolution(Object... variableValuePairs) {
        super(variableValuePairs);
    }

    @Override
    public Result<BooleanSolution> toBoolean() {
        return VariableMap.toBoolean(this);
    }

    @Override
    public String toString() {
        return String.format("ValueSolution[%s]", print());
    }
}
