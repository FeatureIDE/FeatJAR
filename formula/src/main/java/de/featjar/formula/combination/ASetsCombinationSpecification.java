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
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Abstract implementation of {@link ICombinationSpecification} using multiple lists of elements and multiple size values to build combinations.
 * To build a combination, sub-combination are build from the given lists and then concatenated.
 */
public abstract class ASetsCombinationSpecification implements ICombinationSpecification {

    protected int[] tValues;
    protected int[][] elementSets;
    protected VariableMap variableMap;

    protected ASetsCombinationSpecification(int[][] elementSets, int[] tValues, VariableMap variableMap) {
        if (elementSets.length < tValues.length) {
            throw new IllegalArgumentException(String.format(
                    "Number of element sets (%d) and t values (%d) must be equal.",
                    elementSets.length, tValues.length));
        }
        setT(tValues);
        setElements(elementSets, variableMap);
    }

    public int[] t() {
        return tValues;
    }

    public void setT(int[] tValues) {
        for (int t : tValues) {
            if (t < 1) {
                throw new IllegalArgumentException(
                        String.format("Value for t must be greater than 0. Value was %d.", +t));
            }
        }
        this.tValues = tValues;
    }

    public void setElements(int[][] elementSets, VariableMap variableMap) {
        Objects.requireNonNull(elementSets);
        for (int i = 0; i < elementSets.length; i++) {
            if (elementSets[i].length < tValues[i]) {
                throw new IllegalArgumentException(String.format(
                        "Value for t (%d) must be greater than number of elements (%d).",
                        tValues, elementSets[i].length));
            }
        }
        this.elementSets = elementSets;
        this.variableMap = Objects.requireNonNull(variableMap);
    }

    public int[][] elements() {
        return elementSets;
    }

    public VariableMap variableMap() {
        return variableMap;
    }

    public void shuffleElements(Random random) {
        Random curRandom = new Random(random.nextLong());
        for (int[] elements : elementSets) {
            for (int i = elements.length - 1; i >= 0; --i) {
                int swapIndex = curRandom.nextInt(elements.length);
                int temp = elements[i];
                elements[i] = elements[swapIndex];
                elements[swapIndex] = temp;
            }
        }
    }

    public void remap(VariableMap newVariableMap) {
        if (variableMap == newVariableMap) {
            return;
        }
        Objects.requireNonNull(newVariableMap);
        for (int[] elements : elementSets) {
            variableMap.remap(elements, elements, newVariableMap, false);
        }
        variableMap = newVariableMap;
    }

    public abstract void forEach(Consumer<int[]> consumer);

    public abstract <V> void forEach(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator);

    public abstract <V> void forEachParallel(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator);
}
