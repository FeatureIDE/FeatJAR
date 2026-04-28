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

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.base.tree.DataTree;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.IConnective;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Computes the count of each {@link IConnective connective} (and, or,...).
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 * @author Sebastian Krieter
 * */
public class ComputeConstraintNumberOfConnectives extends AFeatureModelComputation<DataTree<Long>> {

    public ComputeConstraintNumberOfConnectives(IComputation<IFeatureModel> featureModel) {
        super(featureModel);
    }

    @Override
    public Result<DataTree<Long>> compute(List<Object> dependencyList, Progress progress) {
        List<DataTree<Long>> counts = new ArrayList<>();
        for (IConstraint constraint : FEATURE_MODEL.get(dependencyList).getConstraints()) {
            counts.add(DataTree.ofMap(
                    "Constraint-" + constraint.getIdentifier().toString(),
                    Long.class,
                    Trees.preOrderStream(constraint.getFormula())
                            .filter(n -> n instanceof IConnective)
                            .peek(n -> FeatJAR.log().error(n.getName()))
                            .collect(Collectors.groupingBy(IExpression::getName, Collectors.counting())),
                    Long::sum,
                    () -> 0L));
        }
        return Result.of(DataTree.ofAggregator("ConnectiveCount", 0L, Long::sum, counts));
    }
}
