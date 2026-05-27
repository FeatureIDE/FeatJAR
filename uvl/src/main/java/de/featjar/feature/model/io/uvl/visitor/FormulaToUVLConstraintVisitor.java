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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import de.vill.model.Feature;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.EquivalenceConstraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.OrConstraint;
import de.vill.model.constraint.ParenthesisConstraint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts an {@link IExpression} to a {@link de.vill.model.constraint.Constraint}.
 *
 * @author Andreas Gerasimow
 * @author Sebastian Krieter
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

        if (node instanceof Variable) {
            return TraversalAction.CONTINUE;
        } else if (node instanceof Literal) {
            constraint = createLiteralConstraint(node);
        } else if (node instanceof Not) {
            constraint = createNotConstraint(node);
        } else if (node instanceof Or) {
            constraint = createOrConstraint(node);
        } else if (node instanceof And) {
            constraint = createAndConstraint(node);
        } else if (node instanceof Implies) {
            constraint = createImplicationConstraint(node);
        } else if (node instanceof BiImplies) {
            constraint = createEquivalenceConstraint(node);
        } else if (node instanceof Reference) {
            return TraversalAction.CONTINUE;
        } else if (node instanceof AtLeast) {
            FeatJAR.log().warning("UVL model does not support operator \"at least\"");
        } else if (node instanceof AtMost) {
            FeatJAR.log().warning("UVL model does not support operator \"at most\"");
        } else if (node instanceof Between) {
            FeatJAR.log().warning("UVL model does not support operator \"between\"");
        } else if (node instanceof Choose) {
            FeatJAR.log().warning("UVL model does not support operator choose");
        } else {
            FeatJAR.log().warning("UVL model does not support operator %s", node.getName());
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

        if (node.getChildren().isEmpty()) {
            FeatJAR.log().warning("Malformed literal %s", node);
            return null;
        }

        LiteralConstraint literalConstraint =
                new LiteralConstraint(new Feature(literal.getChildren().get(0).getName()));
        if (literal.isPositive()) {
            return literalConstraint;
        } else {
            return new NotConstraint(literalConstraint);
        }
    }

    private Constraint createEquivalenceConstraint(IExpression node) {
        if (node.getChildren().size() != 2) {
            FeatJAR.log().warning("Malformed expression %s", node);
            return null;
        }
        return new EquivalenceConstraint(
                uvlConstraints.get(node.getChildren().get(0)),
                uvlConstraints.get(node.getChildren().get(1)));
    }

    private Constraint createImplicationConstraint(IExpression node) {
        if (node.getChildren().size() != 2) {
            FeatJAR.log().warning("Malformed expression %s", node);
            return null;
        }
        return new ImplicationConstraint(
                uvlConstraints.get(node.getChildren().get(0)),
                uvlConstraints.get(node.getChildren().get(1)));
    }

    private NotConstraint createNotConstraint(IExpression node) {
        if (node.getChildren().size() != 1) {
            FeatJAR.log().warning("Malformed expression %s", node);
            return null;
        }
        return new NotConstraint(uvlConstraints.get(node.getChildren().get(0)));
    }

    private Constraint createAndConstraint(IExpression node) {
        Constraint[] constraints =
                node.getChildren().stream().map(uvlConstraints::get).toArray(Constraint[]::new);
        switch (constraints.length) {
            case 0:
                FeatJAR.log().warning("Malformed expression %s", node);
                return null;
            case 1:
                return constraints[0];
            default:
                return new AndConstraint(constraints);
        }
    }

    private Constraint createOrConstraint(IExpression node) {
        Constraint[] constraints =
                node.getChildren().stream().map(uvlConstraints::get).toArray(Constraint[]::new);
        switch (constraints.length) {
            case 0:
                FeatJAR.log().warning("Malformed expression %s", node);
                return null;
            case 1:
                return constraints[0];
            default:
                return new OrConstraint(constraints);
        }
    }
}
