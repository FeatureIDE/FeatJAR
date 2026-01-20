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

import de.featjar.base.data.*;
import de.featjar.feature.model.mixins.IHasCommonAttributes;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.term.value.Variable;
import java.util.LinkedHashSet;

/**
 * A constraint describes some restriction on the valid configurations represented by a {@link FeatureModel}.
 * It is attached to a {@link FeatureModel} and represented as a {@link IFormula} over {@link Feature} variables.
 * For safe mutation, rely only on the methods of {@link IMutableConstraint}.
 *
 * @author Elias Kuiter
 */
public interface IConstraint extends IFeatureModelElement, IHasCommonAttributes {

    IConstraint clone();

    IConstraint clone(IFeatureModel newFeatureModel);

    IFormula getFormula();

    static LinkedHashSet<IFeature> getReferencedFeatures(IFormula formula, IFeatureModel featureModel) {
        return formula.getVariableStream()
                .map(Variable::getName)
                .map(name -> {
                    Result<IFeature> feature = featureModel.getFeature(name);
                    if (feature.isEmpty()) throw new RuntimeException("encountered unknown feature " + name);
                    return feature.get();
                })
                .collect(Sets.toSet());
    }

    default LinkedHashSet<IFeature> getReferencedFeatures() {
        return getReferencedFeatures(getFormula(), getFeatureModel());
    }

    default LinkedHashSet<String> getTags() {
        return getAttributeValue(FeatureModelAttributes.TAGS).get();
    }

    default IMutableConstraint mutate() {
        return (IMutableConstraint) this;
    }

    static interface IMutableConstraint extends IConstraint, IHasMutableCommonAttributes {
        void setFormula(IFormula formula);

        default void remove() {
            getFeatureModel().mutate().removeConstraint(this);
        }

        default void setTags(LinkedHashSet<String> tags) {
            setAttributeValue(FeatureModelAttributes.TAGS, tags);
        }

        default boolean addTag(String tag) {
            return getTags().add(tag);
        }

        default boolean removeTag(String tag) {
            return getTags().remove(tag);
        }
    }
}
