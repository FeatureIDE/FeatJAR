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
package de.featjar.formula.structure;

import de.featjar.base.io.IO;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.structure.ITree;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.IAssignment;
import de.featjar.formula.assignment.ValueAssignment;
import de.featjar.formula.io.textual.ExpressionFormat;
import de.featjar.formula.structure.predicate.ExpressionKind;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import de.featjar.formula.visitor.Evaluator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An expression in propositional or first-order logic.
 * Implemented recursively as a tree of expressions.
 * An expression is either a {@link IFormula}
 * or a {@link ITerm}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IExpression extends ITree<IExpression> {
    /**
     * {@return the name of this expression's operator}
     */
    String getName();

    /**
     * {@return the type this expression evaluates to}
     */
    Class<?> getType();

    /**
     * {@return the evaluation of this expression on a given list of values}
     *
     * @param values the values
     */
    Optional<?> evaluate(List<?> values);

    /**
     * {@return the evaluation of this formula on a given value assignment}
     *
     * @param assignment the value assignment
     */
    default Optional<Object> evaluate(IAssignment<String, Object> assignment) {
        return traverse(new Evaluator(assignment)).orElseThrow();
    }

    /**
     * {@return the evaluation of this formula on a given boolean assignment}
     *
     * @param booleanAssignment the boolean assignment
     * @param variableMap the {@link VariableMap variable map} mapping the indices in the assignment to variable names
     */
    default Optional<Object> evaluate(BooleanAssignment booleanAssignment, VariableMap variableMap) {
        return evaluate(variableMap.toAssignment(booleanAssignment).get());
    }

    /**
     * {@return the evaluation of this formula on a given boolean assignment}
     *
     * @param valueAssignment the boolean assignment
     * @param variableMap the {@link VariableMap variable map} mapping the indices in the assignment to variable names
     */
    default Optional<Object> evaluate(ValueAssignment valueAssignment, VariableMap variableMap) {
        return evaluate(variableMap.toAssignment(valueAssignment).get());
    }

    /**
     * {@return the evaluation of this formula on an empty value assignment}
     */
    default Optional<Object> evaluate() {
        return evaluate(new Assignment());
    }

    /**
     * {@return the type this expression's children must evaluate to, if any}
     */
    default Class<?> getChildrenType() {
        return null;
    }

    @Override
    default Predicate<IExpression> getChildValidator() {
        return expression -> getChildrenType() == null || getChildrenType().isAssignableFrom(expression.getType());
    }

    /**
     * {@return a map of all unique variables in this expression}
     * Uniqueness of variables is determined by their names, not their identity.
     * Thus, only the first instance of a variable with a given name will occur in this stream.
     */
    default LinkedHashMap<String, Variable> getVariableMap() {
        LinkedHashMap<String, Variable> variables = new LinkedHashMap<>();
        Trees.preOrderStream(this)
                .filter(e -> e instanceof Variable)
                .forEach(v -> variables.put(v.getName(), (Variable) v));
        return variables;
    }

    /**
     * {@return a stream of all unique variables in this expression}
     * Uniqueness of variables is determined by their names, not their identity.
     * Thus, only the first instance of a variable with a given name will occur in this stream.
     */
    default Stream<Variable> getVariableStream() {
        return Trees.preOrderStream(this).filter(e -> e instanceof Variable).map(v -> (Variable) v);
    }

    /**
     * {@return a list of all variables in this expression}
     */
    default List<Variable> getVariables() {
        return getVariableStream().collect(Collectors.toList());
    }

    /**
     * {@return a list of all variable names in this expression}
     */
    default LinkedHashSet<String> getVariableNames() {
        return getVariableStream().map(Variable::getName).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * {@return a stream of all constants in this expression}
     */
    default Stream<Constant> getConstantStream() {
        return Trees.preOrderStream(this)
                .filter(e -> e instanceof Constant)
                .map(e -> (Constant) e)
                .distinct();
    }

    /**
     * {@return a list of all constants in this expression}
     */
    default List<Constant> getConstants() {
        return getConstantStream().collect(Collectors.toList());
    }

    /**
     * {@return a list of all constant values in this expression}
     */
    default List<Object> getConstantValues() {
        return getConstantStream().map(Constant::getValue).collect(Collectors.toList());
    }

    /**
     * {@return the expression printed as a string}
     * The string can be parsed using TODO {@link ExpressionFormat}.
     */
    default String printParseable() {
        try (final ByteArrayOutputStream s = new ByteArrayOutputStream()) {
            IO.save(this, s, new ExpressionFormat());
            return s.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    default boolean isKind(ExpressionKind expressionKind) {
        return expressionKind.test(this);
    }

    default ExpressionKind getKind() {
        return ExpressionKind.getExpressionKind(this);
    }
}
