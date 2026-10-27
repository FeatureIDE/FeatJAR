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
package de.featjar.formula.visitor;

import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.ACardinal;
import de.featjar.formula.structure.connective.AQuantifier;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.value.Constant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Simplifies complex connectives using well-known identities.
 * That is, replaces {@link Implies}, {@link BiImplies}, {@link AtLeast}, {@link AtMost}, {@link Between},
 * and {@link Choose} with {@link And}, {@link Or}, and {@link Not}.
 *
 * @author Sebastian Krieter
 */
public class EvaluationSimplifier implements ITreeVisitor<IExpression, Void> {

    @Override
    public Result<Void> nodeValidator(List<IExpression> path) {
        return ITreeVisitor.rootValidator(path, root -> root instanceof Reference, "expected formula reference");
    }

    @Override
    public TraversalAction firstVisit(List<IExpression> path) {
        final IExpression expression = ITreeVisitor.getCurrentNode(path);
        if (expression instanceof AQuantifier) {
            return TraversalAction.FAIL;
        }
        return TraversalAction.CONTINUE;
    }

    @Override
    public TraversalAction lastVisit(List<IExpression> path) {
        ITreeVisitor.getCurrentNode(path).replaceChildren(this::simplify);
        return TraversalAction.CONTINUE;
    }

    private IExpression simplify(IExpression formula) {
        Optional<Object> value = formula.evaluate();
        if (value.isPresent()) {
            if (Boolean.TRUE.equals(value.get())) {
                return True.INSTANCE;
            }
            if (Boolean.FALSE.equals(value.get())) {
                return False.INSTANCE;
            }
            return new Constant(value.get());
        }
        switch (formula) {
            case And and -> {
                if (and.getChildren().stream().anyMatch(Expressions::isTrue)) {
                    return new And(toList(and.getChildren().stream().filter(Expressions::isNotTrue)));
                }
            }
            case Or or -> {
                if (or.getChildren().stream().anyMatch(Expressions::isFalse)) {
                    return new And(toList(or.getChildren().stream().filter(Expressions::isNotFalse)));
                }
            }
            case Implies implies -> {
                IFormula leftChild = (IFormula) implies.getLeftExpression();
                IFormula rightChild = (IFormula) implies.getRightExpression();
                if (Expressions.isTrue(leftChild)) {
                    return rightChild;
                }
                if (Expressions.isFalse(rightChild)) {
                    return new Not(leftChild);
                }
            }
            case BiImplies biimplies -> {
                IFormula leftChild = (IFormula) biimplies.getLeftExpression();
                IFormula rightChild = (IFormula) biimplies.getRightExpression();
                if (Expressions.isTrue(leftChild)) {
                    return rightChild;
                }
                if (Expressions.isTrue(rightChild)) {
                    return leftChild;
                }
                if (Expressions.isFalse(leftChild)) {
                    return new Not(rightChild);
                }
                if (Expressions.isFalse(rightChild)) {
                    return new Not(leftChild);
                }
            }
            case ACardinal cardinal -> {
                int lower = (int) cardinal.getChildren().stream()
                        .filter(Expressions::isTrue)
                        .count();
                int upper = (int) cardinal.getChildren().stream()
                        .filter(Expressions::isFalse)
                        .count();
                if (lower > 0 || upper > 0) {
                    int newLower = cardinal.getRange().getLowerBound() - lower;
                    int newUpper = Math.min(
                            cardinal.getRange().getUpperBound(),
                            cardinal.getChildren().size() - (lower + upper));
                    assert newLower >= 0;
                    assert newUpper == -1 || newUpper >= newLower;
                    return new Between(
                            newLower,
                            newUpper,
                            toList(cardinal.getChildren().stream().filter(Expressions::isNotTrueOrFalse)));
                }
            }
            default -> {}
        }
        ;
        return null;
    }

    private List<IFormula> toList(Stream<? extends IExpression> expressions) {
        return expressions.map(v -> (IFormula) v).toList();
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
