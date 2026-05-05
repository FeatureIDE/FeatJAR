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
package de.featjar.formula.io.textual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.FeatJAR;
import de.featjar.base.tree.Trees;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LaTexSymbolsTest {

    private ExpressionSerializer expressionSerializer;

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void deinit() {
        FeatJAR.deinitialize();
    }

    @BeforeEach
    public void setUp() {
        expressionSerializer = new ExpressionSerializer();
        expressionSerializer.setSymbols(LaTexSymbols.INSTANCE);
    }

    @Test
    public void testInfixNoParenthesisNoEnquote1() {
        IFormula formula = new Implies(Expressions.literal("A"), Expressions.literal("B"));
        Trees.traverse(formula, expressionSerializer);

        assertTrue(expressionSerializer.getResult().isPresent());
        assertEquals("A \\Rightarrow{} B", expressionSerializer.getResult().get());
    }

    @Test
    public void testInfixNoParenthesisNoEnquote2() {
        IFormula formula = new And(
                new Implies(Expressions.literal("A"), Expressions.literal("B")),
                Expressions.literal("C"),
                new BiImplies(Expressions.literal("A"), Expressions.literal("D")));
        Trees.traverse(formula, expressionSerializer);

        assertTrue(expressionSerializer.getResult().isPresent());
        assertEquals(
                "(A \\Rightarrow{} B) \\land{} C \\land{} (A \\Leftrightarrow{} D)",
                expressionSerializer.getResult().get());
    }

    @Test
    public void testInfixNoParenthesisWithEnquote1() {
        IFormula formula = new And(
                new Implies(Expressions.literal("A"), Expressions.literal("B")),
                Expressions.literal("C"),
                new BiImplies(Expressions.literal("A"), Expressions.literal("D")));
        expressionSerializer.setEnquoteAlways(true);
        Trees.traverse(formula, expressionSerializer);

        assertTrue(expressionSerializer.getResult().isPresent());
        assertEquals(
                "(\\text{A} \\Rightarrow{} \\text{B}) \\land{} \\text{C} \\land{} (\\text{A} \\Leftrightarrow{} \\text{D})",
                expressionSerializer.getResult().get());
    }

    @Test
    public void testInfixWithParenthesisWithEnquote1() {
        IFormula formula = new And(
                new Implies(Expressions.literal("A"), Expressions.literal("B")),
                Expressions.literal("C"),
                new BiImplies(Expressions.literal("A"), Expressions.literal("D")));
        expressionSerializer.setEnquoteAlways(true);
        expressionSerializer.setEnforceParentheses(true);
        Trees.traverse(formula, expressionSerializer);

        assertTrue(expressionSerializer.getResult().isPresent());
        assertEquals(
                "((\\text{A} \\Rightarrow{} \\text{B}) \\land{} \\text{C} \\land{} (\\text{A} \\Leftrightarrow{} \\text{D}))",
                expressionSerializer.getResult().get());
    }

    @Test
    public void testInfixNoParenthesisWithEnquote2() {
        IFormula formula = new And(
                new ForAll(new Variable("A"), new Implies(Expressions.literal("A"), Expressions.literal("B"))),
                Expressions.literal("C"),
                new BiImplies(
                        new Not(Expressions.literal("A")), new Exists(new Variable("D"), Expressions.literal("D"))));
        expressionSerializer.setEnquoteAlways(true);
        Trees.traverse(formula, expressionSerializer);

        assertTrue(expressionSerializer.getResult().isPresent());
        assertEquals(
                "\\forall{}(\\text{A} \\Rightarrow{} \\text{B}) \\land{} \\text{C} \\land{} (\\lnot{}\\text{A} \\Leftrightarrow{} \\exists{}(\\text{D}))",
                expressionSerializer.getResult().get());
    }

    @Test
    public void testInfixNoParenthesisWithEnquote3() {
        IFormula formula = new And(
                new Implies(
                        new LessThan(
                                new IntegerAdd(new Constant(1L, Long.class), new Constant(0L, Long.class)),
                                new Constant(10L, Long.class)),
                        Expressions.literal("B")),
                Expressions.literal("C"),
                new BiImplies(Expressions.literal("A"), Expressions.literal("D")));
        expressionSerializer.setEnquoteAlways(true);
        Trees.traverse(formula, expressionSerializer);

        assertTrue(expressionSerializer.getResult().isPresent());
        assertEquals(
                "(<(+(\\text{1} \\text{0}) \\text{10}) \\Rightarrow{} \\text{B}) \\land{} \\text{C} \\land{} (\\text{A} \\Leftrightarrow{} \\text{D})",
                expressionSerializer.getResult().get());
    }
}
