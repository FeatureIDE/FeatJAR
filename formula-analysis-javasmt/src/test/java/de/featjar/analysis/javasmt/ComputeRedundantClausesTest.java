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
import de.featjar.analysis.javasmt.computation.ComputeRedundantClauses;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

public class ComputeRedundantClausesTest {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void formulaHasOneRedundantClause() {
        final Variable a = new Variable("a", Double.class);
        final Variable b = new Variable("b", Double.class);

        final Constant constant2 = new Constant(2L);
        final Constant constant3 = new Constant(3L);

        final GreaterThan greaterThanA = new GreaterThan(a, constant3);
        final LessThan lessThanB = new LessThan(b, constant2);
        final Equals equalsAB = new Equals(a, b);
        final Not redundantClause = new Not(equalsAB);
        final And and = new And(greaterThanA, lessThanB, redundantClause);
        final Reference formula = new Reference(and);

        List<IExpression> expectedSolution = Arrays.asList(redundantClause);

        final Result<List<IExpression>> result = Computations.of(formula)
                .map(ComputeJavaSMTFormula::new)
                .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                .map(ComputeRedundantClauses::new)
                .computeResult();

        assertTrue(result.isPresent(), () -> Problem.printProblems(result.getProblems()));
        assertEquals(expectedSolution, result.get());
    }

    @Test
    public void formulaHasThreeRedundantClauses() {
        final Variable a = new Variable("a", Double.class);
        final Constant constant3 = new Constant(3L);

        final GreaterEqual greaterEqualA = new GreaterEqual(a, constant3);
        final LessEqual lessEqualA = new LessEqual(a, constant3);
        final Equals redundantClause = new Equals(a, constant3);
        final And and = new And(greaterEqualA, lessEqualA, redundantClause);
        final Reference formula = new Reference(and);

        List<IExpression> expectedSolution = Arrays.asList(greaterEqualA, lessEqualA, redundantClause);

        final Result<List<IExpression>> result = Computations.of(formula)
                .map(ComputeJavaSMTFormula::new)
                .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                .map(ComputeRedundantClauses::new)
                .computeResult();

        assertTrue(result.isPresent(), () -> Problem.printProblems(result.getProblems()));
        assertEquals(expectedSolution, result.get());
    }

    @Test
    public void formulaHasFourRedundantClauses() {
        final Variable a = new Variable("a", Double.class);
        final Variable b = new Variable("b", Double.class);
        final Constant constant3 = new Constant(3L);

        final GreaterEqual greaterEqualA = new GreaterEqual(a, constant3);
        final LessEqual lessEqualA = new LessEqual(a, constant3);
        final Equals redundantClause1 = new Equals(a, constant3);
        final GreaterThan greaterThanB = new GreaterThan(b, constant3);
        final Equals equalsAB = new Equals(a, b);
        final Not redundantClause2 = new Not(equalsAB);
        final And and = new And(greaterEqualA, lessEqualA, redundantClause1, greaterThanB, redundantClause2);
        final Reference formula = new Reference(and);

        List<IExpression> expectedSolution =
                Arrays.asList(greaterEqualA, lessEqualA, redundantClause1, redundantClause2);

        final Result<List<IExpression>> result = Computations.of(formula)
                .map(ComputeJavaSMTFormula::new)
                .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                .map(ComputeRedundantClauses::new)
                .computeResult();

        assertTrue(result.isPresent(), () -> Problem.printProblems(result.getProblems()));
        assertEquals(expectedSolution, result.get());
    }
}
