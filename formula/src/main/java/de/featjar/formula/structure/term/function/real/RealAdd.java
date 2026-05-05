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
import de.featjar.formula.structure.term.function.AAdd;
import de.featjar.formula.structure.term.function.IFunction;
import java.util.List;
import java.util.Optional;

/**
 * Adds the values of multiple real terms.
 *
 * @author Sebastian Krieter
 */
public class RealAdd extends AAdd {

    protected RealAdd() {}

    public RealAdd(ITerm... terms) {
        super(terms);
    }

    public RealAdd(List<ITerm> arguments) {
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
        return Optional.ofNullable(IFunction.reduce(values, Double::sum));
    }

    @Override
    public RealAdd cloneNode() {
        return new RealAdd();
    }
}
