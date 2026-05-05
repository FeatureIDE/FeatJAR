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

public class LiteralCombinationSpecification extends ACombinationSpecification {

    public LiteralCombinationSpecification(int t, BooleanAssignmentList list) {
        super(IntStream.of(list.getFirst().get()).distinct().toArray(), t, list.getVariableMap());
    }

    public LiteralCombinationSpecification(int t, BooleanAssignment variables, VariableMap variableMap) {
        super(IntStream.of(variables.get()).distinct().toArray(), t, variableMap);
    }

    public LiteralCombinationSpecification(int t, int[] variables, VariableMap variableMap) {
        super(IntStream.of(variables).distinct().toArray(), t, variableMap);
    }

    public LiteralCombinationSpecification(LiteralCombinationSpecification other) {
        super(other);
    }

    @Override
    public LiteralCombinationSpecification copy() {
        return new LiteralCombinationSpecification(this);
    }

    public void forEach(Consumer<int[]> consumer) {
        CombinationStream.stream(elements, t).forEach(combination -> {
            consumer.accept(combination.select());
        });
    }

    public <V> void forEach(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        CombinationStream.stream(elements, t, environmentCreator).forEach(combination -> {
            consumer.accept(combination.environment(), combination.select());
        });
    }

    @Override
    public void forEachParallel(Consumer<int[]> consumer) {
        CombinationStream.parallelStream(elements, t).forEach(combination -> {
            consumer.accept(combination.select());
        });
    }

    public <V> void forEachParallel(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        CombinationStream.parallelStream(elements, t, environmentCreator).forEach(combination -> {
            consumer.accept(combination.environment(), combination.select());
        });
    }

    @Override
    public long loopCount() {
        try {
            return BinomialCalculator.computeBinomial(elements.length, t);
        } catch (ArithmeticException e) {
            FeatJAR.log().warning("Long overflow for combination count. Using Long.MAX_VALUE.");
            return Long.MAX_VALUE;
        }
    }

    @Override
    public ICombinationSpecification reduceTTo(int newT) {
        return new LiteralCombinationSpecification(newT, elements, variableMap);
    }
}
