/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j.cli;

import de.featjar.analysis.sat4j.slice.CNFSlicer;
import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.ListOption;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.csv.BooleanAssignmentGroupsCSVFormat;
import de.featjar.formula.structure.IFormula;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Removes literals of a given formula using SAT4J.
 *
 * @author Andreas Gerasimow
 */
public class ProjectionCommand extends ACommand {

    /**
     * Literals to be removed
     */
    public static final ListOption<String> LITERALS_SLICE_OPTION = (ListOption<String>)
            Option.newListOption("slice", Option.StringParser).setDescription("Literals to be removed.");

    /**
     * Literals to be kept
     */
    public static final ListOption<String> LITERALS_PROJECT_OPTION = (ListOption<String>)
            Option.newListOption("project", Option.StringParser)
                    .setDescription(
                            "Literals to be projected. If not set, all features will be projected. The slice option has a higher priority, i.e. if both the project and slice option contain the same literal, it will be removed.");

    /**
     * Timeout in seconds.
     */
    public static final Option<Duration> TIMEOUT_OPTION = Option.newOption(
                    "timeout", s -> Duration.ofSeconds(Long.parseLong(s)))
            .setDescription("Timeout in seconds.")
            .setValidator(timeout -> !timeout.isNegative())
            .setDefaultValue(Duration.ZERO);

    @Override
    public int run(OptionList optionParser) {
        Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElse(null);
        List<String> projectLiterals =
                optionParser.getResult(LITERALS_PROJECT_OPTION).orElse(List.of());
        Set<String> sliceLiterals = new LinkedHashSet<>(
                optionParser.getResult(LITERALS_SLICE_OPTION).orElse(List.of()));
        Duration timeout = optionParser.getResult(TIMEOUT_OPTION).get();
        BooleanAssignmentGroupsCSVFormat format = new BooleanAssignmentGroupsCSVFormat();

        IFormula inputFormula = optionParser
                .getResult(INPUT_OPTION)
                .flatMap(p -> IO.load(p, FormulaFormats.getInstance()))
                .orElseThrow();

        BooleanAssignmentList cnf =
                ComputeBooleanClauseList.toBooleanAssignmentList(inputFormula).get();

        VariableMap variableMap = cnf.getVariableMap();

        VariableMap slicedVariableMap = variableMap.clone();

        if (!projectLiterals.isEmpty()) {
            List<String> inverseProjectLiterals = new ArrayList<>(inputFormula.getVariableNames());
            inverseProjectLiterals.removeAll(projectLiterals);
            sliceLiterals.addAll(
                    inverseProjectLiterals); // add all literals that are not in projectLiterals to sliceLiterals
        }

        sliceLiterals.forEach((literal) -> {
            if (slicedVariableMap.get(literal).isEmpty()) { // check if feature name exists
                FeatJAR.log().warning("Feature " + literal + " does not exist in feature model.");
            } else {
                slicedVariableMap.remove(literal);
            }
        });

        slicedVariableMap.normalize();

        int[] array = sliceLiterals.stream()
                .map(variableMap::get)
                .filter(Result::isPresent)
                .mapToInt(Result::get)
                .toArray();

        IComputation<BooleanAssignmentList> computation = Computations.of(cnf)
                .map(CNFSlicer::new)
                .set(CNFSlicer.VARIABLES_TO_REMOVE, new BooleanAssignment(array));

        Result<BooleanAssignmentList> result;

        if (!timeout.isZero()) {
            result = computation.computeResult(true, true, timeout);
            if (result.isEmpty()) {
                FeatJAR.log().error("Timeout");
            }
        } else {
            result = computation.computeResult(true, true);
        }

        if (result.isPresent()) {
            BooleanAssignmentList clauseList = result.get().remap(slicedVariableMap);
            try {
                if (outputPath == null || outputPath.toString().equals("results")) {
                    String string = format.serialize(new BooleanAssignmentGroups(slicedVariableMap, clauseList))
                            .orElseThrow();
                    FeatJAR.log().message(string);
                } else {
                    IO.save(new BooleanAssignmentGroups(slicedVariableMap, clauseList), outputPath, format);
                }
            } catch (IOException | RuntimeException e) {
                FeatJAR.log().error(e);
                return FeatJAR.ERROR_WRITING_RESULT;
            }
        } else {
            FeatJAR.log().problems(result.getProblems());
            FeatJAR.log().error("Couldn't compute result.");
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
        return 0;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Removes literals of a given formula using SAT4J.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("projection-sat4j");
    }
}
