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

import de.featjar.analysis.sat4j.computation.YASALegacy;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.Optional;

/**
 * Computes solutions for a given formula using SAT4J.
 *
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public class LegacyYASACommand extends ATWiseCommand {

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

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes solutions for a given formula using SAT4J. Uses YASA.");
    }

    @Override
    public IComputation<BooleanAssignmentList> newTWiseAnalysis(
            OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        return formula.map(YASALegacy::new)
                .set(YASALegacy.T, optionParser.get(T_OPTION))
                .set(YASALegacy.ITERATIONS, optionParser.get(ITERATIONS_OPTION))
                .set(YASALegacy.SAT_TIMEOUT, optionParser.get(SAT_TIMEOUT_OPTION))
                .set(YASALegacy.INTERNAL_SOLUTION_LIMIT, optionParser.get(INTERNAL_SOLUTION_LIMIT));
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("yasa-legacy");
    }
}
