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
package de.featjar.analysis.ganak.computation;

import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.List;

/**
 * An analysis for checking whether the given formula is satisfiable.
 */
public class ComputeSolutionGanak extends AGanakAnalysis<Boolean> {

    public ComputeSolutionGanak(IComputation<BooleanAssignmentList> cnfFormula) {
        super(cnfFormula);
    }

    public ComputeSolutionGanak(ComputeCountSolutionGanak other) {
        super(other);
    }

    @Override
    public Result<Boolean> compute(List<Object> dependencyList, Progress progress) {
        return initializeSolver(dependencyList).hasSolution();
    }
}
