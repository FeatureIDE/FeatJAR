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
package de.featjar.feature.model.mixins;

import de.featjar.base.data.*;
import de.featjar.base.data.identifier.IIdentifier;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implements a {@link IFeatureModel} mixin for common operations on the {@link IFeatureTree}.
 *
 * @author Elias Kuiter
 */
public interface IHasFeatureTree {
    List<? extends IFeatureTree> getRoots();

    default Stream<IFeatureTree> getFeatureTreeStream() {
        return getRoots().stream().flatMap(Trees::preOrderStream);
    }

    default LinkedHashSet<IFeature> getTreeFeatures() {
        LinkedHashSet<IFeature> featureSet = new LinkedHashSet<>();
        getFeatureTreeStream().map(IFeatureTree::getFeature).forEach(featureSet::add);
        return featureSet;
    }

    default int getNumberOfTreeFeatures() {
        return getTreeFeatures().size();
    }

    default List<IFeature> getRootFeatures() {
        return getRoots().stream().map(IFeatureTree::getFeature).collect(Collectors.toList());
    }

    default Result<IFeature> getTreeFeature(IIdentifier identifier) {
        Objects.requireNonNull(identifier);
        return Result.ofOptional(getFeatureTreeStream()
                .map(IFeatureTree::getFeature)
                .filter(feature -> feature.getIdentifier().equals(identifier))
                .findFirst());
    }

    default Result<IFeature> getTreeFeature(String name) {
        Objects.requireNonNull(name);
        return Result.ofOptional(getFeatureTreeStream()
                .map(IFeatureTree::getFeature)
                .filter(feature -> feature.getName().valueEquals(name))
                .findFirst());
    }

    default Result<IFeatureTree> getFeatureTree(String name) {
        Objects.requireNonNull(name);
        return Result.ofOptional(getFeatureTreeStream()
                .filter(tree -> tree.getFeature().getName().valueEquals(name))
                .findFirst());
    }

    default Result<IFeatureTree> getFeatureTree(IFeature feature) {
        Objects.requireNonNull(feature);
        return Result.ofOptional(getFeatureTreeStream()
                .filter(tree -> tree.getFeature().equals(feature))
                .findFirst());
    }

    default boolean hasTreeFeature(IIdentifier identifier) {
        return getTreeFeature(identifier).isPresent();
    }

    default boolean hasTreeFeature(IFeature feature) {
        return hasTreeFeature(feature.getIdentifier());
    }
}
