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
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.predicate.IPolarPredicate;
import de.featjar.formula.structure.predicate.IPredicate;
import de.featjar.formula.structure.predicate.Literal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces literals with other literals.
 *
 * @author Andreas Gerasimow
 */
public class LiteralReplacer implements ITreeVisitor<IFormula, Void> {
    Map<IPolarPredicate, IExpression> literalMap;

    public LiteralReplacer(Map<IPolarPredicate, IExpression> literalMap) {
        this.literalMap = literalMap;
    }

    public LiteralReplacer(Assignment assignment) {
        this.literalMap = new HashMap<>();
        assignment.getAll().forEach((key, value) -> {
            if (value instanceof IExpression) {
                this.literalMap.put(new Literal(key), (IExpression) value);
            } else {
                throw new IllegalArgumentException("Value " + value + " is not an IExpression.");
            }
        });
    }

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
        formula.replaceChildren(c -> {
            if (c instanceof Literal && literalMap.containsKey(c)) {
                return literalMap.get(c);
            }
            return c;
        });
        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
