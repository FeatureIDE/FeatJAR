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
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * Compute how often features are selected, deselected, or undefined per feature.
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 * @author Sebastian Krieter
 */
public class ComputeNumberOfSelectionsPerFeature extends ABooleanAssignmentListComputation<DataTree<Long>> {

    public ComputeNumberOfSelectionsPerFeature(IComputation<BooleanAssignmentList> booleanAssignmentList) {
        super(booleanAssignmentList);
    }

    @Override
    public Result<DataTree<Long>> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList booleanAssigmenAssignmentList = BOOLEAN_ASSIGNMENT_LIST.get(dependencyList);
        List<DataTree<Long>> counts = new ArrayList<>();
        counts.add(count("Selected", l -> l > 0, booleanAssigmenAssignmentList));
        counts.add(count("Deselected", l -> l < 0, booleanAssigmenAssignmentList));
        counts.add(count("Undefined", l -> l == 0, booleanAssigmenAssignmentList));
        return Result.of(DataTree.ofAggregator("SelectionsPerFeature", 0L, Long::sum, counts));
    }

    private DataTree<Long> count(
            String name, IntPredicate predicate, BooleanAssignmentList booleanAssigmenAssignmentList) {
        List<DataTree<Long>> counts = new ArrayList<>();
        VariableMap variableMap = booleanAssigmenAssignmentList.getVariableMap();
        for (String variableName : variableMap.getVariableNames()) {
            int variableIndex = variableMap.get(variableName).orElseThrow();
            counts.add(DataTree.ofValue(
                    variableName,
                    booleanAssigmenAssignmentList.stream()
                            .mapToInt(a -> a.getValue(variableIndex)
                                    .map(b -> b ? 1 : -1)
                                    .orElse(0))
                            .filter(predicate)
                            .count()));
        }
        return DataTree.ofAggregator(name, 0L, Long::sum, counts);
    }
}
