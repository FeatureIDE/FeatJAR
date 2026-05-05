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
package de.featjar.formula.structure.predicate;

import de.featjar.formula.structure.ANonTerminalExpression;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IUnaryExpression;
import de.featjar.formula.structure.term.value.IValue;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Expresses "A == true" (or A) and "A == false" (or !A) constraints.
 * Evaluates to {@code true} iff its child evaluates to the polarity given by {@link #isPositive()}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Literal extends ANonTerminalExpression implements IUnaryExpression, ILiteral {

    private boolean isPositive;

    public Literal(Literal literal) {
        this.isPositive = literal.isPositive;
    }

    public Literal(boolean isPositive, IValue value) {
        super(value);
        this.isPositive = isPositive;
        if (!Objects.equals(value.getType(), Boolean.class))
            throw new IllegalArgumentException("Boolean literals only accept Boolean values");
    }

    public Literal(IValue value) {
        this(true, value);
    }

    public Literal(String variableName) {
        this(true, new Variable(variableName));
    }

    public Literal(boolean isPositive, String variableName) {
        this(isPositive, new Variable(variableName));
    }

    @Override
    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean isPositive) {
        this.isPositive = isPositive;
    }

    @Override
    public Literal invert() {
        return new Literal(!isPositive, (IValue) getExpression());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends IValue> getChildren() {
        return (List<? extends IValue>) super.getChildren();
    }

    @Override
    public String getName() {
        return isPositive ? "+" : "-";
    }

    @Override
    public Class<Boolean> getChildrenType() {
        return Boolean.class;
    }

    @Override
    public Optional<Boolean> evaluate(List<?> values) {
        final Boolean b = (Boolean) values.get(0);
        return b != null ? Optional.of(isPositive == b) : Optional.empty();
    }

    @Override
    public Literal cloneNode() {
        return new Literal(this);
    }

    @Override
    public boolean equalsNode(IExpression other) {
        return super.equalsNode(other) && isPositive == ((Literal) other).isPositive;
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), isPositive);
    }
}
