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

import de.featjar.analysis.sat4j.computation.ATWiseSampleComputation;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.io.csv.BooleanAssignmentListCSVFormat;
import de.featjar.formula.io.dimacs.BooleanAssignmentListDimacsFormat;
import java.nio.file.Path;

/**
 * Computes solutions for a given formula using SAT4J.
 *
 * @author Sebastian Krieter
 */
public abstract class ATWiseCommand extends ASAT4JAnalysisCommand<BooleanAssignmentList> {

    /**
     * Value of t.
     */
    public static final Option<Integer> T_OPTION = Option.newOption("t", Option.IntegerParser) //
            .setDescription("Value(s) of parameter t.") //
            .setDefaultValue(2);

    /**
     * Maximum number of configurations to be generated.
     */
    public static final Option<Integer> LIMIT_OPTION = Option.newOption("n", Option.IntegerParser) //
            .setDescription("Maximum number of configurations to be generated.") //
            .setDefaultValue(Integer.MAX_VALUE);

    /**
     * Path option for initial fixed sample.
     */
    public static final Option<Path> INITIAL_FIXED_SAMPLE_OPTION = Option.newOption("initial-sample", Option.PathParser)
            .setDescription("Path to initial fixed sample file. Configurations in this sample will not be modified.")
            .setValidator(Option.PathValidator);

    /**
     * Path option for initial variable sample.
     */
    public static final Option<Path> INITIAL_VARIABLE_SAMPLE_OPTION = Option.newOption(
                    "initial-variable-sample", Option.PathParser)
            .setDescription("Path to initial variable sample file. Configurations in this sample can be modified.")
            .setValidator(Option.PathValidator);

    public static final Option<String> FORMAT = Option.newStringEnumOption(
                    "format", BooleanAssignmentListFormats.getInstance().getNames())
            .setDefaultValue(new BooleanAssignmentListCSVFormat().getName())
            .setDescription("Format of the output");

    @Override
    public IComputation<BooleanAssignmentList> newAnalysis(
            OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        IComputation<BooleanAssignmentList> analysis = newTWiseAnalysis(optionParser, formula)
                .set(ATWiseSampleComputation.CONFIGURATION_LIMIT, optionParser.get(LIMIT_OPTION))
                .set(ATWiseSampleComputation.RANDOM_SEED, optionParser.get(RANDOM_SEED_OPTION));

        Result<Path> initialSamplePath = optionParser.getResult(INITIAL_FIXED_SAMPLE_OPTION);
        if (initialSamplePath.isPresent()) {
            BooleanAssignmentGroups initialSample = IO.load(
                            initialSamplePath.get(), BooleanAssignmentGroupsFormats.getInstance())
                    .orElseLog(Verbosity.WARNING);
            if (initialSample != null) {
                analysis.set(ATWiseSampleComputation.INITIAL_FIXED_SAMPLE, initialSample.getFirstGroup());
            }
        }

        initialSamplePath = optionParser.getResult(INITIAL_VARIABLE_SAMPLE_OPTION);
        if (initialSamplePath.isPresent()) {
            BooleanAssignmentGroups initialSample = IO.load(
                            initialSamplePath.get(), BooleanAssignmentGroupsFormats.getInstance())
                    .orElseLog(Verbosity.WARNING);
            if (initialSample != null) {
                analysis.set(ATWiseSampleComputation.INITIAL_VARIABLE_SAMPLE, initialSample.getFirstGroup());
            }
        }
        return analysis;
    }

    protected abstract IComputation<BooleanAssignmentList> newTWiseAnalysis(
            OptionList optionParser, IComputation<BooleanAssignmentList> formula);

    @Override
    protected IFormat<BooleanAssignmentList> getOuputFormat(OptionList optionParser) {
        return BooleanAssignmentListFormats.getInstance()
                .getFormatByName(optionParser.get(FORMAT))
                .orElse(new BooleanAssignmentListDimacsFormat());
    }
}
