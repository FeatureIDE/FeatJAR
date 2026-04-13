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

import de.featjar.analysis.sat4j.computation.YASA;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.combination.VariableCombinationSpecification.VariableCombinationSpecificationComputation;
import java.util.Optional;

/**
 * Computes solutions for a given formula using SAT4J.
 *
 * @author Sebastian Krieter
 */
public class TWiseCommand extends ATWiseCommand {

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes solutions for a given formula using SAT4J. Uses a simple greedy algorithm.");
    }

    @Override
    public IComputation<BooleanAssignmentList> newTWiseAnalysis(
            OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        return formula.map(YASA::new)
                .set(YASA.SAT_TIMEOUT, optionParser.get(SAT_TIMEOUT_OPTION))
                .set(
                        YASA.COMBINATION_SET,
                        formula.map(VariableCombinationSpecificationComputation::new)
                                .set(VariableCombinationSpecificationComputation.T, optionParser.get(T_OPTION)))
                .set(YASA.ITERATIONS, 0);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("t-wise-sat4j");
    }
}
