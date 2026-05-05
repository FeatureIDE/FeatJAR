/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ddnnife.
 *
 * formula-analysis-ddnnife is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ddnnife is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ddnnife. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/formula-analysis-ddnnife> for further information.
 */
package de.featjar.analysis.ddnnife.solver;

import de.featjar.analysis.ISolver;
import de.featjar.analysis.ddnnife.bin.D4Binary;
import de.featjar.analysis.ddnnife.bin.DdnnifeBinary;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.io.dimacs.BooleanAssignmentGroupsDimacsFormat;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DdnnifeWrapper implements ISolver, AutoCloseable {

    protected Duration timeout = Duration.ZERO;

    private Process process;

    private BufferedReader prcIn;
    private BufferedReader prcErr;
    private BufferedWriter prcOut;

    private Path ddnifeFile;

    private BooleanAssignment assumptions;
    private VariableMap variableMap;

    public DdnnifeWrapper(BooleanAssignmentGroups formula) throws Exception {
        variableMap = formula.getVariableMap();
        try {
            ddnifeFile = Files.createTempFile("ddnnifeInput", ".nnf");
            ddnifeFile.toFile().deleteOnExit();

            computeDdnnf(formula);

            process = FeatJAR.extension(DdnnifeBinary.class)
                    .getProcess("-t", Integer.toString(variableMap.size()), "-i", ddnifeFile.toString(), "stream")
                    .start();

            prcErr = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            prcIn = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            prcOut = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));

            if (prcErr.ready()) {
                throw new RuntimeException(prcErr.lines().collect(Collectors.joining("\n")));
            }
            if (prcIn.ready()) {
                throw new RuntimeException(prcIn.lines().collect(Collectors.joining("\n")));
            }
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    private void computeDdnnf(BooleanAssignmentGroups formula) throws IOException, InterruptedException {
        Path d4File = Files.createTempFile("d4Input", ".dimacs");
        d4File.toFile().deleteOnExit();

        IO.save(formula, d4File, new BooleanAssignmentGroupsDimacsFormat());

        FeatJAR.extension(D4Binary.class)
                .getProcess("-i", d4File.toString(), "-m", "ddnnf-compiler", "--dump-ddnnf", ddnifeFile.toString())
                .run();
        Files.deleteIfExists(d4File);
    }

    public Result<String> compute(String query) {
        if (process != null && process.isAlive()) {
            try {
                prcOut.write(query + "\n");
                prcOut.flush();
                return Result.ofNullable(prcIn.readLine());
            } catch (IOException e) {
                return Result.empty(e);
            }
        } else {
            return Result.empty(new Exception("Process was terminated!"));
        }
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                FeatJAR.log().error(e);
            }
        }
    }

    public Result<Boolean> hasSolution() {
        StringBuilder sb = new StringBuilder("sat");
        writeAssumptions(sb);
        return compute(sb.toString()).map("true"::equalsIgnoreCase);
    }

    public Result<BooleanSolution> getSolution() {
        StringBuilder sb = new StringBuilder("enum l 1");
        writeAssumptions(sb);
        return compute(sb.toString()).map(this::formatLiterals).map(BooleanSolution::new);
    }

    public Result<BigInteger> countSolutions() {
        StringBuilder sb = new StringBuilder("count");
        writeAssumptions(sb);
        return compute(sb.toString()).map(BigInteger::new);
    }

    public Result<BooleanAssignment> core() {
        StringBuilder sb = new StringBuilder("core");
        writeAssumptions(sb);
        return compute(sb.toString()).map(this::formatLiterals).map(BooleanAssignment::new);
    }

    public Result<BooleanAssignmentList> getSolutions(int count) {
        StringBuilder sb = new StringBuilder("enum l ");
        sb.append(count);
        writeAssumptions(sb);
        return compute(sb.toString())
                .map(this::formatLiterals)
                .map(this::splitLiterals)
                .map(l -> new BooleanAssignmentList(
                        variableMap, l.stream().map(BooleanSolution::new).collect(Collectors.toList())));
    }

    public Result<BooleanAssignmentList> getRandomSolutions(int count, long seed) {
        StringBuilder sb = new StringBuilder("random l ");
        sb.append(count);
        sb.append(" s ");
        sb.append(seed);
        writeAssumptions(sb);
        return compute(sb.toString())
                .map(this::formatLiterals)
                .map(this::splitLiterals)
                .map(l -> new BooleanAssignmentList(
                        variableMap, l.stream().map(BooleanSolution::new).collect(Collectors.toList())));
    }

    public Result<BooleanAssignmentList> getTWise(int t, long seed) {
        StringBuilder sb = new StringBuilder("t-wise l ");
        sb.append(t);
        sb.append(" s ");
        sb.append(seed);
        writeAssumptions(sb);
        return compute(sb.toString())
                .map(this::formatLiterals)
                .map(this::splitLiterals)
                .map(l -> new BooleanAssignmentList(
                        variableMap, l.stream().map(BooleanSolution::new).collect(Collectors.toList())));
    }

    private int[] formatLiterals(String s) {
        return Arrays.stream(s.split("(\\s+|;)"))
                .filter(s2 -> !s2.isBlank())
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private List<int[]> splitLiterals(int[] literals) {
        ArrayList<int[]> list = new ArrayList<>();
        int variableCount = variableMap.size();
        for (int i = 0; i < literals.length; i += variableCount) {
            int[] literalsPart = new int[variableCount];
            System.arraycopy(literals, i, literalsPart, 0, variableCount);
            list.add(literalsPart);
        }
        return list;
    }

    private void writeAssumptions(StringBuilder sb) {
        if (assumptions != null && !assumptions.isEmpty()) {
            sb.append(" a");
            for (int assumption : assumptions.get()) {
                sb.append(assumption);
            }
        }
    }

    public BooleanAssignment getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(BooleanAssignment assumptions) {
        this.assumptions = assumptions;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        Objects.requireNonNull(timeout);
        FeatJAR.log().debug("setting timeout to " + timeout);
        this.timeout = timeout;
    }

    @Override
    public boolean isTimeoutOccurred() {
        return false;
    }

    @Override
    public final void close() throws Exception {
        try {
            if (process != null && process.isAlive()) {
                prcOut.write("exit\n");
                prcOut.flush();
                process.waitFor(1000, TimeUnit.MILLISECONDS);
            }
        } catch (IOException | InterruptedException e) {
            FeatJAR.log().error(e);
        } finally {
            closeStream(prcErr);
            closeStream(prcIn);
            closeStream(prcOut);
            try {
                Files.deleteIfExists(ddnifeFile);
            } catch (IOException e) {
                FeatJAR.log().error(e);
            }
            if (process != null) {
                process.destroyForcibly();
                process = null;
            }
        }
    }
}
