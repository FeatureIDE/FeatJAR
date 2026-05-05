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

import de.featjar.base.data.BinomialCalculator;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.True;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Converts a {@link IFeatureTree} to an {@link IFormula}.
 *
 * @author Andreas Gerasimow
 */
public class FeatureTreeToFormulaVisitor implements ITreeVisitor<IFeatureTree, IFormula> {

    private HashMap<IFeatureTree, IFormula> formulas = new HashMap<>();
    private IFormula rootFormula;
    private List<Problem> problems;

    /**
     * Constructs a new visitor.
     */
    public FeatureTreeToFormulaVisitor() {
        reset();
    }

    @Override
    public void reset() {
        formulas = new HashMap<>();
        rootFormula = null;
        problems = new ArrayList<>();
    }

    @Override
    public Result<IFormula> getResult() {
        if (rootFormula == null) {
            return Result.empty(problems);
        }
        return Result.of(rootFormula, problems);
    }

    @Override
    public TraversalAction lastVisit(List<IFeatureTree> path) {
        final IFeatureTree node = ITreeVisitor.getCurrentNode(path);
        IFeature feature = node.getFeature();

        Result<String> featureName = feature.getName();
        problems.addAll(featureName.getProblems());
        if (featureName.isEmpty()) {
            problems.add(new Problem("Feature has no name"));
            return TraversalAction.FAIL;
        }

        if (node.getChildrenGroups().isEmpty()) {
            problems.add(new Problem(featureName.get() + " has no group."));
            return TraversalAction.FAIL;
        }

        FeatureTree.Group group =
                node.getChildrenGroups().get(node.getChildrenGroups().size() - 1);

        IFormula currentFormula;

        if (node.getChildren().isEmpty()) { // is leaf node
            if (node.isOptional() || node.isMandatory()) {
                currentFormula = new Literal(featureName.get());
            } else {
                problems.add(new Problem(featureName.get() + " is neither an optional nor a mandatory feature."));
                return TraversalAction.FAIL;
            }
        } else { // node has children
            IFormula childrenFormula;
            if (group.isAlternative()) {
                IFormula[] children = node.getChildren().stream()
                        .map((child) -> formulas.get(child))
                        .toArray(IFormula[]::new);
                childrenFormula = nchoosek(children, 1, false);
            } else if (group.isOr()) {
                IFormula[] children = node.getChildren().stream()
                        .map((child) -> formulas.get(child))
                        .toArray(IFormula[]::new);
                childrenFormula = new Or(children);
            } else if (group.isAnd()) {
                IFormula[] children = node.getChildren().stream()
                        .filter(IFeatureTree::isMandatory) // filter mandatory only
                        .map((child) -> formulas.get(child))
                        .toArray(IFormula[]::new);
                childrenFormula = new And(children);
            } else {
                problems.add(new Problem(featureName.get() + " has no group."));
                return TraversalAction.FAIL;
            }

            if (path.size() == 1) {
                if (childrenFormula.getChildren().isEmpty()) {
                    currentFormula = True.INSTANCE;
                } else {
                    currentFormula = childrenFormula;
                }
            } else if (childrenFormula.getChildren().isEmpty()) {
                currentFormula = new Literal(featureName.get());
            } else if (node.isOptional()) {
                currentFormula = new Implies(new Literal(featureName.get()), childrenFormula);
            } else if (node.isMandatory()) {
                currentFormula = new And(new Literal(featureName.get()), childrenFormula);
            } else {
                problems.add(new Problem(featureName.get() + " is neither an optional nor a mandatory feature."));
                return TraversalAction.FAIL;
            }
        }

        formulas.put(node, currentFormula);
        rootFormula = currentFormula;

        return TraversalAction.CONTINUE;
    }

    /**
     * Creates a new formula where exactly k of the n provided formulas must be satisfied.
     * @param elements The n formulas.
     * @param k Specifies how many of the n formulas must exactly be satisfied.
     * @param negated Negates all literals.
     * @return n choose k formula.
     */
    private static IFormula nchoosek(IFormula[] elements, int k, boolean negated) {
        final int n = elements.length;

        // return tautology
        if ((k == 0) || (k == (n + 1))) {
            return new Or(new Not(elements[0]), elements[0]);
        }

        // return contradiction
        if ((k < 0) || (k > (n + 1))) {
            return new And(new Not(elements[0]), elements[0]);
        }

        final IFormula[] newNodes = new IFormula[(int) BinomialCalculator.computeBinomial(n, k)];
        int j = 0;

        // negate all elements
        if (negated) {
            negateNodes(elements);
        }

        final IFormula[] clause = new IFormula[k];
        final int[] index = new int[k];

        // the position that is currently filled in clause
        int level = 0;
        index[level] = -1;

        while (level >= 0) {
            // fill this level with the next element
            index[level]++;
            // did we reach the maximum for this level
            if (index[level] >= (n - (k - 1 - level))) {
                // go to previous level
                level--;
            } else {
                clause[level] = elements[index[level]];
                if (level == (k - 1)) {
                    newNodes[j++] = new Or(clause);
                } else {
                    // go to next level
                    level++;
                    // allow only ascending orders (to prevent from duplicates)
                    index[level] = index[level - 1];
                }
            }
        }
        if (j != newNodes.length) {
            throw new RuntimeException("Pre-calculation of the number of elements failed!");
        }
        return new And(newNodes);
    }

    /**
     * Warps all given formulas in a Not node.
     * @param nodes the formulas to negate
     */
    private static void negateNodes(IFormula[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Not(nodes[i]);
        }
    }
}
