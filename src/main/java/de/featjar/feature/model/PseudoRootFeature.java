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

import de.featjar.base.data.Attribute;
import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.IIdentifier;
import de.featjar.feature.model.IFeature.IMutableFeature;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PseudoRootFeature implements IMutableFeature {

    protected final IFeatureModel featureModel;
    protected final IIdentifier identifier;

    protected PseudoRootFeature(IFeatureModel featureModel) {
        this.featureModel = featureModel;
        identifier = featureModel.getNewIdentifier();
    }

    protected PseudoRootFeature(PseudoRootFeature otherFeature) {
        this(otherFeature, otherFeature.featureModel);
    }

    protected PseudoRootFeature(PseudoRootFeature otherFeature, IFeatureModel newFeatureModel) {
        this.featureModel = newFeatureModel;
        identifier = otherFeature.getNewIdentifier();
    }

    @Override
    public PseudoRootFeature clone() {
        return new PseudoRootFeature(this);
    }

    @Override
    public PseudoRootFeature clone(IFeatureModel newFeatureModel) {
        return new PseudoRootFeature(this);
    }

    @Override
    public Class<?> getType() {
        return Void.class;
    }

    @Override
    public Result<IFeatureTree> getFeatureTree() {
        return Result.of(featureModel.getPseudoRoot());
    }

    @Override
    public void setType(Class<?> type) {}

    @Override
    public String toString() {
        return String.format("PseudoRootFeature");
    }

    @Override
    public void setName(String name) {}

    @Override
    public void setDescription(String description) {}

    @Override
    public IIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public Optional<Map<IAttribute<?>, Object>> getAttributes() {
        return Optional.empty();
    }

    @Override
    public <S> void setAttributeValue(Attribute<S> attribute, S value) {}

    @Override
    public <S> S removeAttributeValue(Attribute<S> attribute) {
        return null;
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
