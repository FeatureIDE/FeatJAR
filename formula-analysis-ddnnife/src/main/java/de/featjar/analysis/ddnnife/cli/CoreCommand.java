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

import de.featjar.analysis.ddnnife.computation.ComputeCoreDeadDdnnife;
import de.featjar.analysis.ddnnife.computation.ComputeDdnnifeWrapper;
import de.featjar.base.cli.AOption;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.io.dimacs.BooleanAssignmentListDimacsFormat;
import java.util.Optional;

public class CoreCommand extends ADdnnifeAnalysisCommand<BooleanAssignmentList> {

    public static final AOption<IFormat<BooleanAssignmentList>> FORMAT = Options.newOutputFormatOption(
            BooleanAssignmentListFormats.class, new BooleanAssignmentListDimacsFormat().getName());

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes core and dead variables for a given formula using ddnnife");
    }

    @Override
    public IComputation<BooleanAssignmentList> newAnalysis(OptionList optionParser, ComputeDdnnifeWrapper ddnnife) {
        return ddnnife.map(ComputeCoreDeadDdnnife::new)
                .mapResult(CoreCommand.class, "group", a -> new BooleanAssignmentList(variableMap, a));
    }

    @Override
    protected IFormat<BooleanAssignmentList> getOuputFormat(OptionList optionParser) {
        return optionParser.get(FORMAT);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("core-ddnnife");
    }
}
