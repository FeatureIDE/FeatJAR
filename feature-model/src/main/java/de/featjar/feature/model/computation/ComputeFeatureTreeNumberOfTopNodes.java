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
package de.featjar.feature.model.computation;

import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.base.tree.DataTree;
import de.featjar.feature.model.IFeatureModel;
import java.util.List;

/**
 * Computes number of nodes under a root feature in a feature tree.
 *
 * @author Benjamin von Holt
 * @author Sebastian Krieter
 */
public class ComputeFeatureTreeNumberOfTopNodes extends AFeatureModelComputation<DataTree<Long>> {

    public ComputeFeatureTreeNumberOfTopNodes(IComputation<IFeatureModel> featureModel) {
        super(featureModel);
    }

    @Override
    public Result<DataTree<Long>> compute(List<Object> dependencyList, Progress progress) {
        return Result.of(DataTree.ofValue(
                "NumberOfTopNodes",
                FEATURE_MODEL.get(dependencyList).getRoots().stream()
                        .mapToLong(r -> r.getChildrenCount())
                        .sum()));
    }
}
