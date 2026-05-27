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
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

public class SolutionAnalysisTest {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void formulaHasSatisfyingAssignment() {
        final Literal a = Expressions.literal("a");
        final Literal b = Expressions.literal("b");
        final Literal c = Expressions.literal("c");

        final Implies implies1 = new Implies(a, b);
        final Or or = new Or(implies1, c);
        final BiImplies equals = new BiImplies(a, b);
        final And and = new And(equals, c);
        final Implies formula = new Implies(or, and);

        IFormula cnf = formula.toCNF().orElseThrow();

        // retrieve variableMap from first computation using ComputeJavaSMTFormula
        final Result<JavaSMTFormula> javaSMTFormulaResult = Computations.of(cnf)
                .map(ComputeJavaSMTFormula::new)
                .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                .computeResult();
        assertTrue(javaSMTFormulaResult.isPresent(), () -> Problem.printProblems(javaSMTFormulaResult.getProblems()));
        JavaSMTFormula javaSMTFormula = javaSMTFormulaResult.get();
        VariableMap variableMap = javaSMTFormula.getVariableMap();

        // get a satisfying assignment
        final Result<ValueAssignment> valueAssignmentResult =
                Computations.of(javaSMTFormula).map(ComputeSolution::new).computeResult();
        assertTrue(valueAssignmentResult.isPresent(), () -> Problem.printProblems(valueAssignmentResult.getProblems()));
        ValueAssignment valueAssignment = valueAssignmentResult.get();

        // use variableMap for evaluation of formula with the assignment
        boolean satisfiesFormula =
                (Boolean) formula.evaluate(valueAssignment, variableMap).orElseThrow();

        assertEquals(true, satisfiesFormula);
    }
}
