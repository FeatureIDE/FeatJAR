/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.analysis.javasmt.computation.ComputeJavaSMTFormula;
import de.featjar.analysis.javasmt.computation.ComputeSolution;
import de.featjar.analysis.javasmt.solver.JavaSMTFormula;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.ValueAssignment;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.predicate.Literal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class VariableNamesToAssignmentTest {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void variablesAreMappedToCorrespondingAssignment() {
        final Literal a = Expressions.literal("a");
        final Literal b = Expressions.literal("b");
        final Literal c = Expressions.literal("c");

        final And formula = new And(a, new Not(b), c);

        Map<String, Object> solutionAssignment = new HashMap<>();
        solutionAssignment.put("a", true);
        solutionAssignment.put("b", false);
        solutionAssignment.put("c", true);

        // retrieve variableMap from first computation using ComputeJavaSMTFormula
        IFormula cnf = formula.toCNF().orElseThrow();
        final Result<JavaSMTFormula> javaSMTFormulaResult =
                Computations.of(cnf).map(ComputeJavaSMTFormula::new).computeResult();
        assertTrue(javaSMTFormulaResult.isPresent(), () -> Problem.printProblems(javaSMTFormulaResult.getProblems()));
        JavaSMTFormula javaSMTFormula = javaSMTFormulaResult.get();
        VariableMap variableMap = javaSMTFormula.getVariableMap();

        // get a satisfying assignment
        final Result<ValueAssignment> valueAssignmentResult =
                Computations.of(javaSMTFormula).map(ComputeSolution::new).computeResult();
        assertTrue(valueAssignmentResult.isPresent(), () -> Problem.printProblems(valueAssignmentResult.getProblems()));
        ValueAssignment valueAssignment = valueAssignmentResult.get();

        // map each assignment in ValueAssignment to the corresponding variable name
        Map<String, Object> resultAssignment = new HashMap<>();
        for (Map.Entry<Integer, Object> entry : valueAssignment.getAll().entrySet()) {
            Integer index = entry.getKey();
            Object assignment = entry.getValue();

            Result<String> variableNameResult = variableMap.get(index);
            assertTrue(variableNameResult.isPresent(), () -> Problem.printProblems(variableNameResult.getProblems()));
            String variableName = variableMap.get(index).get();
            resultAssignment.put(variableName, assignment);
        }

        assertEquals(solutionAssignment, resultAssignment);
    }
}
