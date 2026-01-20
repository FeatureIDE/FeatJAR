/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.feature.model;

import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.IIdentifier;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.mixins.IHasCommonAttributes;
import de.featjar.feature.model.mixins.IHasConstraints;
import de.featjar.feature.model.mixins.IHasFeatureTree;
import de.featjar.formula.structure.IFormula;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A feature model represents the configuration space of a software system.
 * We equate feature models with feature diagrams
 * (i.e., a {@link FeatureTree} labeled with features and a list of {@link Constraint constraints}).
 * For safe mutation, rely only on the methods of {@link IMutableFeatureModel}.
 *
 * cache assumes that features/constraints are only added/deleted through the mutator, not manually
 *
 * @author Elias Kuiter
 */
public interface IFeatureModel extends IFeatureModelElement, IHasCommonAttributes, IHasFeatureTree, IHasConstraints {
    // TODO put flattened fm into store (maybe dispatch mutators of flattened model to original models)

    // TODO: we allow all kinds of modeling constructs, but not all analyses/computations support all constructs.
    // e.g., multiplicities are difficult to map to SAT. somehow, this should be checked.
    // maybe store required/incompatible capabilities for computations? eg., incompatible with
    // Plaisted-Greenbaum/multiplicities/...?
    // and then implement different alternative algorithms with different capabilities.
    // maybe this could be encoded first-class as a feature model.
    // this could even be used to generate query plans (e.g., find some configuration that counts my formula).
    // every plugin defines a feature model (uvl) that restricts what its extensions can and cannot do (replacing
    // extensions.xml)

    IFeatureModel clone();

    Collection<IFeature> getFeatures();

    PseudoFeatureTreeRoot getPseudoRoot();

    default Stream<IFeatureTree> getFeatureTreeStream() {
        return Trees.preOrderStream(getPseudoRoot()).skip(1);
    }

    int getNumberOfFeatures();

    Result<IFeature> getFeature(IIdentifier identifier);

    Result<IFeature> getFeature(String name);

    default List<? extends IFeatureTree> getFeatureTreeNodes(IFeature feature) {
        return getFeatureTreeNodeStream(feature).collect(Collectors.toList());
    }

    default List<? extends IFeatureTree> getFeatureTreeNodes(String name) {
        return getFeatureTreeNodeStream(name).collect(Collectors.toList());
    }

    default Stream<? extends IFeatureTree> getFeatureTreeNodeStream(IFeature feature) {
        Result<String> name = feature.getName();
        return name.isPresent() ? getFeatureTreeNodeStream(name.get()) : Stream.of();
    }

    default Stream<? extends IFeatureTree> getFeatureTreeNodeStream(String name) {
        return getPseudoRoot().preOrderStream().skip(1).filter(f -> f.getFeature()
                .getName()
                .valueEquals(name));
    }

    boolean hasFeature(IIdentifier identifier);

    boolean hasFeature(IFeature feature);

    default IMutableFeatureModel mutate() {
        return (IMutableFeatureModel) this;
    }

    static interface IMutableFeatureModel extends IFeatureModel, IHasMutableCommonAttributes {

        IFeature addFeature(String name);

        boolean removeFeature(IFeature feature);

        IConstraint addConstraint(IFormula formula);

        boolean removeConstraint(IConstraint constraint);

        IFeatureTree addFeatureTreeRoot(IFeature feature);

        void addFeatureTreeRoot(IFeatureTree featureTree);

        void removeFeatureTreeRoot(IFeatureTree featureTree);

        void removeFeatureTreeRoot(IFeature feature);
    }
}
