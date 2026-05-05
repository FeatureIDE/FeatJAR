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

import de.featjar.analysis.IConfigurationUpdater;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.IntegerList;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanClause;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RandomConfigurationUpdater implements IConfigurationUpdater {
    private final BooleanAssignmentList model;
    private final Random random;

    public RandomConfigurationUpdater(BooleanAssignmentList cnf, Long randomSeed) {
        this.model = cnf;
        random = new Random(randomSeed);
    }

    @Override
    public Result<BooleanSolution> update(BooleanAssignment partialSolution) {
        return Computations.of(model)
                .map(ComputeCoreSAT4J::new)
                .set(ComputeCoreSAT4J.ASSUMED_ASSIGNMENT, partialSolution)
                .computeResult()
                .map(a -> a.getFirst().toSolution(model.getVariableMap().maxIndex()));
    }

    @Override
    public Result<BooleanSolution> complete(
            Collection<int[]> include, Collection<int[]> exclude, Collection<int[]> choose) {
        final VariableMap orgVariableMap = model.getVariableMap();

        List<BooleanAssignment> ll = new ArrayList<>();
        ll.addAll(model.getAll());

        VariableMap newVariableMap = orgVariableMap.clone();
        if (choose != null) {
            int[] newNegativeLiterals = new int[choose.size()];
            int i = 0;
            for (int[] clause : choose) {
                int newVar = newVariableMap.maxIndex() + 1;
                newVariableMap.add(newVar, String.valueOf(newVar));
                newNegativeLiterals[i++] = -newVar;
                for (int l : clause) {
                    ll.add(new BooleanClause(new int[] {l, newVar}));
                }
            }

            ll.add(new BooleanClause(newNegativeLiterals));
            ll.add(new BooleanClause(IntegerList.mergeInt(choose)).negate());
        }
        if (include != null) {
            int[] includeMerge = IntegerList.mergeInt(include);
            for (int literal : includeMerge) {
                ll.add(new BooleanClause(literal));
            }
        }
        if (exclude != null) {
            for (int[] clause : exclude) {
                ll.add(new BooleanClause(clause).negate());
            }
        }

        SAT4JSolutionSolver solver = new SAT4JSolutionSolver(new BooleanAssignmentList(newVariableMap, ll));
        solver.setSelectionStrategy(ISelectionStrategy.random(random));
        solver.shuffleOrder(random);
        return solver.hasSolution()
                .filter(hasSolution -> hasSolution)
                .map(hasSolution -> new BooleanSolution(
                        Arrays.copyOfRange(solver.getInternalSolution(), 0, orgVariableMap.size())));
    }
}
