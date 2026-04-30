/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model-assistance.
 *
 * feature-model-assistance is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model-assistance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model-assistance. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model-assistance> for further information.
 */
package de.featjar.featureide;

import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureModel.IMutableFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.IFeatureTree.IMutableFeatureTree;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NonBooleanLiteral;
import de.featjar.formula.structure.term.value.Variable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convenience class for creating a simple feature model.
 *
 * Upon instantiating this class, it creates a new empty feature model, which can be obtained with {@link #getFeatureModel()}.
 * The feature model can be modified using the methods of this class and also outside this class.
 *
 * @author Sebastian Krieter
 */
public class FeatureModelBuilder {

    private final IMutableFeatureModel featureModel = new FeatureModel().mutate();

    /**
     * {@return the feature model build by this builder}
     */
    public IFeatureModel getFeatureModel() {
        return featureModel;
    }

    /**
     * Adds a root feature to the feature model.
     *
     * @param rootName the name of the new root feature
     * @return the newly created feature
     */
    public IFeature addRoot(String rootName) {
        IFeature rootFeature = featureModel.addFeature(rootName);
        featureModel.addFeatureTreeRoot(rootFeature).mutate().makeMandatory();
        return rootFeature;
    }

    /**
     * Adds a new feature underneath the given feature in the feature tree.
     *
     * @param name the name of the new feature
     * @param parentFeature the parent feature
     * @return the newly created feature
     */
    public IFeature addFeatureBelow(String name, IFeature parentFeature) {
        return parentFeature
                .getFeatureTree()
                .orElseThrow()
                .mutate()
                .addFeatureBelow(featureModel.addFeature(name))
                .getFeature();
    }

    /**
     * Changes the group the given feature is in to an alternative group.
     *
     * @param feature the feature for which the group should be changed
     */
    public void setGroupFeaturesIsInToAlternative(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElseThrow();
        IMutableFeatureTree parentTree = featureTree.getParent().orElseThrow().mutate();
        parentTree.toAlternativeGroup(featureTree.getParentGroupID());
    }

    /**
     * Changes the group the given feature is in to an and group.
     *
     * @param feature the feature for which the group should be changed
     */
    public void setGroupFeaturesIsInToAnd(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElseThrow();
        IMutableFeatureTree parentTree = featureTree.getParent().orElseThrow().mutate();
        parentTree.toAndGroup(featureTree.getParentGroupID());
    }

    /**
     * Changes the group the given feature is in to an or group.
     *
     * @param feature the feature for which the group should be changed
     */
    public void setGroupFeaturesIsInToOr(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElseThrow();
        IMutableFeatureTree parentTree = featureTree.getParent().orElseThrow().mutate();
        parentTree.toOrGroup(featureTree.getParentGroupID());
    }

    /**
     * Set the given feature to mandatory in the feature tree.
     *
     * @param feature the feature to changed
     */
    public void setFeatureToMandatory(IFeature feature) {
        feature.getFeatureTree().orElseThrow().mutate().makeMandatory();
    }

    /**
     * Set the given feature to optional in the feature tree.
     *
     * @param feature the feature to changed
     */
    public void setFeatureToOptional(IFeature feature) {
        feature.getFeatureTree().orElseThrow().mutate().makeOptional();
    }

    /**
     * Adds a new constraint with the given formula to the feature model.
     *
     * @param formula the formula of the new constraint
     * @return the newly created constraint
     */
    public IConstraint addConstraint(IFormula formula) {
        return featureModel.addConstraint(formula);
    }

    /**
     * Create a literal for the given feature, which can be used to create a formula for a constraint.
     *
     * @param feature the feature
     * @return the literal
     *
     * @see IFormula
     * @see And
     * @see Or
     * @see Implies
     * @see Not
     */
    public IFormula createLiteral(IFeature feature) {
        Class<?> type = feature.getType();
        Variable variable = new Variable(feature.getName().get(), type);
        if (type == Boolean.class) {
            return new Literal(variable);
        } else {
            return new NonBooleanLiteral(variable);
        }
    }

    /**
     * Removes a constraint from the feature model.
     *
     * @param constraint the constraint to remove
     */
    public void removeConstraint(IConstraint constraint) {
        featureModel.removeConstraint(constraint);
    }

    /**
     * Removes the given feature and all features below it from the feature model.
     *
     * This fails if any of the features to remove is included in a constraint.
     * If so, remove or modify the constraint(s) before removing the features.
     *
     * @param feature the root feature of the feature tree to remove
     */
    public boolean removeFeatureTree(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElseThrow();
        Set<String> featuresToRemove = Trees.preOrderStream(featureTree)
                .map(IFeatureTree::getFeature)
                .map(IFeature::getName)
                .map(r -> r.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (featureModel.getConstraints().parallelStream()
                .flatMap(c -> c.getFormula().getVariableStream())
                .map(Variable::getName)
                .anyMatch(featuresToRemove::contains)) {
            return false;
        }
        Trees.preOrderStream(featureTree).map(IFeatureTree::getFeature).forEach(featureModel::removeFeature);
        featureTree.getParent().orElseThrow().mutate().removeChild(featureTree);
        return true;
    }

    /**
     * Removes the given feature.
     *
     * This fails if the feature to remove is included in a constraint.
     * If so, remove or modify the constraint(s) before removing the features.
     *
     * @param feature the feature to remove
     */
    public boolean removeFeature(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElseThrow();
        Set<String> featuresToRemove = Trees.preOrderStream(featureTree)
                .map(IFeatureTree::getFeature)
                .map(IFeature::getName)
                .map(r -> r.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (featureModel.getConstraints().parallelStream()
                .flatMap(c -> c.getFormula().getVariableStream())
                .map(Variable::getName)
                .anyMatch(featuresToRemove::contains)) {
            return false;
        }
        featureModel.removeFeature(feature);
        featureTree.mutate().removeFromTree();
        return true;
    }
}
