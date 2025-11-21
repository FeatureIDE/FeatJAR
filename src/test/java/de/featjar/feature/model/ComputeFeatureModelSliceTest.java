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

import de.featjar.Common;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;
import de.featjar.feature.model.transformer.ComputeFeatureModelSlice;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ComputeFeatureModelSliceTest extends Common {

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void deinit() {
        FeatJAR.deinitialize();
    }

    @Test
    public void featureModelFeatureTreeMixin() {
        IFeatureModel featureModel = load("testFeatureModels/car.xml", new XMLFeatureModelFormat());

        IFeatureModel compute = Computations.of(featureModel)
                .map(ComputeFeatureModelSlice::new)
                .set(
                        ComputeFeatureModelSlice.EXCLUDE_FEATURES,
                        new FeatureNameListFilter(List.of("Europe", "Navigation")))
                .compute();
        FeatJAR.log().error(compute.toString());
    }
}
