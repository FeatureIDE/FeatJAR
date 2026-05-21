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

import de.featjar.analysis.javasmt.solver.JavaSMTFormula;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.structure.IFormula;
import java.util.List;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.SolverContext;

/**
 * Transforms a formula into a {@link JavaSMTFormula}.
 *
 * @author Sebastian Krieter
 * @author Klara Surmeier
 */
public class ComputeJavaSMTFormula extends AComputation<JavaSMTFormula> {
    public static final Dependency<IFormula> FORMULA = Dependency.newDependency(IFormula.class);
    public static final Dependency<Solvers> SOLVER = Dependency.newDependency(Solvers.class);

    public ComputeJavaSMTFormula(IComputation<? extends IFormula> formula) {
        super(formula, Computations.of(Solvers.Z3));
    }

    protected ComputeJavaSMTFormula(ComputeJavaSMTFormula other) {
        super(other);
    }

    @Override
    public Result<JavaSMTFormula> compute(List<Object> dependencyList, Progress progress) {
        IFormula originalFormula = (IFormula) FORMULA.get(dependencyList);
        Solvers solver = SOLVER.get(dependencyList);

        VariableMap variableMap = new VariableMap(originalFormula);

        JavaSMTFormula formula = null;
        SolverContext context;

        try {
            final Configuration config = Configuration.defaultConfiguration();

            final LogManager logManager = BasicLogManager.create(config);
            final ShutdownManager shutdownManager = ShutdownManager.create();
            context =
                    SolverContextFactory.createSolverContext(config, logManager, shutdownManager.getNotifier(), solver);

            formula = new JavaSMTFormula(context, originalFormula, variableMap, solver);

        } catch (final InvalidConfigurationException e) {
            FeatJAR.log().error(e);
        }

        return Result.of(formula);
    }
}
