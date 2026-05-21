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
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.term.value.Variable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

/**
 * Finds the maximal value for each numerical variable in a Term. As example we
 * have the following expression:<br>
 * <br>
 *
 * <code> (Price + 233) &gt; -17</code><br>
 * <br>
 *
 * If you want to evaluate the maximal value for the variable
 * <code>Price</code> you need to pass the formula to the
 * analysis.
 *
 * @author Joshua Sprey
 * @author Sebastian Krieter
 * @author Klara Surmeier
 */
public class ComputeMaximalVariableRange extends AJavaSMTAnalysis<Map<Variable, Object>> {

    public ComputeMaximalVariableRange(IComputation<? extends JavaSMTFormula> formula) {
        super(formula);
    }

    protected ComputeMaximalVariableRange(AJavaSMTAnalysis<Map<Variable, Object>> other) {
        super(other);
    }

    @Override
    public Result<Map<Variable, Object>> compute(List<Object> dependencyList, Progress progress) {
        Result<JavaSMTSolver> solverResult = getCompatibleSolver(dependencyList, Solvers.Z3);
        if (solverResult.isEmpty()) {
            return solverResult.nullify();
        }
        JavaSMTSolver solver = solverResult.get();

        List<VariableReference> variablesToJavaSMT =
                solver.getSolverFormula().getTranslator().getMappings();
        Map<Variable, Object> variabelsToMaximalRanges = new HashMap<Variable, Object>();
        for (VariableReference variableToJavaSMT : variablesToJavaSMT) {
            Object maximalRange = solver.maximize(variableToJavaSMT.getJavaSmtVariable());
            variabelsToMaximalRanges.put(variableToJavaSMT.getVariable(), maximalRange);
        }

        return Result.ofNullable(variabelsToMaximalRanges);
    }
}
