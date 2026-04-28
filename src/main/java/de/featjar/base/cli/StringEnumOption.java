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
import java.util.LinkedHashSet;
import java.util.List;

/**
 * An option that takes one value from a predefined list of values given by a list of strings.
 *
 * @author Sebastian Krieter
 */
public class StringEnumOption extends Option<String> {

    private final LinkedHashSet<String> possibleValues;

    /**
     * Creates an enum option.
     *
     * @param name the name of the flag option
     * @param possibleValues the possible values this option can take
     */
    protected StringEnumOption(String name, String... possibleValues) {
        this(name, Arrays.asList(possibleValues));
    }

    /**
     * Creates an enum option.
     *
     * @param name the name of the flag option
     * @param possibleValues the possible values this option can take
     */
    protected StringEnumOption(String name, List<String> possibleValues) {
        super(name, StringParser);
        this.possibleValues = new LinkedHashSet<>(possibleValues);
        validator = s -> this.possibleValues.contains(s);
    }

    @Override
    public String toString() {
        return String.format(
                "%s <value>%s (one of %s)%s",
                getArgumentName(),
                getDescription().map(d -> ": " + d).orElse(""),
                possibleValues.toString(),
                getDefaultValue().map(s -> " (default: " + s + ")").orElse(""));
    }
}
