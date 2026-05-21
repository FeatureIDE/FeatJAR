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
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;
import java.util.Optional;

/**
 * Expresses whether there is a value for a variable.
 * If the literal is positive, it evaluates to {@code true} iff the value of the variable is defined.
 * If the literal is negative, it evaluates to {@code true} iff the value of the variable is undefined.
 *
 * @author Sebastian Krieter
 */
public class DefLiteral extends ANonTerminalExpression implements ILiteral {

    private boolean isPositive;

    public DefLiteral(Variable variable) {
        this(variable, true);
    }

    public DefLiteral(Variable variable, boolean isPositive) {
        super(variable);
        this.isPositive = isPositive;
    }

    private DefLiteral(boolean isPositive) {
        super();
        this.isPositive = isPositive;
    }

    public Range getChildrenCountRange() {
        return Range.exactly(1);
    }

    @Override
    public DefLiteral cloneNode() {
        return new DefLiteral(isPositive);
    }

    @Override
    public DefLiteral invert() {
        return new DefLiteral((Variable) getChildren().get(0), !isPositive);
    }

    @Override
    public String getName() {
        return isPositive ? "defined" : "undefined";
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }

    public Variable getVariable() {
        return (Variable) getChildren().get(0);
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        return Optional.of(isPositive == (values.get(0) != null));
    }

    @Override
    public boolean isPositive() {
        return isPositive;
    }

    @Override
    public void setPositive(boolean isPositive) {
        this.isPositive = isPositive;
    }
}
