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
package de.featjar.base.data.combination;

import de.featjar.base.data.BinomialCalculator;
import java.util.Arrays;

/**
 * Abstract implementation of {@link ICombination}.
 *
 * @author Sebastian Krieter
 */
final class SingleCombination implements ICombination {

    /**
     * The indices of the elements.
     */
    private final int[] selectionIndices;
    /**
     * The index of current internal combination
     */
    long combinationIndex;
    /**
     * The maximum index of the combination enumeration
     */
    long maxCombinationIndex;

    private int maxSelectionIndex;
    private int start;
    private int end;

    /**
     * The highest selection index that was recently changed.
     */
    private int lastChangedSelectionIndex;

    private BinomialCalculator binomialCalculator;

    /**
     * Creates a new combination instance.
     * @param n number of items
     * @param t combination size
     */
    public SingleCombination(int n, int t) {
        this(n, new int[t], 0, t);
    }

    /**
     * Copy constructor.
     * @param other the combination to copy
     */
    public SingleCombination(SingleCombination other) {
        this(other, Arrays.copyOf(other.selectionIndices, other.selectionIndices.length));
    }

    @Override
    public SingleCombination clone() {
        return new SingleCombination(this);
    }

    /**
     * Creates a new combination instance.
     * @param n the number of items in the item set
     * @param start the offset within the selectionIndices array
     * @param end the combination size plus the offset
     * @param selectionIndices the array to store the combination in
     */
    SingleCombination(int n, int[] selectionIndices, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        if (selectionIndices.length < end) {
            throw new IllegalArgumentException();
        }
        this.selectionIndices = selectionIndices;
        this.start = start;
        this.end = end;
        maxSelectionIndex = n;
        binomialCalculator = new BinomialCalculator(end - start, n);
        maxCombinationIndex = binomialCalculator.binomial() - 1;
        reset();
    }

    /**
     * Copy constructor.
     * @param other the combination to copy
     */
    SingleCombination(SingleCombination other, int[] selectionIndices) {
        this.selectionIndices = selectionIndices;
        start = other.start;
        end = other.end;
        lastChangedSelectionIndex = other.lastChangedSelectionIndex;
        maxSelectionIndex = other.maxSelectionIndex;
        binomialCalculator = other.binomialCalculator;
        maxCombinationIndex = other.maxCombinationIndex;
        combinationIndex = other.combinationIndex;
    }

    @Override
    public int t() {
        return end - start;
    }

    @Override
    public long index() {
        return combinationIndex;
    }

    @Override
    public long maxIndex() {
        return maxCombinationIndex;
    }

    @Override
    public void setMaxIndex(long newMaxCombinationIndex) {
        if (newMaxCombinationIndex < 0 || combinationIndex > newMaxCombinationIndex) {
            throw new IndexOutOfBoundsException(newMaxCombinationIndex);
        }
        maxCombinationIndex = newMaxCombinationIndex;
    }

    @Override
    public int[] selectionIndices() {
        return selectionIndices;
    }

    @Override
    public int[] select(int[] selection, int[][] items) {
        for (int i = start; i < end; i++) {
            selection[i] = items[i][selectionIndices[i]];
        }
        return selection;
    }

    @Override
    public <T> T[] select(T[] selection, T[][] items) {
        for (int i = start; i < end; i++) {
            selection[i] = items[i][selectionIndices[i]];
        }
        return selection;
    }

    @Override
    public void reset() {
        combinationIndex = 0;
        for (int i = start; i < end; i++) {
            selectionIndices[i] = i - start;
        }
        lastChangedSelectionIndex = end - 1;
    }

    @Override
    public boolean advance() {
        if (combinationIndex >= maxCombinationIndex) {
            return false;
        }
        combinationIndex++;

        int i = start;
        for (; i < end - 1; i++) {
            if (selectionIndices[i] + 1 < selectionIndices[i + 1]) {
                ++selectionIndices[i];
                resetLowerElements(i);
                return true;
            }
        }
        int lastIndex = selectionIndices[i] + 1;
        if (lastIndex == maxSelectionIndex) {
            resetLowerElements(i + 1);
        } else {
            selectionIndices[i] = lastIndex;
            resetLowerElements(i);
        }
        return true;
    }

    private void resetLowerElements(int i) {
        lastChangedSelectionIndex = i;
        for (int j = i - 1; j >= start; j--) {
            selectionIndices[j] = j;
        }
    }

    public void advanceTo(long newIndex) {
        if (newIndex < 0 || newIndex > maxCombinationIndex) {
            throw new IndexOutOfBoundsException(newIndex);
        }
        combinationIndex = newIndex;
        binomialCalculator.combination(newIndex, selectionIndices, start);
        lastChangedSelectionIndex = end - 1;
    }

    @Override
    public String toString() {
        return Arrays.toString(selectionIndices);
    }
}
