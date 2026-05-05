/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-uvl.
 *
 * uvl is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * uvl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uvl. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-uvl> for further information.
 */
package de.featjar.feature.model.io.uvl;

import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;
import de.vill.main.UVLModelFactory;
import de.vill.model.FeatureModel;

/**
 * Abstract format for UVL files.
 *
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public abstract class AUVLFormat<T> implements IFormat<T> {

    @Override
    public String getFileExtension() {
        return "uvl";
    }

    @Override
    public String getName() {
        return "UVL";
    }

    protected static FeatureModel parseUVLModel(AInputMapper inputMapper) {
        String content = inputMapper.get().text();
        UVLModelFactory uvlModelFactory = new UVLModelFactory();
        return uvlModelFactory.parse(content);
    }
}
