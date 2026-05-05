/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-evaluation.
 *
 * evaluation is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with evaluation. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-evaluation> for further information.
 */
package de.featjar.evaluation.process;

import de.featjar.base.FeatJAR;
import de.featjar.evaluation.streams.ErrStreamLogger;
import de.featjar.evaluation.streams.OutStreamRedirector;
import de.featjar.evaluation.streams.StreamRedirector;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessRunner implements IProcessRunner {

    private long timeout = Long.MAX_VALUE;

    public <R> ProcessResult<R> run(IAlgorithm<R> algorithm) {
        final ProcessResult<R> result = new ProcessResult<>();
        boolean terminatedInTime = false;
        boolean noError = false;
        long startTime = 0, endTime = 0;
        try {
            System.gc();
            algorithm.preProcess();

            FeatJAR.log().debug("Running command: %s", algorithm.getCommand());

            final List<String> command = algorithm.getCommandElements();
            if (!command.isEmpty()) {
                final ProcessBuilder processBuilder = new ProcessBuilder(command);
                Process process = null;

                ErrStreamLogger errStreamLogger = new ErrStreamLogger();
                errStreamLogger.setSource(algorithm.getFullName());
                final StreamRedirector errRedirector = new StreamRedirector(Arrays.asList(errStreamLogger));
                final StreamRedirector outRedirector =
                        new StreamRedirector(Arrays.asList(new OutStreamRedirector(), algorithm));
                final Thread outThread = new Thread(outRedirector);
                final Thread errThread = new Thread(errRedirector);
                try {
                    startTime = System.nanoTime();
                    process = processBuilder.start();

                    outRedirector.setInputStream(process.getInputStream());
                    errRedirector.setInputStream(process.getErrorStream());
                    outThread.start();
                    errThread.start();

                    terminatedInTime = process.waitFor(timeout, TimeUnit.MILLISECONDS);
                    endTime = System.nanoTime();
                    noError = !errStreamLogger.isErrorOccured();
                    result.setTerminatedInTime(terminatedInTime);
                    result.setNoError(noError);
                    result.setTime((endTime - startTime) / 1_000_000L);
                } finally {
                    if (process != null) {
                        process.destroyForcibly();
                    }
                    FeatJAR.log().debug("In time: " + terminatedInTime + ", no error: " + noError);
                }
            } else {
                result.setTerminatedInTime(false);
                result.setNoError(false);
                result.setTime(ProcessResult.INVALID_TIME);
                FeatJAR.log().error("Invalid command for algorithm %s", algorithm.getFullName());
            }
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            result.setTerminatedInTime(false);
            result.setNoError(false);
            result.setTime(ProcessResult.INVALID_TIME);
        }
        try {
            if (terminatedInTime && noError) {
                result.setResult(algorithm.parseResults());
            }
        } catch (final Exception e) {
            FeatJAR.log().error(e);
            result.setNoError(false);
        }
        try {
            algorithm.postProcess();
        } catch (final Exception e) {
            FeatJAR.log().error(e);
        }
        return result;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
