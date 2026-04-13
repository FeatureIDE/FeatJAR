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

import de.featjar.base.data.type.Type;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Describes metadata that can be attached to an object.
 * an {@link IAttribute} does not store any attribute values, it only acts as a key or descriptor.
 *
 * @param <T> the type of the attribute
 *
 * @author Elias Kuiter
 */
public interface IAttribute<T> extends Function<IAttributable, Result<T>> {
    String getNamespace();

    String getSimpleName();

    Name getName();

    Type<T> getType();

    Class<T> getClassType();

    default Result<T> getDefaultValue(IAttributable attributable) {
        return Result.empty();
    }

    default Result<T> copyValue(IAttributable attributable) {
        return Result.empty();
    }

    default Result<String> serializeValue(IAttributable attributable) {
        return Result.empty();
    }

    default BiPredicate<IAttributable, T> getValidator() {
        return (a, o) -> true;
    }

    @Override
    default Result<T> apply(IAttributable attributable) {
        return Result.ofOptional(attributable.getAttributes())
                .map(a -> a.get(this))
                .map(getClassType()::cast)
                .or(getDefaultValue(attributable));
    }

    default String serialize(Object value) {
        Type<T> type = getType();
        return type.serialize(type.getClassType().cast(value));
    }

    default T cast(Object value) {
        Type<T> type = getType();
        return type.getClassType().cast(value);
    }
}
