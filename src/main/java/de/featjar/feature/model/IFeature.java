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

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.base.data.identifier.AIdentifier;
import de.featjar.feature.model.mixins.IHasCommonAttributes;
import java.util.LinkedHashSet;

/**
 * A feature in a {@link FeatureModel} describes some functionality of a software system.
 * It is attached to a {@link FeatureModel} and labels a {@link FeatureTree}.
 * For safe mutation, rely only on the methods of {@link IMutableFeature}.
 * A {@link Feature} is uniquely determined by its immutable {@link AIdentifier}
 * or name (obtained with {@link IHasCommonAttributes#getName()}).
 * In contrast to a feature's identifier, its name is mutable and should therefore be used sparsely
 * to avoid cache invalidation and renaming issues.
 *
 * @author Elias Kuiter
 */
public interface IFeature extends IFeatureModelElement, IHasCommonAttributes {

    Result<IFeatureTree> getFeatureTree();

    Class<?> getType();

    IFeature clone();

    IFeature clone(IFeatureModel newFeatureModel);

    default boolean isAbstract() {
        return (boolean) getAttributeValue(FeatureModelAttributes.ABSTRACT).get();
    }

    default boolean isConcrete() {
        return !isAbstract();
    }

    default boolean isHidden() {
        return (boolean) getAttributeValue(FeatureModelAttributes.HIDDEN).get();
    }

    /**
     * Checks if there is a hidden feature higher up in the hierarchy.
     * Does not only check the direct parent, but all parents above.
     * @return {@code true} if any of this features's parents are hidden, {@code false} otherwise.
     */
    default boolean hasHiddenParent() {
        IFeatureTree parentTreeElement =
                getFeatureTree().orElseThrow(Problem::toException).getParent().orElse(null);

        while (parentTreeElement != null) {
            if (parentTreeElement.getFeature().isHidden()) {
                return true;
            }
            parentTreeElement = parentTreeElement.getParent().orElse(null);
        }
        return false;
    }

    default boolean isVisible() {
        return !isHidden();
    }

    default LinkedHashSet<IConstraint> getReferencingConstraints() {
        return getFeatureModel().getConstraints().stream()
                .filter(constraint ->
                        constraint.getReferencedFeatures().stream().anyMatch(this::equals))
                .collect(Sets.toSet());
    }

    default IMutableFeature mutate() {
        return (IMutableFeature) this;
    }

    static interface IMutableFeature extends IFeature, IHasMutableCommonAttributes {

        void setType(Class<?> type);

        default void setAbstract(boolean value) {
            setAttributeValue(FeatureModelAttributes.ABSTRACT, value);
        }

        default boolean toggleAbstract() {
            return toggleAttributeValue(FeatureModelAttributes.ABSTRACT);
        }

        default void setAbstract() {
            setAbstract(true);
        }

        default void setConcrete() {
            setAbstract(false);
        }

        default void setHidden(boolean value) {
            setAttributeValue(FeatureModelAttributes.HIDDEN, value);
        }

        default boolean toggleHidden() {
            return toggleAttributeValue(FeatureModelAttributes.HIDDEN);
        }

        default void setHidden() {
            setHidden(true);
        }

        default void setVisible() {
            setHidden(false);
        }
    }
}
