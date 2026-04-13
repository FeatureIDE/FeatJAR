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
 * Partial implementation of {@link ISelection}.
 * @param <T> the type of array of items
 * @param <E> the type of the environment object
 */
abstract class ASelection<T, E> implements ISelection<T, E> {

    /**
     * The underlying combination.
     */
    protected final ICombination combination;

    /**
     * The environment object.
     */
    protected final E environment;

    /**
     * Creates a new selection.
     * @param combination the combination
     * @param environment the environment
     */
    protected ASelection(ICombination combination, E environment) {
        this.combination = combination;
        this.environment = environment;
    }

    @Override
    public E environment() {
        return environment;
    }

    @Override
    public long index() {
        return combination.index();
    }

    @Override
    public long maxIndex() {
        return combination.maxIndex();
    }

    @Override
    public int[] selectionIndices() {
        return combination.selectionIndices();
    }

    @Override
    public String toString() {
        return "ASelection [combination=" + combination + "]";
    }
}
