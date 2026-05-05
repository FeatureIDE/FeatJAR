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
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.IFeatureModel;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Computes the number of different group types in a feature tree.
 *
 * @author Benjamin von Holt
 * @author Sebastian Krieter
 */
public class ComputeFeatureTreeNumberOfGroups extends AFeatureModelComputation<DataTree<Long>> {

    public ComputeFeatureTreeNumberOfGroups(IComputation<IFeatureModel> featureModel) {
        super(featureModel);
    }

    @Override
    public Result<DataTree<Long>> compute(List<Object> dependencyList, Progress progress) {
        return Result.of(DataTree.ofMap(
                "NumberOfGroups",
                Long.class,
                FEATURE_MODEL
                        .get(dependencyList)
                        .getFeatureTreeStream()
                        .filter(n -> n.hasChildren())
                        .flatMap(n -> n.getChildrenGroups().stream())
                        .collect(Collectors.groupingBy(Group::toString, Collectors.counting())),
                Long::sum,
                () -> 0L));
    }
}
