/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-cadical.
 *
 * formula-analysis-cadical is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-cadical is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-cadical. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-cadical> for further information.
 */
package de.featjar.analysis.cadical.cli;

import de.featjar.analysis.cadical.computation.ComputeCoreCadiCal;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.dimacs.BooleanAssignmentGroupsDimacsFormat;
import java.util.Optional;

public class CoreCommand extends ACadicalAnalysisCommand<BooleanAssignmentGroups> {

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes core and dead variables for a given formula using cadical");
    }

    @Override
    public IComputation<BooleanAssignmentGroups> newAnalysis(
            OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        return formula.map(ComputeCoreCadiCal::new)
                .mapResult(CoreCommand.class, "group", a -> new BooleanAssignmentGroups(variableMap, a));
    }

    @Override
    protected IFormat<BooleanAssignmentGroups> getOuputFormat(OptionList optionaParser) {
        return new BooleanAssignmentGroupsDimacsFormat();
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("core-cadical");
    }
}
