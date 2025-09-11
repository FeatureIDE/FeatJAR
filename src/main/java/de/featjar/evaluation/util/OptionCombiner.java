/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.evaluation.util;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ListOption;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Iterates over a list of {@link ListOption list options}.
 *
 * @author Sebastian Krieter
 */
@SuppressWarnings("unchecked")
public class OptionCombiner {

    private OptionList optionParser;
    private Option<? extends List<?>>[] options;
    private ProgressTracker progress;

    public OptionCombiner(OptionList parser) {
        this.optionParser = parser;
    }

    /**
     * Executes an operation for each combination of all option values.
     *
     * @param forEachOption The function to be executed for each option combination.
     *     Should return -1 in case of a successful run.
     *     Otherwise it should return the index of the option at which the problem was detected.
     *     The function may also deliberately return a lower index, if runs with the different values of the current options should be skipped.
     */
    public final void loopOverOptions(Function<Integer, Integer> forEachOption, Option<? extends List<?>>... options) {
        init(options);
        loopOverOptions(forEachOption, l -> {});
    }

    public void init(Option<? extends List<?>>... options) {
        this.options = options;

        int[] sizes = new int[options.length];
        for (int i = 0; i < options.length; i++) {
            int size = optionParser.getResult(options[i]).orElseThrow().size();
            if (size <= 0) {
                throw new IllegalArgumentException(
                        String.format("Option list must not be empty. Option: %s", options[i].getName()));
            }
            sizes[i] = size;
        }
        progress = new ProgressTracker(sizes);
    }

    /**
     * Executes an operation for each combination of all option values.
     *
     * @param forEachOption The function to be executed for each option combination.
     *     Should return -1 in case of a successful run.
     *     Otherwise it should return the index of the option at which the problem was detected.
     *     The function may also deliberately return a lower index, if runs with the different values of the current options should be skipped.
     */
    public final void loopOverOptions(Function<Integer, Integer> forEachOption) {
        loopOverOptions(forEachOption, l -> {});
    }

    public final void loopOverOptions(Function<Integer, Integer> forEachOption, Consumer<Integer> errorHandler) {
        Objects.requireNonNull(progress, () -> "Call init method first!");
        FeatJAR.log().info(printOptionNames(options));

        int lastErrorLevel = -1;
        while (progress.hasNext()) {
            if (lastErrorLevel < 0) {
                FeatJAR.log().info(progress::nextAndPrint);
            } else {
                do {
                    progress.next();
                    if (progress.getLastChanged() <= lastErrorLevel) {
                        lastErrorLevel = -1;
                        FeatJAR.log().info(progress::printStatus);
                        break;
                    } else {
                        errorHandler.accept(progress.getLastChanged());
                    }
                } while (progress.hasNext());
                if (lastErrorLevel >= 0) {
                    break;
                }
            }

            try {
                lastErrorLevel = forEachOption.apply(progress.getLastChanged());
            } catch (Exception e) {
                FeatJAR.log().error(e);
                lastErrorLevel = 0;
            }
        }
    }

    public <T> T getValue(int index) {
        int optionIndex = progress.getIndices()[index];
        return optionIndex < 0
                ? null
                : (T) optionParser.getResult(options[index]).orElseThrow().get(optionIndex);
    }

    private String printOptionNames(Option<? extends List<?>>... loptions) {
        StringBuilder optionMessage = new StringBuilder();
        int[] sizes = progress.getSizes();
        for (int i = 0; i < sizes.length; i++) {
            optionMessage.append(loptions[i].getName());
            optionMessage.append(String.format("(%d) ", sizes[i]));
        }
        return optionMessage.toString();
    }
}
