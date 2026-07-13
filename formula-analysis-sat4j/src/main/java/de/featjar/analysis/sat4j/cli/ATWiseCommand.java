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
import de.featjar.analysis.sat4j.computation.ComputeCompleteSample;
import de.featjar.analysis.sat4j.computation.ComputeCoreSAT4J;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.BooleanAssignmentListToVariableMap;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.assignment.conversion.IVariableNameFilter;
import de.featjar.formula.assignment.conversion.VariableMapToVariables;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.io.VariableMapFormats;
import de.featjar.formula.io.csv.BooleanAssignmentListCSVFormat;
import java.nio.file.Path;

/**
 * Computes solutions for a given formula using SAT4J.
 *
 * @author Sebastian Krieter
 */
public abstract class ATWiseCommand extends ASAT4JAnalysisCommand<BooleanAssignmentList> {

    private static enum CompletionStrategy {
        NONE,
        FAST,
        NEGATIVE,
        POSITIVE,
        RANDOM,
    }

    /**
     * Value of t.
     */
    public static final Option<Integer> T_OPTION = Options.newOption("t", Options.IntegerParser) //
            .setDescription("Value(s) of parameter t.") //
            .setDefaultArgument("2");

    /**
     * Maximum number of configurations to be generated.
     */
    public static final Option<Integer> LIMIT_OPTION = Options.newOption("n", Options.IntegerParser) //
            .setDescription("Maximum number of configurations to be generated.") //
            .setDefaultArgument(Integer.toString(Integer.MAX_VALUE));

    /**
     * Path option for initial fixed sample.
     */
    public static final Option<Path> INITIAL_FIXED_SAMPLE_OPTION = Options.newOption(
                    "initial-sample", Options.PathParser)
            .setDescription("Path to initial fixed sample file. Configurations in this sample will not be modified.")
            .setValidator(Options.PathValidator);

    /**
     * Path option for initial variable sample.
     */
    public static final Option<Path> INITIAL_VARIABLE_SAMPLE_OPTION = Options.newOption(
                    "initial-variable-sample", Options.PathParser)
            .setDescription("Path to initial variable sample file. Configurations in this sample can be modified.")
            .setValidator(Options.PathValidator);

    /**
     * Strategy for completing partial configurations.
     */
    public static final Option<CompletionStrategy> COMPLETION_STRATEGY_OPTION = Options.newEnumOption(
                    "completion", CompletionStrategy.class) //
            .setDefaultArgument(CompletionStrategy.NONE.name())
            .setDescription("Strategy for completing partial configurations."); //

    public static final Option<IFormat<BooleanAssignmentList>> FORMAT = Options.newOutputFormatOption(
            BooleanAssignmentListFormats.class, new BooleanAssignmentListCSVFormat().getName());

    @Override
    public IComputation<BooleanAssignmentList> newComputation(OptionList optionParser) {
        ComputeBooleanClauseList formula = createCNFComputation(optionParser);
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
        CompletionStrategy completionStrategy = optionParser.get(COMPLETION_STRATEGY_OPTION);
        if (completionStrategy != CompletionStrategy.NONE) {
            analysis = analysis.map(ComputeCompleteSample::new)
                    .setDependencyComputation(ComputeCompleteSample.BOOLEAN_CLAUSE_LIST, formula)
                    .set(ComputeCompleteSample.RANDOM_SEED, optionParser.get(RANDOM_SEED_OPTION))
                    .set(
                            ComputeCompleteSample.SELECTION_STRATEGY,
                            switch (completionStrategy) {
                                case FAST -> ISelectionStrategy.NonParameterStrategy.ORIGINAL;
                                case NEGATIVE -> ISelectionStrategy.NonParameterStrategy.NEGATIVE;
                                case POSITIVE -> ISelectionStrategy.NonParameterStrategy.POSITIVE;
                                case RANDOM -> ISelectionStrategy.NonParameterStrategy.FAST_RANDOM;
                                default -> throw new IllegalStateException("Unexpected value: " + completionStrategy);
                            });
            VariableMap ignoreFile = optionParser
                    .getResult(IGNORE_VARIABLES)
                    .mapResult(p -> IO.load(p, VariableMapFormats.getInstance()))
                    .orElseLog(Verbosity.WARNING);
            if (ignoreFile != null) {
                analysis.set(
                        ComputeCoreSAT4J.AUXILLIARY_VARIABLES,
                        formula.map(BooleanAssignmentListToVariableMap::new)
                                .map(VariableMapToVariables::new)
                                .set(
                                        VariableMapToVariables.INCLUDE,
                                        IVariableNameFilter.list(ignoreFile.getVariableNames())));
            }
        }
        return analysis;
    }

    protected abstract IComputation<BooleanAssignmentList> newTWiseAnalysis(
            OptionList optionParser, ComputeBooleanClauseList formula);

    @Override
    protected IFormat<BooleanAssignmentList> getOuputFormat(OptionList optionParser) {
        return optionParser.get(FORMAT);
    }
}
