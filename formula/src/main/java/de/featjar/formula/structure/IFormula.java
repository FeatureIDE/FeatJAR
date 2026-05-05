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

import static de.featjar.base.computation.Computations.async;

import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeDNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.connective.AQuantifier;
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.predicate.IPredicate;
import de.featjar.formula.structure.term.value.Variable;
import de.featjar.formula.visitor.ANormalFormTester;
import de.featjar.formula.visitor.CNFTester;
import de.featjar.formula.visitor.DNFTester;
import de.featjar.formula.visitor.NNFTester;
import java.util.LinkedHashSet;

/**
 * A well-formed formula.
 * Evaluates to either {@code true} or {@code false}.
 * In a formula, each {@link Variable} can, but does not have to be bound by a
 * {@link AQuantifier}.
 * A formula is either a {@link IConnective}
 * or a {@link IPredicate}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IFormula extends IExpression {

    private static Result<? extends IFormula> toNormalForm(
            IFormula formula, FormulaNormalForm formulaNormalForm, boolean isStrict) {
        IComputation<IFormula> computation = async(formula);
        switch (formulaNormalForm) {
            case NNF:
                return computation.map(ComputeNNFFormula::new).computeResult();
            case CNF:
                return computation
                        .map(ComputeNNFFormula::new)
                        .map(ComputeCNFFormula::new)
                        .set(ComputeCNFFormula.IS_STRICT, isStrict)
                        .computeResult();
            case DNF:
                return computation
                        .map(ComputeNNFFormula::new)
                        .map(ComputeDNFFormula::new)
                        .set(ComputeDNFFormula.IS_STRICT, isStrict)
                        .computeResult();
        }
        return computation.computeResult();
    }

    default Class<?> getType() {
        return Boolean.class;
    }

    private boolean isNormalForm(FormulaNormalForm formulaNormalForm, boolean isStrict) {
        ANormalFormTester normalFormTester = formulaNormalForm == FormulaNormalForm.NNF
                ? new NNFTester(isStrict)
                : formulaNormalForm == FormulaNormalForm.CNF ? new CNFTester(isStrict) : new DNFTester(isStrict);
        return normalFormTester.test(this);
    }

    /**
     * {@return whether this formula is in the given normal form}
     *
     * @param formulaNormalForm the normal form
     */
    default boolean isNormalForm(FormulaNormalForm formulaNormalForm) {
        return isNormalForm(formulaNormalForm, false);
    }

    /**
     * {@return whether this formula is in the given strict normal form}
     *
     * @param formulaNormalForm the strict normal form
     */
    default boolean isStrictNormalForm(FormulaNormalForm formulaNormalForm) {
        return isNormalForm(formulaNormalForm, true);
    }

    /**
     * {@return whether this formula is in negation normal form}
     */
    default boolean isNNF() {
        return isNormalForm(FormulaNormalForm.NNF);
    }

    /**
     * {@return whether this formula is in conjunctive normal form}
     */
    default boolean isCNF() {
        return isNormalForm(FormulaNormalForm.CNF);
    }

    /**
     * {@return whether this formula is in disjunctive normal form}
     */
    default boolean isDNF() {
        return isNormalForm(FormulaNormalForm.DNF);
    }

    /**
     * {@return a formula in the given normal form that is equivalent to this formula}
     *
     * @param formulaNormalForm the {@link FormulaNormalForm normal form}
     */
    // TODO: use computations
    default Result<? extends IFormula> toNormalForm(FormulaNormalForm formulaNormalForm) {
        return toNormalForm(this, formulaNormalForm, false);
    }

    /**
     * {@return a formula in the given strict normal form that is equivalent to this formula}
     *
     * @param formulaNormalForm the {@link FormulaNormalForm normal form}
     */
    default Result<? extends IFormula> toStrictNormalForm(FormulaNormalForm formulaNormalForm) {
        return toNormalForm(this, formulaNormalForm, true);
    }

    /**
     * {@return a formula in strict negation normal form that is equivalent to this formula}
     */
    default Result<? extends IFormula> toNNF() {
        return toStrictNormalForm(FormulaNormalForm.NNF);
    }

    /**
     * {@return a formula in strict conjunctive normal form that is equivalent to this formula}
     */
    default Result<? extends IFormula> toCNF() {
        return toStrictNormalForm(FormulaNormalForm.CNF);
    }

    /**
     * {@return a formula in strict disjunctive normal form that is equivalent to this formula}
     */
    default Result<? extends IFormula> toDNF() {
        return toStrictNormalForm(FormulaNormalForm.DNF);
    }

    @Override
    default LinkedHashSet<String> getVariableNames() {
        return IExpression.super.getVariableNames();
    }

    // TODO: mutate/analyze analogous to FeatureModel?
}
