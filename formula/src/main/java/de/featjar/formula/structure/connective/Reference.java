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
package de.featjar.formula.structure.connective;

import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.ANonTerminalExpression;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.IUnaryExpression;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A reference to a formula that enables mutating it.
 * Evaluates to {@code true} iff its child evaluates to {@code true}.
 * Semantically, a root is equivalent to a unary {@link And} or {@link Or} and therefore transparent.
 * However, having a {@link Reference} at the top level of a formula helps with mutation:
 * For example, when transforming the formula {@code new Implies(new Literal("a"), new Literal("b"))}
 * into its negation normal form {@code new Or(new Literal(false, "a"), new Literal("b"))} with
 * {@link ComputeNNFFormula}, the top-level {@link Implies} needs to be replaced
 * by a top-level {@link Or}, so the entire formula has to be passed by reference.
 * By wrapping the formula in a {@link Reference}, such a mutation becomes possible;
 * without a {@link Reference}, it must be cloned instead.
 * Algorithms that mutate formulas can specify that they expect a top-level {@link Reference}
 * with {@link ITreeVisitor#rootValidator(List, Function, String)}.
 * <p>
 * This class also enables to include free variables that are not contained in the underlying expression.
 * Use {@link #setFreeVariables(Collection)} to specify additional variables.
 * This is, for instance, useful when loading a formula from a format such as DIMACS, which allows to specify variables without including them in the actual formula.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Reference extends ANonTerminalExpression implements IConnective, IUnaryExpression {

    private Collection<Variable> freeVariables = null;

    protected Reference(Reference reference) {
        this.freeVariables = reference.freeVariables == null ? null : new ArrayList<>(reference.freeVariables);
    }

    public Reference(IFormula formula) {
        super(formula);
    }

    public Reference(IFormula formula, Collection<Variable> freeVariables) {
        super(formula);
        setFreeVariables(freeVariables);
    }

    public Collection<Variable> getFreeVariables() {
        return freeVariables == null ? Collections.emptyList() : Collections.unmodifiableCollection(freeVariables);
    }

    public void setFreeVariables(Collection<Variable> freeVariables) {
        this.freeVariables = new ArrayList<>(freeVariables);
    }

    @Override
    public IFormula getExpression() {
        return (IFormula) IUnaryExpression.super.getExpression();
    }

    @Override
    public void setExpression(IExpression expression) {
        IUnaryExpression.super.setExpression((IFormula) expression);
    }

    /**
     * Convenience method. Same as {@link #setExpression(IExpression)}. In addition, returns this reference.
     * @param formula the new formula to reference
     * @return this
     */
    public Reference setFormula(IFormula formula) {
        setExpression(formula);
        return this;
    }

    @Override
    public LinkedHashMap<String, Variable> getVariableMap() {
        LinkedHashMap<String, Variable> variables = super.getVariableMap();
        if (freeVariables != null) {
            freeVariables.forEach(v -> variables.put(v.getName(), v));
        }
        return variables;
    }

    @Override
    public String getName() {
        return "reference";
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        return Optional.of(values.get(0));
    }

    @Override
    public Reference cloneNode() {
        return new Reference(this);
    }

    /**
     * Clones the referenced formula (including this reference) and executes a given function on the clone.
     * Useful to traverse a formula with {@link ITreeVisitor} that
     * expects a reference when a clone is needed.
     *
     * @param fn the function
     * @return the result of mutating the cloned formula
     */
    public Result<IFormula> mutateClone(Function<Reference, Result<?>> fn) {
        Reference cloneTree = (Reference) cloneTree();
        return fn.apply(cloneTree).map(result -> cloneTree);
    }

    /**
     * Clones the given formula, wraps it inside a reference, and executes a given function.
     * Useful to traverse a formula with {@link ITreeVisitor} that
     * expects a reference when a clone is needed.
     *
     * @param formula the formula
     * @param fn the function
     * @return the result of mutating the cloned formula
     */
    public static Result<IFormula> mutateClone(IFormula formula, Function<Reference, Result<?>> fn) {
        if (formula instanceof Reference) {
            return ((Reference) formula).mutateClone(fn);
        } else {
            Reference formulaReference = new Reference((IFormula) formula.cloneTree());
            return fn.apply(formulaReference).map(result -> formulaReference.getExpression());
        }
    }
}
