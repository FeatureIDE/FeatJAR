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

/**
 * Interface for a combination of t integers without permutations and duplicates.
 * Uses the combinatorial number system to enumerate all possible combinations of this kind.
 * Allows to change the combination to the next in the enumeration and to any arbitrary number.
 *
 * @author Sebastian Krieter
 */
interface ICombination {

    /**
     * {@return the combination size}
     */
    int t();

    /**
     * {@return the index of the current combination in the enumeration}
     */
    long index();

    /**
     * {@return the maximum index of the enumeration}
     */
    long maxIndex();

    /**
     * {@return the array representing the current combination}
     */
    int[] selectionIndices();

    /**
     * Stores items from an item list to a given selection array corresponding to the current combination.
     * Item lists are given for each individual position of the combination.
     * @param selection the selection array
     * @param items the item lists
     * @return the given selection array
     */
    int[] select(int[] selection, int[][] items);

    /**
     * Stores items from an item list to a given selection array corresponding to the current combination.
     * Item lists are given for each individual position of the combination.
     * @param <T> the type of the items
     * @param selection the selection array
     * @param items the item lists
     * @return the given selection array
     */
    <T> T[] select(T[] selection, T[][] items);

    /**
     * Resets this combination to the first in the enumeration.
     */
    void reset();

    /**
     * Changes this combination to the next in the enumeration.
     * @return {@code true} if the combination before the change is not the last and {@code false} otherwise.
     */
    boolean advance();

    /**
     * Changes this combination to the given index in the enumeration.
     * @param newIndex the new index
     */
    void advanceTo(long newIndex);

    /**
     * {@return a copy of this combination}
     */
    ICombination clone();

    /**
     * Set the maximum index.
     * @param newMaxCombinationIndex the new max index.
     */
    void setMaxIndex(long newMaxCombinationIndex);
}
