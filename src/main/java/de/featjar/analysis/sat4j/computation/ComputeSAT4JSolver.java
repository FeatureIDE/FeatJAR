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
package de.featjar.analysis.sat4j.computation;

import de.featjar.analysis.sat4j.solver.SAT4JSolver;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.List;

public class ComputeSAT4JSolver extends ASAT4JAnalysis.Solution<SAT4JSolver> {

    public ComputeSAT4JSolver(IComputation<BooleanAssignmentList> clauseList) {
        super(clauseList);
    }

    protected ComputeSAT4JSolver(ComputeSAT4JSolver other) {
        super(other);
    }

    @Override
    public Result<SAT4JSolver> compute(List<Object> dependencyList, Progress progress) {
        return Result.of(createSolver(dependencyList));
    }
}
