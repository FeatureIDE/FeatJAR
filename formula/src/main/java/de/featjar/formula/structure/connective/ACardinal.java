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
import de.featjar.formula.structure.ANonTerminalExpression;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Expresses cardinality constraints.
 * Evaluates to {@code true} iff the number of its children that evaluate to {@code true} is larger than
 * or equal to a lower bound and smaller than or equal to an upper bound.
 *
 * @author Sebastian Krieter
 */
public abstract class ACardinal extends ANonTerminalExpression implements IConnective {
    protected Range range = Range.open();

    protected ACardinal(Range range, IFormula... formulas) {
        super(formulas);
        setRange(range);
    }

    protected ACardinal(Range range, List<? extends IFormula> formulas) {
        super(formulas);
        setRange(range);
    }

    protected ACardinal(ACardinal cardinal) {
        this.range = cardinal.range;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    @Override
    public Range getChildrenCountRange() {
        return Range.open();
    }

    @Override
    public Optional<Boolean> evaluate(List<?> values) {
        final int trueCount =
                (int) values.stream().filter(v -> Boolean.TRUE.equals(v)).count();
        final int nullCount = (int) values.stream().filter(Objects::isNull).count();
        if (!range.testLowerBound(trueCount + nullCount) || !range.testUpperBound(trueCount)) {
            return Optional.of(Boolean.FALSE);
        }
        if (range.testLowerBound(trueCount) && range.testUpperBound(trueCount + nullCount)) {
            return Optional.of(Boolean.TRUE);
        }
        return Optional.empty();
    }

    @Override
    public boolean equalsNode(IExpression other) {
        return super.equalsNode(other) && Objects.equals(range, ((ACardinal) other).range);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), range);
    }
}
