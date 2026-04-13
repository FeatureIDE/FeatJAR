/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-uvl.
 *
 * uvl is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * uvl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uvl. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-uvl> for further information.
 */
package de.featjar.feature.model.io.uvl.visitor;

import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import de.vill.model.constraint.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts an {@link IExpression} to a {@link de.vill.model.constraint.Constraint}.
 *
 * @author Andreas Gerasimow
 */
public class FormulaToUVLConstraintVisitor implements ITreeVisitor<IExpression, Constraint> {

    private Map<IExpression, Constraint> uvlConstraints;
    private Constraint rootConstraint;

    /**
     * Constructs a new visitor.
     */
    public FormulaToUVLConstraintVisitor() {
        reset();
    }

    @Override
    public void reset() {
        uvlConstraints = new HashMap<>();
        rootConstraint = null;
    }

    @Override
    public Result<Constraint> getResult() {
        if (rootConstraint == null) {
            return Result.empty();
        }
        return Result.of(
                rootConstraint instanceof ParenthesisConstraint
                        ? ((ParenthesisConstraint) rootConstraint).getContent()
                        : rootConstraint);
    }

    @Override
    public TraversalAction lastVisit(List<IExpression> path) {
        final IExpression node = ITreeVisitor.getCurrentNode(path);

        Constraint constraint = null;

        if (node instanceof And) {
            constraint = new ParenthesisConstraint(createAndConstraint(node));
        } else if (node instanceof AtLeast) {
            return TraversalAction.FAIL;
        } else if (node instanceof AtMost) {
            return TraversalAction.FAIL;
        } else if (node instanceof Between) {
            return TraversalAction.FAIL;
        } else if (node instanceof BiImplies) {
            constraint = new ParenthesisConstraint(createEquivalenceConstraint(node));
        } else if (node instanceof Choose) {
            return TraversalAction.FAIL;
        } else if (node instanceof Implies) {
            constraint = new ParenthesisConstraint(createImplicationConstraint(node));
        } else if (node instanceof Not) {
            constraint = createNotConstraint(node);
        } else if (node instanceof Or) {
            constraint = new ParenthesisConstraint(createOrConstraint(node));
        } else if (node instanceof Reference) {
            return TraversalAction.CONTINUE;
        } else if (node instanceof Literal) {
            constraint = createLiteralConstraint(node);
        } else if (node instanceof Variable) {
            return TraversalAction.CONTINUE;
        }

        if (constraint == null) {
            return TraversalAction.FAIL;
        }
        uvlConstraints.put(node, constraint);
        rootConstraint = constraint;
        return TraversalAction.CONTINUE;
    }

    private Constraint createLiteralConstraint(IExpression node) {
        Literal literal = (Literal) node;
        if (!node.getChildren().isEmpty()) {
            if (literal.isPositive()) {
                return new LiteralConstraint(literal.getChildren().get(0).getName());
            } else {
                return new NotConstraint(
                        new LiteralConstraint(literal.getChildren().get(0).getName()));
            }
        }
        return null;
    }

    private EquivalenceConstraint createEquivalenceConstraint(IExpression node) {
        if (node.getChildren().size() > 1) {
            return new EquivalenceConstraint(
                    uvlConstraints.get(node.getChildren().get(0)),
                    uvlConstraints.get(node.getChildren().get(1)));
        }
        return null;
    }

    private ImplicationConstraint createImplicationConstraint(IExpression node) {
        if (node.getChildren().size() > 1) {
            return new ImplicationConstraint(
                    uvlConstraints.get(node.getChildren().get(0)),
                    uvlConstraints.get(node.getChildren().get(1)));
        }
        return null;
    }

    private NotConstraint createNotConstraint(IExpression node) {
        if (!node.getChildren().isEmpty()) {
            return new NotConstraint(uvlConstraints.get(node.getChildren().get(0)));
        }
        return null;
    }

    private Constraint createAndConstraint(IExpression node) {
        List<Constraint> constraints = node.getChildren().stream()
                .map((child) -> uvlConstraints.get(child))
                .collect(Collectors.toList());
        return createAndConstraint(constraints);
    }

    private Constraint createAndConstraint(List<Constraint> constraints) {
        if (constraints.size() == 2) {
            return new AndConstraint(constraints.get(0), constraints.get(1));
        } else if (constraints.size() > 2) {
            AndConstraint andConstraint = new AndConstraint(constraints.get(0), constraints.get(1));

            for (int i = 2; i < constraints.size(); i++) {
                andConstraint = new AndConstraint(andConstraint, constraints.get(i));
            }
            return andConstraint;
        } else if (constraints.size() == 1) {
            return constraints.get(0);
        }
        return null;
    }

    private Constraint createOrConstraint(IExpression node) {
        List<Constraint> constraints = node.getChildren().stream()
                .map((child) -> uvlConstraints.get(child))
                .collect(Collectors.toList());
        return createOrConstraint(constraints);
    }

    private Constraint createOrConstraint(List<Constraint> constraints) {
        if (constraints.size() == 2) {
            return new OrConstraint(constraints.get(0), constraints.get(1));
        } else if (constraints.size() > 2) {
            OrConstraint orConstraint = new OrConstraint(constraints.get(0), constraints.get(1));

            for (int i = 2; i < constraints.size(); i++) {
                orConstraint = new OrConstraint(orConstraint, constraints.get(i));
            }
            return orConstraint;
        } else if (constraints.size() == 1) {
            return constraints.get(0);
        }
        return null;
    }
}
