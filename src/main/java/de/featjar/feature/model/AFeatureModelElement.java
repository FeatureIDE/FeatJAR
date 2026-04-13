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
package de.featjar.feature.model;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.IAttributable.IMutatableAttributable;
import de.featjar.base.data.IAttribute;
import de.featjar.base.data.identifier.AIdentifier;
import de.featjar.base.data.identifier.IIdentifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements identification and attribute valuation.
 * Each {@link FeatureModel} and all its {@link Feature features} and {@link Constraint constraints} are
 * uniquely identified by some {@link AIdentifier}.
 * Also, each element can be annotated with arbitrary {@link Attribute attributes}.
 *
 * @author Elias Kuiter
 */
public abstract class AFeatureModelElement implements IFeatureModelElement, IMutatableAttributable {
    protected final IFeatureModel featureModel;
    protected final IIdentifier identifier;
    protected final LinkedHashMap<IAttribute<?>, Object> attributeValues;

    public AFeatureModelElement(IFeatureModel featureModel) {
        this.featureModel = Objects.requireNonNull(featureModel);
        identifier = featureModel.getNewIdentifier();
        attributeValues = new LinkedHashMap<>(4);
    }

    protected AFeatureModelElement(AFeatureModelElement otherElement, IFeatureModel featureModel) {
        this.featureModel = featureModel;
        identifier = otherElement.getNewIdentifier();
        attributeValues = otherElement.cloneAttributes();
    }

    @Override
    public IIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public Optional<Map<IAttribute<?>, Object>> getAttributes() {
        return Optional.of(Collections.unmodifiableMap(attributeValues));
    }

    @Override
    public <S> void setAttributeValue(Attribute<S> attribute, S value) {
        if (value == null) {
            removeAttributeValue(attribute);
            return;
        }
        checkType(attribute, value);
        validate(attribute, value);
        attributeValues.put(attribute, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S removeAttributeValue(Attribute<S> attribute) {
        return (S) attributeValues.remove(attribute);
    }

    @Override
    public IFeatureModel getFeatureModel() {
        return featureModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getIdentifier().equals(((AFeatureModelElement) o).getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier());
    }
}
