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
package de.featjar.base.cli;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An option that takes one value from a predefined list of values given by an enum.
 *
 * @param <E> the enum type of the option
 *
 * @author Sebastian Krieter
 */
public class EnumOption<E extends Enum<E>> extends Option<E> {

    /**
     * {@return a string that lists all possible values of an enum class}
     * @param enumClass the enum class
     */
    public static <T extends Enum<T>> String possibleValues(Class<T> enumClass) {
        final Object[] enumConstants = enumClass.getEnumConstants();
        return enumConstants == null //
                ? "" //
                : Arrays.stream(enumConstants)
                        .map(Objects::toString)
                        .map(String::toLowerCase)
                        .collect(Collectors.joining(", "));
    }

    private final Class<E> enumClass;

    /**
     * Creates an enum option.
     *
     * @param name the name of the flag option
     * @param enumClass the enum class
     */
    protected EnumOption(String name, Class<E> enumClass) {
        super(name, s -> Enum.valueOf(enumClass, s.toUpperCase(Locale.ENGLISH)));
        this.enumClass = enumClass;
    }

    @Override
    public String toString() {
        return String.format(
                "%s <value>%s (one of [%s])%s",
                getArgumentName(),
                getDescription().map(d -> ": " + d).orElse(""),
                possibleValues(),
                getDefaultValue().map(s -> " (default: " + s + ")").orElse(""));
    }

    /**
     * {@return all possible values of this option's enum}
     */
    public String possibleValues() {
        return possibleValues(enumClass);
    }
}
