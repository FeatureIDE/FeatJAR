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
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.FormulaFormats;
import java.nio.file.Path;
import java.time.Duration;

public abstract class ASAT4JAnalysisCommand<T> extends AAnalysisCommand<T> {

    /**
     * Option for setting the seed for the pseudo random generator.
     */
    public static final Option<Long> RANDOM_SEED_OPTION = Options.newOption("seed", Options.LongParser) //
            .setDescription("Seed for the pseudo random generator") //
            .setDefaultArgument("1");

    /**
     * Option for providing a file listing variables to ignore.
     */
    public static final Option<Path> IGNORE_VARIABLES = Options.newOption("ignore-variables", Options.PathParser) //
            .setDescription("A file listing variables to ignore.");

    /**
     * Timeout option for canceling running computations.
     */
    public static final Option<Duration> SAT_TIMEOUT_OPTION = Options.newOption(
                    "solver_timeout", s -> Duration.ofMillis(Long.parseLong(s)))
            .setDescription("Timeout in milliseconds")
            .setValidator(timeout -> !timeout.isNegative())
            .setDefaultArgument("0");

    protected ComputeBooleanClauseList createCNFComputation(OptionList optionParser) {
        return loadComputation(optionParser, FormulaFormats.getInstance())
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new);
    }

}
