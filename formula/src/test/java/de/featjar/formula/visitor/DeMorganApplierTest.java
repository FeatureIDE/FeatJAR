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
package de.featjar.formula.visitor;

import static de.featjar.formula.structure.Expressions.*;

import org.junit.jupiter.api.Test;

class DeMorganApplierTest {
    @Test
    void doesNothingForNNF() {
        VisitorTest.traverseAndAssertSameFormula(and(literal("x"), literal(false, "y")), new DeMorganApplier());
    }

    @Test
    void eliminatesNotBeforeLiteral() {
        VisitorTest.traverseAndAssertFormulaEquals(
                and(literal("x"), not(literal("y"))), new DeMorganApplier(), and(literal("x"), literal(false, "y")));
    }

    @Test
    void eliminatesNotBeforeAnd() {
        VisitorTest.traverseAndAssertFormulaEquals(
                not(and(literal("x"), literal("y"))),
                new DeMorganApplier(),
                or(literal(false, "x"), literal(false, "y")));
    }

    @Test
    void eliminatesNotBeforeAndAndBeforeLiteral() {
        VisitorTest.traverseAndAssertFormulaEquals(
                not(and(literal("x"), not(literal("y")))),
                new DeMorganApplier(),
                or(literal(false, "x"), literal(true, "y")));
    }
}
