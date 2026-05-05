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

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.io.NonEmptyLineIterator;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.output.AOutput;
import de.featjar.base.io.output.AOutputMapper;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.io.IBooleanAssignmentListFormat;
import java.io.IOException;
import java.text.ParseException;

/**
 * Reads / Writes a list of assignments.
 *
 * @author Sebastian Krieter
 */
public class BooleanAssignmentListGroupedCSVFormat extends AGroupedAssignmentCSVFormat<BooleanAssignmentList>
        implements IBooleanAssignmentListFormat {

    /**
     * The identifier of this format.
     */
    public static final String ID = BooleanAssignmentListGroupedCSVFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public BooleanAssignmentListGroupedCSVFormat getInstance() {
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
    public void write(BooleanAssignmentList assignmentList, AOutputMapper outputMapper) throws IOException {
        AOutput out = outputMapper.get();

        final VariableMap variableMap = assignmentList.getVariableMap();
        writeHeader(out, variableMap);

        int assignmentIndex = 0;
        for (final BooleanAssignment assignment : assignmentList) {
            writeAssignment(out, 0, assignmentIndex++, assignment.toSolution(variableMap.size()));
        }
        outputMapper.close();
    }

    @Override
    public Result<String> serialize(BooleanAssignmentList assignmentList) {
        final StringBuilder csv = new StringBuilder();

        final VariableMap variableMap = assignmentList.getVariableMap();
        serializeHeader(csv, variableMap);

        int assignmentIndex = 0;
        for (final BooleanAssignment assignment : assignmentList) {
            serializeAssignment(csv, 0, assignmentIndex++, assignment.toSolution(variableMap.size()));
        }

        return Result.of(csv.toString());
    }

    @Override
    public Result<BooleanAssignmentList> parse(AInputMapper inputMapper) {
        try {
            final NonEmptyLineIterator lines = inputMapper.get().getNonEmptyLineIterator();

            final VariableMap variableMap = parseHeader(lines.get(), lines.getLineCount());

            BooleanAssignmentList group = new BooleanAssignmentList(variableMap);
            for (String line = lines.get(); line != null; line = lines.get()) {
                Pair<Integer, BooleanSolution> parsedAssignment =
                        parseAssignment(line, lines.getLineCount(), variableMap);
                group.add(parsedAssignment.getSecond());
            }
            return Result.of(group);
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e, e.getErrorOffset()));
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }
}
