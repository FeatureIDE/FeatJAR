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
package de.featjar.formula.structure.predicate;

import de.featjar.base.data.Sets;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.function.integer.IntegerDivide;
import de.featjar.formula.structure.term.function.integer.IntegerMultiply;
import de.featjar.formula.structure.term.function.real.RealAdd;
import de.featjar.formula.structure.term.function.real.RealDivide;
import de.featjar.formula.structure.term.function.real.RealMultiply;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Tests whether a given expression only contains certain kinds of elements.
 * Can be used to test whether an expression is (non-)Boolean.
 *
 * @author Elias Kuiter
 */
public interface ExpressionKind extends Predicate<IExpression> {
    /**
     * {@return the name of this expression kind}
     */
    String getName();

    /**
     * Represents Boolean expressions in strict negation normal form.
     */
    ExpressionKind NNF = of("NNF", Variable.class, Literal.class, And.class, Or.class);

    /**
     * Represents arbitrary Boolean expressions.
     * These can usually be simplified to strict negation normal form.
     * Simplification of {@link True} and {@link False} is only possible if the formula has at least one {@link Variable}.
     */
    ExpressionKind BOOLEAN = extend(
            "Boolean",
            NNF,
            Not.class,
            Implies.class,
            BiImplies.class,
            True.class,
            False.class,
            AtLeast.class,
            AtMost.class,
            Between.class,
            Choose.class);

    /**
     * Represents quantified Boolean expressions.
     */
    ExpressionKind QBF = extend("QBF", BOOLEAN, Exists.class, ForAll.class);

    /**
     * Represents first-order expressions.
     */
    ExpressionKind FIRST_ORDER = extend(
            "first order",
            BOOLEAN,
            Constant.class,
            Equals.class,
            NotEquals.class,
            GreaterEqual.class,
            GreaterThan.class,
            LessEqual.class,
            LessThan.class,
            IntegerAdd.class,
            IntegerDivide.class,
            IntegerMultiply.class,
            RealAdd.class,
            RealDivide.class,
            RealMultiply.class);

    /**
     * Represents all expressions.
     */
    ExpressionKind ARBITRARY = extend("arbitrary", FIRST_ORDER, ProblemFormula.class, Reference.class);

    Collection<Class<? extends IExpression>> getAllowedClasses();

    @Override
    default boolean test(IExpression expression) {
        return expression.preOrderStream().allMatch(e -> getAllowedClasses().stream()
                .anyMatch(c -> c.isAssignableFrom(e.getClass())));
    }

    default void assertFor(IExpression expression) {
        if (!test(expression)) throw new ExpressionKindNotSupportedException(this);
    }

    @SafeVarargs
    static ExpressionKind of(String name, Class<? extends IExpression>... allowedClasses) {
        LinkedHashSet<Class<? extends IExpression>> allowedClassesSet = Sets.of(allowedClasses);
        return new ExpressionKind() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Collection<Class<? extends IExpression>> getAllowedClasses() {
                return allowedClassesSet;
            }
        };
    }

    @SafeVarargs
    static ExpressionKind extend(
            String name, ExpressionKind expressionKind, Class<? extends IExpression>... allowedClasses) {
        LinkedHashSet<Class<? extends IExpression>> allowedClassesSet =
                Sets.union(expressionKind.getAllowedClasses(), List.of(allowedClasses));
        return new ExpressionKind() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Collection<Class<? extends IExpression>> getAllowedClasses() {
                return allowedClassesSet;
            }
        };
    }

    static ExpressionKind getExpressionKind(IExpression expression) {
        return Stream.of(NNF, BOOLEAN, QBF, FIRST_ORDER, ARBITRARY)
                .filter(expressionKind -> expressionKind.test(expression))
                .findFirst()
                .orElse(null);
    }
}
