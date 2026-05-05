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

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.ExpressionKind;
import de.featjar.formula.structure.predicate.Literal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transforms a formula into strict normal form using the distributive law.
 * Does not modify its input.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class DistributiveTransformer implements Function<IFormula, Result<IFormula>> {
    /**
     * Thrown to show that an ongoing distributive transformation has been cancelled.
     */
    public static class CancelledException extends Exception {
        private static final long serialVersionUID = -383875723565837234L;

        /**
         * Creates a new cancelled exception.
         *
         * @param cause the cause
         */
        public CancelledException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Predicate for determining whether to cancel an ongoing distributive transformation.
     */
    public interface ICancelPredicate extends Function<LinkedHashSet<Literal>, Throwable> {}

    /**
     * Cancels an ongoing distributive transformation when a given maximum number of literals has been exceeded.
     * Limits the maximum number of literals in the resulting formula.
     */
    public static class MaximumNumberOfLiteralsCancelPredicate implements ICancelPredicate {
        protected final int maximumNumberOfLiterals;
        int currentNumberOfLiterals = 0;

        /**
         * Creates a maximum number of literals cancel predicate.
         *
         * @param maximumNumberOfLiterals the maximum number of literals
         */
        public MaximumNumberOfLiteralsCancelPredicate(int maximumNumberOfLiterals) {
            this.maximumNumberOfLiterals = maximumNumberOfLiterals;
        }

        @Override
        public Throwable apply(LinkedHashSet<Literal> clause) {
            currentNumberOfLiterals += clause.size();
            return currentNumberOfLiterals > maximumNumberOfLiterals
                    ? new RuntimeException("exceeded maximum number of literals " + maximumNumberOfLiterals
                            + " with clause of size " + currentNumberOfLiterals)
                    : null;
        }
    }

    protected static class PathElement {
        protected final IExpression expression;
        protected final List<IExpression> newChildren = new ArrayList<>();
        protected int maximumDepth = 0;

        PathElement(IExpression expression) {
            this.expression = expression;
        }
    }

    protected final boolean isCNF;
    protected final Class<? extends IConnective> clauseClass;
    protected final Function<List<? extends IFormula>, IFormula> clauseConstructor;
    protected final ICancelPredicate cancelPredicate;

    /**
     * Creates a new distributive transformer.
     */
    public DistributiveTransformer() {
        this(true, null);
    }

    /**
     * Creates a new distributive transformer.
     *
     * @param isCNF whether strict CNF or DNF should be computed
     * @param cancelPredicate the cancel predicate, if any
     */
    public DistributiveTransformer(boolean isCNF, ICancelPredicate cancelPredicate) {
        this.cancelPredicate = cancelPredicate != null ? cancelPredicate : clause -> null;
        this.isCNF = isCNF;
        if (this.isCNF) {
            clauseClass = Or.class;
            clauseConstructor = Or::new;
        } else {
            clauseClass = And.class;
            clauseConstructor = And::new;
        }
    }

    @Override
    public Result<IFormula> apply(IFormula formula) {
        ExpressionKind.NNF.assertFor(formula);
        formula = (IFormula) formula.cloneTree();
        if (isCNF) formula = (formula instanceof And) ? (And) formula : new And(formula);
        else formula = (formula instanceof Or) ? (Or) formula : new Or(formula);

        ArrayList<PathElement> path = new ArrayList<>();
        ArrayDeque<IFormula> stack = new ArrayDeque<>();
        stack.addLast(formula);
        while (!stack.isEmpty()) {
            IFormula currentFormula = stack.getLast();
            boolean firstEncounter = path.isEmpty() || (currentFormula != path.get(path.size() - 1).expression);
            if (firstEncounter) {
                if (currentFormula instanceof Literal) {
                    PathElement parent = path.get(path.size() - 1);
                    parent.newChildren.add(currentFormula);
                    stack.removeLast();
                } else {
                    path.add(new PathElement(currentFormula));
                    currentFormula.getChildren().forEach(e -> stack.addLast((IFormula) e));
                }
            } else {
                PathElement currentElement = path.remove(path.size() - 1);
                currentFormula.setChildren(currentElement.newChildren);

                if (!path.isEmpty()) {
                    PathElement parentElement = path.get(path.size() - 1);
                    parentElement.maximumDepth = Math.max(currentElement.maximumDepth + 1, parentElement.maximumDepth);
                }

                if ((clauseClass == currentFormula.getClass()) && (currentElement.maximumDepth > 0)) {
                    PathElement parentElement = path.get(path.size() - 1);
                    try {
                        parentElement.newChildren.addAll(transform(currentFormula));
                    } catch (CancelledException e) {
                        return Result.empty(e);
                    }
                    parentElement.maximumDepth = 1;
                } else {
                    if (!path.isEmpty()) {
                        PathElement parentElement = path.get(path.size() - 1);
                        parentElement.newChildren.add(currentFormula);
                    }
                }
                stack.removeLast();
            }
        }
        return Result.of(formula);
    }

    @SuppressWarnings("unchecked")
    private List<IFormula> transform(IFormula formula) throws CancelledException {
        if (formula instanceof Literal) {
            return new ArrayList<>();
        } else {
            ArrayList<LinkedHashSet<Literal>> clauses = new ArrayList<>();
            List<IFormula> children = new ArrayList<>((List<IFormula>) formula.getChildren());
            children.sort(Comparator.comparingInt(ITree::getChildrenCount));
            transform(children, clauses, new LinkedHashSet<>(children.size() * 2), 0);

            List<IFormula> filteredClauseList = new ArrayList<>(clauses.size());
            clauses.sort(Comparator.comparingInt(Set::size));
            int lastIndex = clauses.size();
            for (int i = 0; i < lastIndex; i++) {
                LinkedHashSet<Literal> set = clauses.get(i);
                if (set != null) {
                    for (int j = i + 1; j < lastIndex; j++) {
                        LinkedHashSet<Literal> set2 = clauses.get(j);
                        if (set2 != null) {
                            if (set2.containsAll(set)) {
                                clauses.set(j, null);
                            }
                        }
                    }
                    filteredClauseList.add(clauseConstructor.apply(new ArrayList<>(set)));
                }
            }

            return filteredClauseList;
        }
    }

    @SuppressWarnings("unchecked")
    private void transform(
            List<IFormula> children, List<LinkedHashSet<Literal>> clauses, LinkedHashSet<Literal> literals, int index)
            throws CancelledException {
        if (index == children.size()) {
            LinkedHashSet<Literal> newClause = new LinkedHashSet<>(literals);
            Throwable cancelThrowable = cancelPredicate.apply(newClause);
            if (cancelThrowable != null) {
                throw new CancelledException(cancelThrowable);
            }
            clauses.add(newClause);
        } else {
            IExpression child = children.get(index);
            if (child instanceof Literal) {
                Literal clauseLiteral = (Literal) child;
                if (literals.contains(clauseLiteral)) {
                    transform(children, clauses, literals, index + 1);
                } else if (!literals.contains(clauseLiteral.invert())) {
                    literals.add(clauseLiteral);
                    transform(children, clauses, literals, index + 1);
                    literals.remove(clauseLiteral);
                }
            } else {
                if (isRedundant(literals, child)) {
                    transform(children, clauses, literals, index + 1);
                } else {
                    for (IExpression grandChild : child.getChildren()) {
                        if (grandChild instanceof Literal) {
                            Literal newlyAddedLiteral = (Literal) grandChild;
                            if (!literals.contains(newlyAddedLiteral.invert())) {
                                literals.add(newlyAddedLiteral);
                                transform(children, clauses, literals, index + 1);
                                literals.remove(newlyAddedLiteral);
                            }
                        } else {
                            List<Literal> greatGrandChildren = (List<Literal>) grandChild.getChildren();
                            if (containsNoComplements(literals, greatGrandChildren)) {
                                List<Literal> newlyAddedLiterals = greatGrandChildren.stream()
                                        .filter(literals::add)
                                        .collect(Collectors.toList());
                                transform(children, clauses, literals, index + 1);
                                newlyAddedLiterals.forEach(literals::remove);
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean containsNoComplements(LinkedHashSet<Literal> literals, List<Literal> greatGrandChildren) {
        return greatGrandChildren.stream().map(Literal::invert).noneMatch(literals::contains);
    }

    protected boolean isRedundant(LinkedHashSet<Literal> literals, IExpression child) {
        return child.getChildren().stream().anyMatch(e -> isRedundant(e, literals));
    }

    @SuppressWarnings("unchecked")
    protected static boolean isRedundant(IExpression expression, LinkedHashSet<Literal> literals) {
        return (expression instanceof Literal)
                ? literals.contains(expression)
                : literals.containsAll((List<Literal>) expression.getChildren());
    }
}
