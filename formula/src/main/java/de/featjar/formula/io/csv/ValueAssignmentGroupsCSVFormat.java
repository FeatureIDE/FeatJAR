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
package de.featjar.formula.io.csv;

import de.featjar.base.data.Maps;
import de.featjar.base.data.Result;
import de.featjar.base.io.NonEmptyLineIterator;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.ValueAssignment;
import de.featjar.formula.assignment.ValueAssignmentGroups;
import de.featjar.formula.assignment.ValueAssignmentList;
import de.featjar.formula.io.textual.ValueAssignmentFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Reads / Writes a list of configuration.
 *
 * @author Sebastian Krieter
 */
public class ValueAssignmentGroupsCSVFormat implements IFormat<ValueAssignmentGroups> {
    private static final String ASSIGNMENT_COLUMN_NAME = "ID";
    private static final String GROUP_COLUMN_NAME = "Group";
    private static final String VALUE_SEPARATOR = ";";
    private static final String LINE_SEPARATOR = "\n";

    @Override
    public Result<String> serialize(ValueAssignmentGroups assignmentSpace) {
        final StringBuilder csv = new StringBuilder();
        csv.append(ASSIGNMENT_COLUMN_NAME);
        csv.append(VALUE_SEPARATOR);
        csv.append(GROUP_COLUMN_NAME);
        final VariableMap variableMap = assignmentSpace.getVariableMap();
        final List<String> names = variableMap.getVariableNames();
        for (final String name : names) {
            csv.append(VALUE_SEPARATOR);
            csv.append(name);
        }
        csv.append(LINE_SEPARATOR);
        int groupIndex = 0;
        int configurationIndex = 0;
        final List<? extends ValueAssignmentList> groups = assignmentSpace.getGroups();
        for (ValueAssignmentList group : groups) {
            for (final ValueAssignment configuration : group) {
                csv.append(configurationIndex++);
                csv.append(VALUE_SEPARATOR);
                csv.append(groupIndex);
                for (final String name : names) {
                    csv.append(VALUE_SEPARATOR);
                    configuration.getValue(variableMap.get(name).get()).ifPresent(csv::append);
                }
                csv.append(LINE_SEPARATOR);
            }
            groupIndex++;
        }
        return Result.of(csv.toString());
    }

    @Override
    public Result<ValueAssignmentGroups> parse(AInputMapper inputMapper) {
        try {
            final NonEmptyLineIterator lines = inputMapper.get().getNonEmptyLineIterator();
            final String[] headerColumns = lines.get().split(VALUE_SEPARATOR);
            if (headerColumns.length < 2) {
                throw new ParseException(
                        "Missing first two columns " + ASSIGNMENT_COLUMN_NAME + " and " + GROUP_COLUMN_NAME,
                        lines.getLineCount());
            }
            if (!ASSIGNMENT_COLUMN_NAME.equals(headerColumns[0])) {
                throw new ParseException("First column name must be " + ASSIGNMENT_COLUMN_NAME, lines.getLineCount());
            }
            if (!GROUP_COLUMN_NAME.equals(headerColumns[1])) {
                throw new ParseException("Second column name must be " + GROUP_COLUMN_NAME, lines.getLineCount());
            }
            final VariableMap variableMap = new VariableMap();
            for (int i = 2; i < headerColumns.length; i++) {
                variableMap.add(headerColumns[i]);
            }
            final ArrayList<ValueAssignmentList> groups = new ArrayList<>();
            for (String line = lines.get(); line != null; line = lines.get()) {
                final String[] values = line.split(VALUE_SEPARATOR);
                if (headerColumns.length != values.length) {
                    throw new ParseException(
                            String.format(
                                    "Number of values (%d) does not match number of columns (%d)",
                                    values.length, headerColumns.length),
                            lines.getLineCount());
                }
                try {
                    Integer.parseInt(values[0]);
                } catch (NumberFormatException e) {
                    throw new ParseException(
                            String.format("First value must be an integer number, but was %s", values[0]),
                            lines.getLineCount());
                }
                final ValueAssignmentList group;
                try {
                    final int groupIndex = Integer.parseInt(values[1]);
                    for (int i = groups.size() - 1; i < groupIndex; i++) {
                        groups.add(new ValueAssignmentList(variableMap));
                    }
                    group = groups.get(groupIndex);
                } catch (NumberFormatException e) {
                    throw new ParseException(
                            String.format("Second value must be an integer number, but was %s", values[0]),
                            lines.getLineCount());
                }
                LinkedHashMap<Integer, Object> variableValuePairs = Maps.empty();
                for (int i = 2; i < values.length; i++) {
                    final String value = values[i];
                    try {
                        variableValuePairs.put(i - 1, ValueAssignmentFormat.parseValue(value));
                    } catch (Exception e) {
                        throw new ParseException(
                                String.format("Could not parse value %s", value), lines.getLineCount());
                    }
                }
                group.add(new ValueAssignment(variableValuePairs));
            }
            return Result.of(new ValueAssignmentGroups(variableMap, groups));
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e, e.getErrorOffset()));
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }

    @Override
    public String getFileExtension() {
        return "csv";
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
        return "ValueAssignmentCSV";
    }
}
