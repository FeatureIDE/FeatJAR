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
package de.featjar.shell.type;

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.shell.type.IVariableType;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import java.nio.file.Path;

/**
 * Loads a {@link FeatureModel} into the shell.
 *
 * @author Niclas Kleinert
 */
public class FeatureModelType implements IVariableType<IFeatureModel> {

    @Override
    public Class<IFeatureModel> getClassType() {
        return IFeatureModel.class;
    }

    @Override
    public Result<IFeatureModel> load(Path path) {
        return IO.load(path, FeatureModelFormats.getInstance());
    }

    @Override
    public String getName() {
        return "fm";
    }
}
