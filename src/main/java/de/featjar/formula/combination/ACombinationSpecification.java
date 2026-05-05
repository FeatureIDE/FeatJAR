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
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Abstract implementation of {@link ICombinationSpecification} using a single list of elements build combination with a given size.
 */
public abstract class ACombinationSpecification implements ICombinationSpecification {

    protected int t;
    protected int[] elements;
    protected VariableMap variableMap;

    protected ACombinationSpecification(int[] elements, int t, VariableMap variableMap) {
        setT(t);
        setElements(elements, variableMap);
    }

    protected ACombinationSpecification(int t) {
        setT(t);
    }

    protected ACombinationSpecification(ACombinationSpecification other) {
        t = other.t;
        elements = Arrays.copyOf(other.elements, other.elements.length);
        variableMap = other.variableMap;
    }

    public int t() {
        return t;
    }

    public void setT(int t) {
        if (t < 1) {
            throw new IllegalArgumentException(String.format("Value for t must be greater than 0. Value was %d.", +t));
        }
        this.t = t;
    }

    @Override
    public int maxT() {
        return t;
    }

    public void setElements(int[] elements, VariableMap variableMap) {
        Objects.requireNonNull(elements);
        if (elements.length < t) {
            throw new IllegalArgumentException(String.format(
                    "Value for t (%d) must be greater than number of elements (%d).", t, elements.length));
        }
        this.elements = elements;
        this.variableMap = Objects.requireNonNull(variableMap);
    }

    public int[] elements() {
        return elements;
    }

    @Override
    public VariableMap variableMap() {
        return variableMap;
    }

    @Override
    public void shuffleElements(Random random) {
        Random curRandom = new Random(random.nextLong());
        for (int i = elements.length - 1; i >= 0; --i) {
            int swapIndex = curRandom.nextInt(elements.length);
            int temp = elements[i];
            elements[i] = elements[swapIndex];
            elements[swapIndex] = temp;
        }
    }

    @Override
    public void remap(VariableMap newVariableMap) {
        if (variableMap == newVariableMap) {
            return;
        }
        Objects.requireNonNull(newVariableMap);
        if (elements == null) {
            setElements(newVariableMap.getVariables().get(), newVariableMap);
        } else {
            variableMap.remap(elements, elements, newVariableMap, false);
            variableMap = newVariableMap;
        }
    }

    public abstract void forEach(Consumer<int[]> consumer);

    public abstract <V> void forEach(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator);

    public abstract <V> void forEachParallel(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator);
}
