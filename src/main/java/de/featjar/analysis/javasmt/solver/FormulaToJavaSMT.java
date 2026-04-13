/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt.solver;

import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.function.AAdd;
import de.featjar.formula.structure.term.function.AMultiply;
import de.featjar.formula.structure.term.function.IFunction;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.RationalFormula;
import org.sosy_lab.java_smt.api.RationalFormulaManager;
import org.sosy_lab.java_smt.api.SolverContext;

/**
 * Class containing functions that are used to translate formulas to java smt.
 *
 * @author Joshua Sprey
 * @author Sebastian Krieter
 */
public class FormulaToJavaSMT {

    private FormulaManager currentFormulaManager;
    private BooleanFormulaManager currentBooleanFormulaManager;
    private IntegerFormulaManager currentIntegerFormulaManager;
    private RationalFormulaManager currentRationalFormulaManager;
    private boolean isPrincess = false;
    private boolean createVariables = true;

    private final Map<String, VariableReference> variableMap = new LinkedHashMap<>();

    private static class VariableReference {
        private int index;
        private Variable variable;
        private Formula javaSmtVariable;
    }

    public FormulaToJavaSMT(SolverContext context) {
        setContext(context);
    }

    public void setContext(SolverContext context) {
        currentFormulaManager = context.getFormulaManager();
        currentBooleanFormulaManager = currentFormulaManager.getBooleanFormulaManager();
        currentIntegerFormulaManager = currentFormulaManager.getIntegerFormulaManager();
        if (context.getSolverName() != Solvers.PRINCESS) { // Princess does not support Rationals
            isPrincess = false;
            currentRationalFormulaManager = currentFormulaManager.getRationalFormulaManager();
        } else {
            isPrincess = true;
        }
    }

    public BooleanFormula nodeToFormula(IExpression expression) {
        if (expression instanceof Reference) {
            return nodeToFormula(expression.getChildren().get(0));
        } else if (expression instanceof True) {
            return currentBooleanFormulaManager.makeTrue();
        } else if (expression instanceof False) {
            return currentBooleanFormulaManager.makeFalse();
        } else if (expression instanceof Not) {
            return createNot(nodeToFormula(expression.getChildren().get(0)));
        } else if (expression instanceof Or) {
            return createOr(getChildren(expression));
        } else if (expression instanceof And) {
            return createAnd(getChildren(expression));
        } else if (expression instanceof BiImplies) {
            return createBiimplies(
                    nodeToFormula(expression.getChildren().get(0)),
                    nodeToFormula(expression.getChildren().get(1)));
        } else if (expression instanceof Implies) {
            return createImplies(
                    nodeToFormula(expression.getChildren().get(0)),
                    nodeToFormula(expression.getChildren().get(1)));
        } else if (expression instanceof Literal) {
            return handleLiteralNode((Literal) expression);
        } else if (expression instanceof LessThan) {
            return handleLessThanNode((LessThan) expression);
        } else if (expression instanceof GreaterThan) {
            return handleGreaterThanNode((GreaterThan) expression);
        } else if (expression instanceof LessEqual) {
            return handleLessEqualNode((LessEqual) expression);
        } else if (expression instanceof GreaterEqual) {
            return handleGreaterEqualNode((GreaterEqual) expression);
        } else if (expression instanceof Equals) {
            return handleEqualNode((Equals) expression);
        } else {
            throw new RuntimeException(
                    "The nodes of type: " + expression.getClass() + " are not supported by JavaSmt.");
        }
    }

    private List<BooleanFormula> getChildren(IExpression expression) {
        return expression.getChildren().stream() //
                .map(this::nodeToFormula) //
                .collect(Collectors.toList());
    }

    public BooleanFormula createAnd(List<BooleanFormula> collect) {
        return currentBooleanFormulaManager.and(collect);
    }

    public BooleanFormula createImplies(final BooleanFormula leftChild, final BooleanFormula rightChild) {
        return currentBooleanFormulaManager.implication(leftChild, rightChild);
    }

