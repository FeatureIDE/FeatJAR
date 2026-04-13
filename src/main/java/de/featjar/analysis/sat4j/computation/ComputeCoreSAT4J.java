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
import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Finds core and dead features.
 *
 * @author Sebastian Krieter
 */
public class ComputeCoreSAT4J extends ASAT4JAnalysis.Solution<BooleanAssignmentList> {
    protected static final Dependency<BooleanAssignment> VARIABLES_OF_INTEREST =
            Dependency.newDependency(BooleanAssignment.class);

    public ComputeCoreSAT4J(IComputation<BooleanAssignmentList> clauseList) {
        super(clauseList, new ComputeConstant<>(new BooleanAssignment()));
    }

    protected ComputeCoreSAT4J(ComputeCoreSAT4J other) {
        super(other);
    }

    @Override
    public Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        SAT4JSolutionSolver solver = createSolver(dependencyList);
        Random random = new Random(RANDOM_SEED.get(dependencyList));
        BooleanAssignment variablesOfInterest = VARIABLES_OF_INTEREST.get(dependencyList);
        VariableMap variableMap = BOOLEAN_CLAUSE_LIST.get(dependencyList).getVariableMap();
        int variableCount = variableMap.size();

        checkCancel();
        progress.setTotalSteps(variableCount + 2);

        solver.setSelectionStrategy(ISelectionStrategy.positive());
        Result<Boolean> hasSolution = solver.hasSolution();
        if (hasSolution.isEmpty()) {
            return hasSolution.nullify();
        } else if (hasSolution.valueEquals(Boolean.FALSE)) {
            return Result.of(new BooleanAssignmentList(variableMap));
        }
        int[] potentialCore = Arrays.copyOf(solver.getInternalSolution(), variableCount);

        progress.incrementCurrentStep();
        checkCancel();

        solver.setSelectionStrategy(ISelectionStrategy.inverse(potentialCore));
        hasSolution = solver.hasSolution();
        if (hasSolution.isEmpty()) {
            return hasSolution.nullify();
        }
        BooleanSolution.removeConflictsInplace(potentialCore, solver.getInternalSolution());
        solver.shuffleOrder(random);
        solver.setSelectionStrategy(ISelectionStrategy.random(random));

        progress.incrementCurrentStep();
        checkCancel();

        if (!variablesOfInterest.isEmpty()) {
            for (int l : variablesOfInterest.get()) {
                potentialCore[l - 1] = 0;
            }
        }

        for (int i = 0; i < variableCount; i++) {
            progress.incrementCurrentStep();
            checkCancel();
            final int l = potentialCore[i];
            if (l != 0) {
                solver.getAssignment().add(-l);
                hasSolution = solver.hasSolution();
                if (hasSolution.valueEquals(false)) {
                    solver.getAssignment().replaceLast(l);
                } else if (hasSolution.isEmpty()) {
                    solver.getAssignment().remove();
                } else if (hasSolution.valueEquals(true)) {
                    solver.getAssignment().remove();
                    BooleanSolution.removeConflictsInplace(potentialCore, solver.getInternalSolution());
                    solver.shuffleOrder(random);
                }
            }
        }

        return solver.createResult(
                new BooleanAssignmentList(variableMap, solver.getAssignment().toAssignment()));
    }
}
