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

/**
 * Represents a type.
 *
 * @param <T> the type class
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Type<T> extends Comparable<Type<?>> {

    Class<T> getClassType();

    default T copy(T value) {
        return value;
    }

    default String serialize(T value) {
        return String.valueOf(value);
    }

    T parse(String text);

    default int compareTo(Type<?> o) {
        return getClassType().getName().compareTo(o.getClassType().getName());
    }
}
