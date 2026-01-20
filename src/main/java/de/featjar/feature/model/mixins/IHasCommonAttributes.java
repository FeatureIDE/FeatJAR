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
package de.featjar.feature.model.mixins;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.IAttributable;
import de.featjar.base.data.Result;
import de.featjar.feature.model.*;

/**
 * Implements accessors for commonly used {@link Attribute attributes}.
 * That is, all {@link IFeatureModel feature models}, {@link IFeature features},
 * and {@link IConstraint constraints} can have names and descriptions.
 *
 * @author Elias Kuiter
 */
public interface IHasCommonAttributes extends IAttributable {
    default Result<String> getName() {
        return getAttributeValue(FeatureModelAttributes.NAME);
    }

    default Result<String> getDescription() {
        return getAttributeValue(FeatureModelAttributes.DESCRIPTION);
    }

    public static interface IHasMutableCommonAttributes extends IMutatableAttributable {
        void setName(String name);

        void setDescription(String description);
    }
}
