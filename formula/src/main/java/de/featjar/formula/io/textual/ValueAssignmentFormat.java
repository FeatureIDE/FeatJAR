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
package de.featjar.formula.io.textual;

import de.featjar.base.data.Maps;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.assignment.ValueAssignment;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * Textual format for serializing and parsing an {@link ValueAssignment}.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public class ValueAssignmentFormat implements IFormat<ValueAssignment> {

    public static Object parseValue(String s) {
        if (s == null || s.equalsIgnoreCase("null")) return null;
        else if (s.equalsIgnoreCase("true")) return Boolean.TRUE;
        else if (s.equalsIgnoreCase("false")) return Boolean.FALSE;
        else if (s.matches("\\d+(l)?")) return Long.valueOf(s);
        else if (s.matches("\\d+([.]\\d)?\\d*(f|d)?")) return Double.valueOf(s);
        else return s;
    }

    @Override
    public Result<String> serialize(ValueAssignment valueAssignment) {
        // TODO escape spaces and =
        return Result.of(valueAssignment.getAll().entrySet().stream()
                .map(e -> {
                    Integer variable = e.getKey();
                    Object value = e.getValue();
                    if (value instanceof Boolean) {
                        return String.format("%s%s", ((boolean) value) ? "+" : "-", variable);
                    } else {
                        return String.format("%s=%s", variable, value);
                    }
                })
                .collect(Collectors.joining(",")));
    }

    @Override
    public Result<ValueAssignment> parse(AInputMapper inputMapper) {
        LinkedHashMap<String, Object> variableValuePairs = Maps.empty();
        String[] variableValues = inputMapper
                .get()
                .getLineStream()
                .collect(Collectors.joining(","))
                .split(",");
        for (String variableValue : variableValues) {
            String[] pair = variableValue.split("=");
            if (pair.length == 0 || pair.length > 2) {
                return Result.empty(new Problem("expected variable-value pair, got " + variableValue, Severity.ERROR));
            } else {
                String variable = pair[0].trim();
                if (pair.length == 2) {
                    variableValuePairs.put(variable, parseValue(pair[1].trim()));
                } else if (variable.startsWith("-")) {
                    variableValuePairs.put(variable.substring(1), Boolean.FALSE);
                } else if (variable.startsWith("+")) {
                    variableValuePairs.put(variable.substring(1), Boolean.TRUE);
                } else {
                    return Result.empty(new Problem("expected (+|-)variableName, got " + variable, Severity.ERROR));
                }
            }
        }
        return Result.of(new ValueAssignment(variableValuePairs));
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public String getName() {
        return "ValueAssignment";
    }
}
