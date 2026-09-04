/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ganak.
 *
 * formula-analysis-ganak is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ganak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ganak. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-ganak> for further information.
 */
package de.featjar.analysis.ganak.solver;

import de.featjar.analysis.ISolver;
import de.featjar.analysis.ganak.bin.GanakBinary;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.env.Process;
import de.featjar.base.env.TempFile;
import de.featjar.base.io.IO;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.dimacs.BooleanAssignmentListDimacsFormat;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A solver implementation for the Ganak Solver.
 */
public class GanakSolver implements ISolver {
    protected final BooleanAssignmentList formula;
    protected Duration timeout = Duration.ZERO;
    protected boolean isTimeoutOccurred;

    /**
     * regex for matching the solution line
     */
    protected static Pattern pattern = Pattern.compile("c s exact arb int (\\d+)");

    public GanakSolver(BooleanAssignmentList formula) {
        this.formula = formula;
    }

    public BooleanAssignmentList getFormula() {
        return formula;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        Objects.requireNonNull(timeout);
        FeatJAR.log().debug("setting timeout to " + timeout);
        this.timeout = timeout;
    }

    public boolean isTimeoutOccurred() {
        return isTimeoutOccurred;
    }

    /**
     * Checks whether the formula is satisfiable.
     * @return true if the formula has at least one satisfying assignment, false otherwise.
     */
    public Result<Boolean> hasSolution() {
        if (countSolution().get().compareTo(BigInteger.ONE) >= 0) {
            return Result.of(true);
        }
        return Result.of(false);
    }

    /**
     * Counts the number of satisfying assignments of a formula.
     * @return the number of satisfying assignments, if present
     */
    public Result<BigInteger> countSolution() {
        return countSolution(null);
    }

    /**
     * Counts the number of satisfying assignments of a formula, but with the additional option to
     * project some variables, given their indices to which they are mapped in the {@link VariableMap}.
     * @param include variable indices to be projected
     * @return the number of satisfying assignments, if present
     */
    public Result<BigInteger> countSolution(BooleanAssignment include) {
        isTimeoutOccurred = false;

        // the input format of the Ganak Solver requires a comment which indicates the type of model counting
        // at the beginning of the file, here pmc stands for projected model counting
        StringBuilder fileContent = new StringBuilder();
        fileContent.append("c t ");
        fileContent.append(include != null ? "pmc" : "mc");
        fileContent.append(" \n");

        // append the formula in DIMACS-format to the file
        try {
            fileContent.append(IO.print(formula, new BooleanAssignmentListDimacsFormat()));
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }

        if (include != null) {
            // construct a DIMACS comment which indicates which variables are in the projection set
            fileContent.append("c p show ");
            for (int index : include.get()) {
                fileContent.append(index).append(" ");
            }
            fileContent.append("0 \n");
        }

        try (TempFile tempFile = new TempFile("ganakInput", ".dimacs")) {
            IO.write(fileContent.toString(), tempFile.getPath());

            final Process process = FeatJAR.extension(GanakBinary.class)
                    .getProcess(List.of(tempFile.getPath().toString()), timeout);
            final Result<List<String>> result = process.get();
            isTimeoutOccurred = !process.isTerminatedInTime();
            return result.mapResult(this::parseCount);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    /**
     * Parses the output from the Ganak Solver.
     * @param lines the output from the Ganak Solver
     * @return the solution line containing the number of satisfying assignments, if present
     */
    private Result<BigInteger> parseCount(List<String> lines) {
        if (lines.isEmpty()) {
            return Result.empty(new RuntimeException("Not output from solver"));
        }

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return Result.of(new BigInteger(matcher.group(1)));
            }
        }

        return Result.empty(new RuntimeException(String.format("Could not parse: %s", String.join("\n", lines))));
    }
}
