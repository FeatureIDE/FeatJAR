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
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.predicate.DefLiteral;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.value.Variable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces literals with other literals.
 *
 * @author Andreas Gerasimow
 */
public class ExpressionReplacer implements ITreeVisitor<IExpression, Void> {

    private final Map<IExpression, IExpression> replacementMap;

    public ExpressionReplacer(Map<IExpression, IExpression> literalMap) {
        this.replacementMap = literalMap;
    }

    public static Map<IExpression, IExpression> createAtomicSetsReplacementMap(BooleanAssignmentList atomicSets) {
        final VariableMap variableMap = atomicSets.getVariableMap();
        final Map<IExpression, IExpression> replacementMap = new LinkedHashMap<>();
        for (BooleanAssignment atomicSet : atomicSets) {
            if (atomicSet.countNonZero() < 2) {
                continue;
            }
            final IExpression[] substitutes = new IExpression[3];
            final IExpression[] originals = new IExpression[3];

            final int[] literals = atomicSet.get();
            int i = 0;
            for (; i < literals.length; i++) {
                int substitute = literals[i];
                if (substitute != 0) {
                    createLiteralExpressions(variableMap, substitute, substitutes);
                    break;
                }
            }
            for (i++; i < literals.length; i++) {
                int original = literals[i];
                if (original != 0) {
                    createLiteralExpressions(variableMap, original, originals);
                    for (int j = 0; j < substitutes.length; j++) {
                        replacementMap.put(originals[j], substitutes[j]);
                    }
                }
            }
        }
        return replacementMap;
    }

    public static Map<IExpression, IExpression> createCoreReplacementMap(BooleanAssignmentList core) {
        final VariableMap variableMap = core.getVariableMap();
        final Map<IExpression, IExpression> replacementMap = new LinkedHashMap<>();
        for (BooleanAssignment coreSet : core) {
            if (coreSet.countNonZero() == 0) {
                continue;
            }
            final IExpression[] substitutes = new IExpression[3];
            final IExpression[] originals = new IExpression[3];

            substitutes[0] = True.INSTANCE;
            substitutes[1] = False.INSTANCE;
            substitutes[2] = True.INSTANCE;

            final int[] literals = coreSet.get();
            for (int i = 0; i < literals.length; i++) {
                int original = literals[i];
                if (original != 0) {
                    createLiteralExpressions(variableMap, original, originals);
                    for (int j = 0; j < substitutes.length; j++) {
                        replacementMap.put(originals[j], substitutes[j]);
                    }
                }
            }
        }
        return replacementMap;
    }

    private static void createLiteralExpressions(
            final VariableMap variableMap, int literal, final IExpression[] literalExpressions) {
        final Variable variable =
                new Variable(variableMap.get(Math.abs(literal)).get());
        final boolean isPositive = literal > 0;
        literalExpressions[0] = new Literal(isPositive, variable);
        literalExpressions[1] = new Literal(!isPositive, variable);
        literalExpressions[2] = new DefLiteral(variable);
    }

    @Override
    public TraversalAction lastVisit(List<IExpression> path) {
        final IExpression formula = ITreeVisitor.getCurrentNode(path);
        formula.replaceChildren(c -> {
            final IExpression substitute = replacementMap.get(c);
            return substitute != null ? substitute.cloneTree() : null;
        });
        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