    public BooleanFormula createBiimplies(final BooleanFormula leftChild, final BooleanFormula rightChild) {
        return currentBooleanFormulaManager.equivalence(leftChild, rightChild);
    }

    public BooleanFormula createOr(List<BooleanFormula> collect) {
        return currentBooleanFormulaManager.or(collect);
    }

    public BooleanFormula createNot(final BooleanFormula childFormula) {
        return currentBooleanFormulaManager.not(childFormula);
    }

    private BooleanFormula handleEqualNode(Equals node) {
        final NumeralFormula leftTerm = termToFormula((ITerm) node.getLeftExpression());
        final NumeralFormula rightTerm = termToFormula((ITerm) node.getRightExpression());
        return createEqual(leftTerm, rightTerm);
    }

    public BooleanFormula createEqual(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
        if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
            return currentRationalFormulaManager.equal(leftTerm, rightTerm);
        } else {
            return currentIntegerFormulaManager.equal((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
        }
    }

    private BooleanFormula handleGreaterEqualNode(GreaterEqual node) {
        final NumeralFormula leftTerm = termToFormula((ITerm) node.getLeftExpression());
        final NumeralFormula rightTerm = termToFormula((ITerm) node.getRightExpression());
        return createGreaterEqual(leftTerm, rightTerm);
    }

    public BooleanFormula createGreaterEqual(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
        if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
            return currentRationalFormulaManager.greaterOrEquals(leftTerm, rightTerm);
        } else {
            return currentIntegerFormulaManager.greaterOrEquals((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
        }
    }

    private BooleanFormula handleLessEqualNode(LessEqual node) {
        final NumeralFormula leftTerm = termToFormula((ITerm) node.getLeftExpression());
        final NumeralFormula rightTerm = termToFormula((ITerm) node.getRightExpression());
        return createLessEqual(leftTerm, rightTerm);
    }

    public BooleanFormula createLessEqual(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
        if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
            return currentRationalFormulaManager.lessOrEquals(leftTerm, rightTerm);
        } else {
            return currentIntegerFormulaManager.lessOrEquals((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
        }
    }

    private BooleanFormula handleGreaterThanNode(GreaterThan node) {
        final NumeralFormula leftTerm = termToFormula((ITerm) node.getLeftExpression());
        final NumeralFormula rightTerm = termToFormula((ITerm) node.getRightExpression());
        return createGreaterThan(leftTerm, rightTerm);
    }

    public BooleanFormula createGreaterThan(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
        if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
            return currentRationalFormulaManager.greaterThan(leftTerm, rightTerm);
        } else {
            return currentIntegerFormulaManager.greaterThan((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
        }
    }

    private BooleanFormula handleLessThanNode(LessThan node) {
        final NumeralFormula leftTerm = termToFormula((ITerm) node.getLeftExpression());
        final NumeralFormula rightTerm = termToFormula((ITerm) node.getRightExpression());
        return createLessThan(leftTerm, rightTerm);
    }

    public BooleanFormula createLessThan(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
        if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
            return currentRationalFormulaManager.lessThan(leftTerm, rightTerm);
        } else {
            return currentIntegerFormulaManager.lessThan((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
        }
    }

    private NumeralFormula termToFormula(ITerm term) {
        if (term instanceof Constant) {
            return createConstant(((Constant) term).getValue());
        } else if (term instanceof Variable) {
            final Variable variable = (Variable) term;
            return handleVariable(variable);
        } else if (term instanceof IFunction) {
            return handleFunction((IFunction) term);
        } else {
            throw new RuntimeException("The given term is not supported by JavaSMT: " + term.getClass());
        }
    }

    private NumeralFormula handleFunction(IFunction function) {
        final NumeralFormula[] children = new NumeralFormula[function.getChildrenCount()];
        int index = 0;
        for (final IExpression term : function.getChildren()) {
            children[index++] = termToFormula((ITerm) term);
        }
        if (function.getType() == Double.class) {
            if (isPrincess) {
                throw new UnsupportedOperationException("Princess does not support variables from type: Double");
            }
            if (function instanceof AAdd) {
                return currentRationalFormulaManager.add(children[0], children[1]);
            } else if (function instanceof AMultiply) {
                return currentRationalFormulaManager.multiply(children[0], children[1]);
            } else {
                throw new RuntimeException(
                        "The given function is not supported by JavaSMT Rational Numbers: " + function.getClass());
            }
        } else if (function.getType() == Long.class) {
            if (function instanceof AAdd) {
                return currentIntegerFormulaManager.add((IntegerFormula) children[0], (IntegerFormula) children[1]);
            } else if (function instanceof AMultiply) {
                return currentIntegerFormulaManager.multiply(
                        (IntegerFormula) children[0], (IntegerFormula) children[1]);
            } else {
                throw new RuntimeException(
                        "The given function is not supported by JavaSMT Rational Numbers: " + function.getClass());
            }
        } else {
            throw new UnsupportedOperationException("Unknown function type: " + function.getType());
        }
    }

    public NumeralFormula createConstant(Object value) {
        if (value instanceof Long) {
            return currentIntegerFormulaManager.makeNumber((long) value);
        } else if (value instanceof Double) {
            if (isPrincess) {
                throw new UnsupportedOperationException("Princess does not support constants from type: Double");
            }
            return currentRationalFormulaManager.makeNumber((double) value);
        } else {
            throw new UnsupportedOperationException("Unknown constant type: " + value.getClass());
        }
    }

    private NumeralFormula handleVariable(Variable variable) {
        final Optional<Formula> formula =
                Optional.ofNullable(variableMap.get(variable.getName())).map(r -> r.javaSmtVariable);
        if (variable.getType() == Double.class) {
            if (isPrincess) {
                throw new UnsupportedOperationException("Princess does not support variables from type: Double");
            }
            return (NumeralFormula) formula.orElseGet(
                    () -> newVariable(variable, currentRationalFormulaManager::makeVariable).javaSmtVariable);
        } else if (variable.getType() == Long.class) {
            return (NumeralFormula) formula.orElseGet(
                    () -> newVariable(variable, currentIntegerFormulaManager::makeVariable).javaSmtVariable);
        } else {
            throw new UnsupportedOperationException("Unknown variable type: " + variable.getType());
        }
    }

    private BooleanFormula handleLiteralNode(Literal literal) {
        final VariableReference variableReference = Optional.ofNullable(
                        variableMap.get(((Variable) literal.getExpression()).getName()))
                .orElseGet(() ->
                        newVariable((Variable) literal.getExpression(), currentBooleanFormulaManager::makeVariable));
        BooleanFormula variable = (BooleanFormula) variableReference.javaSmtVariable;
        return literal.isPositive() ? variable : createNot(variable);
    }

    private VariableReference newVariable(
            final Variable variable, java.util.function.Function<String, ? extends Formula> variableCreator) {
        if (createVariables) {
            final Formula newVariable = variableCreator.apply(variable.getName());
            VariableReference variableReference = new VariableReference();
            variableReference.index = variableMap.size();
            variableReference.variable = variable;
            variableReference.javaSmtVariable = newVariable;
            variableMap.put(variable.getName(), variableReference);
            return variableReference;
        } else {
            throw new RuntimeException(variable.getName());
        }
    }

    public Optional<Integer> getVariableIndex(String name) {
        return Optional.ofNullable(variableMap.get(name)).map(r -> r.index);
    }

    public Optional<org.sosy_lab.java_smt.api.Formula> getVariableFormula(String name) {
        return Optional.ofNullable(variableMap.get(name)).map(r -> r.javaSmtVariable);
    }

    public Optional<Variable> getVariable(String name) {
        return Optional.ofNullable(variableMap.get(name)).map(r -> r.variable);
    }

    public List<org.sosy_lab.java_smt.api.Formula> getVariableFormulas() {
        return variableMap.values().stream().map(r -> r.javaSmtVariable).collect(Collectors.toList());
    }

    public List<Variable> getVariables() {
        return variableMap.values().stream().map(r -> r.variable).collect(Collectors.toList());
    }
}
