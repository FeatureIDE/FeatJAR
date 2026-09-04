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

import de.featjar.analysis.ganak.computation.ComputeSolutionGanak;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.Optional;

/**
 * A command which counts the number of satisfying assignments of a formula using Ganak, optionally with
 * projected or sliced literals.
 */
public class SolutionCommand extends AGanakAnalysisCommand<Boolean> {

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes the number of satisfying assignments using ganak");
    }

    @Override
    public IComputation<Boolean> newAnalysis(OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        return formula.map(ComputeSolutionGanak::new);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("solution-ganak");
    }
}
