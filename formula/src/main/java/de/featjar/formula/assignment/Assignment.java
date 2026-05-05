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
package de.featjar.formula.assignment;

import de.featjar.base.data.Maps;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An assignment of arbitrary values to variable names.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Assignment implements IClause<String, Object>, ISolution<String, Object> {

    final LinkedHashMap<String, Object> variableValuePairs;

    public Assignment() {
        this(Maps.empty());
    }

    public Assignment(LinkedHashMap<String, Object> variableValuePairs) {
        this.variableValuePairs = variableValuePairs;
    }

    public Assignment(Object... variableValuePairs) {
        this.variableValuePairs = Maps.empty();
        if (variableValuePairs.length % 2 == 1)
            throw new IllegalArgumentException("expected a list of variable-value pairs for this value assignment");
        for (int i = 0; i < variableValuePairs.length; i += 2) {
            this.variableValuePairs.put((String) variableValuePairs[i], variableValuePairs[i + 1]);
        }
    }

    public Assignment(Assignment valueAssignment) {
        this(new LinkedHashMap<>(valueAssignment.variableValuePairs));
    }

    @Override
    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(variableValuePairs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return Objects.equals(variableValuePairs, that.variableValuePairs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableValuePairs);
    }

    public String print() {
        return variableValuePairs.entrySet().stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return String.format("Assignment[%s]", print());
    }

    @Override
    public Assignment toAssignment() {
        return this;
    }

    @Override
    public Assignment toClause() {
        return this;
    }

    @Override
    public Assignment toSolution() {
        return this;
    }
}
