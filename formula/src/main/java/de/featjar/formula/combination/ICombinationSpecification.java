/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.combination;

import de.featjar.formula.VariableMap;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A generic specification of a set of literal combinations.
 */
public interface ICombinationSpecification {

    /**
     * {@return the number of combinations described by this specification}
     */
    long loopCount();

    /**
     * Randomizes the elements used four building combinations.
     * @param random the random instance to use
     */
    void shuffleElements(Random random);

    /**
     * Adapts the literal IDs to a new variable map.
     * @param variableMap the variable map
     */
    void remap(VariableMap variableMap);

    /**
     * Applies the given consumer to each combination sequentially.
     * @param consumer the consumer function
     */
    void forEach(Consumer<int[]> consumer);

    /**
     * Applies the given consumer to each combination sequentially.
     * The consumer also receives an environment object for context.
     *
     * @param <V> the type of the environment object
     * @param consumer the consumer function
     * @param environmentCreator a supplier for an environment object
     */
    <V> void forEach(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator);

    /**
     * Applies the given consumer to each combination in parallel.
     * @param consumer the consumer function
     */
    void forEachParallel(Consumer<int[]> consumer);

    /**
     * Applies the given consumer to each combination in parallel.
     * The consumer also receives an environment object for context.
     *
     * @param <V> the type of the environment object
     * @param consumer the consumer function
     * @param environmentCreator a supplier for an environment object
     */
    <V> void forEachParallel(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator);

    /**
     * {@return the variable map}
     */
    VariableMap variableMap();

    /**
     * {@return the maximum combination size among the combinations described by this specification}
     */
    int maxT();

    /**
     * Reduces the size of each combination described by this specification to the given maxT, if the combination is larger than maxT.
     * @param newT the new maximum combination size
     * @return a new instance of the reduced specification
     */
    ICombinationSpecification reduceTTo(int newT);

    default ICombinationSpecification copy() {
        throw new UnsupportedOperationException();
    }
}
