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
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.assignment.BooleanAssignment;
import java.time.Duration;
import java.util.List;

/**
 * Base class for analyses using a SharpSat Solver.
 *
 * @param <T> Type of the analysis result.
 *
 * @author Sebastian Krieter
 */
public abstract class ADdnnifeAnalysis<T> extends AComputation<T> {
    public static final Dependency<DdnnifeWrapper> DDNNIFE_WRAPPER = Dependency.newDependency(DdnnifeWrapper.class);
    public static final Dependency<BooleanAssignment> ASSUMED_ASSIGNMENT =
            Dependency.newDependency(BooleanAssignment.class);
    public static final Dependency<Duration> SAT_TIMEOUT = Dependency.newDependency(Duration.class);

    public ADdnnifeAnalysis(IComputation<DdnnifeWrapper> ddnnifeWrapper, Object... computations) {
        super(ddnnifeWrapper, Computations.of(new BooleanAssignment()), Computations.of(Duration.ZERO), computations);
    }

    protected ADdnnifeAnalysis(ADdnnifeAnalysis<T> other) {
        super(other);
    }

    public DdnnifeWrapper setup(List<Object> dependencyList) {
        DdnnifeWrapper ddnnife = DDNNIFE_WRAPPER.get(dependencyList);
        BooleanAssignment assumedAssignment = ASSUMED_ASSIGNMENT.get(dependencyList);
        FeatJAR.log().debug("assuming %s", assumedAssignment);
        ddnnife.setAssumptions(assumedAssignment);
        ddnnife.setTimeout(SAT_TIMEOUT.get(dependencyList));
        return ddnnife;
    }
}
