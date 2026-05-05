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

import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.IPredicate;
import de.featjar.formula.structure.predicate.True;
import java.util.ArrayList;
import java.util.List;

/**
 * Replaces trivial tautologies with {@link True} and trivial contradictions with {@link False}.
 *
 * @author Elias Kuiter
 */
public class TrueFalseSimplifier implements ITreeVisitor<IFormula, Void> {
    @Override
    public TraversalAction firstVisit(List<IFormula> path) {
        final IFormula formula = ITreeVisitor.getCurrentNode(path);
        if (formula instanceof IPredicate) {
            return TraversalAction.SKIP_CHILDREN;
        } else if (formula instanceof IConnective) {
            return TraversalAction.CONTINUE;
        } else {
            return TraversalAction.FAIL;
        }
    }

    @Override
    public TraversalAction lastVisit(List<IFormula> path) {
        final IFormula formula = ITreeVisitor.getCurrentNode(path);
        formula.replaceChildren((child) -> {
            if (child instanceof And) {
                return simplifyAnd(child);
            } else if (child instanceof Or) {
                return simplifyOr(child);
            } else if (child instanceof Implies) {
                return simplifyImplies(child);
            } else if (child instanceof BiImplies) {
                return simplifyBiImplies(child);
            } else if (child instanceof ACardinal) {
                return simplifyCardinal(child);
            }
            return null;
        });
        return TraversalAction.CONTINUE;
    }

    public IExpression simplifyAnd(IExpression child) {
        if (child.getChildren().stream().anyMatch(c -> c == Expressions.False)) {
            return Expressions.False;
        } else {
            child.flatReplaceChildren((child2) -> child2 instanceof True ? new ArrayList<>() : null);
        }
        return (child.getChildrenCount() == 0) ? Expressions.True : null;
    }

    public IExpression simplifyOr(IExpression child) {
        if (child.getChildren().stream().anyMatch(c -> c == Expressions.True)) {
            return Expressions.True;
        } else {
            child.flatReplaceChildren((child2) -> child2 instanceof False ? new ArrayList<>() : null);
        }
        return (child.getChildrenCount() == 0) ? Expressions.False : null;
    }

    private IExpression simplifyImplies(final IExpression child) {
        assert child.getChildrenCount() == 2;
        if (child.getChildren().get(0) instanceof False) {
            return True.INSTANCE;
        } else if (child.getChildren().get(0) instanceof True) {
            return child.getChildren().get(1);
        } else if (child.getChildren().get(1) instanceof False
                && child.getChildren().get(0) instanceof IFormula) {
            return new Not((IFormula) child.getChildren().get(0));
        } else if (child.getChildren().get(1) instanceof True) {
            return True.INSTANCE;
        } else if (child.getChildren().get(0).equals(child.getChildren().get(1))) {
            return True.INSTANCE;
        }
        return null;
    }

    private IExpression simplifyBiImplies(final IExpression child) {
        assert child.getChildrenCount() == 2;
        if ((child.getChildren().get(0) instanceof False && child.getChildren().get(1) instanceof True)
                || (child.getChildren().get(0) instanceof True
                        && child.getChildren().get(1) instanceof False)) {
            return False.INSTANCE;
        } else if ((child.getChildren().get(0) instanceof True
                        && child.getChildren().get(1) instanceof True)
                || (child.getChildren().get(0) instanceof False
                        && child.getChildren().get(1) instanceof False)) {
            return True.INSTANCE;
        } else if (child.getChildren().get(0) instanceof False
                && child.getChildren().get(1) instanceof IFormula) {
            return new Not((IFormula) child.getChildren().get(1));
        } else if (child.getChildren().get(1) instanceof False
                && child.getChildren().get(0) instanceof IFormula) {
            return new Not((IFormula) child.getChildren().get(0));
        } else if (child.getChildren().get(0) instanceof True) {
            return child.getChildren().get(1);
        } else if (child.getChildren().get(1) instanceof True) {
            return child.getChildren().get(0);
        } else if (child.getChildren().get(0).equals(child.getChildren().get(1))) {
            return True.INSTANCE;
        }
        return null;
    }

    private IExpression simplifyCardinal(final IExpression child) {
        // false entfernen und trues auch entfernen und range anpassen
        int trueCounter = 0;
        int otherCounter = 0;
        for (var childChild : child.getChildren()) {
            if (!(childChild instanceof True) && !(childChild instanceof False)) {
                otherCounter++;
            } else if (childChild instanceof True) {
                trueCounter++;
            }
        }
        ACardinal cardinal = (ACardinal) child;
        int lowerBound = cardinal.getRange().getLowerBound();
        int upperBound = cardinal.getRange().getUpperBound();
        if (lowerBound <= trueCounter && (upperBound == Range.OPEN || upperBound >= trueCounter + otherCounter)) {
            return True.INSTANCE;
        } else if (lowerBound > trueCounter + otherCounter || (upperBound != Range.OPEN && upperBound < trueCounter)) {
            return False.INSTANCE;
        }

        if (trueCounter > 0) {
            cardinal.setRange(Range.of(
                    lowerBound == Range.OPEN ? Range.OPEN : Math.max(lowerBound - trueCounter, 0),
                    upperBound == Range.OPEN ? Range.OPEN : Math.max(upperBound - trueCounter, 0)));
        }
        child.flatReplaceChildren(
                (childChild) -> childChild instanceof False || childChild instanceof True ? new ArrayList<>() : null);

        return null;
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
