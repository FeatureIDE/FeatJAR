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

import java.util.Arrays;

/**
 * Combination object used by lexicographic iterators.
 * Supports multiple item sets and combination sizes (t) per item set.
 *
 * @author Sebastian Krieter
 */
final class MultiCombination implements ICombination {

    private SingleCombination[] combinations;
    private int[] selectionIndices;
    private long maxCombinationIndex;

    /**
     * Creates a new combination instance.
     * @param ns the item sets sizes
     * @param ts the combination size per item set
     */
    public MultiCombination(int[] ns, int[] ts) {
        combinations = new SingleCombination[ts.length];
        int length = 0;
        for (int i = 0; i < combinations.length; i++) {
            length += ts[i];
        }
        selectionIndices = new int[length];
        int offset = 0;
        for (int i = 0; i < combinations.length; i++) {
            combinations[i] = new SingleCombination(ns[i], selectionIndices, offset, offset = offset + ts[i]);
        }
        maxCombinationIndex = 1;
        for (int k = 0; k < combinations.length; k++) {
            maxCombinationIndex *= combinations[k].maxCombinationIndex + 1;
        }
    }

    /**
     * Copy constructor.
     * @param other the combination to copy
     */
    public MultiCombination(MultiCombination other) {
        selectionIndices = Arrays.copyOf(other.selectionIndices, other.selectionIndices.length);
        combinations = new SingleCombination[other.combinations.length];
        for (int i = 0; i < combinations.length; i++) {
            combinations[i] = new SingleCombination(other.combinations[i], selectionIndices);
        }
        maxCombinationIndex = other.maxCombinationIndex;
    }

    @Override
    public MultiCombination clone() {
        return new MultiCombination(this);
    }

    @Override
    public int t() {
        return selectionIndices.length;
    }

    @Override
    public long index() {
        long index = combinations[combinations.length - 1].combinationIndex;
        for (int i = 2; i <= combinations.length; i++) {
            index = (combinations[i - 2].maxCombinationIndex + 1) * index
                    + combinations[combinations.length - i].combinationIndex;
        }
        return index;
    }

    @Override
    public void setMaxIndex(long newMaxCombinationIndex) {
        if (newMaxCombinationIndex < 0 || index() > newMaxCombinationIndex) {
            throw new IndexOutOfBoundsException(newMaxCombinationIndex);
        }
        maxCombinationIndex = newMaxCombinationIndex;
    }

    @Override
    public long maxIndex() {
        return maxCombinationIndex;
    }

    @Override
    public int[] selectionIndices() {
        return selectionIndices;
    }

    @Override
    public int[] select(int[] selection, int[][] items) {
        for (int i = 0; i < selectionIndices.length; i++) {
            selection[i] = items[i][selectionIndices[i]];
        }
        return selection;
    }

    @Override
    public <T> T[] select(T[] selection, T[][] items) {
        for (int i = 0; i < selectionIndices.length; i++) {
            selection[i] = items[i][selectionIndices[i]];
        }
        return selection;
    }

    @Override
    public void reset() {
        for (SingleCombination c : combinations) {
            c.reset();
        }
    }

    @Override
    public boolean advance() {
        for (SingleCombination c : combinations) {
            if (c.advance()) {
                return true;
            } else {
                c.reset();
            }
        }
        return false;
    }

    @Override
    public void advanceTo(long newIndex) {
        if (newIndex < 0 || newIndex > maxIndex()) {
            throw new IndexOutOfBoundsException(newIndex);
        }
        for (int i = 1; i < combinations.length; i++) {
            combinations[i - 1].advanceTo(newIndex % (combinations[i].maxCombinationIndex + 1));
            newIndex /= combinations[i].maxCombinationIndex + 1;
        }
        combinations[combinations.length - 1].advanceTo(newIndex);
    }

    @Override
    public String toString() {
        return Arrays.toString(selectionIndices);
    }
}
