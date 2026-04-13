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
 * A selection over a {@link ICombination combination} of items. Intended to use
 * with {@link CombinationStream}. Also stores an environment object for use in
 * parallel streams.
 *
 * @param <T> the type of an array of items
 * @param <E> the type of the environment object
 */
public interface ISelection<T, E> {

    /**
     * {@return the environment object}
     */
    E environment();
    /**
     * {@return the current index of the underlying combination}
     * @see ICombination#index()
     */
    long index();
    /**
     * {@return the maximum index of the underlying combination}
     * @see ICombination#maxIndex()
     */
    long maxIndex();
    /**
     * {@return the current selection indices of the underlying combination}
     * @see ICombination#selectionIndices()
     */
    int[] selectionIndices();

    /**
     * Selects the items specified by the current selection indices in the underlying combination and stores them in the internal array.
     * @return the current internal selection array (not a copy)
     */
    T select();

    /**
     * Selects the items specified by the current selection indices in the underlying combination and stores them in the given array.
     * @param selection the selection array to store the selected items in
     * @return the given selection array
     */
    T select(T selection);

    /**
     * Selects the items specified by the current selection indices in the underlying combination and stores them in a new array.
     * @return a copy of the internal selection array
     */
    T createSelection();
}
