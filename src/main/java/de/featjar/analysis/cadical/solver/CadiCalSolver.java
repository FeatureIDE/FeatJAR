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
package de.featjar.analysis.cadical.solver;

import de.featjar.analysis.ISolver;
import de.featjar.analysis.RuntimeTimeoutException;
import de.featjar.analysis.cadical.bin.CadiBackBinary;
import de.featjar.analysis.cadical.bin.CadiCalBinary;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.env.Process;
import de.featjar.base.env.TempFile;
import de.featjar.base.io.IO;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.io.dimacs.BooleanAssignmentGroupsDimacsFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CadiCalSolver implements ISolver {
    protected final BooleanAssignmentList formula;
    protected Duration timeout = Duration.ZERO;
    protected boolean isTimeoutOccurred;

    public CadiCalSolver(BooleanAssignmentList formula) { // todo: use boolean clause list input
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

    public Result<BooleanSolution> getSolution() {
        isTimeoutOccurred = false;
        CadiCalBinary extension = FeatJAR.extension(CadiCalBinary.class);
        try (TempFile tempFile = new TempFile("cadiCalInput", ".dimacs")) {
            IO.save(
                    new BooleanAssignmentGroups(formula),
                    tempFile.getPath(),
                    new BooleanAssignmentGroupsDimacsFormat());
            Process process = extension.getProcess(
                    "--sat",
                    "-q",
                    "-t",
                    String.valueOf(timeout.toSeconds()),
                    tempFile.getPath().toString());

            return process.get().map(this::parseSolution);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    private BooleanSolution parseSolution(List<String> lines) {
        if (lines.isEmpty()) {
            throw new RuntimeException("Not output from solver");
        }
        String satResult = lines.get(0);
        switch (satResult) {
            case "s SATISFIABLE":
                if (lines.size() < 2) {
                    throw new RuntimeException("Solver did not provide solution");
                }
                return new BooleanSolution(lines.stream()
                        .skip(1)
                        .map(l -> l.split(" "))
                        .flatMapToInt(s -> Arrays.stream(s).skip(1).mapToInt(Integer::parseInt))
                        .filter(v -> v != 0)
                        .toArray());
            case "c UNKNOWN":
                isTimeoutOccurred = true;
                return null;
            case "s UNSATISFIABLE":
                return null;
            default:
                throw new RuntimeException(String.format("Could not parse: %s", String.join("\n", lines)));
        }
    }

    public Result<Boolean> hasSolution() {
        isTimeoutOccurred = false;
        CadiCalBinary extension = FeatJAR.extension(CadiCalBinary.class);
        try (TempFile tempFile = new TempFile("cadiCalInput", ".dimacs")) {
            IO.save(
                    new BooleanAssignmentGroups(formula),
                    tempFile.getPath(),
                    new BooleanAssignmentGroupsDimacsFormat());
            Process process = extension.getProcess(
                    "--sat",
                    "-q",
                    "-t",
                    String.valueOf(timeout.toSeconds()),
                    tempFile.getPath().toString());

            return process.get().mapResult(this::parseSatisfiable);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    private Result<Boolean> parseSatisfiable(List<String> lines) {
        if (lines.isEmpty()) {
            throw new RuntimeException("Not output from solver");
        }
        if (lines.size() > 2) {
            throw new RuntimeException(String.format("Could not parse: %s", String.join("\n", lines)));
        }
        String satResult = lines.get(0);
        switch (satResult) {
            case "s SATISFIABLE":
                return Result.of(Boolean.TRUE);
            case "c UNKNOWN":
                isTimeoutOccurred = true;
                return Result.empty(new RuntimeTimeoutException());
            case "s UNSATISFIABLE":
                return Result.of(Boolean.FALSE);
            default:
                throw new RuntimeException(String.format("Could not parse: %s", String.join("\n", lines)));
        }
    }

    public Result<BooleanAssignment> core() {
        // TODO implement timeout
        CadiBackBinary extension = FeatJAR.extension(CadiBackBinary.class);
        try {
            Process process = extension.getProcess("-q");
            List<String> output = new ArrayList<>();
            Result<Void> result = process.run(
                    IO.print(new BooleanAssignmentGroups(formula), new BooleanAssignmentGroupsDimacsFormat()),
                    output::add,
                    output::add);
            return result.map(r -> parseCore(output));
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    private BooleanAssignment parseCore(List<String> lines) {
        if (lines.isEmpty()) {
            throw new RuntimeException("Not output from solver");
        }
        if (lines.size() < 2) {
            if ("s UNSATISFIABLE".equals(lines.get(0))) {
                return new BooleanAssignment();
            } else {
                throw new RuntimeException(String.format("Could not parse: %s", String.join("\n", lines)));
            }
        }
        int[] core = new int[lines.size() - 2]; // ignore last two lines with "b 0" and SAT result
        for (int i = 0; i < lines.size() - 2; i++) {
            core[i] = Integer.parseInt(lines.get(i).substring(2)); // ignore leading "b "
        }
        return new BooleanAssignment(core);
    }
}
