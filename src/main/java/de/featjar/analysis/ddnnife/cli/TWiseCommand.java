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
import de.featjar.analysis.ddnnife.computation.ComputeTWiseSampleDdnnife;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.io.csv.BooleanAssignmentGroupsUngroupedCSVFormat;
import java.util.Optional;

public class TWiseCommand extends ADdnnifeAnalysisCommand<BooleanAssignmentGroups> {

    public static final Option<Integer> T_OPTION = Option.newOption("t", Option.IntegerParser) //
            .setDescription("Value of parameter t.") //
            .setDefaultValue(2);

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes a t-wise sample for a given formula using ddnnife");
    }

    @Override
    public IComputation<BooleanAssignmentGroups> newAnalysis(OptionList optionParser, ComputeDdnnifeWrapper formula) {
        return formula.map(ComputeTWiseSampleDdnnife::new)
                .set(ComputeTWiseSampleDdnnife.T, optionParser.get(T_OPTION))
                .set(ComputeTWiseSampleDdnnife.RANDOM_SEED, optionParser.get(RANDOM_SEED_OPTION))
                .mapResult(TWiseCommand.class, "group", BooleanAssignmentGroups::new);
    }

    @Override
    protected IFormat<BooleanAssignmentGroups> getOuputFormat(OptionList optionaParser) {
        return new BooleanAssignmentGroupsUngroupedCSVFormat();
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("t-wise-ddnnife");
    }
}
