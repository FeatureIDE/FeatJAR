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

import de.featjar.base.data.Result;
import de.featjar.base.io.NonEmptyLineIterator;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.output.AOutput;
import de.featjar.base.io.output.AOutputMapper;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.IBooleanAssignmentGroupsFormat;
import java.io.IOException;
import java.text.ParseException;

/**
 * Reads / Writes a list of assignments.
 *
 * @author Sebastian Krieter
 */
public class BooleanAssignmentGroupsCSVFormat extends ASimpleAssignmentCSVFormat<BooleanAssignmentGroups>
        implements IBooleanAssignmentGroupsFormat {

    /**
     * The identifier of this format.
     */
    public static final String ID = BooleanAssignmentGroupsCSVFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public BooleanAssignmentGroupsCSVFormat getInstance() {
        return this;
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
    public void write(BooleanAssignmentGroups booleanAssignmentGroups, AOutputMapper outputMapper) throws IOException {
        AOutput output = outputMapper.get();

        VariableMap variableMap = booleanAssignmentGroups.getVariableMap();
        writeHeader(output, variableMap);

        int configurationIndex = 0;
        for (final BooleanAssignment configuration : booleanAssignmentGroups.getMergedGroups()) {
            writeAssignment(output, configurationIndex++, configuration.toSolution(variableMap.size()));
        }
    }

    @Override
    public Result<String> serialize(BooleanAssignmentGroups booleanAssignmentGroups) {
        final StringBuilder csv = new StringBuilder();
        VariableMap variableMap = booleanAssignmentGroups.getVariableMap();
        serializeHeader(csv, variableMap);
        int configurationIndex = 0;
        for (final BooleanAssignment configuration : booleanAssignmentGroups.getMergedGroups()) {
            serializeAssignment(csv, configurationIndex++, configuration.toSolution(variableMap.size()));
        }
        return Result.of(csv.toString());
    }

    @Override
    public Result<BooleanAssignmentGroups> parse(AInputMapper inputMapper) {
        try {
            final NonEmptyLineIterator lines = inputMapper.get().getNonEmptyLineIterator();

            final VariableMap variableMap = parseHeader(lines.get(), lines.getLineCount());

            final BooleanAssignmentList group = new BooleanAssignmentList(variableMap);
            for (String line = lines.get(); line != null; line = lines.get()) {
                group.add(parseAssignment(line, lines.getLineCount(), variableMap));
            }
            return Result.of(new BooleanAssignmentGroups(group));
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e, e.getErrorOffset()));
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }
}
