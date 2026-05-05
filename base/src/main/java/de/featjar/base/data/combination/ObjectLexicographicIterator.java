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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This iterator provides all t-wise combinations of a given set of integer items without permutations.
 * It uses the combinatorial number system to enumerate combinations and enable parallel processing.
 *
 * @param <T> the type of combination
 * @param <E> the type of the environment for the combination
 * @author Sebastian Krieter
 */
public final class ObjectLexicographicIterator<T, E> implements Spliterator<ISelection<T[], E>> {

    private static class ObjectSelection<T, E> extends ASelection<T[], E> {

        private final T[][] items;
        private final T[] selection;

        private ObjectSelection(ICombination combination, T[][] items, T[] selection, E environment) {
            super(combination, environment);
            this.items = items;
            this.selection = selection;
        }

        @Override
        public T[] createSelection() {
            return combination.select(Arrays.copyOf(selection, selection.length), items);
        }

        @Override
        public T[] select() {
            return combination.select(selection, items);
        }

        @Override
        public T[] select(T[] selection) {
            return combination.select(selection, items);
        }
    }

    private static final int MINIMUM_SPLIT_SIZE = 10;

    private final ICombination combination;
    private final ObjectSelection<T, E> selection;

    private final T[][] orderedItems;
    private final Supplier<E> environmentSupplier;

    /**
     * Constructs a new instance of the iterator with the given items and combination size.
     *
     * @param ts the combination sizes for each item set
     * @param items the items sets
     * @param environmentSupplier the supplier of the environment object
     */
    @SuppressWarnings("unchecked")
    public ObjectLexicographicIterator(T[][] items, int[] ts, Supplier<E> environmentSupplier) {
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
        orderedItems = (T[][]) Array.newInstance(items[0].getClass(), selectionLength);
        int index = 0;
        for (int i = 0; i < ts.length; i++) {
            int t = ts[i];
            T[] itemSet = items[i];
            for (int j = 0; j < t; j++) {
                orderedItems[index++] = itemSet;
            }
        }

        this.environmentSupplier = environmentSupplier != null ? environmentSupplier : () -> null;
        this.selection = new ObjectSelection<>(
                combination,
                orderedItems,
                (T[]) Array.newInstance(items[0][0].getClass(), selectionLength),
                environmentSupplier.get());
    }

    /**
     * Constructs a new instance of the iterator with the given items and combination size.
     *
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment object
     */
    @SuppressWarnings("unchecked")
    public ObjectLexicographicIterator(T[] items, int t, Supplier<E> environmentSupplier) {
        combination = new SingleCombination(items.length, t);
        int selectionLength = combination.t();
        orderedItems = (T[][]) Array.newInstance(items[0].getClass(), selectionLength);
        int index = 0;
        for (int j = 0; j < t; j++) {
            orderedItems[index++] = items;
        }

        this.environmentSupplier = environmentSupplier != null ? environmentSupplier : () -> null;
        this.selection = new ObjectSelection<>(
                combination,
                orderedItems,
                (T[]) Array.newInstance(items[0].getClass(), selectionLength),
                environmentSupplier.get());
    }

    /**
     * Copy constructor. Used by {@link #trySplit()}.
     * @param other the iterator to copy
     */
    private ObjectLexicographicIterator(ObjectLexicographicIterator<T, E> other) {
        environmentSupplier = other.environmentSupplier;
        orderedItems = other.orderedItems;
        combination = other.combination.clone();

        long currentIndex = other.combination.index();
        long newStart = currentIndex + ((other.combination.maxIndex() - currentIndex) / 2);
        other.combination.advanceTo(newStart);
        combination.setMaxIndex(newStart);

        T[] selectionCopy = Arrays.copyOf(other.selection.selection, other.selection.selection.length);
        Arrays.fill(selectionCopy, null);
        selection = new ObjectSelection<>(combination, orderedItems, selectionCopy, environmentSupplier.get());
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
    public Spliterator<ISelection<T[], E>> trySplit() {
        return (estimateSize() < MINIMUM_SPLIT_SIZE) ? null : new ObjectLexicographicIterator<>(this);
    }

    @Override
    public boolean tryAdvance(Consumer<? super ISelection<T[], E>> action) {
        action.accept(selection);
        return combination.advance();
    }
}
