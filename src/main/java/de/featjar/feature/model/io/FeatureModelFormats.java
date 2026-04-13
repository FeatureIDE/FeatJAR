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
package de.featjar.feature.model.io;

import de.featjar.base.FeatJAR;
import de.featjar.base.io.format.AFormats;
import de.featjar.feature.model.IFeatureModel;

/**
 * Manages all formats for {@link IFeatureModel feature models}.
 *
 * @author Sebastian Krieter
 */
public class FeatureModelFormats extends AFormats<IFeatureModel> {

    public static FeatureModelFormats getInstance() {
        return FeatJAR.extensionPoint(FeatureModelFormats.class);
    }

    @Override
    public Class<IFeatureModel> getType() {
        return IFeatureModel.class;
    }
}
