/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sharpsat.
 *
 * formula-analysis-sharpsat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sharpsat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sharpsat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sharpsat> for further information.
 */
package de.featjar.analysis.sharpsat.solver;

import de.featjar.analysis.ISolver;
import de.featjar.analysis.sharpsat.bin.SharpSATBinary;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.env.Process;
import de.featjar.base.env.TempFile;
import de.featjar.base.io.IO;
import de.featjar.formula.io.dimacs.FormulaDimacsFormat;
import de.featjar.formula.structure.IFormula;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class SharpSATSolver implements ISolver {
    protected final IFormula formula;
    protected Duration timeout = Duration.ZERO;
    protected boolean isTimeoutOccurred;

    public SharpSATSolver(IFormula formula) { // todo: use boolean clause list input
        this.formula = formula;
    }

    public IFormula getFormula() {
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

    public Result<BigInteger> countSolutions() {
        SharpSATBinary extension = FeatJAR.extension(SharpSATBinary.class);
        try (TempFile tempFile = new TempFile("sharpSATinput", ".dimacs")) {
            IO.save(formula, tempFile.getPath(), new FormulaDimacsFormat());
            Process process = extension.getProcess(
                    "-noCC",
                    "-noIBCP",
                    "-t",
                    String.valueOf(timeout.toSeconds()),
                    tempFile.getPath().toString());

            Result<List<String>> result = process.get();
            return result.map(lines -> lines.isEmpty()
                    ? BigInteger.valueOf(-1)
                    : "TIMEOUT".equals(lines.get(0)) ? BigInteger.valueOf(-1) : new BigInteger(lines.get(0)));
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    public Result<Boolean> hasSolution() {
        final int comparison =
                countSolutions().map(c -> c.compareTo(BigInteger.ZERO)).orElse(-1);
        switch (comparison) {
            case -1:
                return Result.empty();
            case 0:
                return Result.of(false);
            case 1:
                return Result.of(true);
            default:
                throw new IllegalStateException(String.valueOf(comparison));
        }
    }
}
