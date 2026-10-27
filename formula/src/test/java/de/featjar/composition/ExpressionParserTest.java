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
package de.featjar.composition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import org.junit.jupiter.api.Test;

class ExpressionParserTest {

    @Test
    void parsesGreaterThanExpression() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a > 5").get();

        assertTrue(expression instanceof GreaterThan);

        GreaterThan greaterThan = (GreaterThan) expression;

        assertTrue(greaterThan.getLeftExpression() instanceof Variable);
        assertTrue(greaterThan.getRightExpression() instanceof Constant);

        Variable variable = (Variable) greaterThan.getLeftExpression();

        Constant constant = (Constant) greaterThan.getRightExpression();

        assertEquals("a", variable.getName());
        assertEquals(Long.class, variable.getType());

        assertEquals(5L, constant.getValue());
        assertEquals(Long.class, constant.getType());
    }

    @Test
    void parsesLessThanExpression() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a < 5").get();

        assertTrue(expression instanceof LessThan);
    }

    @Test
    void parsesGreaterEqualExpression() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a >= 5").get();

        assertTrue(expression instanceof GreaterEqual);
    }

    @Test
    void parsesLessEqualExpression() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a <= 5").get();

        assertTrue(expression instanceof LessEqual);
    }

    @Test
    void parsesComparisonBetweenVariables() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a > b").get();

        assertTrue(expression instanceof GreaterThan);

        GreaterThan greaterThan = (GreaterThan) expression;

        assertTrue(greaterThan.getLeftExpression() instanceof Variable);
        assertTrue(greaterThan.getRightExpression() instanceof Variable);

        Variable left = (Variable) greaterThan.getLeftExpression();

        Variable right = (Variable) greaterThan.getRightExpression();

        assertEquals("a", left.getName());
        assertEquals("b", right.getName());

        assertEquals(Long.class, left.getType());
        assertEquals(Long.class, right.getType());
    }

    @Test
    void parsesBooleanAndNonBooleanCondition() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("A & (a > 5)").get();

        assertTrue(expression instanceof IFormula);
    }
}
