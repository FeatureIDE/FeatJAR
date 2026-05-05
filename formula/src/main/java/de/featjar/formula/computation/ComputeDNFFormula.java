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
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.ExpressionKind;
import de.featjar.formula.structure.predicate.Literal;
import java.util.List;

/**
 * Transforms a formula into strict disjunctive normal form.
 *
 * @author Sebastian Krieter
 */
public class ComputeDNFFormula extends AComputation<IFormula> {
    protected static final Dependency<IFormula> NNF_FORMULA = Dependency.newDependency(IFormula.class);

    /**
     * Determines whether the resulting formula is strict.
     */
    public static final Dependency<Boolean> IS_STRICT = Dependency.newDependency(Boolean.class);

    public ComputeDNFFormula(IComputation<IFormula> nnfFormula) {
        super(nnfFormula, Computations.of(Boolean.TRUE));
    }

    protected ComputeDNFFormula(ComputeDNFFormula other) {
        super(other);
    }

    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        IFormula nnfFormula = NNF_FORMULA.get(dependencyList);
        final Reference referenceFormula;
        if (nnfFormula instanceof Reference) {
            referenceFormula = (Reference) nnfFormula;
            nnfFormula = referenceFormula.getExpression();
        } else {
            referenceFormula = null;
        }
        if (!ExpressionKind.NNF.test(nnfFormula)) {
            throw new IllegalArgumentException("Formula is not in NNF");
        }
        boolean isStrict = IS_STRICT.get(dependencyList);
        DistributiveTransformer formulaToDistributiveNFFormula = new DistributiveTransformer(false, null);
        return formulaToDistributiveNFFormula
                .apply(nnfFormula)
                .map(f -> isStrict ? toStrictForm(f) : f)
                .map(f -> referenceFormula == null ? f : referenceFormula.setFormula(f));
    }

    private static IFormula toStrictForm(IFormula formula) {
        if (formula instanceof Literal) {
            formula = new Or(new And(formula));
        } else if (formula instanceof And) {
            formula = new Or(formula);
        } else {
            formula.replaceChildren(child -> (child instanceof Literal) ? new And((IFormula) child) : child);
        }
        return formula;
    }
}
