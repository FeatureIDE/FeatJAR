/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.assignment.conversion;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.List;

/**
 * Transforms a {@link BooleanAssignmentList} into a {@link BooleanAssignment}.
 * Takes the first assignment within the list.
 *
 * @author Sebastian Krieter
 */
public class BooleanAssignmentListToBooleanAssignment extends AComputation<BooleanAssignment> {

    protected static final Dependency<BooleanAssignmentList> LIST =
            Dependency.newDependency(BooleanAssignmentList.class);

    public BooleanAssignmentListToBooleanAssignment(IComputation<BooleanAssignmentList> booleanAssignmentList) {
        super(booleanAssignmentList);
    }

    protected BooleanAssignmentListToBooleanAssignment(BooleanAssignmentListToBooleanAssignment other) {
        super(other);
    }

    @Override
    public Result<BooleanAssignment> compute(List<Object> dependencyList, Progress progress) {
        return Result.ofNullable(LIST.get(dependencyList).getFirst()).map(BooleanAssignment::new);
    }
}
