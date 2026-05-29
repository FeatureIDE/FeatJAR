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
import de.featjar.base.data.BinomialCalculator;
import de.featjar.base.data.combination.CombinationStream;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.Variables;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class VariableSetsCombinationSpecification extends ASetsCombinationSpecification {

    public VariableSetsCombinationSpecification(int[] t, BooleanAssignmentList list) {
        super(list.stream().map(a -> a.toVariables().get()).toArray(int[][]::new), t, list.getVariableMap());
    }

    public VariableSetsCombinationSpecification(int[] tValues, int[][] elementSets, VariableMap variableMap) {
        super(elementSets, tValues, variableMap);
    }

    public VariableSetsCombinationSpecification(int[] tValues, Variables[] variableSets, VariableMap variableMap) {
        super(Arrays.stream(variableSets).map(Variables::get).toArray(int[][]::new), tValues, variableMap);
    }

    public void forEach(Consumer<int[]> consumer) {
        CombinationStream.stream(elementSets, tValues).forEach(combination -> {
            consumer.accept(combination.select());
        });
    }

    public <V> void forEach(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        CombinationStream.stream(elementSets, tValues, environmentCreator).forEach(combination -> {
            consumer.accept(combination.environment(), combination.select());
        });
    }

    public void forEachParallel(Consumer<int[]> consumer) {
        CombinationStream.stream(elementSets, tValues).forEach(combination -> {
            consumer.accept(combination.select());
        });
    }

    public <V> void forEachParallel(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        CombinationStream.parallelStream(elementSets, tValues, environmentCreator)
                .forEach(combination -> {
                    consumer.accept(combination.environment(), combination.select());
                });
    }

    @Override
    public long loopCount() {
        try {
            long count = 1;
            for (int i = 0; i < elementSets.length; i++) {
                count = Math.multiplyExact(count, 1 << tValues[i]);
                count = Math.multiplyExact(
                        count, BinomialCalculator.computeBinomial(elementSets[i].length, tValues[i]));
            }
            return count;
        } catch (ArithmeticException e) {
            FeatJAR.log().warning("Long overflow for combination count. Using Long.MAX_VALUE.");
            return Long.MAX_VALUE;
        }
    }

    @Override
    public int maxT() {
        return IntStream.of(tValues).max().orElse(0);
    }

    @Override
    public ICombinationSpecification reduceTTo(int newT) {
        int[] reducedTValues = new int[tValues.length];
        for (int i = 0; i < reducedTValues.length; i++) {
            reducedTValues[i] = Math.min(newT, tValues[i]);
        }
        return new VariableSetsCombinationSpecification(reducedTValues, elementSets, variableMap);
    }
}
