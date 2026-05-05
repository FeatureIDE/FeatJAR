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
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.IUnaryExpression;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Expresses quantified Boolean formulas.
 * Evaluates to {@code true} iff there is a value of the bound variable such that the formula evaluates to {@code true}.
 *
 * @author Sebastian Krieter
 */
public abstract class AQuantifier extends ANonTerminalExpression implements IConnective, IUnaryExpression {
    protected Variable boundVariable;

    protected AQuantifier(AQuantifier quantifier) {
        setBoundVariable(quantifier.boundVariable);
    }

    public AQuantifier(Variable boundVariable, IFormula formula) {
        super(formula);
        setBoundVariable(boundVariable);
    }

    public Variable getBoundVariable() {
        return boundVariable;
    }

    public void setBoundVariable(Variable boundVariable) {
        Objects.requireNonNull(boundVariable);
        this.boundVariable = boundVariable;
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        return Optional.empty(); // TODO
    }

    @Override
    public AQuantifier cloneNode() {
        throw new IllegalStateException(); // TODO
    }

    @Override
    public boolean equalsNode(IExpression other) {
        return super.equalsNode(other) && Objects.equals(boundVariable, ((AQuantifier) other).boundVariable);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), boundVariable);
    }
}
