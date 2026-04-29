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

import de.featjar.analysis.RuntimeTimeoutException;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy.NonParameterStrategy;
import de.featjar.analysis.sat4j.solver.SAT4JAssignment;
import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.analysis.sat4j.solver.SAT4JSolver;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Converts a partial into a complete sample.
 *
 * @author Sebastian Krieter
 */
public class ComputeCompleteSample extends AComputation<BooleanAssignmentList> {

    public static final Dependency<BooleanAssignmentList> SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);

    public static final Dependency<BooleanAssignmentList> BOOLEAN_CLAUSE_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<BooleanAssignment> ASSUMED_ASSIGNMENT =
            Dependency.newDependency(BooleanAssignment.class);
    public static final Dependency<BooleanAssignmentList> ASSUMED_CLAUSE_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<Duration> SAT_TIMEOUT = Dependency.newDependency(Duration.class);

    public static final Dependency<ISelectionStrategy.NonParameterStrategy> SELECTION_STRATEGY =
            Dependency.newDependency(ISelectionStrategy.NonParameterStrategy.class);

    public static final Dependency<Long> RANDOM_SEED = Dependency.newDependency(Long.class);

    public ComputeCompleteSample(IComputation<BooleanAssignmentList> sample) {
        super(
                sample,
                Computations.of(new BooleanAssignmentList(null, 0)),
                Computations.of(new BooleanAssignment()),
                Computations.of(new BooleanAssignmentList(null, 0)),
                Computations.of(Duration.ZERO),
                Computations.of(ISelectionStrategy.NonParameterStrategy.FAST_RANDOM),
                Computations.of(1L));
    }

    protected Random random;

    protected BooleanAssignmentList partialSample;

    @Override
    public final Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        partialSample = SAMPLE.get(dependencyList);
        random = new Random(RANDOM_SEED.get(dependencyList));

        BooleanAssignmentList clauseList = BOOLEAN_CLAUSE_LIST.get(dependencyList);
        BooleanAssignment assumedAssignment = ASSUMED_ASSIGNMENT.get(dependencyList);
        BooleanAssignmentList assumedClauseList = ASSUMED_CLAUSE_LIST.get(dependencyList);
        Duration timeout = SAT_TIMEOUT.get(dependencyList);

        if (!Objects.equals(clauseList.getVariableMap(), partialSample.getVariableMap())) {
            throw new IllegalArgumentException("Variable maps of partial sample and clause list do not match.");
        }

        SAT4JSolutionSolver solver = new SAT4JSolutionSolver(clauseList);
        SAT4JSolver.initializeSolver(solver, clauseList, assumedAssignment, assumedClauseList, timeout);

        final NonParameterStrategy strategy = SELECTION_STRATEGY.get(dependencyList);
        switch (strategy) {
            case FAST_RANDOM:
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

        progress.setTotalSteps(partialSample.size());

        BooleanAssignmentList completeSample = new BooleanAssignmentList(partialSample.getVariableMap());
        for (BooleanAssignment configuration : partialSample) {
            int[] literals = configuration.get();
            SAT4JAssignment assignment = solver.getAssignment();
            final int orgAssignmentSize = assignment.size();
            for (int i = 0; i < literals.length; i++) {
                int l = literals[i];
                if (l != 0) {
                    assignment.add(l);
                }
            }
            try {
                Result<BooleanSolution> hasSolution = solver.findSolution();
                if (hasSolution.isPresent()) {
                    completeSample.add(new BooleanSolution(hasSolution.get()));
                    if (strategy == NonParameterStrategy.FAST_RANDOM) {
                        solver.shuffleOrder(random);
                    }
                } else {
                    return Result.empty(new RuntimeTimeoutException());
                }
            } finally {
                assignment.clear(orgAssignmentSize);
            }
            progress.incrementCurrentStep();
        }
        return Result.of(completeSample);
    }
}
