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
package de.featjar.formula.structure.term.function.real;

import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.function.ADivide;
import de.featjar.formula.structure.term.function.IFunction;
import java.util.List;
import java.util.Optional;

/**
 * Divides the values of two real terms.
 *
 * @author Sebastian Krieter
 */
public class RealDivide extends ADivide {

    protected RealDivide() {}

    public RealDivide(ITerm leftTerm, ITerm rightTerm) {
        super(leftTerm, rightTerm);
    }

    public RealDivide(List<ITerm> arguments) {
        super(arguments);
    }

    @Override
    public Class<Double> getType() {
        return Double.class;
    }

    @Override
    public Class<Double> getChildrenType() {
        return Double.class;
    }

    @Override
    public Optional<Double> evaluate(List<?> values) {
        return Optional.ofNullable(IFunction.reduce(values, (a, b) -> a / b));
    }

    @Override
    public RealDivide cloneNode() {
        return new RealDivide();
    }
}
