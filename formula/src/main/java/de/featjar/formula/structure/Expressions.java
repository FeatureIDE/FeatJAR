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

import de.featjar.base.data.Problem;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.TreePrinter;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Exists;
import de.featjar.formula.structure.connective.ForAll;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NotEquals;
import de.featjar.formula.structure.predicate.ProblemFormula;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.function.integer.IntegerDivide;
import de.featjar.formula.structure.term.function.integer.IntegerMultiply;
import de.featjar.formula.structure.term.function.real.RealAdd;
import de.featjar.formula.structure.term.function.real.RealDivide;
import de.featjar.formula.structure.term.function.real.RealMultiply;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.IValue;
import de.featjar.formula.structure.term.value.Variable;

/**
 * Creates expressions in propositional or first-order logic.
 * Wraps constructors conveniently to allow for a static on-demand import using
 * {@code import static de.featjar.formula.structure.Expressions.*;}.
 * Some parameters are intentionally named after their containing method to avoid inlay hints in IntelliJ IDEA.
 *
 * @author Elias Kuiter
 */
public class Expressions {
    /**
     * A tautology.
     */
    public static final True True = de.featjar.formula.structure.predicate.True.INSTANCE;
    /**
     * A contradiction.
     */
    public static final False False = de.featjar.formula.structure.predicate.False.INSTANCE;

    /**
     * {@return a human-readable string of the given expression}
     * Default print method for expressions, see {@link TreePrinter} for more options.
     * @param expression the expression to print
     */
    public static String print(IExpression expression) {
        return Trees.traverse(
                        expression,
                        new TreePrinter().setFilter(t -> !(t instanceof Variable) && !(t instanceof Constant)))
                .get();
    }

    /**
     * {@return a formula that evaluates to true iff all of the given formulas evaluate to true}
     *
     * @param formulas the formulas
     */
    public static And and(IFormula... formulas) {
        return new And(formulas);
    }

    /**
     * {@return a formula that evaluates to true iff at least a given number of the given formulas evaluate to true}
     *
     * @param minimum the minimum
     * @param formulas the formulas
     */
    public static AtLeast atLeast(int minimum, IFormula... formulas) {
        return new AtLeast(minimum, formulas);
    }

    /**
     * {@return a formula that evaluates to true iff at most a given number of the given formulas evaluate to true}
     *
     * @param maximum the maximum
     * @param formulas the formulas
     */
    public static AtMost atMost(int maximum, IFormula... formulas) {
        return new AtMost(maximum, formulas);
    }

    /**
     * {@return a formula that evaluates to true iff the number of the given formulas that evaluate to true is in a given range}
     *
     * @param minimum the minimum
     * @param maximum the maximum
     * @param formulas the formulas
     */
    public static Between between(int minimum, int maximum, IFormula... formulas) {
        return new Between(minimum, maximum, formulas);
    }

    /**
     * {@return a formula that evaluates to true iff both given formulas evaluate to the same value}
     *
     * @param leftFormula the left formula
     * @param rightFormula the right formula
     */
    public static BiImplies biImplies(IFormula leftFormula, IFormula rightFormula) {
        return new BiImplies(leftFormula, rightFormula);
    }

    /**
     * {@return a formula that evaluates to true iff the number of the given formulas that evaluate to true is equal to a given number}
     *
     * @param bound the bound
     * @param formulas the formulas
     */
    public static Choose choose(int bound, IFormula... formulas) {
        return new Choose(bound, formulas);
    }

    /**
     * {@return a formula that evaluates to true iff there is a value of the given bound variable such that the given formula evaluates to true}
     *
     * @param boundVariable the bound variable
     * @param formula the formula
     */
    public static Exists exists(Variable boundVariable, IFormula formula) {
        return new Exists(boundVariable, formula);
    }

    /**
     * {@return a formula that evaluates to true iff for all values of the given bound variable, the given formula evaluates to true}
     *
     * @param boundVariable the bound variable
     * @param formula the formula
     */
    public static ForAll forAll(Variable boundVariable, IFormula formula) {
        return new ForAll(boundVariable, formula);
    }

    /**
     * {@return a formula that evaluates to true iff the given left formula evaluates to false or
     * the given right formula evaluates to true}
     *
     * @param leftFormula the left formula
     * @param rightFormula the right formula
     */
    public static Implies implies(IFormula leftFormula, IFormula rightFormula) {
        return new Implies(leftFormula, rightFormula);
    }

    /**
     * {@return a formula that evaluates to true iff the given formula evaluates to false}
     *
     * @param formula the formula
     */
    public static Not not(IFormula formula) {
        return new Not(formula);
    }

    /**
     * {@return a formula that evaluates to true iff at least one of the given formulas evaluate to true}
     *
     * @param formulas the formulas
     */
    public static Or or(IFormula... formulas) {
        return new Or(formulas);
    }

