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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * An option that takes a list of predefined values given by a enum.
 *
 * @param <E> the enum type of the option
 *
 * @author Sebastian Krieter
 */
public class EnumListOption<E extends Enum<E>> extends Option<List<E>> {

    private final Class<E> enumClass;

    /**
     * Creates a list option with a given enum as possible values.
     *
     * @param name the name of the list enum option
     * @param enumClass the class of the enum
     */
    protected EnumListOption(String name, Class<E> enumClass) {
        super(
                name,
                s -> Arrays.stream(s.split("[,\n]"))
                        .map(o -> Enum.valueOf(enumClass, o.toUpperCase(Locale.ENGLISH)))
                        .collect(Collectors.toList()),
                List.of());
        this.enumClass = enumClass;
    }

    @Override
    public String toString() {
        return String.format(
                "%s <value1,value2,...>%s (one or more of [%s])%s",
                getArgumentName(),
                getDescription().map(d -> ": " + d).orElse(""),
                possibleValues(),
                getDefaultValue().map(s -> " (default: " + s + ")").orElse(""));
    }

    /**
     * {@return all possible values of this option's enum}
     */
    public String possibleValues() {
        return EnumOption.possibleValues(enumClass);
    }
}
