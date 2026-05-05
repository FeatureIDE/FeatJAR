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
package de.featjar.analysis.sat4j.solver;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * Base class for solvers using Sat4J.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class SAT4JSolver implements de.featjar.analysis.ISolver {
    protected final ISolver internalSolver = newInternalSolver();
    protected final SAT4JClauseList clauseList;
    protected final SAT4JAssignment assignment = new SAT4JAssignment();
    protected Duration timeout = Duration.ZERO;
    protected boolean globalTimeout;

    protected boolean isTimeoutOccurred;
    protected boolean trivialContradictionFound;

    public static void initializeSolver(
            SAT4JSolver solver,
            BooleanAssignmentList clauseList,
            BooleanAssignment assumedAssignment,
            BooleanAssignmentList assumedClauseList,
            Duration timeout) {
        FeatJAR.log().debug("initializing SAT4J");
        FeatJAR.log().debug("variables %s", clauseList.getVariableMap());
        FeatJAR.log().debug("clauses %s", clauseList);
        FeatJAR.log().debug("assuming %s", assumedAssignment);
        FeatJAR.log().debug("assuming %s", assumedClauseList);
        solver.getClauseList().addAll(assumedClauseList);
        solver.getAssignment().addAll(assumedAssignment);
        solver.setTimeout(timeout);
        solver.setGlobalTimeout(true);
    }

    /**
     * Replaces all values in {@code model} that are different in {@code otherModel}
     * with zero. Does not modify {@code otherModel}. Assumes that {@code model} and
     * {@code otherModel} have the same length.
     *
     * @param model      First model
     * @param otherModel Second model, is not modified
     */
    public static void zeroConflicts(int[] model, int[] otherModel) {
        assert model.length == otherModel.length;
        for (int i = 0; i < model.length; i++) {
            final int literal = model[i];
            if (literal != 0 && literal != otherModel[i]) {
                model[i] = 0;
            }
        }
    }

    public SAT4JSolver(BooleanAssignmentList clauseList, boolean allowSimplification) {
        internalSolver.setDBSimplificationAllowed(allowSimplification);
        internalSolver.setKeepSolverHot(false);
        internalSolver.setVerbose(false);
        this.clauseList = new SAT4JClauseList(this, clauseList);

        final int size = clauseList.getVariableMap().size();
        if (!clauseList.isEmpty()) {
            internalSolver.setExpectedNumberOfClauses(clauseList.size() + 1);
        }
        if (size > 0) {
            internalSolver.newVar(size);
            for (int i = 1; i <= size; i++) {
                internalSolver.registerLiteral(i);
            }
        }
    }

    protected abstract ISolver newInternalSolver();

    public SAT4JClauseList getClauseList() {
        return clauseList;
    }

    public SAT4JAssignment getAssignment() {
        return assignment;
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(Duration timeout) {
        Objects.requireNonNull(timeout);
        FeatJAR.log().debug("setting timeout to " + timeout);
        this.timeout = timeout;
        if (!timeout.isZero()) internalSolver.setTimeoutMs(timeout.toMillis());
        else internalSolver.expireTimeout();
    }

    public boolean isGlobalTimeout() {
        return globalTimeout;
    }

    public void setGlobalTimeout(boolean globalTimeout) {
        this.globalTimeout = globalTimeout;
    }

    @Override
    public boolean isTimeoutOccurred() {
        return isTimeoutOccurred;
    }

    public boolean isTrivialContradictionFound() {
        return trivialContradictionFound;
    }

    public Result<BooleanSolution> findSolution() {
        final Result<Boolean> hasSolution = hasSolution();
        return hasSolution.isPresent()
                ? hasSolution().get() ? Result.of(getSolution()) : Result.empty()
                : Result.empty(hasSolution.getProblems());
    }

    public Result<Boolean> hasSolution(int... integers) {
        if (trivialContradictionFound) {
            return Result.of(Boolean.FALSE);
        }
        return callSat4J(new VecInt(integers));
    }

    public Result<Boolean> hasSolution() {
        if (trivialContradictionFound) {
            return Result.of(Boolean.FALSE);
        }
        return callSat4J(assignment.getIntegers());
    }

    private Result<Boolean> callSat4J(VecInt integers) {
        try {
            FeatJAR.log().debug("calling SAT4J");
            if (internalSolver.isSatisfiable(integers, globalTimeout)) {
                FeatJAR.log().debug("has solution");
                return Result.of(Boolean.TRUE);
            } else {
                FeatJAR.log().debug("no solution");
                return Result.of(Boolean.FALSE);
            }
        } catch (final TimeoutException e) {
            FeatJAR.log().debug("solver timeout occurred");
            isTimeoutOccurred = true;
            return Result.empty(de.featjar.analysis.ISolver.getTimeoutProblem(null));
        }
    }

    /**
     * Does only consider the given {@code assignment} and <b>not</b> the global
     * assignment variable of the solver.
     */
    public Result<Boolean> hasSolution(BooleanAssignment assignment) {
        return hasSolution(assignment.get());
    }

    public BooleanSolution getSolution() {
        int[] internalSolution = getInternalSolution();
        final int[] sortedIntegers = new int[internalSolution.length];
        Arrays.stream(internalSolution)
                .filter(integer -> integer != 0)
                .forEach(integer -> sortedIntegers[Math.abs(integer) - 1] = integer);
        return new BooleanSolution(sortedIntegers, false);
    }

    public int[] getInternalSolution() {
        return internalSolver.model();
    }
}
