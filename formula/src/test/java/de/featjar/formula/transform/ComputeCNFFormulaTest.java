/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.transform;

import static de.featjar.base.computation.Computations.async;
import static de.featjar.formula.structure.Expressions.and;
import static de.featjar.formula.structure.Expressions.biImplies;
import static de.featjar.formula.structure.Expressions.implies;
import static de.featjar.formula.structure.Expressions.literal;
import static de.featjar.formula.structure.Expressions.not;
import static de.featjar.formula.structure.Expressions.or;
import static de.featjar.formula.structure.Expressions.reference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.Common;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.VariableMap;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ComputeCNFFormulaTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    IComputation<? extends IFormula> toCNF(IComputation<IFormula> formula) {
        return formula.map(ComputeNNFFormula::new).map(ComputeCNFFormula::new);
    }

    @Test
    public void doesNothing() {
        TransformationTest.traverseAndAssertSameFormula(
                and(or(literal("a"), literal("b")), or(literal("c"))), this::toCNF);
    }

    @Test
    public void toCNF() {
        TransformationTest.traverseAndAssertFormulaEquals(
                or(and(literal("a"), literal("b")), and(literal("c"))),
                this::toCNF,
                and(or(literal("c"), literal("b")), or(literal("c"), literal("a"))));
    }

    @Test
    void basic() {
        IFormula formula = loadFormula("testFeatureModels/basic.xml");
        assertEquals(
                reference(and(
                        literal("Root"),
                        implies(literal("A"), literal("Root")),
                        implies(literal("B"), literal("Root")),
                        and(literal("A"), literal("B")))),
                formula);
        IFormula finalFormula = formula;
        formula = Computations.of(finalFormula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .get()
                .get();
        assertEquals(
                reference(and(
                        or(literal("Root")),
                        or(literal(false, "A"), literal("Root")),
                        or(literal(false, "B"), literal("Root")),
                        or(literal("A")),
                        or(literal("B")))),
                formula);
    }

    @Test
    void tseitin() {
        IFormula formula = not(
                or(and(literal("C"), biImplies(or(literal("D"), literal("E")), literal("C"))), and(or(literal("E")))));
        IFormula distributiveCNF = async(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .get()
                .get();
        IFormula tseitinCNF = async(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .set(ComputeCNFFormula.MAXIMUM_NUMBER_OF_LITERALS, 0)
                .get()
                .get();

        VariableMap variableMap = new VariableMap(formula);
        FormulaCreator.streamAllAssignments(formula.getVariables().size()).forEach(assignment -> {
            Object formulaEvaluate = formula.evaluate(assignment, variableMap).orElse(null);
            Object distributiveEvaluate =
                    distributiveCNF.evaluate(assignment, variableMap).orElse(null);
            assertEquals(formulaEvaluate, distributiveEvaluate, assignment::print);
            Object tseitinEvaluate =
                    tseitinCNF.evaluate(assignment, variableMap).orElse(null);
            assertTrue(tseitinEvaluate == null || Objects.equals(tseitinEvaluate, formulaEvaluate), assignment::print);
        });
    }
}
