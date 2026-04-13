/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.data.type;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Sebastian Krieter
 */
public class GenericType<T> implements Type<T> {

    private final Class<T> type;

    private Function<T, T> copyValueFunction = t -> t;
    private Function<T, String> serializeValueFunction = String::valueOf;
    private Function<String, T> parseValueFunction = s -> {
        throw new UnsupportedOperationException();
    };

    /**
     * Constructs a new generic type.
     * @param type the class object of the type
     */
    public GenericType(Class<T> type) {
        this.type = Objects.requireNonNull(type);
    }

    /**
     * {@return the copy value function}
     */
    public Function<T, T> getCopyValueFunction() {
        return copyValueFunction;
    }

    /**
     * Sets the copy value function.
     *
     * @param copyValueFunction the function
     * @return this attribute
     */
    public GenericType<T> setCopyValueFunction(Function<T, T> copyValueFunction) {
        this.copyValueFunction = Objects.requireNonNull(copyValueFunction);
        return this;
    }

    public Function<T, String> getSerializeValueFunction() {
        return serializeValueFunction;
    }

    public void setSerializeValueFunction(Function<T, String> serializeValueFunction) {
        this.serializeValueFunction = Objects.requireNonNull(serializeValueFunction);
    }

    public Function<String, T> getParseValueFunction() {
        return parseValueFunction;
    }

    public void setParseValueFunction(Function<String, T> parseValueFunction) {
        this.parseValueFunction = Objects.requireNonNull(parseValueFunction);
    }

    public String toTypeString() {
        return String.format("Generic:%s", type.getTypeName());
    }

    @Override
    public String toString() {
        return toTypeString();
    }

    @Override
    public T copy(T value) {
        return copyValueFunction.apply(value);
    }

    @Override
    public T parse(String text) {
        return parseValueFunction.apply(text);
    }

    @Override
    public String serialize(T value) {
        return serializeValueFunction.apply(value);
    }

    @Override
    public Class<T> getClassType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
