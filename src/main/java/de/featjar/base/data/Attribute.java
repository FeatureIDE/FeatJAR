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
package de.featjar.base.data;

import de.featjar.base.data.type.BooleanType;
import de.featjar.base.data.type.DoubleType;
import de.featjar.base.data.type.FloatType;
import de.featjar.base.data.type.GenericType;
import de.featjar.base.data.type.IntegerType;
import de.featjar.base.data.type.LongType;
import de.featjar.base.data.type.StringType;
import de.featjar.base.data.type.Type;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Primary implementation of {@link IAttribute}.
 *
 * @param <T> the type of the attribute
 *
 * @author Elias Kuiter
 */
public class Attribute<T> implements IAttribute<T> {

    private final Name name;
    private final Type<T> type;
    private BiPredicate<IAttributable, T> validator;
    private Function<IAttributable, T> defaultValueFunction;
    private Function<T, T> copyValueFunction = t -> t;

    /**
     * Constructs a new attribute with the {@link Name#DEFAULT_NAMESPACE default name space}.
     * @param name the name of the attribute
     * @param type the class object of the attribute type
     */
    Attribute(String name, Class<T> type) {
        this(new Name(name), type);
    }

    /**
     * Constructs a new attribute.
     * @param name the name of the attribute
     * @param type the class object of the attribute type
     */
    @SuppressWarnings("unchecked")
    Attribute(Name name, Class<T> type) {
        this.name = Objects.requireNonNull(name);
        if (type == Boolean.class) {
            this.type = (Type<T>) BooleanType.INSTANCE;
        } else if (type == Integer.class) {
            this.type = (Type<T>) IntegerType.INSTANCE;
        } else if (type == Double.class) {
            this.type = (Type<T>) DoubleType.INSTANCE;
        } else if (type == Long.class) {
            this.type = (Type<T>) LongType.INSTANCE;
        } else if (type == String.class) {
            this.type = (Type<T>) StringType.INSTANCE;
        } else if (type == Float.class) {
            this.type = (Type<T>) FloatType.INSTANCE;
        } else {
            this.type = new GenericType<>(Objects.requireNonNull(type));
        }
    }

    @Override
    public String getNamespace() {
        return name.getNamespace();
    }

    @Override
    public String getSimpleName() {
        return name.getName();
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public Type<T> getType() {
        return type;
    }

    @Override
    public Class<T> getClassType() {
        return type.getClassType();
    }

    /**
     * {@return this attribute's default value function}
     */
    public Result<Function<IAttributable, T>> getDefaultValueFunction() {
        return Result.ofNullable(defaultValueFunction);
    }

    /**
     * {@return this attribute's copy value function}
     */
    public Result<Function<T, T>> getCopyValueFunction() {
        return Result.ofNullable(copyValueFunction);
    }

    /**
     * {@return this attribute's copy value function}
     */
    public Result<Function<T, String>> getSerializeValueFunction() {
        return Result.of(type::serialize);
    }

    /**
     * Sets this attribute's default value function.
     *
     * @param defaultValueFunction the function
     * @return this attribute
     */
    public Attribute<T> setDefaultValueFunction(Function<IAttributable, T> defaultValueFunction) {
        this.defaultValueFunction = defaultValueFunction;
        return this;
    }

    /**
     * Sets this attribute's copy value function.
     *
     * @param copyValueFunction the function
     * @return this attribute
     */
    public Attribute<T> setCopyValueFunction(Function<T, T> copyValueFunction) {
        this.copyValueFunction = copyValueFunction;
        return this;
    }

    /**
     * {@return this attribute's default value for a given object}
     */
    @Override
    public Result<T> getDefaultValue(IAttributable attributable) {
        return getDefaultValueFunction().map(f -> f.apply(attributable));
    }

    /**
     * Sets this attribute's default value.
     *
     * @param defaultValue the default value
     * @return this attribute
     */
    public Attribute<T> setDefaultValue(T defaultValue) {
        return setDefaultValueFunction(o -> defaultValue);
    }

    /**
     * {@return a copy of this attribute's value for a given IAttributable}
     */
    @Override
    public Result<T> copyValue(IAttributable attributable) {
        return getCopyValueFunction()
                .flatMap(f -> attributable.getAttributeValue(this).map(o -> f.apply((T) o)));
    }

    @Override
    public Result<String> serializeValue(IAttributable attributable) {
        return getSerializeValueFunction()
                .flatMap(f -> attributable.getAttributeValue(this).map(o -> f.apply((T) o)));
    }

    /**
     * {@return this attribute's validator}
     */
    @Override
    public BiPredicate<IAttributable, T> getValidator() {
        return Result.ofNullable(validator).orElse(IAttribute.super.getValidator());
    }

    /**
     * Sets this attribute's validator.
     *
     * @param validator the validator
     * @return this attribute
     */
    public Attribute<T> setValidator(BiPredicate<IAttributable, T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Attribute{%s}", name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute<?> attribute = (Attribute<?>) o;
        return name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
