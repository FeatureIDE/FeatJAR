/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt.computation;

import de.featjar.analysis.javasmt.solver.FormulaToJavaSMT.VariableReference;
import de.featjar.analysis.javasmt.solver.JavaSMTFormula;
import de.featjar.analysis.javasmt.solver.JavaSMTSolver;
import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.term.value.Variable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.Formula;

/**
 * Finds core features.
 *
 * @author Sebastian Krieter
 * @author Klara Surmeier
 */
public class ComputeCore extends AJavaSMTAnalysis<Map<Variable, Object>> {
    public static final Dependency<VariableNamesList> VARIABLES_OF_INTEREST =
            Dependency.newDependency(VariableNamesList.class);

    public ComputeCore(IComputation<? extends JavaSMTFormula> formula) {
        super(formula, new ComputeConstant<>(new VariableNamesList()));
    }

    protected ComputeCore(AJavaSMTAnalysis<Map<Variable, Object>> other) {
        super(other);
    }

    @Override
    public Result<Map<Variable, Object>> compute(List<Object> dependencyList, Progress progress) {
        Result<JavaSMTSolver> solverResult = getCompatibleSolver(dependencyList, Solvers.Z3);
        if (solverResult.isEmpty()) {
            return solverResult.nullify();
        }
        JavaSMTSolver solver = solverResult.get();

        List<String> variablesOfInterest = VARIABLES_OF_INTEREST.get(dependencyList);
        List<VariableReference> variablesToJavaSMT =
                solver.getSolverFormula().getTranslator().getMappings(variablesOfInterest);

        Map<Variable, Object> variabelsToMinimalRanges = new HashMap<Variable, Object>();
        Map<Variable, Object> variabelsToMaximalRanges = new HashMap<Variable, Object>();
        for (VariableReference variableToJavaSMT : variablesToJavaSMT) {
            Formula javaSMTVariable = variableToJavaSMT.getJavaSmtVariable();

            Object minimalRange = solver.minimize(javaSMTVariable);
            Object maximalRange = solver.maximize(javaSMTVariable);

            variabelsToMinimalRanges.put(variableToJavaSMT.getVariable(), minimalRange);
            variabelsToMaximalRanges.put(variableToJavaSMT.getVariable(), maximalRange);
        }

        Map<Variable, Object> coreVariableRanges = new HashMap<Variable, Object>();
        for (Map.Entry<Variable, Object> variableToMinimalRange : variabelsToMinimalRanges.entrySet()) {
            Variable variable = variableToMinimalRange.getKey();
            Object minimalRange = variableToMinimalRange.getValue();

            if (variabelsToMaximalRanges.get(variable).equals(minimalRange)) {
                coreVariableRanges.put(variable, minimalRange);
            }
        }

        return Result.ofNullable(coreVariableRanges);
    }
}
