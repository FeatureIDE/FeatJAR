/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt.cli;

import de.featjar.analysis.javasmt.computation.ComputeSolutionCount;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.structure.IFormula;
import java.math.BigInteger;
import java.util.Optional;

public class CountCommand extends AJavasmtAnalysisCommand<BigInteger> {

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes the number of solutions for a given formula using javasmt");
    }

    @Override
    public IComputation<BigInteger> newAnalysis(IComputation<? extends IFormula> formula) {
        return formula.map(ComputeSolutionCount::new);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("count-javasmt");
    }
}
