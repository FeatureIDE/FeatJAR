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
package de.featjar.analysis.javasmt.solver;

import de.featjar.formula.VariableMap;
import de.featjar.formula.structure.IExpression;
import java.util.List;
import java.util.stream.Collectors;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.NumeralFormula;
import org.sosy_lab.java_smt.api.SolverContext;

/**
 * Formula for {@link JavaSMTSolver}.
 *
 * @author Sebastian Krieter
 */
// TODO rename
public class JavaSMTFormula {

    private final IExpression originalFormula;
    private final FormulaToJavaSMT translator;
    private final VariableMap variableMap;
    private final SolverContext solverContext;
    private final Solvers solverName;

    // maybe split JavaSMTFormula in two dependencies?
    // one for SolverContext, one for Expression
    public JavaSMTFormula(
            SolverContext solverContext, IExpression expression, VariableMap variableMap, Solvers solverName) {
        this.solverContext = solverContext;
        this.variableMap = variableMap;
        this.translator = new FormulaToJavaSMT(solverContext);
        this.originalFormula = expression;
        this.solverName = solverName;
    }

    public FormulaToJavaSMT getTranslator() {
        return translator;
    }

    public IExpression getOriginalFormula() {
        return originalFormula;
    }

    public VariableMap getVariableMap() {
        return variableMap;
    }

    public SolverContext getContext() {
        return solverContext;
    }

    public Solvers getSolverName() {
        return solverName;
    }

    public List<BooleanFormula> getBooleanVariables() {
        return translator.getVariableFormulas().stream()
                .filter(f -> f instanceof BooleanFormula)
                .map(f -> (BooleanFormula) f)
                .collect(Collectors.toList());
    }

    public List<NumeralFormula> getNumeralVariables() {
        return translator.getVariableFormulas().stream()
                .filter(f -> f instanceof NumeralFormula)
                .map(f -> (NumeralFormula) f)
                .collect(Collectors.toList());
    }
}
