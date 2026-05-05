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
package de.featjar.formula.structure.term.function.string;

import de.featjar.formula.structure.ANonTerminalExpression;
import de.featjar.formula.structure.IUnaryExpression;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.function.IFunction;
import java.util.List;
import java.util.Optional;

public class StringLength extends ANonTerminalExpression implements IFunction, IUnaryExpression {

    protected StringLength() {}

    public StringLength(ITerm variable) {
        super(variable);
    }

    @Override
    public String getName() {
        return "len";
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }

    @Override
    public Class<String> getChildrenType() {
        return String.class;
    }

    @Override
    public Optional<Integer> evaluate(List<?> values) {
        return getChildren().get(0).evaluate(values).map(v -> ((String) v).length());
    }

    @Override
    public StringLength cloneNode() {
        return new StringLength();
    }
}
