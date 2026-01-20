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
import de.featjar.feature.model.IFeature.IMutableFeature;

public class Feature extends AFeatureModelElement implements IMutableFeature {
    protected Class<?> type;

    protected Feature(IFeatureModel featureModel) {
        super(featureModel);
        type = Boolean.class;
    }

    protected Feature(Feature otherFeature) {
        this(otherFeature, otherFeature.featureModel);
    }

    protected Feature(Feature otherFeature, IFeatureModel newFeatureModel) {
        super(otherFeature, newFeatureModel);
        type = otherFeature.type;
    }

    @Override
    public Feature clone() {
        return new Feature(this);
    }

    @Override
    public Feature clone(IFeatureModel newFeatureModel) {
        return new Feature(this);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Result<IFeatureTree> getFeatureTree() {
        return featureModel.getFeatureTree(this);
    }

    @Override
    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("Feature{name=%s}", getName().orElse(""));
    }

    @Override
    public void setName(String name) {
        attributeValues.put(FeatureModelAttributes.NAME, name);
    }

    @Override
    public void setDescription(String description) {
        attributeValues.put(FeatureModelAttributes.DESCRIPTION, description);
    }
}
