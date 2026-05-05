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

import de.featjar.base.data.Maps;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.ExpressionKind;
import de.featjar.formula.structure.predicate.IPredicate;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transforms a formula into strict normal form by introducing auxiliary variables.
 * Does not modify its input.
 * Auxiliary variables are incrementally numbered and may clash if composed with other formulas.
 * todo: encode the original formula in the variable name (e.g., lossless base64), this would also make unification unnecessary
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class TseitinTransformer
        implements ITreeVisitor<IExpression, IExpression>, Function<IFormula, List<TseitinTransformer.Substitution>> {
    /**
     * Prefix for naming auxiliary variables.
     */
    public static final String AUXILIARY_VARIABLE_NAME_PREFIX = "_aux_";

    /**
     * A substitution of a formula with an auxiliary variable.
     * Hashed over the original (i.e., substituted) formula to simplify unification (i.e., using the same variable for the same substituted formula).
     */
    public static class Substitution {
        protected final IFormula originalFormula;
        protected final Variable auxiliaryVariable;
        protected final List<IFormula> clauseFormulas;

        protected Substitution(IFormula originalFormula, Variable auxiliaryVariable, int numberOfClauses) {
            this.originalFormula = originalFormula;
            this.auxiliaryVariable = auxiliaryVariable;
            this.clauseFormulas = new ArrayList<>(numberOfClauses);
        }

        protected Substitution(IFormula originalFormula, Variable auxiliaryVariable, IFormula clause) {
            this.originalFormula = originalFormula;
            this.auxiliaryVariable = auxiliaryVariable;
            this.clauseFormulas = new ArrayList<>(1);
            clauseFormulas.add(clause);
        }

        protected Substitution(
                IFormula originalFormula, Variable auxiliaryVariable, List<? extends IFormula> clauseFormulas) {
            this.originalFormula = originalFormula;
            this.auxiliaryVariable = auxiliaryVariable;
            this.clauseFormulas = new ArrayList<>(clauseFormulas);
        }

        /**
         * {@return this substitute's auxiliary variable}
         */
        public Variable getAuxiliaryVariable() {
            return auxiliaryVariable;
        }

        /**
         * {@return this substitute's clause formulas}
         * This formula encodes the definition of the auxiliary variable as the original formula.
         */
        public List<IFormula> getClauseFormulas() {
            return clauseFormulas;
        }

        protected void addClauseFormula(IFormula clauseFormula) {
            clauseFormulas.add(clauseFormula);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(originalFormula);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj != null)
                    && (getClass() == obj.getClass())
                    && Objects.equals(originalFormula, ((Substitution) obj).originalFormula);
        }
    }

    /**
     * Unifies a given list of substitutions.
     * That is, removes all duplicate substitutions.
     * @param substitutions the list of substitutions
     */
    public static void unify(List<Substitution> substitutions) {
        int currentAuxiliaryVariableIndex = 0;
        LinkedHashMap<Substitution, Substitution> unifiedSubstitutions = Maps.empty();
        for (Substitution substitution : substitutions) {
            Substitution storedSubstitution = unifiedSubstitutions.get(substitution);
            if (storedSubstitution == null) {
                unifiedSubstitutions.put(substitution, substitution);
                Variable variable = substitution.getAuxiliaryVariable();
                if (variable != null) {
                    variable.setName(AUXILIARY_VARIABLE_NAME_PREFIX + (++currentAuxiliaryVariableIndex));
                }
            } else {
                Variable variable = storedSubstitution.getAuxiliaryVariable();
                if (variable != null) {
                    substitution.getAuxiliaryVariable().setName(variable.getName());
                }
            }
        }
        substitutions.clear();
        substitutions.addAll(unifiedSubstitutions.keySet());
    }

    /**
     * {@return the clause formulas for a given list of substitutions}
     * Thus, encodes the definitions of all given substitutions.
     *
     * @param substitutions the list of substitutions
     */
    public static List<IFormula> getClauseFormulas(List<Substitution> substitutions) {
        return substitutions.stream()
                .map(Substitution::getClauseFormulas)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    protected final List<Substitution> substitutions = new ArrayList<>();
    protected final ArrayDeque<IFormula> stack = new ArrayDeque<>();
    protected final boolean isPlaistedGreenbaum;
    protected int currentAuxiliaryVariableIndex = 0;

    /**
     * Creates a new Tseitin transformer.
     */
    public TseitinTransformer() {
        this(false);
    }

    /**
     * Creates a new Tseitin transformer.
     *
     * @param isPlaistedGreenbaum whether to use the Plaisted-Greenbaum optimization
     */
    public TseitinTransformer(boolean isPlaistedGreenbaum) {
        this.isPlaistedGreenbaum = isPlaistedGreenbaum;
    }

    @Override
    public List<Substitution> apply(IFormula formula) {
        ExpressionKind.NNF.assertFor(formula);
        substitutions.clear();
        stack.clear();
        formula.traverse(this);
        return substitutions;
    }

    @Override
    public TraversalAction firstVisit(List<IExpression> path) {
        IExpression expression = ITreeVisitor.getCurrentNode(path);
        if (expression instanceof IPredicate) {
            return TraversalAction.SKIP_CHILDREN;
        } else if ((expression instanceof IConnective)) {
            stack.push((IFormula) expression);
            return TraversalAction.CONTINUE;
        } else {
            return TraversalAction.FAIL;
        }
    }

    @Override
    public TraversalAction lastVisit(List<IExpression> path) {
        IFormula formula = (IFormula) ITreeVisitor.getCurrentNode(path);
        if (formula instanceof IPredicate) {
            stack.push(formula);
        } else {
            List<Literal> newChildren = new ArrayList<>();
            IFormula lastFormula = stack.pop();
            while (lastFormula != formula) {
                newChildren.add((Literal) lastFormula);
                lastFormula = stack.pop();
            }

            if (stack.isEmpty()) {
                if (lastFormula instanceof And) {
                    substitutions.add(new Substitution(lastFormula, null, newChildren));
                } else {
                    substitutions.add(new Substitution(lastFormula, null, new Or(newChildren)));
                }
            } else {
                stack.push(new Literal(newAuxiliaryVariable(newChildren, lastFormula)));
            }
        }
        return TraversalAction.CONTINUE;
    }

    protected Variable newAuxiliaryVariable(List<Literal> newChildren, IFormula originalFormula) {
        Variable variable = new Variable(AUXILIARY_VARIABLE_NAME_PREFIX + (++currentAuxiliaryVariableIndex));
        Substitution substitution = new Substitution(originalFormula, variable, newChildren.size() + 1);
        substitutions.add(substitution);

        Literal auxiliaryLiteral = new Literal(substitution.auxiliaryVariable);
        if (originalFormula instanceof And) {
            ArrayList<Literal> flippedChildren = new ArrayList<>();
            for (Literal l : newChildren) {
                substitution.addClauseFormula(new Or(auxiliaryLiteral.invert(), l));
                if (!isPlaistedGreenbaum) flippedChildren.add(l.invert());
            }
            if (!isPlaistedGreenbaum) {
                flippedChildren.add(auxiliaryLiteral);
                substitution.addClauseFormula(new Or(flippedChildren));
            }
        } else if (originalFormula instanceof Or) {
            ArrayList<Literal> flippedChildren = new ArrayList<>();
            for (Literal l : newChildren) {
                if (!isPlaistedGreenbaum) substitution.addClauseFormula(new Or(auxiliaryLiteral, l.invert()));
                flippedChildren.add(l);
            }
            flippedChildren.add(auxiliaryLiteral.invert());
            substitution.addClauseFormula(new Or(flippedChildren));
        }
        return substitution.auxiliaryVariable;
    }
}
