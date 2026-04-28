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

import de.featjar.analysis.AAnalysisCommand;
import de.featjar.analysis.sat4j.computation.ComputeConstraintedTWiseCoverage;
import de.featjar.analysis.sat4j.io.textual.CoverageStatisticTextFormat;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.formula.CoverageStatistic;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.combination.VariableCombinationSpecification.VariableCombinationSpecificationComputation;
import de.featjar.formula.computation.AComputeTWiseCoverage;
import de.featjar.formula.computation.ComputeAbsoluteTWiseCoverage;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.computation.ComputeRelativeTWiseCoverage;
import de.featjar.formula.index.SampleBitIndex;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import de.featjar.formula.io.FormulaFormats;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Computes t-wise coverage for a given formula using SAT4J.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public class TWiseCoverageCommand extends AAnalysisCommand<CoverageStatistic> {

    /**
     * Input option for feature model path.
     */
    public static final Option<Path> FM_OPTION = Option.newOption("fm", Option.PathParser)
            .setDescription("Path to feature model. Cannot be chosen together with --ref")
            .setValidator(Option.PathValidator);

    /**
     * Input option for feature model path.
     */
    public static final Option<Path> REFERENCE_SAMPLE_OPTION = Option.newOption("ref", Option.PathParser)
            .setDescription("Path to reference sample. Cannot be chosen together with --fm")
            .setValidator(Option.PathValidator);

    /**
     * Value of t.
     */
    public static final Option<Integer> T_OPTION = Option.newOption("t", Option.IntegerParser) //
            .setDescription("Value of parameter t.") //
            .setDefaultValue(2);

    public static final Option<Path> INCLUDE_INTERACTIONS = Option.newOption("include-interactions", Option.PathParser)
            .setDescription("Path to list of interactions that will be considered.")
            .setValidator(Option.PathValidator);

    public static final Option<Path> EXCLUDE_INTERACTIONS = Option.newOption("exclude-interactions", Option.PathParser)
            .setDescription("Path to list of interactions that will be ignored.")
            .setValidator(Option.PathValidator);

    public static final Option<Boolean> COVERAGE_ONLY_OPTION = Option.newFlag("coverage-only") //
            .setDescription("Shows only coverage value.");

    public static final Option<Boolean> COUNT_ONLY_OPTION = Option.newFlag("count-only") //
            .setDescription("Shows only the interaction count: covered, uncovered, invalid, ignored (line separated)");

    private boolean coverageOnly, countOnly;

    @Override
    public Optional<String> getDescription() {
        return Optional.of(
                "Computes the t-wise coverage of a given sample. To calculate the number of invalid interactions either a feature model or a reference sample must be provided.");
    }

    @Override
    protected IComputation<CoverageStatistic> newComputation(OptionList optionParser) {
        coverageOnly = optionParser.getResult(COVERAGE_ONLY_OPTION).orElseThrow();
        countOnly = optionParser.getResult(COUNT_ONLY_OPTION).orElseThrow();

        if (optionParser.has(FM_OPTION) && optionParser.has(REFERENCE_SAMPLE_OPTION)) {
            throw new IllegalArgumentException("Cannot set " + FM_OPTION.getArgumentName() + " and "
                    + REFERENCE_SAMPLE_OPTION.getArgumentName() + " at the same time!");
        }

        Path fmPath = optionParser.getResult(FM_OPTION).orElse(null);
        Path referencePath = optionParser.getResult(REFERENCE_SAMPLE_OPTION).orElse(null);
        int t = optionParser.get(T_OPTION);

        if (fmPath != null && referencePath != null) {
            throw new IllegalArgumentException("Cannot set " + FM_OPTION.getArgumentName() + " and "
                    + REFERENCE_SAMPLE_OPTION.getArgumentName() + " at the same time!");
        }
        IComputation<BooleanAssignmentList> sample = readFromInput(
                        optionParser, BooleanAssignmentGroupsFormats.getInstance())
                .map(BooleanAssignmentGroups::getFirstGroup)
                .toComputation();

        IComputation<CoverageStatistic> coverageComputation;
        if (fmPath != null) {
            coverageComputation = computeFMCoverage(sample, fmPath, t);
        } else if (referencePath != null) {
            coverageComputation = computeRelativeCoverage(sample, referencePath, t);
        } else {
            coverageComputation = computeAbsoluteCoverage(sample);
            coverageComputation.set(
                    AComputeTWiseCoverage.COMBINATION_SET,
                    sample.map(VariableCombinationSpecificationComputation::new)
                            .set(VariableCombinationSpecificationComputation.T, t));
        }

        Result<Path> consideredInteractionsPath = optionParser.getResult(INCLUDE_INTERACTIONS);
        if (consideredInteractionsPath.isPresent()) {
            BooleanAssignmentGroups consideredInteractions = IO.load(
                            consideredInteractionsPath.get(), new BooleanAssignmentGroupsFormats())
                    .orElseLog(Verbosity.WARNING);
            if (consideredInteractions != null) {
                coverageComputation.set(
                        AComputeTWiseCoverage.INCLUDE_INTERACTIONS,
                        new SampleBitIndex(consideredInteractions.getMergedGroups()));
            }
        }
        Result<Path> ignoreInteractionsPath = optionParser.getResult(EXCLUDE_INTERACTIONS);
        if (ignoreInteractionsPath.isPresent()) {
            BooleanAssignmentGroups ignoreInteractions = IO.load(
                            ignoreInteractionsPath.get(), new BooleanAssignmentGroupsFormats())
                    .orElseLog(Verbosity.WARNING);
            if (ignoreInteractions != null) {
                coverageComputation.set(
                        AComputeTWiseCoverage.EXCLUDE_INTERACTIONS,
                        new SampleBitIndex(ignoreInteractions.getMergedGroups()));
            }
        }

        return coverageComputation;
    }

    private IComputation<CoverageStatistic> computeAbsoluteCoverage(IComputation<BooleanAssignmentList> sample) {
        return sample.map(ComputeAbsoluteTWiseCoverage::new);
    }

    private IComputation<CoverageStatistic> computeRelativeCoverage(
            IComputation<BooleanAssignmentList> sample, Path referencePath, int t) {
        BooleanAssignmentList referenceSample = IO.load(referencePath, BooleanAssignmentGroupsFormats.getInstance())
                .map(BooleanAssignmentGroups::getFirstGroup)
                .orElseThrow();

        return sample.map(ComputeRelativeTWiseCoverage::new)
                .set(ComputeRelativeTWiseCoverage.REFERENCE_SAMPLE, referenceSample)
                .set(
                        ComputeRelativeTWiseCoverage.COMBINATION_SET,
                        new VariableCombinationSpecificationComputation(
                                Computations.of(referenceSample), Computations.of(t)));
    }

    private IComputation<CoverageStatistic> computeFMCoverage(
            IComputation<BooleanAssignmentList> sample, Path fmPath, int t) {
        BooleanAssignmentList formula = IO.load(fmPath, BooleanAssignmentGroupsFormats.getInstance())
                .map(cnf -> (IComputation<BooleanAssignmentList>)
                        Computations.of(cnf.getFirstGroup().toClauseList()))
                .orElseGet(() -> IO.load(fmPath, FormulaFormats.getInstance())
                        .toComputation()
                        .map(ComputeNNFFormula::new)
                        .map(ComputeCNFFormula::new)
                        .map(ComputeBooleanClauseList::new))
                .computeResult()
                .orElseThrow();
        return sample.map(ComputeConstraintedTWiseCoverage::new)
                .set(ComputeConstraintedTWiseCoverage.BOOLEAN_CLAUSE_LIST, formula)
                .set(
                        AComputeTWiseCoverage.COMBINATION_SET,
                        new VariableCombinationSpecificationComputation(Computations.of(formula), Computations.of(t)));
    }

    @Override
    protected IFormat<CoverageStatistic> getOuputFormat(OptionList optionParser) {
        return new CoverageStatisticTextFormat(coverageOnly, countOnly);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("t-wise-coverage");
    }
}
