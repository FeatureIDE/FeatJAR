/*
 * Copyright (C) 2026 FeatJAR-Development-Team
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

import de.featjar.analysis.sat4j.computation.CompactYASA;
import de.featjar.analysis.sat4j.computation.YASA;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.combination.VariableCombinationSpecification.VariableCombinationSpecificationComputation;
import de.featjar.formula.index.SampleBitIndex;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Computes solutions for a given formula using SAT4J.
 *
 * @author Sebastian Krieter
 */
public class YASACommand extends ATWiseCommand {

    /**
     * Number of iterations.
     */
    public static final Option<Integer> ITERATIONS_OPTION = Option.newOption("i", Option.IntegerParser) //
            .setDescription("Number of iterations.") //
            .setDefaultValue(1);

    public static final Option<Integer> INTERNAL_SOLUTION_LIMIT = Option.newOption(
                    "internal-limit", Option.IntegerParser) //
            .setDescription("Number of internally cached configurations.")
            .setDefaultValue(65_536);

    public static final Option<Boolean> COMPACT = Option.newFlag("c") //
            .setDescription("Use a more memory efficient version of YASA.");

    public static final Option<Boolean> INCREMENTAL = Option.newFlag("incremental") //
            .setDescription("Start with smaller values for t.");

    public static final Option<Path> INCLUDE_INTERACTIONS = Option.newOption("include-interactions", Option.PathParser)
            .setDescription("Path to list of interactions that will be considered.")
            .setValidator(Option.PathValidator);

    public static final Option<Path> EXCLUDE_INTERACTIONS = Option.newOption("exclude-interactions", Option.PathParser)
            .setDescription("Path to list of interactions that will be ignored.")
            .setValidator(Option.PathValidator);

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes solutions for a given formula using SAT4J. Uses the most recent version of YASA.");
    }

    @Override
    public IComputation<BooleanAssignmentList> newTWiseAnalysis(
            OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        IComputation<BooleanAssignmentList> analysis;
        if (optionParser.get(COMPACT)) {
            analysis = formula.map(CompactYASA::new)
                    .set(
                            CompactYASA.COMBINATION_SET,
                            formula.map(VariableCombinationSpecificationComputation::new)
                                    .set(VariableCombinationSpecificationComputation.T, optionParser.get(T_OPTION)))
                    .set(CompactYASA.SAT_TIMEOUT, optionParser.get(SAT_TIMEOUT_OPTION))
                    .set(CompactYASA.ITERATIONS, optionParser.get(ITERATIONS_OPTION))
                    .set(CompactYASA.INTERNAL_SOLUTION_LIMIT, optionParser.get(INTERNAL_SOLUTION_LIMIT))
                    .set(CompactYASA.INCREMENTAL_T, optionParser.get(INCREMENTAL));
        } else {
            analysis = formula.map(YASA::new)
                    .set(
                            YASA.COMBINATION_SET,
                            formula.map(VariableCombinationSpecificationComputation::new)
                                    .set(VariableCombinationSpecificationComputation.T, optionParser.get(T_OPTION)))
                    .set(YASA.SAT_TIMEOUT, optionParser.get(SAT_TIMEOUT_OPTION))
                    .set(YASA.ITERATIONS, optionParser.get(ITERATIONS_OPTION))
                    .set(YASA.INTERNAL_SOLUTION_LIMIT, optionParser.get(INTERNAL_SOLUTION_LIMIT))
                    .set(YASA.INCREMENTAL_T, optionParser.get(INCREMENTAL));
        }

        Result<Path> consideredInteractionsPath = optionParser.getResult(INCLUDE_INTERACTIONS);
        if (consideredInteractionsPath.isPresent()) {
            BooleanAssignmentGroups consideredInteractions = IO.load(
                            consideredInteractionsPath.get(), new BooleanAssignmentGroupsFormats())
                    .orElseLog(Verbosity.WARNING);
            if (consideredInteractions != null) {
                analysis.set(YASA.INCLUDE_INTERACTIONS, new SampleBitIndex(consideredInteractions.getMergedGroups()));
            }
        }
        Result<Path> ignoreInteractionsPath = optionParser.getResult(EXCLUDE_INTERACTIONS);
        if (ignoreInteractionsPath.isPresent()) {
            BooleanAssignmentGroups ignoreInteractions = IO.load(
                            ignoreInteractionsPath.get(), new BooleanAssignmentGroupsFormats())
                    .orElseLog(Verbosity.WARNING);
            if (ignoreInteractions != null) {
                analysis.set(YASA.EXCLUDE_INTERACTIONS, new SampleBitIndex(ignoreInteractions.getMergedGroups()));
            }
        }
        return analysis;
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("yasa");
    }
}
