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
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.predicate.IPredicate;
import java.util.List;
import java.util.function.Predicate;

/**
 * Tests whether a formula is in (strict) normal form.
 * Strict normal form is a special case of each normal form and usually easier to process in an automated fashion.
 * Thus, we usually allow normal forms as input and use strict normal form as output.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class ANormalFormTester implements ITreeVisitor<IFormula, Boolean>, Predicate<IFormula> {
    protected boolean isNormalForm = true;
    protected boolean isStrictNormalForm = true;
    protected final boolean isStrict;

    public ANormalFormTester(boolean isStrict) {
        this.isStrict = isStrict;
    }

    @Override
    public void reset() {
        isNormalForm = true;
        isStrictNormalForm = true;
    }

    @Override
    public Result<Boolean> getResult() {
        return Result.of(isStrict ? isStrictNormalForm : isNormalForm);
    }

    @Override
    public boolean test(IFormula formula) {
        Trees.traverse(formula, this);
        return getResult().get();
    }

    public boolean isNormalForm() {
        return isNormalForm;
    }

    public boolean isStrictNormalForm() {
        return isStrictNormalForm;
    }

    protected TraversalAction processLevelOne(
            List<IFormula> path, IFormula formula, Class<? extends IConnective> connectiveClass) {
        if (path.size() > 1) {
            isNormalForm = false;
            isStrictNormalForm = false;
            return TraversalAction.SKIP_ALL;
        }
        for (final IExpression child : formula.getChildren()) {
            if (!connectiveClass.isInstance(child)) {
                if (!(child instanceof IPredicate)) {
                    isNormalForm = false;
                    isStrictNormalForm = false;
                    return TraversalAction.SKIP_ALL;
                }
                isStrictNormalForm = false;
            }
        }
        return TraversalAction.CONTINUE;
    }

    protected TraversalAction processLevelTwo(List<IFormula> path, IFormula formula) {
        if (path.size() > 2) {
            isNormalForm = false;
            isStrictNormalForm = false;
            return TraversalAction.SKIP_ALL;
        }
        if (path.size() < 2) {
            isStrictNormalForm = false;
        }
        for (final IExpression child : formula.getChildren()) {
            if (!(child instanceof IPredicate)) {
                isNormalForm = false;
                isStrictNormalForm = false;
                return TraversalAction.SKIP_ALL;
            }
        }
        return TraversalAction.CONTINUE;
    }

    protected TraversalAction processLevelThree(List<IFormula> path) {
        if (path.size() > 3) {
            isNormalForm = false;
            isStrictNormalForm = false;
            return TraversalAction.SKIP_ALL;
        }
        if (path.size() < 3) {
            isStrictNormalForm = false;
        }
        return TraversalAction.SKIP_CHILDREN;
    }
}
