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
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This iterator provides all t-wise combinations of a given set of integer items without permutations.
 * It uses the combinatorial number system to enumerate combinations and enable parallel processing.
 *
 * @param <E> the type of the environment for the combination
 * @author Sebastian Krieter
 */
public final class IntLexicographicIterator<E> implements Spliterator<ISelection<int[], E>> {

    private static class IntSelection<E> extends ASelection<int[], E> {

        private final int[][] items;
        private final int[] selection;

        IntSelection(ICombination combination, int[][] items, int[] selection, E environment) {
            super(combination, environment);
            this.items = items;
            this.selection = selection;
        }

        @Override
        public int[] createSelection() {
            return combination.select(Arrays.copyOf(selection, selection.length), items);
        }

        @Override
        public int[] select() {
            return combination.select(selection, items);
        }

        @Override
        public int[] select(int[] selection) {
            return combination.select(selection, items);
        }
    }

    private static final int MINIMUM_SPLIT_SIZE = 10;

    private final ICombination combination;
    private final IntSelection<E> selection;

    private final int[][] orderedItems;
    private final Supplier<E> environmentSupplier;

    /**
     * Constructs a new instance of the iterator with the given items and combination size.
     *
     * @param ts the combination sizes for each item set
     * @param items the items sets
     * @param environmentSupplier the supplier of the environment object
     */
    public IntLexicographicIterator(int[][] items, int[] ts, Supplier<E> environmentSupplier) {
        Objects.requireNonNull(items);
        Objects.requireNonNull(ts);
        if (items.length != ts.length) {
            throw new IllegalArgumentException(String.format(
                    "Number of item sets (%d) must be the same as index length (%d)", items.length, ts.length));
        }
        if (ts.length == 0) {
            throw new IllegalArgumentException();
        } else if (ts.length == 1) {
            combination = new SingleCombination(items[0].length, ts[0]);
        } else {
            int[] ns = new int[items.length];
            for (int i = 0; i < items.length; i++) {
                ns[i] = items[i].length;
            }
            combination = new MultiCombination(ns, ts);
        }
        int selectionLength = combination.t();
        orderedItems = new int[selectionLength][];
        int index = 0;
        for (int i = 0; i < ts.length; i++) {
            int t = ts[i];
            int[] itemSet = items[i];
            for (int j = 0; j < t; j++) {
                orderedItems[index++] = itemSet;
            }
        }

        this.environmentSupplier = environmentSupplier != null ? environmentSupplier : () -> null;
        this.selection =
                new IntSelection<>(combination, orderedItems, new int[selectionLength], environmentSupplier.get());
    }

    /**
     * Constructs a new instance of the iterator with the given items and combination size.
     *
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment object
     */
    public IntLexicographicIterator(int[] items, int t, Supplier<E> environmentSupplier) {
        combination = new SingleCombination(items.length, t);
        int selectionLength = combination.t();
        orderedItems = new int[selectionLength][];
        int index = 0;
        for (int j = 0; j < t; j++) {
            orderedItems[index++] = items;
        }

        this.environmentSupplier = environmentSupplier != null ? environmentSupplier : () -> null;
        this.selection =
                new IntSelection<>(combination, orderedItems, new int[selectionLength], environmentSupplier.get());
    }

    /**
     * Copy constructor. Used by {@link #trySplit()}.
     * @param other the iterator to copy
     */
    private IntLexicographicIterator(IntLexicographicIterator<E> other) {
        environmentSupplier = other.environmentSupplier;
        orderedItems = other.orderedItems;
        combination = other.combination.clone();

        long currentIndex = other.combination.index();
        long newStart = currentIndex + (((other.combination.maxIndex() + 1) - currentIndex) / 2);
        other.combination.advanceTo(newStart);
        combination.setMaxIndex(newStart - 1);

        selection = new IntSelection<>(
                combination, orderedItems, new int[other.selection.selection.length], environmentSupplier.get());
    }

    @Override
    public int characteristics() {
        return ORDERED | DISTINCT | SIZED | NONNULL | IMMUTABLE | SUBSIZED;
    }

    @Override
    public long estimateSize() {
        return combination.maxIndex() - combination.index();
    }

    @Override
    public Spliterator<ISelection<int[], E>> trySplit() {
        return (estimateSize() < MINIMUM_SPLIT_SIZE) ? null : new IntLexicographicIterator<>(this);
    }

    @Override
    public boolean tryAdvance(Consumer<? super ISelection<int[], E>> action) {
        action.accept(selection);
        return combination.advance();
    }
}
