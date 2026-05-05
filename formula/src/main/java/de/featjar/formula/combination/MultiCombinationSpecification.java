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

import de.featjar.base.FeatJAR;
import de.featjar.formula.VariableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MultiCombinationSpecification implements ICombinationSpecification {

    private final List<ICombinationSpecification> combinationSets;

    public MultiCombinationSpecification(ICombinationSpecification... combinationSets) {
        this.combinationSets = Arrays.asList(combinationSets);
    }

    public MultiCombinationSpecification(List<ICombinationSpecification> combinationSets) {
        this.combinationSets = combinationSets;
    }

    @Override
    public long loopCount() {
        try {
            long sum = 0;
            for (ICombinationSpecification combinationSet : combinationSets) {
                sum = Math.addExact(sum, combinationSet.loopCount());
            }
            return sum;
        } catch (ArithmeticException e) {
            FeatJAR.log().warning("Long overflow for combination count. Using Long.MAX_VALUE.");
            return Long.MAX_VALUE;
        }
    }

    @Override
    public void shuffleElements(Random random) {
        for (ICombinationSpecification combinationSet : combinationSets) {
            combinationSet.shuffleElements(random);
        }
    }

    @Override
    public void remap(VariableMap variableMap) {
        for (ICombinationSpecification combinationSet : combinationSets) {
            combinationSet.remap(variableMap);
        }
    }

    @Override
    public void forEach(Consumer<int[]> consumer) {
        for (ICombinationSpecification combinationSet : combinationSets) {
            combinationSet.forEach(consumer);
        }
    }

    @Override
    public <V> void forEach(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        for (ICombinationSpecification combinationSet : combinationSets) {
            combinationSet.forEach(consumer, environmentCreator);
        }
    }

    @Override
    public void forEachParallel(Consumer<int[]> consumer) {
        for (ICombinationSpecification combinationSet : combinationSets) {
            combinationSet.forEachParallel(consumer);
        }
    }

    @Override
    public <V> void forEachParallel(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        for (ICombinationSpecification combinationSet : combinationSets) {
            combinationSet.forEachParallel(consumer, environmentCreator);
        }
    }

    @Override
    public VariableMap variableMap() {
        VariableMap mergedVariableMap = new VariableMap(combinationSets.stream()
                .map(ICombinationSpecification::variableMap)
                .collect(Collectors.toList()));
        remap(mergedVariableMap);
        return mergedVariableMap;
    }

    @Override
    public int maxT() {
        return combinationSets.stream()
                .mapToInt(ICombinationSpecification::maxT)
                .max()
                .orElse(0);
    }

    @Override
    public ICombinationSpecification reduceTTo(int newT) {
        return new MultiCombinationSpecification(
                combinationSets.stream().map(s -> s.reduceTTo(newT)).collect(Collectors.toList()));
    }
}
