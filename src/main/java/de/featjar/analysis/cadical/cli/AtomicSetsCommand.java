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

import de.featjar.analysis.cadical.computation.ComputeAtomicCadiCal;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.dimacs.BooleanAssignmentGroupsDimacsFormat;
import java.util.Optional;

public class AtomicSetsCommand extends ACadicalAnalysisCommand<BooleanAssignmentGroups> {

    public static final Option<Boolean> OMIT_SINGLE_SETS = Option.newFlag("omit-singles")
            .setDefaultValue(Boolean.FALSE)
            .setDescription("Omits sets with only one element");
    public static final Option<Boolean> OMIT_CORE =
            Option.newFlag("omit-core").setDefaultValue(Boolean.FALSE).setDescription("Omits set containing core");

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes atomic sets for a given formula using cadical.");
    }

    @Override
    public IComputation<BooleanAssignmentGroups> newAnalysis(
            OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        return formula.map(ComputeAtomicCadiCal::new)
                .set(ComputeAtomicCadiCal.OMIT_CORE, optionParser.get(OMIT_CORE))
                .set(ComputeAtomicCadiCal.OMIT_SINGLE_SETS, optionParser.get(OMIT_SINGLE_SETS))
                .mapResult(AtomicSetsCommand.class, "group", BooleanAssignmentGroups::new);
    }

    @Override
    protected IFormat<BooleanAssignmentGroups> getOuputFormat(OptionList optionaParser) {
        return new BooleanAssignmentGroupsDimacsFormat();
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("atomic-sets-cadical");
    }
}
