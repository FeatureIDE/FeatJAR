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
package de.featjar.analysis.ddnnife.computation;

import de.featjar.analysis.ddnnife.solver.DdnnifeWrapper;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.List;

/**
 * Counts the number of valid solutions to a formula.
 *
 * @author Sebastian Krieter
 */
public class ComputeSolutionDdnnife extends ADdnnifeAnalysis<BooleanSolution> {

    public ComputeSolutionDdnnife(IComputation<DdnnifeWrapper> ddnnifeWrapper) {
        super(ddnnifeWrapper);
    }

    protected ComputeSolutionDdnnife(ComputeSolutionDdnnife other) {
        super(other);
    }

    @Override
    public Result<BooleanSolution> compute(List<Object> dependencyList, Progress progress) {
        return setup(dependencyList).getSolution();
    }
}
