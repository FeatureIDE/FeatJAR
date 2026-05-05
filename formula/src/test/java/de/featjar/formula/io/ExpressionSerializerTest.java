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
package de.featjar.formula.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.Common;
import de.featjar.base.FeatJAR;
import de.featjar.base.tree.Trees;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.ExpressionSerializer.Notation;
import de.featjar.formula.io.textual.JavaSymbols;
import de.featjar.formula.io.textual.TextualSymbols;
import de.featjar.formula.structure.IFormula;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ExpressionSerializer}.
 *
 * @author Sebastian Krieter
 */
public class ExpressionSerializerTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void Formula_ABC_nAnBnC() {
        final IFormula formula = getFormula("ABC-nAnBnC");
        final ExpressionSerializer s = new ExpressionSerializer();
        s.setSymbols(JavaSymbols.INSTANCE);
        s.setNotation(Notation.INFIX);
        assertEquals(
                "(A || B || C) && (!A || (!B || !C))",
                Trees.traverse(formula, s).get());
        s.setNotation(Notation.PREFIX);
        assertEquals(
                "&&(||(A B C) ||(!(A) ||(!(B) !(C))))",
                Trees.traverse(formula, s).get());
        s.setNotation(Notation.POSTFIX);
        assertEquals("(A B C)|| (A! (B! C!)||)||&&", Trees.traverse(formula, s).get());

        s.setSymbols(TextualSymbols.INSTANCE);
        s.setNotation(Notation.INFIX);
        assertEquals(
                "(A or B or C) and (not A or (not B or not C))",
                Trees.traverse(formula, s).get());
        s.setNotation(Notation.PREFIX);
        assertEquals(
                "and(or(A B C) or(not(A) or(not(B) not(C))))",
                Trees.traverse(formula, s).get());
        s.setNotation(Notation.POSTFIX);
        assertEquals(
                "(A B C)or (A not (B not C not)or)or and",
                Trees.traverse(formula, s).get());
    }

    @Test
    public void Formula_nAB() {
        final IFormula formula = getFormula("nAB");
        final ExpressionSerializer s = new ExpressionSerializer();
        s.setSymbols(JavaSymbols.INSTANCE);
        s.setNotation(Notation.INFIX);
        assertEquals("!A || B", Trees.traverse(formula, s).get());
        s.setNotation(Notation.PREFIX);
        assertEquals("||(!(A) B)", Trees.traverse(formula, s).get());
        s.setNotation(Notation.POSTFIX);
        assertEquals("A! B||", Trees.traverse(formula, s).get());

        s.setSymbols(TextualSymbols.INSTANCE);
        s.setNotation(Notation.INFIX);
        assertEquals("not A or B", Trees.traverse(formula, s).get());
        s.setNotation(Notation.PREFIX);
        assertEquals("or(not(A) B)", Trees.traverse(formula, s).get());
        s.setNotation(Notation.POSTFIX);
        assertEquals("A not B or", Trees.traverse(formula, s).get());
    }
}
