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
 * A value clause; that is, a disjunction of equalities.
 * Often used as input to an SMT {@link ISolver}.
 *
 * @author Elias Kuiter
 */
public class ValueClause extends ValueAssignment implements IClause<Integer, Object> {
    public ValueClause() {}

    public ValueClause(LinkedHashMap<Integer, Object> variableValuePairs) {
        super(variableValuePairs);
    }

    public ValueClause(ValueClause predicateClause) {
        this(new LinkedHashMap<>(predicateClause.variableValuePairs));
    }

    public ValueClause(Object... variableValuePairs) {
        super(variableValuePairs);
    }

    @Override
    public Result<BooleanClause> toBoolean() {
        return VariableMap.toBoolean(this);
    }

    @Override
    public String toString() {
        return String.format("ValueClause[%s]", print());
    }
}
