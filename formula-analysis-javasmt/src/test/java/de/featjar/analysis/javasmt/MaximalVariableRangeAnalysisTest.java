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
import de.featjar.analysis.javasmt.computation.ComputeMaximalVariableRange;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

public class MaximalVariableRangeAnalysisTest {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void formulaHasTwoVariablesWithMaximalRange2And6() {
        final Variable a = new Variable("a", Double.class);
        final Variable b = new Variable("b", Double.class);
        final Constant constant3 = new Constant(3L);
        final Constant constant7 = new Constant(7L);

        final LessThan lessThanA = new LessThan(a, constant3);
        final LessThan lessThanB = new LessThan(b, constant7);
        final And formula = new And(lessThanA, lessThanB);

        Map<Variable, Object> solutionMaximalRanges = new HashMap<>();
        solutionMaximalRanges.put(a, Rational.ofString("2999/1000"));
        solutionMaximalRanges.put(b, Rational.ofString("6999/1000"));

        final Result<Map<Variable, Object>> result = Computations.of(formula)
                .map(ComputeJavaSMTFormula::new)
                .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                .map(ComputeMaximalVariableRange::new)
                .computeResult();

        assertTrue(result.isPresent(), () -> Problem.printProblems(result.getProblems()));
        assertEquals(solutionMaximalRanges, result.get());
    }
}
