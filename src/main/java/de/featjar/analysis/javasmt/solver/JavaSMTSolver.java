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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.IExpression;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BasicProverEnvironment.AllSatCallback;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;
import org.sosy_lab.java_smt.api.OptimizationProverEnvironment;
import org.sosy_lab.java_smt.api.OptimizationProverEnvironment.OptStatus;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

/**
 * SMT solver using JavaSMT.
 *
 * @author Joshua Sprey
 */
public class JavaSMTSolver {

    private static final class SatCallback implements AllSatCallback<Result<BigInteger>> {
        BigInteger count = BigInteger.ZERO;

        @Override
        public void apply(List<BooleanFormula> model) {
            if (!model.isEmpty()) {
                count = count.add(BigInteger.ONE);
            }
        }

        @Override
        public Result<BigInteger> getResult() throws InterruptedException {
            return Result.of(count);
        }
    }

    private JavaSMTFormula formula;

    /**
     * The current context of the solver. Used by the translator to translate prop4J
     * nodes to JavaSMT formulas.
     */
    public SolverContext context;

    public JavaSMTSolver(IExpression expression, Solvers solver) {
        try {
            final Configuration config = Configuration.defaultConfiguration();
            final LogManager logManager = BasicLogManager.create(config);
            final ShutdownManager shutdownManager = ShutdownManager.create();
            context =
                    SolverContextFactory.createSolverContext(config, logManager, shutdownManager.getNotifier(), solver);
            this.formula = new JavaSMTFormula(context, expression);
        } catch (final InvalidConfigurationException e) {
            FeatJAR.log().error(e);
        }
    }

    public Result<BigInteger> countSolutions() {
        try (ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_ALL_SAT)) {
            prover.addConstraint(formula.getFormula());
            return prover.allSat(new SatCallback(), formula.getBooleanVariables());
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }

    public de.featjar.formula.assignment.ValueAssignment getSolution() {
        try (ProverEnvironment prover = context.newProverEnvironment()) {
            prover.addConstraint(formula.getFormula());
            if (!prover.isUnsat()) {
                final LinkedHashMap<String, Object> solution = new LinkedHashMap<>();
                for (ValueAssignment assignment : prover.getModel()) {
                    solution.put(assignment.getName(), assignment.getValue());
                }
                return new de.featjar.formula.assignment.ValueAssignment(solution);
            } else {
                return null;
            }
        } catch (final SolverException e) {
            return null;
        } catch (final InterruptedException e) {
            return null;
        }
    }

    public Result<de.featjar.formula.assignment.ValueAssignment> findSolution() {
        return Result.ofNullable(getSolution());
    }

    public Rational minimize(Formula formula) {
        try (OptimizationProverEnvironment prover = context.newOptimizationProverEnvironment()) {
            prover.addConstraint(this.formula.getFormula());
            final int handleY = prover.minimize(formula);
            final OptStatus status = prover.check();
            assert status == OptStatus.OPT;
            final Optional<Rational> lower = prover.lower(handleY, Rational.ofString("1/1000"));
            return lower.orElse(null);
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            return null;
        }
    }

    public Rational maximize(Formula formula) {
        try (OptimizationProverEnvironment prover = context.newOptimizationProverEnvironment()) {
            prover.addConstraint(this.formula.getFormula());
            final int handleX = prover.maximize(formula);
            final OptStatus status = prover.check();
            assert status == OptStatus.OPT;
            final Optional<Rational> upper = prover.upper(handleX, Rational.ofString("1/1000"));
            return upper.orElse(null);
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            return null;
        }
    }

    public Result<Boolean> hasSolution() {
        try (ProverEnvironment prover = context.newProverEnvironment()) {
            prover.addConstraint(formula.getFormula());
            return Result.of(!prover.isUnsat());
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }

    public List<BooleanFormula> getMinimalUnsatisfiableSubset() throws IllegalStateException {
        try (ProverEnvironment prover = context.newProverEnvironment()) {
            prover.addConstraint(formula.getFormula());
            if (prover.isUnsat()) {
                final List<BooleanFormula> formula = prover.getUnsatCore();
                return formula.stream().filter(Objects::nonNull).collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            return null;
        }
    }

    public List<List<BooleanFormula>> getAllMinimalUnsatisfiableSubsets() throws IllegalStateException {
        return Collections.singletonList(getMinimalUnsatisfiableSubset());
    }

    public JavaSMTFormula getSolverFormula() {
        return formula;
    }
}
