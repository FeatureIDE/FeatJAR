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

import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy.NonParameterStrategy;
import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.List;
import java.util.Random;

public class ComputeSolutionSAT4J extends ASAT4JAnalysis.Solution<BooleanSolution> {

    public static final Dependency<ISelectionStrategy.NonParameterStrategy> SELECTION_STRATEGY =
            Dependency.newDependency(ISelectionStrategy.NonParameterStrategy.class);

    public ComputeSolutionSAT4J(IComputation<BooleanAssignmentList> clauseList) {
        super(clauseList, Computations.of(ISelectionStrategy.NonParameterStrategy.ORIGINAL));
    }

    protected ComputeSolutionSAT4J(ComputeSolutionSAT4J other) {
        super(other);
    }

    @Override
    public Result<BooleanSolution> compute(List<Object> dependencyList, Progress progress) {
        SAT4JSolutionSolver solver = (SAT4JSolutionSolver) createSolver(dependencyList);
        final NonParameterStrategy strategy = SELECTION_STRATEGY.get(dependencyList);
        switch (strategy) {
            case FAST_RANDOM:
                Random random = new Random(RANDOM_SEED.get(dependencyList));
                solver.setSelectionStrategy(ISelectionStrategy.random(random));
                solver.shuffleOrder(random);
                break;
            case NEGATIVE:
                solver.setSelectionStrategy(ISelectionStrategy.negative());
                break;
            case ORIGINAL:
                break;
            case POSITIVE:
                solver.setSelectionStrategy(ISelectionStrategy.positive());
                break;
            default:
                break;
        }
        return solver.findSolution();
    }
}
