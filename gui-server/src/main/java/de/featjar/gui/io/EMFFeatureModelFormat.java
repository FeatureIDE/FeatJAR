/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-gui-server.
 *
 * gui-server is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * gui-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gui-server. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE> for further information.
 */
package de.featjar.gui.io;

import de.featjar.base.data.Result;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.input.InputHeader;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.IFeatureModelFormat;

/**
 * Reads and writes feature models in the FeatJAR EMF format.
 *
 */
public class EMFFeatureModelFormat implements IFeatureModelFormat {

    public static final String GROUP_NODE_LIST = "groupNodeList";
    public static final String FEATURE_LIST = "featureList";
    public static final String CARDINALITY = "cardinality";
    public static final String LOWER_BOUND = "lowerBound";
    public static final String UPPER_BOUND = "upperBound";
    public static final String CONSTRAINTS = "constraints";

    public static final String ID = "id";
    public static final String ROOTS = "roots";
    public static final String ATTRIBUTES = "attributes";
    public static final String NAME = "name";
    public static final String KEY = "key";
    public static final String VALUE = "value";

    @Override
    public String getName() {
        return "FeatJAR-EMF";
    }

    @Override
    public String getFileExtension() {
        return "featuremodel";
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public boolean supportsContent(InputHeader inputHeader) {
        return supportsParse() && inputHeader.get().contains("featJAR:FeatureModel");
    }

    @Override
    public Result<IFeatureModel> parse(AInputMapper inputMapper) {
        return new EMFFeatureModelParser().parse(inputMapper);
    }

    @Override
    public Result<String> serialize(IFeatureModel object) {
        return new EMFFeatureModelWriter().serialize(object);
    }
}
