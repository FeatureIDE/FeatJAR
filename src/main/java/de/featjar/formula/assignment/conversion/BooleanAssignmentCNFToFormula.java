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
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.Assignments;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.structure.IFormula;
import java.util.List;

/**
 * Transforms a CNF as {@link BooleanAssignmentList} into a {@link IFormula}.
 *
 * @author Sebastian Krieter
 */
public class BooleanAssignmentCNFToFormula extends AComputation<IFormula> {

    protected static final Dependency<BooleanAssignmentList> CNF =
            Dependency.newDependency(BooleanAssignmentList.class);
    protected static final Dependency<VariableMap> VARIABLE_MAP = Dependency.newDependency(VariableMap.class);

    public BooleanAssignmentCNFToFormula(IComputation<BooleanAssignmentList> cnf) {
        super(cnf, new BooleanAssignmentListToVariableMap(cnf));
    }

    protected BooleanAssignmentCNFToFormula(BooleanAssignmentCNFToFormula other) {
        super(other);
    }

    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        return Result.of(Assignments.toCNFFormula(CNF.get(dependencyList), VARIABLE_MAP.get(dependencyList)));
    }
}
