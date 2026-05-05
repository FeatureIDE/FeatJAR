/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-cadical.
 *
 * formula-analysis-cadical is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-cadical is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-cadical. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-cadical> for further information.
 */
package de.featjar.analysis.cadical.computation;

import de.featjar.analysis.cadical.solver.CadiCalSolver;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.time.Duration;
import java.util.BitSet;
import java.util.List;

public class ComputeAtomicCadiCal extends ACadiCalAnalysis<BooleanAssignmentList> {

    public static final Dependency<BooleanAssignment> VARIABLES_OF_INTEREST =
            Dependency.newDependency(BooleanAssignment.class);
    public static final Dependency<Boolean> OMIT_SINGLE_SETS = Dependency.newDependency(Boolean.class);
    public static final Dependency<Boolean> OMIT_CORE = Dependency.newDependency(Boolean.class);

    public ComputeAtomicCadiCal(IComputation<BooleanAssignmentList> clauseList) {
        super(
                clauseList,
                Computations.of(new BooleanAssignment()),
                Computations.of(Boolean.FALSE),
                Computations.of(Boolean.FALSE));
    }

    public ComputeAtomicCadiCal(ComputeAtomicCadiCal other) {
        super(other);
    }

    @Override
    public Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList formula = FORMULA.get(dependencyList);
        Duration timeout = TIMEOUT.get(dependencyList);
        FeatJAR.log().debug("initializing cadical solver");
        FeatJAR.log().debug(formula);
        VariableMap variableMap = formula.getVariableMap();

        BitSet computedVariables = new BitSet(variableMap.size() + 1);
        BooleanAssignment variables = VARIABLES_OF_INTEREST.get(dependencyList);
        if (variables.isEmpty()) {
            variables = variableMap.getVariables();
        }
        boolean omitCore = OMIT_CORE.get(dependencyList);
        boolean omitSingles = OMIT_SINGLE_SETS.get(dependencyList);

        BooleanAssignmentList atomicSets = new BooleanAssignmentList(variableMap);

        CadiCalSolver solver = new CadiCalSolver(formula);
        solver.setTimeout(timeout);
        Result<BooleanAssignment> coreResult = solver.core();
        if (coreResult.isEmpty()) {
            return coreResult.nullify();
        }
        BooleanAssignment core = coreResult.get();
        for (int l : core.get()) {
            computedVariables.set(Math.abs(l));
        }
        if (!omitCore) {
            atomicSets.add(core);
        }

        for (int variable : variables.get()) {
            if (!computedVariables.get(variable)) {
                formula.add(new BooleanAssignment(variable));
                solver = new CadiCalSolver(formula);
                solver.setTimeout(timeout);
                Result<BooleanAssignment> condionalCore1 = solver.core();
                if (condionalCore1.isEmpty()) {
                    return condionalCore1.nullify();
                }
                formula.remove();

                if (condionalCore1.get().size() > core.size() + 1) {
                    formula.add(new BooleanAssignment(-variable));
                    solver = new CadiCalSolver(formula);
                    solver.setTimeout(timeout);
                    Result<BooleanAssignment> condionalCore2 = solver.core();
                    if (condionalCore2.isEmpty()) {
                        return condionalCore2.nullify();
                    }
                    formula.remove();

                    BooleanAssignment atomic = condionalCore1.get().retainAllNegated(condionalCore2.get());
                    for (int l : atomic.get()) {
                        computedVariables.set(Math.abs(l));
                    }
                    if (!omitSingles || atomic.size() > 1) {
                        atomicSets.add(atomic);
                    }
                } else {
                    if (!omitSingles) {
                        atomicSets.add(new BooleanAssignment(variable));
                    }
                }
            }
        }
        return Result.of(atomicSets);
    }
}
