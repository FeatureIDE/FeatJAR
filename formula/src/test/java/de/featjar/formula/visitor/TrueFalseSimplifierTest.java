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

class TrueFalseSimplifierTest {
    @Test
    void trueDominatesOr() {
        VisitorTest.traverseAndAssertFormulaEquals(or(literal("x"), True), new TrueFalseSimplifier(), True);
    }

    @Test
    void falseDominatesAnd() {
        VisitorTest.traverseAndAssertFormulaEquals(and(literal("x"), False), new TrueFalseSimplifier(), False);
    }

    @Test
    void trueNeutralAnd() {
        VisitorTest.traverseAndAssertFormulaEquals(
                and(literal("x"), True), new TrueFalseSimplifier(), and(literal("x")));
    }

    @Test
    void falseNeutralOr() {
        VisitorTest.traverseAndAssertFormulaEquals(
                or(literal("x"), False), new TrueFalseSimplifier(), or(literal("x")));
    }

    @Test
    void simplifiesImplies() {
        VisitorTest.traverseAndAssertSameFormula(implies(literal("x"), literal("y")), new TrueFalseSimplifier());
        VisitorTest.traverseAndAssertFormulaEquals(
                implies(literal("x"), literal("x")), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(implies(False, literal("x")), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(
                implies(True, literal("x")), new TrueFalseSimplifier(), literal("x"));
        VisitorTest.traverseAndAssertFormulaEquals(
                implies(literal("x"), False), new TrueFalseSimplifier(), not(literal("x")));
        VisitorTest.traverseAndAssertFormulaEquals(implies(literal("x"), True), new TrueFalseSimplifier(), True);
    }

    @Test
    void simplifiesBiImplies() {
        VisitorTest.traverseAndAssertSameFormula(biImplies(literal("x"), literal("y")), new TrueFalseSimplifier());
        VisitorTest.traverseAndAssertFormulaEquals(
                biImplies(literal("x"), literal("x")), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(biImplies(True, False), new TrueFalseSimplifier(), False);
        VisitorTest.traverseAndAssertFormulaEquals(biImplies(False, True), new TrueFalseSimplifier(), False);
        VisitorTest.traverseAndAssertFormulaEquals(biImplies(False, False), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(biImplies(True, True), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(
                biImplies(False, literal("x")), new TrueFalseSimplifier(), not(literal("x")));
        VisitorTest.traverseAndAssertFormulaEquals(
                biImplies(literal("x"), False), new TrueFalseSimplifier(), not(literal("x")));
        VisitorTest.traverseAndAssertFormulaEquals(
                biImplies(True, literal("x")), new TrueFalseSimplifier(), literal("x"));
        VisitorTest.traverseAndAssertFormulaEquals(
                biImplies(literal("x"), True), new TrueFalseSimplifier(), literal("x"));
    }

    @Test
    void simplifiesCardinal() {
        VisitorTest.traverseAndAssertSameFormula(
                choose(3, literal("x"), literal("y"), literal("z")), new TrueFalseSimplifier());
        VisitorTest.traverseAndAssertSameFormula(
                between(1, 2, literal("x"), literal("y"), literal("z")), new TrueFalseSimplifier());
        VisitorTest.traverseAndAssertSameFormula(
                atMost(2, literal("x"), literal("y"), literal("z")), new TrueFalseSimplifier());
        VisitorTest.traverseAndAssertSameFormula(
                atLeast(2, literal("x"), literal("y"), literal("z")), new TrueFalseSimplifier());

        VisitorTest.traverseAndAssertFormulaEquals(
                choose(3, literal("x"), True, True, False, False), new TrueFalseSimplifier(), choose(1, literal("x")));
        VisitorTest.traverseAndAssertFormulaEquals(
                between(3, 4, literal("x"), literal("y"), True, True, False, False),
                new TrueFalseSimplifier(),
                between(1, 2, literal("x"), literal("y")));
        VisitorTest.traverseAndAssertFormulaEquals(
                atMost(2, literal("z"), True, True, False, False), new TrueFalseSimplifier(), atMost(0, literal("z")));
        VisitorTest.traverseAndAssertFormulaEquals(
                atLeast(3, literal("z"), True, True, False, False),
                new TrueFalseSimplifier(),
                atLeast(1, literal("z")));
    }

    @Test
    void simplifiesChoose() {
        VisitorTest.traverseAndAssertFormulaEquals(choose(3, True, True, True, False), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(
                choose(3, True, True, False, False), new TrueFalseSimplifier(), False);
    }

    @Test
    void simplifiesBetween() {
        VisitorTest.traverseAndAssertFormulaEquals(
                between(2, 3, True, True, literal("z"), False), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(
                between(3, 4, True, literal("z"), False, False), new TrueFalseSimplifier(), False);
        VisitorTest.traverseAndAssertFormulaEquals(
                between(1, 2, True, True, True, literal("z"), False), new TrueFalseSimplifier(), False);
    }

    @Test
    void simplifiesAtMost() {
        VisitorTest.traverseAndAssertFormulaEquals(
                atMost(2, True, literal("z"), False), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(
                atMost(2, True, True, True, literal("z"), False), new TrueFalseSimplifier(), False);
    }

    @Test
    void simplifiesAtLeast() {
        VisitorTest.traverseAndAssertFormulaEquals(
                atLeast(2, True, True, literal("z"), False), new TrueFalseSimplifier(), True);
        VisitorTest.traverseAndAssertFormulaEquals(
                atLeast(3, True, literal("z"), False), new TrueFalseSimplifier(), False);
    }

    @Test
    void simplifyComplex() {
        VisitorTest.traverseAndAssertFormulaEquals(
                and(
                        literal("a"),
                        and(literal("b"), literal("c"), True),
                        and(literal("b"), True),
                        or(literal("x"), False)),
                new TrueFalseSimplifier(),
                and(literal("a"), and(literal("b"), literal("c")), and(literal("b")), or(literal("x"))));
    }
}
