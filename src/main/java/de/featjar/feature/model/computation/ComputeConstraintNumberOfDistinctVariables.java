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
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes the count of each distinct {@link Variable variable}.
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 * @author Sebastian Krieter
 * */
public class ComputeConstraintNumberOfDistinctVariables extends AFeatureModelComputation<DataTree<Long>> {

    public ComputeConstraintNumberOfDistinctVariables(IComputation<IFeatureModel> featureModel) {
        super(featureModel);
    }

    @Override
    public Result<DataTree<Long>> compute(List<Object> dependencyList, Progress progress) {
        Set<String> modelVariables = new HashSet<>();
        List<DataTree<?>> counts = new ArrayList<>();
        for (IConstraint constraint : FEATURE_MODEL.get(dependencyList).getConstraints()) {
            Set<String> constraintVariables = Trees.postOrderStream(constraint.getFormula())
                    .filter(n -> n instanceof Variable)
                    .map(IExpression::getName)
                    .collect(Collectors.toSet());
            counts.add(DataTree.ofValue(
                    "Constraint-" + constraint.getIdentifier().toString(), Long.valueOf(constraintVariables.size())));
            modelVariables.addAll(constraintVariables);
        }
        return Result.of(DataTree.ofValue("DistinctVariableCount", Long.valueOf(modelVariables.size()), counts));
    }
}
