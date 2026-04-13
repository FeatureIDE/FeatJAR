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
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.structure.IExpression;
import java.util.List;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

/**
 * Base class for analyses using a {@link JavaSMTSolver}.
 *
 * @param <T> the type of the analysis result.
 *
 * @author Joshua Sprey
 * @author Sebastian Krieter
 */
public abstract class AJavaSMTAnalysis<T> extends AComputation<T> {

    public static final Dependency<IExpression> FORMULA = Dependency.newDependency(IExpression.class);

    public AJavaSMTAnalysis(IComputation<? extends IExpression> formula, Object... computations) {
        super(formula, computations);
    }

    protected AJavaSMTAnalysis(AJavaSMTAnalysis<T> other) {
        super(other);
    }

    protected JavaSMTSolver newSolver(IExpression formula) {
        return new JavaSMTSolver(formula, Solvers.SMTINTERPOL);
    }

    public JavaSMTSolver initializeSolver(List<Object> dependencyList, boolean empty) {
        IExpression formula = FORMULA.get(dependencyList);
        FeatJAR.log().debug("initializing JavaSmt");
        FeatJAR.log().debug(formula);
        return newSolver(formula);
    }

    public JavaSMTSolver initializeSolver(List<Object> dependencyList) {
        return initializeSolver(dependencyList, false);
    }
}
