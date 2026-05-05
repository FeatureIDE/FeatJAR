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

import de.featjar.base.data.Range;
import de.featjar.formula.structure.ANonTerminalExpression;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;
import java.util.Optional;

/**
 * Expresses "A != c" constraints, where A is a variable and c is some constant (default 0).
 * If the literal is positive, it evaluates to {@code true} iff A evaluates to something other than c.
 * If the literal is negative, it evaluates to {@code true} iff A evaluates to c.
 *
 * @author Sebastian Krieter
 */
public class NonBooleanLiteral extends ANonTerminalExpression implements ILiteral {

    static Constant nullConstant(Variable variable) {
        if (variable.getType().equals(Integer.class)) {
            return new Constant(0);
        } else if (variable.getType().equals(Double.class)) {
            return new Constant(0.0);
        } else if (variable.getType().equals(Long.class)) {
            return new Constant(0L);
        } else if (variable.getType().equals(Float.class)) {
            return new Constant(0.0f);
        } else {
            return new Constant(null, variable.getType());
        }
    }

    private boolean isPositive;

    protected NonBooleanLiteral() {}

    public NonBooleanLiteral(Variable variable) {
        this(variable, nullConstant(variable));
    }

    public NonBooleanLiteral(Variable variable, Constant constant) {
        this(variable, constant, true);
    }

    public NonBooleanLiteral(Variable variable, Constant constant, boolean isPositive) {
        super(variable, constant);
        this.isPositive = isPositive;
    }

    public Range getChildrenCountRange() {
        return Range.exactly(2);
    }

    @Override
    public NonBooleanLiteral cloneNode() {
        return new NonBooleanLiteral();
    }

    @Override
    public NonBooleanLiteral invert() {
        return new NonBooleanLiteral(
                (Variable) getChildren().get(0), (Constant) getChildren().get(1), !isPositive);
    }

    @Override
    public String getName() {
        return isPositive ? "!=" : "==";
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Optional<?> evaluate(List<?> values) {
        final Comparable v1 = (Comparable) values.get(0);
        final Comparable v2 = (Comparable) values.get(1);
        return (v1 != null && v2 != null) ? Optional.of(isPositive == (v1.compareTo(v2) != 0)) : Optional.empty();
    }

    @Override
    public boolean isPositive() {
        return isPositive;
    }

    @Override
    public void setPositive(boolean isPositive) {
        this.isPositive = isPositive;
    }

    public Constant getConstant() {
        return (Constant) getChildren().get(1);
    }
}
