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
package de.featjar.formula.structure.term.value;

import de.featjar.formula.structure.ATerminalExpression;
import de.featjar.formula.structure.IExpression;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A constant.
 * Is identified by its value.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Constant extends ATerminalExpression implements IValue {
    protected Object value;
    protected Class<?> type;

    private Constant(Constant constant) {
        setValue(constant.value);
        setType(constant.type);
    }

    public Constant(Object value, Class<?> type) {
        this.type = Objects.requireNonNull(type);
        if (value != null && !type.isInstance(value)) {
            throw new IllegalArgumentException(
                    String.format("expected value of type %s, got %s", getType(), value.getClass()));
        }
        this.value = value;
    }

    public Constant(Object value) {
        this.value = Objects.requireNonNull(value);
        this.type = value.getClass();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return String.valueOf(value);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public Constant cloneNode() {
        return new Constant(this);
    }

    @Override
    public boolean equalsNode(IExpression other) {
        return super.equalsNode(other) && Objects.equals(value, ((Constant) other).value);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), value);
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        return Optional.ofNullable(value);
    }
}
