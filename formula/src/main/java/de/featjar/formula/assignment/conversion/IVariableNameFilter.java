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
package de.featjar.formula.assignment.conversion;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Krieter
 */
public interface IVariableNameFilter {

    static IVariableNameFilter of(boolean testResult) {
        return new IVariableNameFilter() {
            @Override
            public boolean test(String name) {
                return testResult;
            }
        };
    }

    static IVariableNameFilter pattern(String regex) {
        return new IVariableNameFilter() {
            private final Pattern pattern = Pattern.compile(regex);

            @Override
            public boolean test(String name) {
                return pattern.matcher(name).matches();
            }
        };
    }

    static IVariableNameFilter list(Collection<String> names) {
        return new IVariableNameFilter() {
            private final LinkedHashSet<String> set = new LinkedHashSet<>(names);

            @Override
            public boolean test(String name) {
                return set.contains(name);
            }
        };
    }

    /**
     * {@return true iff the given literal combination matches the filter}
     *
     * @param literals the literal combination to check
     */
    boolean test(String name);
}
