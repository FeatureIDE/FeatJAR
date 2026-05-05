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
package de.featjar.formula.structure.connective;

import de.featjar.base.data.Range;
import de.featjar.formula.structure.IFormula;
import java.util.List;

/**
 * Expresses "between K and L" constraints.
 * Evaluates to {@code true} iff the number of children that evaluate to {@code true} is in a given range.
 *
 * @author Sebastian Krieter
 */
public class Between extends ACardinal {
    private Between(Between between) {
        super(between);
    }

    public Between(int minimum, int maximum, IFormula... formulas) {
        super(Range.of(minimum, maximum), formulas);
    }

    public Between(int minimum, int maximum, List<IFormula> formulas) {
        super(Range.of(minimum, maximum), formulas);
    }

    @Override
    public String getName() {
        return String.format("between-%d-%d", getMaximum(), getMinimum());
    }

    @Override
    public Between cloneNode() {
        return new Between(this);
    }

    public int getMinimum() {
        return super.getRange().getLowerBound();
    }

    public void setMinimum(int minimum) {
        super.getRange().setLowerBound(minimum);
    }

    public int getMaximum() {
        return super.getRange().getUpperBound();
    }

    public void setMaximum(int maximum) {
        super.getRange().setUpperBound(maximum);
    }
}
