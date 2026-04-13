/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j.computation;

import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds redundant clauses with respect to a given CNF. This
 * analysis works by iteratively adding each clause of the given {@link BooleanAssignmentList} to a solver. If a clause
 * is implied by the current formula, it is marked as redundant and is removed
 * from it. Otherwise it is kept as part of the formula for the
 * remaining analysis. Clauses are added in the same order a they appear in the
 * given clauses list.
 *
 * @author Sebastian Krieter
 */
public class ComputeRedundantClausesSat4J extends ASAT4JAnalysis.Solution<BooleanAssignmentList> {

    public ComputeRedundantClausesSat4J(IComputation<BooleanAssignmentList> clauseList) {
        super(clauseList);
    }

    protected ComputeRedundantClausesSat4J(ComputeRedundantClausesSat4J other) {
        super(other);
    }

    @Override
    public Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList clauseList = BOOLEAN_CLAUSE_LIST.get(dependencyList);
        SAT4JSolutionSolver solver = createSolver(dependencyList, true);
        final ArrayList<BooleanAssignment> result = new ArrayList<>();

        for (BooleanAssignment clause : clauseList) {
            checkCancel();

            solver.getClauseList().add(clause.negate());
            if (solver.isTrivialContradictionFound()) {
                solver.getClauseList().remove();
                result.add(clause);
            } else if (solver.hasSolution().valueEquals(Boolean.FALSE)) {
                solver.getClauseList().remove();
                result.add(clause);
            } else {
                solver.getClauseList().remove();
                solver.getClauseList().add(clause);
            }
        }

        return Result.of(new BooleanAssignmentList(clauseList.getVariableMap(), result));
    }
}
