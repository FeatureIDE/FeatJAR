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

import de.featjar.analysis.javasmt.solver.JavaSMTSolver;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.IExpression;
import java.util.List;
import org.sosy_lab.java_smt.api.Formula;

/**
 * Finds the minimum and maximum value of a Term. As example we have the
 * following expression:<br>
 * <br>
 *
 * <code> (Price + 233) &gt; -17</code><br>
 * <br>
 *
 * If you want to evaluate the maximum and minimum value for the variable
 * <code>Price</code> you need to pass the name of the variable to the
 * analysis.
 *
 * @author Joshua Sprey
 * @author Sebastian Krieter
 */
public class ComputeVariableRange extends AJavaSMTAnalysis<Object[]> {

    public static final Dependency<String> VARIABLE = Dependency.newDependency(String.class);

    public ComputeVariableRange(IComputation<? extends IExpression> formula) {
        super(formula);
    }

    protected ComputeVariableRange(AJavaSMTAnalysis<Object[]> other) {
        super(other);
    }

    @Override
    public Result<Object[]> compute(List<Object> dependencyList, Progress progress) {
        JavaSMTSolver solver = initializeSolver(dependencyList);
        String variableName = VARIABLE.get(dependencyList);
        final Object[] result = new Object[2];
        Formula variable = solver.getSolverFormula()
                .getTranslator()
                .getVariableFormula(variableName)
                .orElseThrow();
        result[0] = solver.minimize(variable);
        result[1] = solver.maximize(variable);
        return Result.ofNullable(result);
    }
}
