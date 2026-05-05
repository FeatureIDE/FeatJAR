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
package de.featjar.evaluation.util;

import java.util.Iterator;

/**
 * Iterates over a given list of options with different ranges.
 *
 * @author Sebastian Krieter
 */
public class ProgressTracker implements Iterator<int[]> {

    private int[] sizes, indices;
    private int totalSize, totalIndex, lastIndexChanged;

    public ProgressTracker(int... optionRanges) {
        sizes = new int[optionRanges.length];
        indices = new int[optionRanges.length];
        totalIndex = -1;
        totalSize = 1;
        for (int i = 0; i < optionRanges.length; i++) {
            int size = optionRanges[i];
            if (size <= 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid range for option %d (%d). Must be larger than 0.", i, optionRanges[i]));
            }
            sizes[i] = size;
            indices[i] = size - 1;
            totalSize *= size;
        }
        assert totalSize >= 1;
    }

    public String nextAndPrint() {
        next();
        return printStatus();
    }

    public String printStatus() {
        StringBuilder statusMessage = new StringBuilder();
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] > 1) {
                statusMessage.append(String.format("%d/%d ", indices[i] + 1, sizes[i]));
            }
        }
        statusMessage.append(
                String.format("%5.1f", ((Math.floor(((double) (totalIndex + 1) / totalSize) * 1000)) / 10.0)));
        statusMessage.append('%');
        return statusMessage.toString();
    }

    public int[] getSizes() {
        return sizes;
    }

    public int[] getIndices() {
        return indices;
    }

    @Override
    public boolean hasNext() {
        return totalIndex < totalSize - 1;
    }

    @Override
    public int[] next() {
        if (!hasNext()) {
            return null;
        }
        int i = sizes.length - 1;
        for (; i >= 0; i--) {
            final int index = indices[i];
            if (index == sizes[i] - 1) {
                indices[i] = 0;
            } else {
                indices[i] = index + 1;
                i--;
                break;
            }
        }
        totalIndex++;
        assert totalIndex >= 0;
        assert totalIndex < totalSize;
        lastIndexChanged = i + 1;
        return indices;
    }

    public int getLastChanged() {
        return lastIndexChanged;
    }
}
