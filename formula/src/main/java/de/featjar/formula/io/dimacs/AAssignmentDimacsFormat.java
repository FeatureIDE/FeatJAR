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
package de.featjar.formula.io.dimacs;

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reads / Writes a list of assignments.
 *
 * @author Sebastian Krieter
 */
public class AAssignmentDimacsFormat<T> extends ADimacsFormat<T> {

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    protected Result<String> serializeGroups(BooleanAssignmentGroups assignmentGroups) {
        Objects.requireNonNull(assignmentGroups);
        return Result.of(DimacsSerializer.serializeGroups(
                assignmentGroups.getVariableMap(),
                assignmentGroups.getGroups().stream()
                        .map(BooleanAssignmentList::getAll)
                        .collect(Collectors.toList()),
                BooleanAssignment::get));
    }

    protected Result<BooleanAssignmentGroups> parseGroups(AInputMapper inputMapper) {
        final DimacsParser parser = new DimacsParser();
        parser.setReadingVariableDirectory(true);
        try {
            Pair<VariableMap, List<List<int[]>>> parsingResult = parser.parse(inputMapper);
            VariableMap variableMap = parsingResult.getKey();
            List<BooleanAssignmentList> groups = new ArrayList<>();
            for (List<int[]> group : parsingResult.getValue()) {
                groups.add(new BooleanAssignmentList(
                        variableMap, group.stream().map(BooleanSolution::new).collect(Collectors.toList())));
            }
            return Result.of(new BooleanAssignmentGroups(variableMap, groups));
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e, e.getErrorOffset()));
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }
}
