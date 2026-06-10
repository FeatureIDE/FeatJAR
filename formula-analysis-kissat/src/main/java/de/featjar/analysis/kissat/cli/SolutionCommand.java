/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-kissat.
 *
 * formula-analysis-kissat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-kissat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-kissat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-cadical> for further information.
 */
package de.featjar.analysis.kissat.cli;

import de.featjar.analysis.kissat.computation.ComputeGetSolutionKissat;
import de.featjar.base.cli.AOption;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.io.csv.BooleanAssignmentListCSVFormat;
import java.util.Optional;

public class SolutionCommand extends AKissatAnalysisCommand<BooleanAssignmentList> {

    public static final AOption<IFormat<BooleanAssignmentList>> FORMAT = Options.newOutputFormatOption(
            BooleanAssignmentListFormats.class, new BooleanAssignmentListCSVFormat().getName());

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes a solution for a given formula using kissat");
    }

    @Override
    public IComputation<BooleanAssignmentList> newAnalysis(
            OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        return formula.map(ComputeGetSolutionKissat::new)
                .mapResult(SolutionCommand.class, "group", a -> new BooleanAssignmentList(variableMap, a));
    }

    @Override
    protected IFormat<BooleanAssignmentList> getOuputFormat(OptionList optionParser) {
        return optionParser.get(FORMAT);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("solution-kissat");
    }
}
