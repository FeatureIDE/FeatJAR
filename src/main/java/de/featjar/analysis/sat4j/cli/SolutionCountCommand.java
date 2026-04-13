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

import de.featjar.analysis.sat4j.computation.ComputeSolutionCountSAT4J;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.math.BigInteger;
import java.util.Optional;

/**
 * Computes number of solutions for a given formula using SAT4J.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 * @author Andreas Gerasimow
 */
public class SolutionCountCommand extends ASAT4JAnalysisCommand<BigInteger> {

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes number of solutions for a given formula using SAT4J.");
    }

    @Override
    public IComputation<BigInteger> newAnalysis(OptionList optionParser, IComputation<BooleanAssignmentList> formula) {
        return formula.map(ComputeSolutionCountSAT4J::new);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("count-sat4j");
    }
}
