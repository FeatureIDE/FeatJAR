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
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.predicate.IPredicate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simplifies complex connectives using well-known identities.
 * That is, replaces {@link Implies}, {@link BiImplies}, {@link AtLeast}, {@link AtMost}, {@link Between},
 * and {@link Choose} with {@link And}, {@link Or}, and {@link Not}.
 *
 * @author Sebastian Krieter
 */
public class ConnectiveSimplifier implements ITreeVisitor<IFormula, Void> {

    private boolean fail;

    @Override
    public void reset() {
        fail = false;
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
            if (formula instanceof AQuantifier) {
                return TraversalAction.FAIL;
            }
            return TraversalAction.CONTINUE;
        } else {
            return TraversalAction.FAIL;
        }
    }

    @Override
    public TraversalAction lastVisit(List<IFormula> path) {
        final IFormula formula = ITreeVisitor.getCurrentNode(path);
        if (!(formula instanceof IPredicate)) formula.replaceChildren(this::replace);
        if (fail) {
            return TraversalAction.FAIL;
        }
        return TraversalAction.CONTINUE;
    }

    @SuppressWarnings("unchecked")
    private IFormula replace(IExpression formula) {
        if ((formula instanceof IPredicate)
                || (formula instanceof And)
                || (formula instanceof Or)
                || (formula instanceof Not)) {
            return null;
        }
        final List<IFormula> children = (List<IFormula>) formula.getChildren();
        IFormula newFormula;
        if (formula instanceof Implies) {
            newFormula = new Or(new Not(children.get(0)), children.get(1));
        } else if (formula instanceof BiImplies) {
            newFormula = new And( //
                    new Or(new Not(children.get(0)), children.get(1)),
                    new Or(new Not(children.get(1)), children.get(0)));
        } else if (formula instanceof AtLeast) {
            newFormula = new And(atLeastK(children, ((AtLeast) formula).getMinimum()));
        } else if (formula instanceof AtMost) {
            newFormula = new And(atMostK(children, ((AtMost) formula).getMaximum()));
        } else if (formula instanceof Between) {
            final Between between = (Between) formula;
            newFormula = new And(
                    new And(atLeastK(children, between.getMinimum())),
                    new And(atMostK(children, between.getMaximum())));
        } else if (formula instanceof Choose) {
            final Choose choose = (Choose) formula;
            newFormula = new And(
                    new And(atLeastK(children, choose.getBound())), new And(atMostK(children, choose.getBound())));
        } else {
            fail = true;
            return null;
        }
        return newFormula;
    }

    private void atMostOneRec(List<? extends IFormula> elements, List<IFormula> groupedElements) {
        final int n = elements.size();
        if (n > 1) {
            int half = n / 2;
            List<? extends IFormula> left = elements.subList(0, half);
            List<? extends IFormula> right = elements.subList(half, n);
            atMostOneRec(left, groupedElements);
            atMostOneRec(right, groupedElements);
            groupedElements.add(new Or(new And(left), new And(right)));
        }
    }

    private List<IFormula> atMostK(List<? extends IFormula> elements, int k) {
        final int n = elements.size();

        // return tautology
        if (k <= 0) {
            return Collections.singletonList(Expressions.False);
        }

        // return contradiction
        if (k > n) {
            return Collections.singletonList(Expressions.True);
        }

        List<Not> negatedElements = elements.stream().map(Not::new).collect(Collectors.toList());
        if (k == 1) {
            final List<IFormula> groupedElements = new ArrayList<>(n - 1);
            atMostOneRec(negatedElements, groupedElements);
            return groupedElements;
        } else {
            return groupElements(negatedElements, k, n);
        }
    }

    private List<IFormula> atLeastK(List<? extends IFormula> elements, int k) {
        final int n = elements.size();

        // return tautology
        if (k <= 0) {
            return Collections.singletonList(Expressions.True);
        }

        // return contradiction
        if (k > n) {
            return Collections.singletonList(Expressions.False);
        }

        return groupElements(elements, n - k, n);
    }

    public static List<IFormula> groupElements(List<? extends IFormula> elements, int k, final int n) {
        final List<IFormula> groupedElements = new ArrayList<>();
        final IFormula[] clause = new IFormula[k + 1];
        final int[] index = new int[k + 1];

        // the position that is currently filled in clause
        int level = 0;
        index[level] = -1;

        while (level >= 0) {
            // fill this level with the next element
            index[level]++;
            // did we reach the maximum for this level
            if (index[level] >= (n - (k - level))) {
                // go to previous level
                level--;
            } else {
                clause[level] = elements.get(index[level]);
                if (level == k) {
                    final IFormula[] clonedClause = new IFormula[clause.length];
                    System.arraycopy(clause, 0, clonedClause, 0, clause.length);
                    groupedElements.add(new Or(clonedClause));
                } else {
                    // go to next level
                    level++;
                    // allow only ascending orders (to prevent from duplicates)
                    index[level] = index[level - 1];
                }
            }
        }
        return groupedElements;
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