    /**
     * {@return a reference to a formula that evaluates to true iff the given formula evaluates to true}
     *
     * @param formula the formula
     */
    public static Reference reference(IFormula formula) {
        return new Reference(formula);
    }

    /**
     * {@return a formula that evaluates to true iff both given terms evaluate to the same value}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static Equals equals(ITerm leftTerm, ITerm rightTerm) {
        return new Equals(leftTerm, rightTerm);
    }

    /**
     * {@return a formula that evaluates to true iff the given left term evaluates to a larger or the same value as the given right term}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static GreaterEqual greaterEqual(ITerm leftTerm, ITerm rightTerm) {
        return new GreaterEqual(leftTerm, rightTerm);
    }

    /**
     * {@return a formula that evaluates to true iff the given left term evaluates to a larger value as the given right term}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static GreaterThan greaterThan(ITerm leftTerm, ITerm rightTerm) {
        return new GreaterThan(leftTerm, rightTerm);
    }

    /**
     * {@return a formula that evaluates to true iff the given left term evaluates to a smaller or the same value as the given right term}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static LessEqual lessEqual(ITerm leftTerm, ITerm rightTerm) {
        return new LessEqual(leftTerm, rightTerm);
    }

    /**
     * {@return a formula that evaluates to true iff the given left term evaluates to a smaller value as the given right term}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static LessThan lessThan(ITerm leftTerm, ITerm rightTerm) {
        return new LessThan(leftTerm, rightTerm);
    }

    /**
     * {@return a formula that evaluates to true iff the given term evaluates to the given polarity}
     *
     * @param isPositive the polarity
     * @param value the term
     */
    public static Literal literal(boolean isPositive, IValue value) {
        return new Literal(isPositive, value);
    }

    /**
     * {@return a formula that evaluates to true iff the given term evaluates to true}
     *
     * @param value the term
     */
    public static Literal literal(IValue value) {
        return new Literal(value);
    }

    /**
     * {@return a formula that evaluates to true iff the Boolean variable with the given name evaluates to the given polarity}
     *
     * @param l1 the polarity
     * @param l2 the variable name
     */
    public static Literal literal(boolean l1, String l2) {
        return new Literal(l1, l2);
    }

    /**
     * {@return a formula that evaluates to true iff the Boolean variable with the given name evaluates to true}
     *
     * @param literal the variable name
     */
    public static Literal literal(String literal) {
        return new Literal(literal);
    }

    /**
     * {@return a formula that evaluates to true iff both given terms evaluate to different values}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static NotEquals notEquals(ITerm leftTerm, ITerm rightTerm) {
        return new NotEquals(leftTerm, rightTerm);
    }

    /**
     * {@return a placeholder for when an expression cannot be parsed due to some given problem}
     *
     * @param problem the problem
     */
    public static ProblemFormula problemFormula(Problem problem) {
        return new ProblemFormula(problem);
    }

    /**
     * {@return a term that adds the values of two integer terms}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static IntegerAdd integerAdd(ITerm leftTerm, ITerm rightTerm) {
        return new IntegerAdd(leftTerm, rightTerm);
    }

    /**
     * {@return a term that divides the values of two integer terms}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static IntegerDivide integerDivide(ITerm leftTerm, ITerm rightTerm) {
        return new IntegerDivide(leftTerm, rightTerm);
    }

    /**
     * {@return a term that multiplies the values of two integer terms}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static IntegerMultiply integerMultiply(ITerm leftTerm, ITerm rightTerm) {
        return new IntegerMultiply(leftTerm, rightTerm);
    }

    /**
     * {@return a term that adds the values of two real terms}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static RealAdd realAdd(ITerm leftTerm, ITerm rightTerm) {
        return new RealAdd(leftTerm, rightTerm);
    }

    /**
     * {@return a term that divides the values of two real terms}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static RealDivide realDivide(ITerm leftTerm, ITerm rightTerm) {
        return new RealDivide(leftTerm, rightTerm);
    }

    /**
     * {@return a term that multiplies the values of two real terms}
     *
     * @param leftTerm the left term
     * @param rightTerm the right term
     */
    public static RealMultiply realMultiply(ITerm leftTerm, ITerm rightTerm) {
        return new RealMultiply(leftTerm, rightTerm);
    }

    /**
     * {@return a constant term that evaluates to a given value, the term having the given type}
     *
     * @param value the value
     * @param type the type
     */
    public static Constant constant(Object value, Class<?> type) {
        return new Constant(value, type);
    }

    /**
     * {@return a constant term that evaluates to a given value, the term having the type of the given value}
     *
     * @param value the value
     */
    public static Constant constant(Object value) {
        return new Constant(value);
    }

    /**
     * {@return a variable term for a given name, the term having the given type}
     *
     * @param name the name
     * @param type the type
     */
    public static Variable variable(String name, Class<?> type) {
        return new Variable(name, type);
    }

    /**
     * {@return a Boolean variable term for a given name}
     *
     * @param name the name
     */
    public static Variable variable(String name) {
        return new Variable(name);
    }
}
