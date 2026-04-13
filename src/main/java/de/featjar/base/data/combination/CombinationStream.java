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

import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides streams of t-wise combinations of a given set of items without permutations.
 *
 * @author Sebastian Krieter
 */
public final class CombinationStream {

    /**
     * {@return a sequential stream with the given items and combination size}
     *
     * @param <T> the type of the items
     * @param t the combination size
     * @param items the items
     */
    public static <T> Stream<ISelection<T[], Void>> stream(T[][] items, int[] t) {
        return StreamSupport.stream(new ObjectLexicographicIterator<>(items, t, () -> null), false);
    }

    /**
     * {@return a sequential stream with the given items and combination size}
     *
     * @param <T> the type of the items
     * @param t the combination size
     * @param items the items
     */
    public static <T> Stream<ISelection<T[], Void>> stream(T[] items, int t) {
        return StreamSupport.stream(new ObjectLexicographicIterator<>(items, t, () -> null), false);
    }

    /**
     * {@return a sequential stream with the given items and combination size}
     *
     * @param <T> the type of the items
     * @param <E> the type of the environment for the selections returned by the stream
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment objects
     */
    public static <T, E> Stream<ISelection<T[], E>> stream(T[] items, int t, Supplier<E> environmentSupplier) {
        return StreamSupport.stream(new ObjectLexicographicIterator<>(items, t, environmentSupplier), false);
    }

    /**
     * {@return a sequential stream with the given items and combination size}
     *
     * @param <T> the type of the items
     * @param <E> the type of the environment for the selections returned by the stream
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment objects
     */
    public static <T, E> Stream<ISelection<T[], E>> stream(T[][] items, int[] t, Supplier<E> environmentSupplier) {
        return StreamSupport.stream(new ObjectLexicographicIterator<>(items, t, environmentSupplier), false);
    }

    /**
     * {@return a parallel stream with the given items and combination size}
     *
     * @param <T> the type of the items
     * @param t the combination size
     * @param items the items
     */
    public static <T> Stream<ISelection<T[], Void>> parallelStream(T[] items, int t) {
        return StreamSupport.stream(new ObjectLexicographicIterator<>(items, t, () -> null), true);
    }

    /**
     * {@return a parallel stream with the given items and combination size}
     *
     * @param <T> the type of the items
     * @param <E> the type of the environment for the selections returned by the stream
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment objects
     */
    public static <T, E> Stream<ISelection<T[], E>> parallelStream(T[] items, int t, Supplier<E> environmentSupplier) {
        return StreamSupport.stream(new ObjectLexicographicIterator<>(items, t, environmentSupplier), true);
    }

    /**
     * {@return a parallel stream with the given items and combination size}
     *
     * @param <T> the type of the items
     * @param t the combination size
     * @param items the items
     */
    public static <T> Stream<ISelection<T[], Void>> parallelStream(T[][] items, int[] t) {
        return StreamSupport.stream(new ObjectLexicographicIterator<>(items, t, () -> null), true);
    }

    /**
     * {@return a parallel stream with the given items and combination size}
     *
     * @param <T> the type of the items
     * @param <E> the type of the environment for the selections returned by the stream
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment objects
     */
    public static <T, E> Stream<ISelection<T[], E>> parallelStream(
            T[][] items, int[] t, Supplier<E> environmentSupplier) {
        return StreamSupport.stream(new ObjectLexicographicIterator<>(items, t, environmentSupplier), true);
    }

    /**
     * {@return a sequential stream with the given items and combination size}
     *
     * @param t the combination size
     * @param items the items
     */
    public static Stream<ISelection<int[], Void>> stream(int[][] items, int[] t) {
        return StreamSupport.stream(new IntLexicographicIterator<>(items, t, () -> null), false);
    }

    /**
     * {@return a sequential stream with the given items and combination size}
     *
     * @param t the combination size
     * @param items the items
     */
    public static Stream<ISelection<int[], Void>> stream(int[] items, int t) {
        return StreamSupport.stream(new IntLexicographicIterator<>(items, t, () -> null), false);
    }

    /**
     * {@return a sequential stream with the given items and combination size}
     *
     * @param <E> the type of the environment for the selections returned by the stream
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment objects
     */
    public static <E> Stream<ISelection<int[], E>> stream(int[] items, int t, Supplier<E> environmentSupplier) {
        return StreamSupport.stream(new IntLexicographicIterator<>(items, t, environmentSupplier), false);
    }

    /**
     * {@return a sequential stream with the given items and combination size}
     *
     * @param <E> the type of the environment for the selections returned by the stream
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment objects
     */
    public static <E> Stream<ISelection<int[], E>> stream(int[][] items, int[] t, Supplier<E> environmentSupplier) {
        return StreamSupport.stream(new IntLexicographicIterator<>(items, t, environmentSupplier), false);
    }

    /**
     * {@return a parallel stream with the given items and combination size}
     *
     * @param t the combination size
     * @param items the items
     */
    public static Stream<ISelection<int[], Void>> parallelStream(int[] items, int t) {
        return StreamSupport.stream(new IntLexicographicIterator<>(items, t, () -> null), true);
    }

    /**
     * {@return a parallel stream with the given items and combination size}
     *
     * @param <E> the type of the environment for the selections returned by the stream
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment objects
     */
    public static <E> Stream<ISelection<int[], E>> parallelStream(int[] items, int t, Supplier<E> environmentSupplier) {
        return StreamSupport.stream(new IntLexicographicIterator<>(items, t, environmentSupplier), true);
    }

    /**
     * {@return a parallel stream with the given items and combination size}
     *
     * @param t the combination size
     * @param items the items
     */
    public static Stream<ISelection<int[], Void>> parallelStream(int[][] items, int[] t) {
        return StreamSupport.stream(new IntLexicographicIterator<>(items, t, () -> null), true);
    }

    /**
     * {@return a parallel stream with the given items and combination size}
     *
     * @param <E> the type of the environment for the selections returned by the stream
     * @param t the combination size
     * @param items the items
     * @param environmentSupplier the supplier of the environment objects
     */
    public static <E> Stream<ISelection<int[], E>> parallelStream(
            int[][] items, int[] t, Supplier<E> environmentSupplier) {
        return StreamSupport.stream(new IntLexicographicIterator<>(items, t, environmentSupplier), true);
    }
}
