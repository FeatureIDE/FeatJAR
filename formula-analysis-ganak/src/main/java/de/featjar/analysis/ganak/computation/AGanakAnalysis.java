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

import de.featjar.analysis.ganak.solver.GanakSolver;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.time.Duration;
import java.util.List;

/**
 * Base class for analyses using a Ganak Solver.
 *
 * @param <T> type of the analysis result
 */
public abstract class AGanakAnalysis<T> extends AComputation<T> {
    public static final Dependency<BooleanAssignmentList> FORMULA =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<Duration> TIMEOUT = Dependency.newDependency(Duration.class);

    public AGanakAnalysis(IComputation<BooleanAssignmentList> formula, Object... dependencies) {
        super(formula, Computations.of(Duration.ZERO), dependencies);
    }

    public AGanakAnalysis(AGanakAnalysis<?> other) {
        super(other);
    }

    public GanakSolver initializeSolver(List<Object> dependencyList) {
        BooleanAssignmentList formula = FORMULA.get(dependencyList);
        Duration timeout = TIMEOUT.get(dependencyList);
        FeatJAR.log().debug("initializing ganak solver");
        FeatJAR.log().debug(formula);
        GanakSolver solver = new GanakSolver(formula);
        solver.setTimeout(timeout);
        return solver;
    }
}
