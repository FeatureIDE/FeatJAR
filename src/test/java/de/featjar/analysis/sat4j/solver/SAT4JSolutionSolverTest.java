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
package de.featjar.analysis.sat4j.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class SAT4JSolutionSolverTest {

    @Test
    void solverSolutionsContainCorrectValuesForFreeVariables() {
        VariableMap variableMap = new VariableMap(Arrays.asList("A", "B", "C"));
        BooleanAssignment clause1 = variableMap.getVariables(Arrays.asList("A"));
        BooleanAssignment clause2 = variableMap.getVariables(Arrays.asList("B"));
        BooleanAssignment clause3 = variableMap.getVariables(Arrays.asList("A", "B"));
        SAT4JSolutionSolver solver =
                new SAT4JSolutionSolver(new BooleanAssignmentList(variableMap, clause1, clause2, clause3), false);

        solver.setSelectionStrategy(ISelectionStrategy.positive());
        final Result<BooleanSolution> positiveSolution = solver.findSolution();
        assertTrue(positiveSolution.isPresent(), () -> Problem.printProblems(positiveSolution.getProblems()));
        BooleanSolution positiveAssignment =
                variableMap.getVariables(Arrays.asList("A", "B", "C")).toSolution();
        assertEquals(positiveAssignment, positiveSolution.get());

        solver.setSelectionStrategy(ISelectionStrategy.negative());
        final Result<BooleanSolution> negativeSolution = solver.findSolution();
        assertTrue(negativeSolution.isPresent(), () -> Problem.printProblems(negativeSolution.getProblems()));
        BooleanSolution negativeAssignment = new BooleanSolution(new int[] {1, 2, -3});
        assertEquals(negativeAssignment, negativeSolution.get());
    }
}
