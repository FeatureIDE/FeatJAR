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

import de.featjar.formula.structure.ANonTerminalExpression;
import de.featjar.formula.structure.IBinaryExpression;
import de.featjar.formula.structure.IFormula;
import java.util.List;
import java.util.Optional;

/**
 * Expresses "if A, then B" constraints (i.e., implication).
 * Evaluates to {@code true} iff the left child evaluates to {@code false} or
 * the right child evaluates to {@code true}.
 *
 * @author Sebastian Krieter
 */
public class Implies extends ANonTerminalExpression implements IConnective, IBinaryExpression {

    protected Implies() {}

    public Implies(IFormula leftFormula, IFormula rightFormula) {
        super(leftFormula, rightFormula);
    }

    public Implies(List<? extends IFormula> formulas) {
        super(formulas);
    }

    @Override
    public String getName() {
        return "implies";
    }

    @Override
    public Optional<Boolean> evaluate(List<?> values) {
        Object a = values.get(0);
        Object b = values.get(1);
        return (Boolean.FALSE.equals(a) || Boolean.TRUE.equals(b))
                ? Optional.of(Boolean.TRUE)
                : (Boolean.TRUE.equals(a) && Boolean.FALSE.equals(b)) ? Optional.of(Boolean.FALSE) : Optional.empty();
    }

    @Override
    public Implies cloneNode() {
        return new Implies();
    }
}
