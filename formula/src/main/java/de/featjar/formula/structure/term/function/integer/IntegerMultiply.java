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
package de.featjar.formula.structure.term.function.integer;

import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.function.AMultiply;
import de.featjar.formula.structure.term.function.IFunction;
import java.util.List;
import java.util.Optional;

/**
 * Multiplies the values of two integer terms.
 *
 * @author Sebastian Krieter
 */
public class IntegerMultiply extends AMultiply {

    protected IntegerMultiply() {}

    public IntegerMultiply(ITerm leftTerm, ITerm rightTerm) {
        super(leftTerm, rightTerm);
    }

    public IntegerMultiply(List<ITerm> arguments) {
        super(arguments);
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }

    @Override
    public Class<Long> getChildrenType() {
        return Long.class;
    }

    @Override
    public Optional<Long> evaluate(List<?> values) {
        return Optional.ofNullable(IFunction.reduce(values, (a, b) -> a * b));
    }

    @Override
    public IntegerMultiply cloneNode() {
        return new IntegerMultiply();
    }
}
