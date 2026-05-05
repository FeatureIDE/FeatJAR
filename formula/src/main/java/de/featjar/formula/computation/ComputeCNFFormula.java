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
package de.featjar.formula.computation;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import de.featjar.formula.structure.FormulaNormalForm;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.ExpressionKind;
import de.featjar.formula.structure.predicate.Literal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Transforms a formula into strict conjunctive normal form.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ComputeCNFFormula extends AComputation<IFormula> {
    public static final Dependency<IFormula> NNF_FORMULA = Dependency.newDependency(IFormula.class);
    /**
     * Determines whether this computation uses the Plaisted-Greenbaum optimization.
     */
    public static final Dependency<Boolean> IS_PLAISTED_GREENBAUM = Dependency.newDependency(Boolean.class);
    /**
     * Determines the maximum number of literals available for distributive
     * transformation.
     */
    public static final Dependency<Integer> MAXIMUM_NUMBER_OF_LITERALS = Dependency.newDependency(Integer.class);
    /**
     * Determines whether this computation is parallel. Use with care! Enabling
     * parallel computation makes the result of this computation indeterministic.
     */
    public static final Dependency<Boolean> IS_PARALLEL = Dependency.newDependency(Boolean.class);

    /**
     * Determines whether the resulting formula is strict.
     */
    public static final Dependency<Boolean> IS_STRICT = Dependency.newDependency(Boolean.class);

    /**
     * Creates a new CNF formula computation.
     *
     * @param nnfFormula the input NNF formula computation
     */
    public ComputeCNFFormula(IComputation<IFormula> nnfFormula) {
        super(
                nnfFormula, //
                Computations.of(Boolean.FALSE), //
                Computations.of(Integer.MAX_VALUE), //
                Computations.of(Boolean.FALSE), //
                Computations.of(Boolean.TRUE));
    }

    protected ComputeCNFFormula(ComputeCNFFormula other) {
        super(other);
    }

    /**
     * Sets whether this computation introduces auxiliary variables.
     *
     * @param tseitin whether this computation introduces auxiliary variables
     */
    public void setTseitin(IComputation<Boolean> tseitin) {
        setDependencyComputation(
                MAXIMUM_NUMBER_OF_LITERALS,
                tseitin.mapResult(ComputeCNFFormula.class, "setTseitin", b -> b ? 0 : Integer.MAX_VALUE));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        IFormula nnfFormula = NNF_FORMULA.get(dependencyList);
        Reference referenceFormula = null;
        if (nnfFormula instanceof Reference) {
            referenceFormula = (Reference) nnfFormula;
            nnfFormula = referenceFormula.getExpression();
        }
        if (!ExpressionKind.NNF.test(nnfFormula)) {
            throw new IllegalArgumentException("Formula is not in NNF");
        }
        int maximumNumberOfLiterals = MAXIMUM_NUMBER_OF_LITERALS.get(dependencyList);
        boolean isPlaistedGreenbaum = IS_PLAISTED_GREENBAUM.get(dependencyList);
        boolean isParallel = IS_PARALLEL.get(dependencyList);
        boolean isStrict = IS_STRICT.get(dependencyList);

        List<IFormula> clauseFormulas =
                isParallel ? Collections.synchronizedList(new ArrayList<>()) : new ArrayList<>();
        List<TseitinTransformer.Substitution> substitutions =
                isParallel ? Collections.synchronizedList(new ArrayList<>()) : new ArrayList<>();
        Consumer<IFormula> transformer = formula -> {
            transform(formula, clauseFormulas, substitutions, isPlaistedGreenbaum, maximumNumberOfLiterals);
            progress.incrementCurrentStepSynchronized();
        };

        if (nnfFormula instanceof And) {
            List<IFormula> children = (List<IFormula>) nnfFormula.getChildren();
            progress.setTotalSteps(children.size());
            if (isParallel) {
                children.parallelStream().forEach(transformer);
            } else {
                children.forEach(transformer);
            }
        } else {
            progress.setTotalSteps(1);
            transformer.accept(nnfFormula);
        }

        TseitinTransformer.unify(substitutions);
        clauseFormulas.addAll(TseitinTransformer.getClauseFormulas(substitutions));

        IFormula cnf = new And(clauseFormulas);
        if (isStrict) {
            cnf = toStrictForm(cnf);
        }
        if (referenceFormula != null) {
            cnf = referenceFormula.setFormula(cnf);
        }
        return Result.of(cnf);
    }

    @SuppressWarnings("unchecked")
    private void transform(
            IFormula formula,
            List<IFormula> clauseFormulas,
            List<TseitinTransformer.Substitution> substitutions,
            boolean isPlaistedGreenbaum,
            int maximumNumberOfLiterals) {
        if (formula.isStrictNormalForm(FormulaNormalForm.CNF)) {
            clauseFormulas.addAll((List<? extends IFormula>) formula.getChildren());
        } else if (formula.isNormalForm(FormulaNormalForm.CNF)) {
            clauseFormulas.addAll(
                    (List<? extends IFormula>) toStrictForm(formula).getChildren());
        } else {
            Result<IFormula> transformationResult = distributiveTransform(
                    formula,
                    new DistributiveTransformer.MaximumNumberOfLiteralsCancelPredicate(maximumNumberOfLiterals));
            if (transformationResult.isPresent()) {
                clauseFormulas.addAll(
                        (List<? extends IFormula>) transformationResult.get().getChildren());
                return;
            }
            substitutions.addAll(tseitinTransform(formula, isPlaistedGreenbaum));
        }
    }

    protected Result<IFormula> distributiveTransform(
            IFormula formula, DistributiveTransformer.ICancelPredicate cancelPredicate) {
        return new DistributiveTransformer(true, cancelPredicate).apply(formula);
    }

    protected List<TseitinTransformer.Substitution> tseitinTransform(IFormula formula, boolean isPlaistedGreenbaum) {
        return new TseitinTransformer(isPlaistedGreenbaum).apply(formula);
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputeCNFFormula(this);
    }

    private static IFormula toStrictForm(IFormula formula) {
        if (formula instanceof Literal) {
            formula = new And(new Or(formula));
        } else if (formula instanceof Or) {
            formula = new And(formula);
        } else {
            formula.replaceChildren(child -> (child instanceof Literal) ? new Or((IFormula) child) : child);
        }
        return formula;
    }
}
