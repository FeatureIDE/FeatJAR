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

import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.IPredicate;
import java.util.List;

/**
 * Tests whether a formula is in negation normal form.
 * The formula {@code new Not(new Not(new Literal("x")))} is neither in NNF nor in strict NNF.
 * The formula {@code new Not(new Literal("x"))} is in NNF, but not in strict NNF.
 * The formula {@code new Literal(false, "x")} is in NNF and in strict NNF.
 */
public class NNFTester extends ANormalFormTester {

    public NNFTester(boolean isStrict) {
        super(isStrict);
    }

    @Override
    public TraversalAction firstVisit(List<IFormula> path) {
        final IFormula formula = ITreeVisitor.getCurrentNode(path);
        if (formula instanceof IPredicate) {
            return TraversalAction.SKIP_CHILDREN;
        } else if (formula instanceof IConnective) {
            if (formula instanceof Not) {
                isStrictNormalForm = false;
                if (!(((Not) formula).getExpression() instanceof IPredicate)) {
                    isNormalForm = false;
                }
            } else if (!(formula instanceof And) && !(formula instanceof Or)) {
                isNormalForm = false;
                isStrictNormalForm = false;
            }
            return TraversalAction.CONTINUE;
        } else {
            return TraversalAction.FAIL;
        }
    }
}
