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
package de.featjar.feature.configuration.computation;

import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.base.tree.DataTree;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.List;

/**
 * Compute how many Variables are in the assignmentList
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 * @author Sebastian Krieter
 */
public class ComputeNumberOfVariables extends ABooleanAssignmentListComputation<DataTree<Integer>> {

    public ComputeNumberOfVariables(IComputation<BooleanAssignmentList> booleanAssignmentList) {
        super(booleanAssignmentList);
    }

    @Override
    public Result<DataTree<Integer>> compute(List<Object> dependencyList, Progress progress) {
        return Result.of(DataTree.ofValue(
                "NumberOfVariables",
                BOOLEAN_ASSIGNMENT_LIST.get(dependencyList).getVariableMap().size()));
    }
}
