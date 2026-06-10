/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ddnnife.
 *
 * formula-analysis-ddnnife is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ddnnife is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ddnnife. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/formula-analysis-ddnnife> for further information.
 */
package de.featjar.analysis.ddnnife.cli;

import de.featjar.analysis.ddnnife.computation.ComputeDdnnifeWrapper;
import de.featjar.analysis.ddnnife.computation.ComputeRandomSolutionsDdnnife;
import de.featjar.base.cli.AOption;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.io.csv.BooleanAssignmentListCSVFormat;
import java.util.Optional;

public class RandomSolutionsCommand extends ADdnnifeAnalysisCommand<BooleanAssignmentList> {

    public static final AOption<IFormat<BooleanAssignmentList>> FORMAT = Options.newOutputFormatOption(
            BooleanAssignmentListFormats.class, new BooleanAssignmentListCSVFormat().getName());

    public static final AOption<Integer> SOLUTION_COUNT_OPTION = Options.newOption("limit", Options.IntegerParser) //
            .setDescription("Number of solutions to compute") //
            .setDefaultValue("1");

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes a list of random solutions for a given formula using ddnnife");
    }

    @Override
    public IComputation<BooleanAssignmentList> newAnalysis(OptionList optionParser, ComputeDdnnifeWrapper formula) {
        return formula.map(ComputeRandomSolutionsDdnnife::new)
                .set(ComputeRandomSolutionsDdnnife.SOLUTION_COUNT, optionParser.get(SOLUTION_COUNT_OPTION))
                .set(ComputeRandomSolutionsDdnnife.RANDOM_SEED, optionParser.get(RANDOM_SEED_OPTION));
    }

    @Override
    protected IFormat<BooleanAssignmentList> getOuputFormat(OptionList optionParser) {
        return optionParser.get(FORMAT);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("random-solutions-ddnnife");
    }
}
