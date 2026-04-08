/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sharpsat.
 *
 * formula-analysis-sharpsat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sharpsat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sharpsat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sharpsat> for further information.
 */
package de.featjar.analysis.sharpsat.computation;

import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.IFormula;
import java.math.BigInteger;
import java.util.List;

public class ComputeSolutionCountSharpSAT extends ASharpSATAnalysis<BigInteger> {
    public ComputeSolutionCountSharpSAT(IComputation<? extends IFormula> cnfFormula) {
        super(cnfFormula);
    }

    public ComputeSolutionCountSharpSAT(ComputeSolutionCountSharpSAT other) {
        super(other);
    }

    @Override
    public Result<BigInteger> compute(List<Object> dependencyList, Progress progress) {
        return initializeSolver(dependencyList).countSolutions();
    }
}
