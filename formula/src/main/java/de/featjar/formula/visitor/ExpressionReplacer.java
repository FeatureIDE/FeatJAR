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
import de.featjar.base.tree.visitor.TreeNodeReplacer;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Replaces nodes within a tree with different nodes.
 *
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public class ExpressionReplacer extends TreeNodeReplacer<IExpression> {

    public static ExpressionReplacer insertKnownVariables(Assignment assignment) {
        return new ExpressionReplacer(node -> {
            if (node instanceof Variable variable) {
                Result<Object> assignedValue = assignment.getValue(variable.getName());
                if (assignedValue.isPresent()) {
                    return new Constant(assignedValue.get());
                }
            }
            return null;
        });
    }

    public static ExpressionReplacer renameVariables(Map<String, String> nameMap) {
        return new ExpressionReplacer(node -> {
            if (node instanceof Variable variable) {
                String newName = nameMap.get(variable.getName());
                if (newName != null) {
                    return new Variable(newName, variable.getType());
                }
            }
            return null;
        });
    }

    public static ExpressionReplacer substituteVariables(Map<Variable, Variable> variableMap) {
        return new ExpressionReplacer(node -> {
            if (node instanceof Variable variable) {
                Variable newVariable = variableMap.get(variable);
                if (newVariable != null) {
                    return newVariable;
                }
            }
            return null;
        });
    }

    public static ExpressionReplacer substituteExpressions(Map<IExpression, IExpression> expressionMap) {
        return new ExpressionReplacer(expressionMap::get);
    }

    public static ExpressionReplacer substituteExpressions(Function<IExpression, ? extends IExpression> function) {
        return new ExpressionReplacer(function);
    }

    public ExpressionReplacer(Function<IExpression, ? extends IExpression> function) {
        super(function);
    }

    @Override
    public Result<Void> nodeValidator(List<IExpression> path) {
        return ITreeVisitor.rootValidator(path, root -> root instanceof Reference, "expected formula reference");
    }

    @Override
    public TraversalAction lastVisit(List<IExpression> path) {
        ITreeVisitor.getCurrentNode(path).replaceChildren(function);
        return TraversalAction.CONTINUE;
    }
}
