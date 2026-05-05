/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.log;

import de.featjar.base.computation.Progress;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Returns an estimation of the remaining time for a {@link Progress progress} object.
 * The output format is: {@code ETR: dd hh:mm:ss} or {@code "ETR:  > 99 days "} or {@code "ETR: N/A         "}.
 *
 * @author Sebastian Krieter
 */
public final class RemainingTimeMessage implements IMessage {

    private static final int slidingWindowSize = 60;

    private long startTime;
    private long[] timeSlidingWindow = new long[slidingWindowSize];
    private double[] workSlidingWindow = new double[slidingWindowSize];

    private int slidingWindowIndex = 0;

    private Supplier<Double> progressSupplier;

    /**
     * Constructs a new message supplier.
     * @param progress the progress object
     */
    public RemainingTimeMessage(Supplier<Double> progress) {
        startTime = System.currentTimeMillis() / 1000L;
        this.progressSupplier = progress;
        Arrays.fill(timeSlidingWindow, startTime);
    }

    public String get() {
        double relativeProgress = progressSupplier.get();
        if (relativeProgress < 0) {
            relativeProgress = 0;
        } else if (relativeProgress > 1) {
            relativeProgress = 1;
        }
        long curTime = System.currentTimeMillis() / 1000L;

        if (relativeProgress == 0) {
            return "ETR: N/A        ";
        } else {
            double curWork = relativeProgress;
            timeSlidingWindow[slidingWindowIndex] = curTime;
            workSlidingWindow[slidingWindowIndex] = curWork;
            long[] rel = new long[slidingWindowSize];

            long average = 0;
            long count = 0;

            for (int i = 1; i < slidingWindowSize; i++) {
                int j = (slidingWindowIndex + i) % slidingWindowSize;
                long timeDiff = curTime - timeSlidingWindow[j];
                double workDiff = curWork - workSlidingWindow[j];
                if (timeDiff > 0 && workDiff > 0) {
                    rel[j] = (long) ((timeDiff * (1 - relativeProgress)) / workDiff);
                    //                    average += (long) ((timeDiff * (1 - relativeProgress)) / workDiff);
                    count++;
                }
            }

            Arrays.sort(rel);
            long median;
            if (rel.length % 2 == 0) median = (rel[rel.length / 2] + rel[rel.length / 2 - 1]) / 2;
            else median = rel[rel.length / 2];

            average = median;
            slidingWindowIndex = (slidingWindowIndex + 1) % slidingWindowSize;

            if (count == 0) {
                return "ETR: N/A        ";
            } else {
                //                average /= count;

                int day = (int) (average / 86400L);
                if (day > 99) {
                    return "ETR: > 99 days ";
                } else {
                    int sec = (int) (average % 60L);
                    int min = (int) ((average / 60L) % 60L);
                    int hour = (int) ((average / 3600L) % 24L);
                    return String.format("ETR: %02d %02d:%02d:%02d", day, hour, min, sec);
                }
            }
        }
    }
}
