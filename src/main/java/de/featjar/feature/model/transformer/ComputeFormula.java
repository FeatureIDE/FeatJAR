/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.transformer;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Transforms a feature model into a boolean formula.
 *
 * @author Sebastian Krieter
 */
public class ComputeFormula extends AComputation<IFormula> {
    protected static final Dependency<IFeatureModel> FEATURE_MODEL = Dependency.newDependency(IFeatureModel.class);

    public ComputeFormula(IComputation<IFeatureModel> formula) {
        super(formula);
    }

    protected ComputeFormula(ComputeFormula other) {
        super(other);
    }

    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        IFeatureModel featureModel = FEATURE_MODEL.get(dependencyList);
        ArrayList<IFormula> constraints = new ArrayList<>();
        HashSet<Variable> variables = new HashSet<>();
        featureModel.getFeatureTreeStream().forEach(node -> {
            // TODO use better error value
            IFeature feature = node.getFeature();
            String featureName = feature.getName().orElse("");
            Variable variable = new Variable(featureName, feature.getType());
            variables.add(variable);

            // TODO take featureRanges into Account
            Result<IFeatureTree> potentialParentTree = node.getParent();
            Literal featureLiteral = Expressions.literal(featureName);
            if (potentialParentTree.isEmpty()) {
                handleRoot(constraints, featureLiteral, node);
            } else {
                handleParent(constraints, featureLiteral, node);
            }
            handleGroups(constraints, featureLiteral, node);
        });
        Reference reference = new Reference(new And(constraints));
        reference.setFreeVariables(variables);
        return Result.of(reference);
    }

    private void handleParent(ArrayList<IFormula> constraints, Literal featureLiteral, IFeatureTree node) {
        constraints.add(new Implies(
                featureLiteral,
                Expressions.literal(
                        node.getParent().get().getFeature().getName().orElse(""))));
    }

    private void handleRoot(ArrayList<IFormula> constraints, Literal featureLiteral, IFeatureTree node) {
        if (node.isMandatory()) {
            constraints.add(featureLiteral);
        }
    }

    private void handleGroups(ArrayList<IFormula> constraints, Literal featureLiteral, IFeatureTree node) {
        List<Group> childrenGroups = node.getChildrenGroups();
        int groupCount = childrenGroups.size();
        ArrayList<List<IFormula>> groupLiterals = new ArrayList<>(groupCount);
        for (int i = 0; i < groupCount; i++) {
            groupLiterals.add(null);
        }
        List<? extends IFeatureTree> children = node.getChildren();
        for (IFeatureTree childNode : children) {
            Literal childLiteral =
                    Expressions.literal(childNode.getFeature().getName().orElse(""));

            if (childNode.isMandatory()) {
                constraints.add(new Implies(featureLiteral, childLiteral));
            }

            int groupID = childNode.getParentGroupID();
            List<IFormula> list = groupLiterals.get(groupID);
            if (list == null) {
                groupLiterals.set(groupID, list = new ArrayList<>());
            }
            list.add(childLiteral);
        }
        for (int i = 0; i < groupCount; i++) {
            Group group = childrenGroups.get(i);
            if (group != null) {
                if (group.isOr()) {
                    constraints.add(new Implies(featureLiteral, new Or(groupLiterals.get(i))));
                } else if (group.isAlternative()) {
                    constraints.add(new Implies(featureLiteral, new Choose(1, groupLiterals.get(i))));
                } else {
                    int lowerBound = group.getLowerBound();
                    int upperBound = group.getUpperBound();
                    if (lowerBound > 0) {
                        if (upperBound != Range.OPEN) {
                            constraints.add(new Implies(
                                    featureLiteral, new Between(lowerBound, upperBound, groupLiterals.get(i))));
                        } else {
                            constraints.add(new Implies(featureLiteral, new AtMost(upperBound, groupLiterals.get(i))));
                        }
                    } else {
                        if (upperBound != Range.OPEN) {
                            constraints.add(new Implies(featureLiteral, new AtLeast(lowerBound, groupLiterals.get(i))));
                        }
                    }
                }
            }
        }
    }
}
