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
 * Expresses "choose K" constraints.
 * Evaluates to {@code true} iff the number of children that evaluate to {@code true} is equal to a given number.
 *
 * @author Sebastian Krieter
 */
public class Choose extends ACardinal {
    private Choose(Choose choose) {
        super(choose);
    }

    public Choose(int bound, IFormula... formulas) {
        super(Range.exactly(bound), formulas);
    }

    public Choose(int minimum, List<? extends IFormula> formulas) {
        super(Range.exactly(minimum), formulas);
    }

    @Override
    public String getName() {
        return "choose-" + getBound();
    }

    @Override
    public Choose cloneNode() {
        return new Choose(this);
    }

    public int getBound() {
        return super.getRange().getLowerBound();
    }

    public void setBound(int bound) {
        super.setRange(Range.exactly(bound));
    }
}
