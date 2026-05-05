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
import java.util.List;
import java.util.Optional;

/**
 * A variable.
 * Is identified by its name.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Variable extends ATerminalExpression implements IValue {
    protected String name;
    protected Class<?> type;

    private Variable(Variable variable) {
        setName(variable.name);
        setType(variable.type);
    }

    public Variable(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public Variable(String name) {
        this(name, Boolean.class);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public Variable cloneNode() {
        return new Variable(this);
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        if (!getType().isInstance(values)) throw new IllegalArgumentException("value not of type " + getType());
        return Optional.ofNullable(values.get(0));
    }
}
