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
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Primary implementation of {@link ValueAssignment}.
 * To be used when neither CNF nor DNF semantics are associated with an assignment.
 *
 * @author Elias Kuiter
 */
public class ValueAssignment implements IAssignment<Integer, Object>, IValueRepresentation {

    protected final LinkedHashMap<Integer, Object> variableValuePairs;

    public ValueAssignment() {
        this.variableValuePairs = Maps.empty();
    }

    public ValueAssignment(LinkedHashMap<Integer, Object> variableValuePairs) {
        this.variableValuePairs = variableValuePairs;
    }

    public ValueAssignment(Assignment assignment, VariableMap map) {
        this.variableValuePairs = Maps.empty();
        assignment.getAll().forEach((key, value) -> {
            Result<Integer> index = map.get(key);

            if (index.isEmpty()) {
                throw new IllegalArgumentException("Variable " + key + " does not exist in the variable map.");
            }
            variableValuePairs.put(index.get(), value);
        });
    }

    public ValueAssignment(Object... variableValuePairs) {
        this.variableValuePairs = Maps.empty();
        if (variableValuePairs.length % 2 == 1)
            throw new IllegalArgumentException("expected a list of variable-value pairs for this value assignment");
        for (int i = 0; i < variableValuePairs.length; i += 2) {
            this.variableValuePairs.put((Integer) variableValuePairs[i], variableValuePairs[i + 1]);
        }
    }

    public ValueAssignment(ValueAssignment valueAssignment) {
        this(new LinkedHashMap<>(valueAssignment.variableValuePairs));
    }

    @Override
    public ValueAssignment toAssignment() {
        return this;
    }

    @Override
    public ValueClause toClause() {
        return new ValueClause(variableValuePairs);
    }

    @Override
    public ValueSolution toSolution() {
        return new ValueSolution(variableValuePairs);
    }

    @Override
    public Map<Integer, Object> getAll() {
        return Collections.unmodifiableMap(variableValuePairs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueAssignment that = (ValueAssignment) o;
        return Objects.equals(variableValuePairs, that.variableValuePairs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableValuePairs);
    }

    @Override
    public String print() {
        return variableValuePairs.entrySet().stream()
                .map(e -> {
                    Integer variable = e.getKey();
                    Object value = e.getValue();
                    if (value instanceof Boolean) {
                        return String.format("%s%s", ((boolean) value) ? "+" : "-", variable);
                    } else {
                        return String.format("%s=%s", variable, value);
                    }
                })
                .collect(Collectors.joining(","));
    }

    @Override
    public Result<? extends BooleanAssignment> toBoolean() {
        return VariableMap.toBoolean(this);
    }

    @Override
    public String toString() {
        return String.format("ValueClause[%s]", print());
    }
}
