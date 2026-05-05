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

import de.featjar.base.io.csv.ACSVFormat;
import de.featjar.base.io.input.InputHeader;
import de.featjar.base.io.output.AOutput;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanSolution;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Reads / Writes a list of assignments.
 *
 * @author Sebastian Krieter
 */
public abstract class ASimpleAssignmentCSVFormat<T> extends ACSVFormat<T> {

    public static final String CONFIG_COLUMN = "Configuration";
    public static final String POSITIVE_VALUE = "+";
    public static final String NEGATIVE_VALUE = "-";
    public static final String NULL_VALUE = "0";

    @Override
    public String getName() {
        return "SimpleCSV";
    }

    @Override
    public boolean supportsContent(InputHeader inputHeader) {
        StringBuilder header = new StringBuilder();
        header.append(CONFIG_COLUMN);
        header.append(VALUE_SEPARATOR);
        return inputHeader.get().startsWith(header.toString());
    }

    protected void serializeHeader(final StringBuilder csv, VariableMap variableMap) {
        csv.append(CONFIG_COLUMN);
        final List<String> names = variableMap.getObjects(true);
        for (final String name : names) {
            csv.append(VALUE_SEPARATOR);
            csv.append(name != null ? name : "");
        }
        csv.append(LINE_SEPARATOR);
    }

    protected void serializeAssignment(final StringBuilder csv, int index, BooleanSolution assignment) {
        csv.append(index);
        for (int l : assignment.get()) {
            csv.append(VALUE_SEPARATOR);
            csv.append(l == 0 ? NULL_VALUE : l > 0 ? POSITIVE_VALUE : NEGATIVE_VALUE);
        }
        csv.append(LINE_SEPARATOR);
    }

    protected void writeHeader(AOutput output, VariableMap variableMap) throws IOException {
        output.writeText(CONFIG_COLUMN);
        final List<String> names = variableMap.getObjects(true);
        for (final String name : names) {
            output.writeText(VALUE_SEPARATOR);
            output.writeText(name != null ? name : "");
        }
        output.writeText(LINE_SEPARATOR);
    }

    protected void writeAssignment(AOutput output, int index, BooleanSolution assignment) throws IOException {
        output.writeText(Integer.toString(index));
        for (int l : assignment.get()) {
            output.writeText(VALUE_SEPARATOR);
            output.writeText(l == 0 ? NULL_VALUE : l > 0 ? POSITIVE_VALUE : NEGATIVE_VALUE);
        }
        output.writeText(LINE_SEPARATOR);
    }

    protected VariableMap parseHeader(String line, int lineCount) throws ParseException {
        final String[] headerColumns = line.split(VALUE_SEPARATOR);
        if (headerColumns.length < 1) {
            throw new ParseException("Missing first column " + CONFIG_COLUMN, lineCount);
        }
        if (!CONFIG_COLUMN.equals(headerColumns[0])) {
            throw new ParseException("First column name must be " + CONFIG_COLUMN, lineCount);
        }
        final VariableMap variableMap = new VariableMap();
        for (int i = 1; i < headerColumns.length; i++) {
            String name = headerColumns[i];
            variableMap.add(name.isEmpty() ? null : name);
        }
        return variableMap;
    }

    protected BooleanSolution parseAssignment(String line, int lineCount, final VariableMap variableMap)
            throws ParseException {
        final String[] values = line.split(VALUE_SEPARATOR);
        if (variableMap.size() + 1 != values.length) {
            throw new ParseException(
                    String.format(
                            "Number of values (%d) does not match number of columns (%d)",
                            values.length, variableMap.size()),
                    lineCount);
        }
        try {
            Integer.parseInt(values[0]);
        } catch (NumberFormatException e) {
            throw new ParseException(String.format("First value must be a number, but was %s", values[0]), lineCount);
        }
        final int[] literals = new int[values.length - 1];
        for (int i = 1; i < values.length; i++) {
            String value = values[i];
            switch (value) {
                case POSITIVE_VALUE:
                    literals[i - 1] = i;
                    break;
                case NEGATIVE_VALUE:
                    literals[i - 1] = -(i);
                    break;
                case NULL_VALUE:
                    literals[i - 1] = 0;
                    break;
                default:
                    throw new ParseException(String.format("Unknown value %s", value), lineCount);
            }
        }
        return new BooleanSolution(literals, false);
    }
}
