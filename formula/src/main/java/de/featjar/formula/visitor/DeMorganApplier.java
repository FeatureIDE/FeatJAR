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
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.predicate.IInvertiblePredicate;
import de.featjar.formula.structure.predicate.IPredicate;
import de.featjar.formula.structure.predicate.Literal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Applies De Morgan's laws to push {@link Not} down towards a {@link IPredicate}.
 *
 * @author Sebastian Krieter
 */
public class DeMorganApplier implements ITreeVisitor<IFormula, Void> {
    @Override
    public Result<Void> nodeValidator(List<IFormula> path) {
        return ITreeVisitor.rootValidator(path, root -> root instanceof Reference, "expected formula reference");
    }

    @Override
    public TraversalAction firstVisit(List<IFormula> path) {
        final IFormula formula = ITreeVisitor.getCurrentNode(path);
        if (formula instanceof IPredicate) {
            return TraversalAction.SKIP_CHILDREN;
        } else if (formula instanceof IConnective) {
            formula.replaceChildren(expression -> replace((IFormula) expression));
            return TraversalAction.CONTINUE;
        } else {
            return TraversalAction.FAIL;
        }
    }

    protected IFormula replace(IFormula formula) {
        IFormula newFormula = formula;
        while (newFormula instanceof Not) {
            final IFormula notChild = (IFormula) ((Not) newFormula).getExpression();
            if (notChild instanceof IInvertiblePredicate) {
                newFormula = ((Literal) notChild).invert();
            } else if (notChild instanceof Not) {
                newFormula = (IFormula) ((Not) notChild).getExpression();
            } else if (notChild instanceof Or) {
                newFormula = new And(notChild.getChildren().stream()
                        .map(c -> new Not((IFormula) c))
                        .collect(Collectors.toList()));
            } else if (notChild instanceof And) {
                newFormula = new Or(notChild.getChildren().stream()
                        .map(c -> new Not((IFormula) c))
                        .collect(Collectors.toList()));
            }
        }
        return newFormula;
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
