/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.analysis;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.graphviz.GraphVizComputationTreeFormat;
import de.featjar.base.io.text.GenericTextFormat;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Computes an analysis result for a formula.
 *
 * @param <T> the type of the analysis result
 */
public abstract class AAnalysisCommand<T> extends ACommand {

    public static final Option<Boolean> BROWSE_CACHE_OPTION =
            Option.newFlag("browse-cache").setDescription("Show cache contents in default browser");

    public static final Option<Boolean> NON_PARALLEL = Option.newFlag("non-parallel")
            .setDescription(
                    "Disable parallel computation. (Ignored if timeout option is specified, as computations with timeout are always non-parallel.)");

    public static final Option<Duration> TIMEOUT_OPTION = Option.newOption(
                    "timeout", s -> Duration.ofSeconds(Long.parseLong(s)))
            .setDescription("Timeout in seconds. (Disables parallel computing.)")
            .setValidator(timeout -> !timeout.isNegative())
            .setDefaultValue(Duration.ZERO);

    /**
     * Output option for execution time.
     */
    public static final Option<Path> TIME_OPTION = Option.newOption("write-time-to-file", Option.PathParser)
            .setDescription("Path to file containig the execution time");

    @Override
    public int run(OptionList optionParser) {
        boolean browseCache = optionParser.getResult(BROWSE_CACHE_OPTION).get();
        boolean parallel = !optionParser.getResult(NON_PARALLEL).get();
        Duration timeout = optionParser.getResult(TIMEOUT_OPTION).get();
        Path timePath = optionParser.getResult(TIME_OPTION).orElse(null);

        IComputation<T> computation;
        try {
            computation = newComputation(optionParser);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            FeatJAR.log().plainMessage(OptionList.printHelp(this));
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
        FeatJAR.log().debug("running computation %s", computation.print());

        final Result<T> result;
        final long timeNeeded;
        if (!timeout.isZero()) {
            final long localTime = System.nanoTime();
            result = computation.computeResult(true, true, timeout);
            timeNeeded = System.nanoTime() - localTime;
        } else if (parallel) {
            final long localTime = System.nanoTime();
            result = computation.computeFutureResult(true, true).get();
            timeNeeded = System.nanoTime() - localTime;
        } else {
            final long localTime = System.nanoTime();
            result = computation.computeResult(true, true);
            timeNeeded = System.nanoTime() - localTime;
        }
        if (timePath == null) {
            FeatJAR.log().info("time needed for computation: " + ((timeNeeded / 1_000_000) / 1000.0) + "s");
        } else {
            try {
                IO.write(String.valueOf(timeNeeded), timePath);
            } catch (IOException e) {
                FeatJAR.log().error(e);
            }
        }

        if (browseCache) {
            FeatJAR.cache().browse(new GraphVizComputationTreeFormat());
        }

        if (result.isPresent()) {
            try {
                writeToOutput(result.get(), getOuputFormat(optionParser), optionParser);
            } catch (IOException e) {
                FeatJAR.log().error(e);
                return FeatJAR.ERROR_WRITING_RESULT;
            }
            return 0;
        } else {
            FeatJAR.log().problems(result.getProblems());
            FeatJAR.log().error("Could not compute result.");
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
    }

    protected abstract IComputation<T> newComputation(OptionList optionParser);

    protected IFormat<T> getOuputFormat(OptionList optionParser) {
        return new GenericTextFormat<>();
    }
}
