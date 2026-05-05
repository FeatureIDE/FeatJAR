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
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class LiteralSetsCombinationSpecification extends ASetsCombinationSpecification {

    static int[][] convert(BooleanAssignmentList list) {
        int[][] elementSets = new int[list.size()][];
        int index = 0;
        for (BooleanAssignment booleanAssignment : list) {
            elementSets[index++] =
                    IntStream.of(booleanAssignment.get()).distinct().toArray();
        }
        return elementSets;
    }

    public LiteralSetsCombinationSpecification(int[] t, BooleanAssignmentList list) {
        super(convert(list), t, list.getVariableMap());
    }

    public LiteralSetsCombinationSpecification(int[] reducedTValues, int[][] elementSets, VariableMap variableMap) {
        super(elementSets, reducedTValues, variableMap);
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

    @Override
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
        return new LiteralSetsCombinationSpecification(reducedTValues, elementSets, variableMap);
    }
}
