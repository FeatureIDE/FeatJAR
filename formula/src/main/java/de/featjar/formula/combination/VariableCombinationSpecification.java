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
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.BinomialCalculator;
import de.featjar.base.data.Ints;
import de.featjar.base.data.Result;
import de.featjar.base.data.combination.CombinationStream;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class VariableCombinationSpecification extends ACombinationSpecification {

    public static final class VariableCombinationSpecificationComputation
            extends AComputation<ICombinationSpecification> {

        public static final Dependency<BooleanAssignmentList> BOOLEAN_CLAUSE_LIST =
                Dependency.newDependency(BooleanAssignmentList.class);
        public static final Dependency<Integer> T = Dependency.newDependency(Integer.class);

        public VariableCombinationSpecificationComputation(IComputation<BooleanAssignmentList> clauseList) {
            super(clauseList, Computations.of(1));
        }

        public VariableCombinationSpecificationComputation(
                IComputation<BooleanAssignmentList> clauseList, IComputation<Integer> t) {
            super(clauseList, t);
        }

        public VariableCombinationSpecificationComputation(IComputation<BooleanAssignmentList> clauseList, int t) {
            super(clauseList, Computations.of(t));
        }

        @Override
        public Result<ICombinationSpecification> compute(List<Object> dependencyList, Progress progress) {
            VariableMap variableMap = BOOLEAN_CLAUSE_LIST.get(dependencyList).getVariableMap();
            return Result.of(new VariableCombinationSpecification(
                    T.get(dependencyList), variableMap.getVariables(), variableMap));
        }
    }

    public VariableCombinationSpecification(int t, VariableMap variableMap) {
        super(variableMap.getVariables().get(), t, variableMap);
    }

    public VariableCombinationSpecification(int t, BooleanAssignment variables, VariableMap variableMap) {
        super(IntStream.of(variables.get()).map(Math::abs).distinct().toArray(), t, variableMap);
    }

    public VariableCombinationSpecification(int t, int[] variables, VariableMap variableMap) {
        super(IntStream.of(variables).map(Math::abs).distinct().toArray(), t, variableMap);
    }

    public VariableCombinationSpecification(int t) {
        super(t);
    }

    public VariableCombinationSpecification(VariableCombinationSpecification other) {
        super(other);
    }

    @Override
    public VariableCombinationSpecification copy() {
        return new VariableCombinationSpecification(this);
    }

    public void forEach(Consumer<int[]> consumer) {
        final int[] gray = Ints.grayCode(t);
        CombinationStream.stream(elements, t).forEach(combination -> {
            final int[] combinationLiterals = combination.select();
            for (int g : gray) {
                consumer.accept(combinationLiterals);
                combinationLiterals[g] = -combinationLiterals[g];
            }
        });
    }

    public <V> void forEach(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        final int[] gray = Ints.grayCode(t);
        CombinationStream.stream(elements, t, environmentCreator).forEach(combination -> {
            final int[] combinationLiterals = combination.select();
            final V environment = combination.environment();
            for (int g : gray) {
                consumer.accept(environment, combinationLiterals);
                combinationLiterals[g] = -combinationLiterals[g];
            }
        });
    }

    public void forEachParallel(Consumer<int[]> consumer) {
        final int[] gray = Ints.grayCode(t);
        CombinationStream.parallelStream(elements, t).forEach(combination -> {
            final int[] combinationLiterals = combination.select();
            for (int g : gray) {
                consumer.accept(combinationLiterals);
                combinationLiterals[g] = -combinationLiterals[g];
            }
        });
    }

    public <V> void forEachParallel(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        final int[] gray = Ints.grayCode(t);
        CombinationStream.parallelStream(elements, t, environmentCreator).forEach(combination -> {
            final int[] combinationLiterals = combination.select();
            final V environment = combination.environment();
            for (int g : gray) {
                consumer.accept(environment, combinationLiterals);
                combinationLiterals[g] = -combinationLiterals[g];
            }
        });
    }

    @Override
    public long loopCount() {
        try {
            return Math.multiplyExact(1 << t, BinomialCalculator.computeBinomial(elements.length, t));
        } catch (ArithmeticException e) {
            FeatJAR.log().warning("Long overflow for combination count. Using Long.MAX_VALUE.");
            return Long.MAX_VALUE;
        }
    }

    @Override
    public ICombinationSpecification reduceTTo(int newT) {
        return new VariableCombinationSpecification(newT, elements, variableMap);
    }
}
