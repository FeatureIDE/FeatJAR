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
package de.featjar.formula.assignment;

import de.featjar.formula.VariableMap;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Util class for assignments.
 *
 * @author Sebastian Krieter
 */
public final class Assignments {

    private Assignments() {}

    public static List<Variable> variablesFromMap(VariableMap variableMap) {
        return variableMap.getVariableNames().stream().map(Variable::new).collect(Collectors.toList());
    }

    public static List<Variable> variablesFromMap(VariableMap variableMap, BooleanAssignment variables) {
        return IntStream.of(variables.get())
                .mapToObj(l -> new Variable(variableMap.get(Math.abs(l)).orElseThrow()))
                .collect(Collectors.toList());
    }

    public static List<Literal> toFormulaLiterals(BooleanAssignment assignment, VariableMap variableMap) {
        List<Literal> list = new ArrayList<>(assignment.size());
        for (int literal : assignment.get()) {
            if (literal != 0) {
                list.add(new Literal(
                        literal > 0, variableMap.get(Math.abs(literal)).get()));
            }
        }
        return list;
    }

    public static IFormula toCNFFormula(BooleanAssignmentList clauseList, VariableMap variableMap) {
        List<IFormula> clauses = new ArrayList<>();
        for (BooleanAssignment clause : clauseList) {
            clauses.add(new Or(Assignments.toFormulaLiterals(clause, variableMap)));
        }
        return new Reference(new And(clauses), variablesFromMap(variableMap));
    }

    public static IFormula toCNFFormula(BooleanAssignmentList clauseList) {
        return toCNFFormula(clauseList, clauseList.getVariableMap());
    }

    public static IFormula toDNFFormula(BooleanAssignmentList clauseList, VariableMap variableMap) {
        List<IFormula> clauses = new ArrayList<>();
        for (BooleanAssignment clause : clauseList) {
            clauses.add(new And(Assignments.toFormulaLiterals(clause, variableMap)));
        }
        return new Reference(new Or(clauses), variablesFromMap(variableMap));
    }

    public static IFormula toDNFFormula(BooleanAssignmentList clauseList) {
        return toDNFFormula(clauseList, clauseList.getVariableMap());
    }

    public static int[] toBooleanLiterals(IFormula clause, VariableMap variableMap) {
        int[] literals = new int[clause.getChildrenCount()];
        int i = 0;
        for (final IExpression child : clause.getChildren()) {
            final Literal l = (Literal) child;
            final int index = variableMap.get(l.getExpression().getName()).orElseThrow();
            literals[i++] = l.isPositive() ? index : -index;
        }
        return literals;
    }

    public static BooleanAssignmentList toBooleanAssignmentList(IFormula cnf, VariableMap variableMap) {
        if (cnf instanceof Reference) {
            cnf = ((Reference) cnf).getExpression();
        }
        return new BooleanAssignmentList(
                variableMap,
                cnf.getChildren().stream()
                        .map(c -> new BooleanAssignment(toBooleanLiterals((IFormula) c, variableMap)))
                        .collect(Collectors.toList()));
    }
}
