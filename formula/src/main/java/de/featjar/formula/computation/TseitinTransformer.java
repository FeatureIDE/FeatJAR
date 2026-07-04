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
import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.ExpressionKind;
import de.featjar.formula.structure.predicate.IPredicate;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
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
    public static final String AUXILIARY_VARIABLE_DEFAULT_PREFIX = "_aux_";

    public static final String AUXILIARY_VARIABLE_DEFAULT_SUFFIX = "";
    public static final String AUXILIARY_VARIABLE_NAME_PATTERN = "%s_%s-%d";

    /**
     * A substitution of a formula with an auxiliary variable.
     * Hashed over the original (i.e., substituted) formula to simplify unification (i.e., using the same variable for the same substituted formula).
     */
    public static class Substitution {
        protected final IFormula originalFormula;
        protected final List<IFormula> clauseFormulas;
        protected Variable auxiliaryVariable;

        protected Substitution(IFormula originalFormula, Variable auxiliaryVariable, int numberOfClauses) {
            this.originalFormula = originalFormula;
            this.auxiliaryVariable = auxiliaryVariable;
            this.clauseFormulas = new ArrayList<>(numberOfClauses);
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
    public static List<Substitution> unify(List<Substitution> substitutions, String prefix, String suffix) {
        int auxiliaryVariableIndex = 0;
        LinkedHashMap<Substitution, Substitution> unifiedSubstitutions = Maps.empty();
        for (Substitution substitution : substitutions) {
            Substitution storedSubstitution = unifiedSubstitutions.get(substitution);
            final Variable auxiliaryVariable = substitution.getAuxiliaryVariable();
            if (storedSubstitution == null) {
                unifiedSubstitutions.put(substitution, substitution);
                if (auxiliaryVariable != null) {
                    auxiliaryVariable.setName(variableName(prefix, suffix, ++auxiliaryVariableIndex));
                }
            } else {
                if (auxiliaryVariable != null) {
                    auxiliaryVariable.setName(
                            storedSubstitution.getAuxiliaryVariable().getName());
                }
            }
        }
        return new ArrayList<>(unifiedSubstitutions.keySet());
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

    protected String prefix;
    protected String suffix;

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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public List<Substitution> apply(IFormula formula) {
        ExpressionKind.NNF.requireKind(formula);
        reset();
        formula.traverse(this);
        return substitutions;
    }

    @Override
    public void reset() {
        substitutions.clear();
        stack.clear();
    }

    @Override
    public Result<IExpression> getResult() {
        return Result.of(new And(getClauseFormulas(unify(substitutions, prefix, suffix))));
    }

    @Override
    public TraversalAction firstVisit(List<IExpression> path) {
        IExpression expression = ITreeVisitor.getCurrentNode(path);
        if (expression instanceof IPredicate) {
            return TraversalAction.SKIP_CHILDREN;
        } else if (expression instanceof Reference) {
            return TraversalAction.CONTINUE;
        } else if (expression instanceof IConnective) {
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
        } else if (!(formula instanceof Reference)) {
            List<Literal> newChildren = new ArrayList<>();
            for (IFormula lastFormula = stack.pop(); lastFormula != formula; lastFormula = stack.pop()) {
                newChildren.add((Literal) lastFormula);
            }
            final Literal literal = new Literal(newAuxiliaryVariable(newChildren, formula));
            if (stack.isEmpty()) {
                substitutions.getLast().addClauseFormula(new Or(literal));
            } else {
                stack.push(literal);
            }
        }
        return TraversalAction.CONTINUE;
    }

    protected Variable newAuxiliaryVariable(List<Literal> newChildren, IFormula originalFormula) {
        Substitution substitution = new Substitution(
                originalFormula,
                new Variable(variableName(prefix, suffix, ++currentAuxiliaryVariableIndex)),
                newChildren.size() + 1);
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

    static String variableName(String prefix, String suffix, int index) {
        return String.format(
                AUXILIARY_VARIABLE_NAME_PATTERN,
                prefix != null ? prefix : AUXILIARY_VARIABLE_DEFAULT_PREFIX,
                suffix != null ? suffix : AUXILIARY_VARIABLE_DEFAULT_SUFFIX,
                index);
    }
}
