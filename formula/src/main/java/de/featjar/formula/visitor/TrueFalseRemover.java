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
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.IPredicate;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;

/**
 * Removes occurrences of {@link True} and {@link False} with a tautology or contradiction, respectively.
 *
 * @author Elias Kuiter
 */
public class TrueFalseRemover implements ITreeVisitor<IFormula, Void> {
    protected final Variable variable;

    public TrueFalseRemover(Variable variable) {
        this.variable = variable;
    }

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
            return TraversalAction.CONTINUE;
        } else {
            return TraversalAction.FAIL;
        }
    }

    @Override
    public TraversalAction lastVisit(List<IFormula> path) {
        final IFormula formula = ITreeVisitor.getCurrentNode(path);
        formula.replaceChildren(c -> c.equals(Expressions.False)
                ? new And(new Literal(variable), new Literal(false, variable))
                : c.equals(Expressions.True) ? new Or(new Literal(variable), new Literal(false, variable)) : null);
        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
