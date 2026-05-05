/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-kissat.
 *
 * formula-analysis-kissat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-kissat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-kissat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-cadical> for further information.
 */
package de.featjar.analysis.kissat.computation;

import de.featjar.analysis.kissat.solver.KissatSolver;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.time.Duration;
import java.util.List;

public abstract class AKissatAnalysis<T> extends AComputation<T> {
    public static final Dependency<BooleanAssignmentList> FORMULA =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<Duration> TIMEOUT = Dependency.newDependency(Duration.class);

    public AKissatAnalysis(IComputation<BooleanAssignmentList> formula, Object... dependencies) {
        super(formula, Computations.of(Duration.ZERO), dependencies);
    }

    public AKissatAnalysis(AKissatAnalysis<?> other) {
        super(other);
    }

    public KissatSolver initializeSolver(List<Object> dependencyList) {
        BooleanAssignmentList formula = FORMULA.get(dependencyList);
        Duration timeout = TIMEOUT.get(dependencyList);
        FeatJAR.log().debug("initializing kissat solver");
        FeatJAR.log().debug(formula);
        KissatSolver solver = new KissatSolver(formula);
        solver.setTimeout(timeout);
        return solver;
    }
}
