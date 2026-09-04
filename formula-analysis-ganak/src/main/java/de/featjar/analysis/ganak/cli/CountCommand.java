/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ganak.
 *
 * formula-analysis-ganak is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ganak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ganak. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-ganak> for further information.
 */
package de.featjar.analysis.ganak.cli;

import de.featjar.analysis.ganak.computation.ComputeCountSolutionGanak;
import de.featjar.base.cli.MultiOption;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * A command which counts the number of satisfying assignments of a formula using Ganak, optionally with
 * projected or sliced literals.
 */
public class CountCommand extends AGanakAnalysisCommand<BigInteger> {

    /**
     * Option for setting the literals to be removed
     */
    public static final MultiOption<String> LITERALS_SLICE_OPTION =
            Options.newListOption("slice", Options.StringParser).setDescription("Literals to be removed.");

    /**
     * Option for setting the literals to be kept
     */
    public static final MultiOption<String> LITERALS_PROJECT_OPTION = Options.newListOption(
                    "project", Options.StringParser)
            .setDescription(
                    "Literals to be projected. If not set, all features will be projected. The slice option has a higher priority, i.e. if both the project and slice option contain the same literal, it will be removed.");

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes the number of satisfying assignments using ganak");
    }

    @Override
    public IComputation<BigInteger> newAnalysis(OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        // already compute cnf in order to obtain the VariableMap
        formula.computeResult();

        // retrieve variable indices using the VariableMap
        return formula.map(ComputeCountSolutionGanak::new)
                .set(
                        ComputeCountSolutionGanak.VARIABLES_TO_REMOVE,
                        getVariableIndices(optionParser, LITERALS_SLICE_OPTION))
                .set(
                        ComputeCountSolutionGanak.VARIABLES_TO_KEEP,
                        getVariableIndices(optionParser, LITERALS_PROJECT_OPTION));
    }

    private BooleanAssignment getVariableIndices(
            OptionList optionParser, final MultiOption<String> literalsSliceOption) {
        return new BooleanAssignment(optionParser.getResult(literalsSliceOption).orElse(List.of()).stream()
                .map(variableMap::get)
                .filter(Result::isPresent)
                .mapToInt(Result::get)
                .toArray());
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("count-ganak");
    }
}
