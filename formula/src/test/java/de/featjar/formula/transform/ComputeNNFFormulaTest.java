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

import static de.featjar.formula.structure.Expressions.False;
import static de.featjar.formula.structure.Expressions.True;
import static de.featjar.formula.structure.Expressions.and;
import static de.featjar.formula.structure.Expressions.forAll;
import static de.featjar.formula.structure.Expressions.implies;
import static de.featjar.formula.structure.Expressions.literal;
import static de.featjar.formula.structure.Expressions.not;
import static de.featjar.formula.structure.Expressions.or;
import static de.featjar.formula.structure.Expressions.variable;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.predicate.ExpressionKind;
import org.junit.jupiter.api.Test;

class ComputeNNFFormulaTest {

    void nnf(IFormula formula, IFormula newFormula) {
        TransformationTest.traverseAndAssertFormulaEquals(formula, ComputeNNFFormula::new, newFormula);
        assertTrue(newFormula.isKind(ExpressionKind.BOOLEAN));
    }

    void fails(IFormula formula) {
        TransformationTest.traverseAndAssertFail(formula, ComputeNNFFormula::new);
    }

    @Test
    public void toNNF() {
        fails(and());
        fails(True);
        fails(implies(literal("a"), forAll(variable("x"), True)));
        nnf(and(literal("a")), literal("a"));
        nnf(and(literal("a"), True), literal("a"));
        nnf(implies(literal("a"), False), literal(false, "a"));
        nnf(not(or(literal("a"), literal("b"))), and(literal(false, "a"), literal(false, "b")));
        nnf(
                and(not(or(literal("a"), literal("b"))), literal("c")),
                and(literal(false, "a"), literal(false, "b"), literal("c")));
        nnf(
                and(True, and(True, and(True), or(False), or(True, False)), literal("x")),
                and(literal("x"), literal(false, "x")));
    }
}
