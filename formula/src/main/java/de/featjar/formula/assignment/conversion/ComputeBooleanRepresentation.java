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

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.Reference;
import java.util.List;

/**
 * Transforms a formula, which is assumed to be in strict conjunctive normal form, into an indexed CNF representation.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ComputeBooleanRepresentation extends AComputation<BooleanAssignmentGroups> {

    protected static final Dependency<Object> CNF = Dependency.newDependency();

    public ComputeBooleanRepresentation(IComputation<? extends IFormula> cnfFormula) {
        super(cnfFormula);
    }

    protected ComputeBooleanRepresentation(ComputeBooleanRepresentation other) {
        super(other);
    }

    @Override
    public Result<BooleanAssignmentGroups> compute(List<Object> dependencyList, Progress progress) {
        IFormula formula = (IFormula) CNF.get(dependencyList);
        FeatJAR.log()
                .debug("initializing variable map for " + formula.getClass().getName());
        VariableMap variableMap = new VariableMap(formula);
        FeatJAR.log().debug(variableMap);
        if (formula instanceof Reference) {
            formula = (IFormula) ((Reference) formula).getExpression();
        }
        return ComputeBooleanClauseList.toBooleanAssignmentList(formula, variableMap)
                .map(BooleanAssignmentGroups::new);
    }
}
