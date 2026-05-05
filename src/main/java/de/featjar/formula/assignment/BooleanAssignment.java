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
package de.featjar.formula.assignment;

import de.featjar.analysis.ISolver;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.IntegerList;
import de.featjar.base.data.Maps;
import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.formula.VariableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Assigns Boolean values to integer-identified
 * {@link de.featjar.formula.structure.term.value.Variable variables}. Can be
 * used to represent a set of literals for use in a satisfiability
 * {@link ISolver}. Implemented as an unordered list of indices to variables in
 * some unspecified {@link VariableMap}. An index can be negative, indicating a
 * negated occurrence of its variable, or 0, indicating no occurrence, and it
 * may occur multiple times. For specific use cases, consider using
 * {@link BooleanClause} (a disjunction of literals) or {@link BooleanSolution}
 * (a conjunction of literals).
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class BooleanAssignment extends IntegerList implements IAssignment<Integer, Boolean>, IBooleanRepresentation {

    public static class VariablesComputation extends AComputation<BooleanAssignment> {

        public static final Dependency<BooleanAssignmentList> ASSIGNMENTS =
                Dependency.newDependency(BooleanAssignmentList.class);

        public VariablesComputation(IComputation<BooleanAssignmentList> computation) {
            super(computation);
        }

        @Override
        public Result<BooleanAssignment> compute(List<Object> dependencyList, Progress progress) {
            return Result.of(ASSIGNMENTS.get(dependencyList).getVariableMap()).map(VariableMap::getVariables);
        }
    }

    private static final long serialVersionUID = 1614980283996088122L;

    /**
     * Constructs a new literal array from the given clause with all literals
     * removed that evaluate to {@code false}. Returns {@code null} if any literal
     * evaluate to {@code true} or the clause contains a literal and its complement.
     *
     * @param clause the given clause
     * @param core   the known literals
     * @return a new array containing only unknown literals or {@code null} if the
     *         clause is a tautology
     */
    public static int[] unitPropagation(BooleanClause clause, BooleanAssignment core) {
        final int[] literals = clause.get();
        final LinkedHashSet<Integer> literalSet = new LinkedHashSet<>(literals.length << 1);

        for (int var : literals) {
            if (core.indexOf(var) >= 0) {
                return null;
            } else if (core.indexOf(-var) < 0) {
                if (literalSet.contains(-var)) {
                    return null;
                } else {
                    literalSet.add(var);
                }
            }
        }
        final int[] literalArray = new int[literalSet.size()];
        int i = 0;
        for (final int lit : literalSet) {
            literalArray[i++] = lit;
        }
        return literalArray;
    }

    public static int[] simplify(int[] literals) {
        final LinkedHashSet<Integer> integerSet = Sets.empty();
        for (final int integer : literals) {
            if (integer != 0 && integerSet.contains(-integer)) {
                // If this assignment is a contradiction or tautology, it can be simplified.
                return new int[] {};
            } else {
                integerSet.add(integer);
            }
        }
        if (integerSet.size() == literals.length) {
            return Arrays.copyOf(literals, literals.length);
        }
        int[] newArray = new int[integerSet.size()];
        int i = 0;
        for (final int lit : integerSet) {
            newArray[i++] = lit;
        }
        return newArray;
    }

    public static int[] shuffle(int[] literals, Random random) {
        final long seed = random.nextLong();
        Random curRandom = new Random(seed);

        final LinkedHashSet<Integer> integerSet = Sets.empty();
        for (final int integer : literals) {
            if (integer != 0) {
                integerSet.add(integer);
            }
        }
        int[] newArray = new int[integerSet.size()];
        int i = -1;
        for (final int lit : integerSet) {
            newArray[++i] = lit;
        }
        for (; i > 0; --i) {
            int swapIndex = curRandom.nextInt(literals.length);
            int temp = literals[i];
            literals[i] = literals[swapIndex];
            literals[swapIndex] = temp;
        }
        return newArray;
    }

    public BooleanAssignment(int... integers) {
        super(integers);
    }

    public BooleanAssignment(Collection<Integer> integers) {
        super(integers);
    }

    public BooleanAssignment(BooleanAssignment booleanAssignment) {
        super(booleanAssignment);
    }

    public final int[] simplify() {
        return simplify(elements);
    }

    /**
     * Changes the literals in this assignment to a new mapping.
     * This creates a copy of this assignment.
     * A call of this method is equivalent to a call of {@link #remap(VariableMap, VariableMap, boolean) adapt(newVariables, false);}.
     *
     * @param oldVariableMap the old variable map
     * @param newVariableMap the new variable map
     * @return the new assignment with changed mapping
     */
    public BooleanAssignment remap(VariableMap oldVariableMap, VariableMap newVariableMap) {
        return remap(oldVariableMap, newVariableMap, false);
    }

    /**
     * Changes the literals in this assignment to a new mapping.
     * This creates a copy of this assignment.
     *
     * @param oldVariableMap the old variable map
     * @param newVariableMap the new variable map
     * @param integrateOldVariables whether variable names from the old variable map are added to the new variable map, if missing
     * @return the new assignment with changed mapping
     */
    public BooleanAssignment remap(
            VariableMap oldVariableMap, VariableMap newVariableMap, boolean integrateOldVariables) {
        final int[] newElements = new int[elements.length];
        oldVariableMap.remap(elements, newElements, newVariableMap, integrateOldVariables);
        return new BooleanAssignment(newElements);
    }

    public BooleanAssignment shuffle(Random random) {
        return new BooleanAssignment(shuffle(elements, random));
    }

    public int indexOfVariable(int variable) {
        if (variable < 0) {
            throw new IllegalArgumentException(String.format("%d is negative", variable));
        }
        return IntStream.range(0, elements.length)
                .filter(i -> Math.abs(elements[i]) == variable)
                .findFirst()
                .orElse(-1);
    }

    public int[] indicesOfVariable(int variable) {
        if (variable < 0) {
            throw new IllegalArgumentException(String.format("%d is negative", variable));
        }
        return IntStream.range(0, elements.length)
                .filter(i -> Math.abs(elements[i]) == variable)
                .toArray();
    }

    public final boolean containsVariable(int integer) {
        return indexOfVariable(integer) >= 0;
    }

    public final boolean containsAnyVariable(int... integers) {
        return Arrays.stream(integers).anyMatch(integer -> containsVariable(integer));
    }

    public final boolean containsAllVariables(int... integers) {
        return Arrays.stream(integers).allMatch(integer -> containsVariable(integer));
    }

    public final boolean containsNoneVariables(int... integers) {
        return Arrays.stream(integers).noneMatch(integer -> containsVariable(integer));
    }

    /**
     * {@return the intersection of this integer list with the given integers}
     *
     * @param integers the integers
     */
    public int[] retainAllVariablesInts(int... integers) {
        boolean[] intersectionMarker = new boolean[elements.length];
        int count = 0;
        for (int integer : integers) {
            final int[] indices = indicesOfVariable(integer);
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                if (index >= 0 && !intersectionMarker[index]) {
                    count++;
                    intersectionMarker[index] = true;
                }
            }
        }

        int[] newArray = new int[count];
        int j = 0;
        for (int i = 0; i < elements.length; i++) {
            if (intersectionMarker[i]) {
                newArray[j++] = elements[i];
            }
        }
        assert Arrays.stream(elements)
                .allMatch(e -> Arrays.stream(newArray).anyMatch(i -> i == e)
                        == Arrays.stream(integers).anyMatch(i -> i == Math.abs(e)));
        return newArray;
    }

    /**
     * {@return the intersection of this integer list with the given integers}
     *
     * @param integers the integers
     */
    public int[] retainAllNegatedInts(int... integers) {
        boolean[] intersectionMarker = new boolean[elements.length];
        int count = 0;
        for (int integer : integers) {
            final int[] indices = indicesOf(-integer);
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                if (index >= 0 && !intersectionMarker[index]) {
                    count++;
                    intersectionMarker[index] = true;
                }
            }
        }

        int[] newArray = new int[count];
        int j = 0;
        for (int i = 0; i < elements.length; i++) {
            if (intersectionMarker[i]) {
                newArray[j++] = elements[i];
            }
        }
        assert Arrays.stream(elements)
                .allMatch(e -> Arrays.stream(newArray).anyMatch(i -> i == e)
                        == Arrays.stream(integers).anyMatch(i -> i == Math.abs(e)));
        return newArray;
    }

    /**
     * {@return the difference of this integer list and the given integers}
     *
     * @param integers the integers
     */
    public int[] removeAllVariablesInts(int... integers) {
        boolean[] intersectionMarker = new boolean[elements.length];
        int count = 0;
        for (int integer : integers) {
            final int[] indices = indicesOfVariable(integer);
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                if (index >= 0 && !intersectionMarker[index]) {
                    count++;
                    intersectionMarker[index] = true;
                }
            }
        }

        int[] newArray = new int[elements.length - count];
        int j = 0;
        for (int i = 0; i < elements.length; i++) {
            if (!intersectionMarker[i]) {
                newArray[j++] = elements[i];
            }
        }
        assert Arrays.stream(elements)
                .allMatch(e -> Arrays.stream(newArray).anyMatch(i -> i == e)
                        ^ Arrays.stream(integers).anyMatch(i -> i == Math.abs(e)));
        return newArray;
    }

    @Override
    public ValueAssignment toValue() {
        return VariableMap.toValue(this);
    }

    @Override
    public LinkedHashMap<Integer, Boolean> getAll() {
        LinkedHashMap<Integer, Boolean> map = Maps.empty();
        for (int literal : elements) {
            if (literal != 0) {
                map.put(literal, literal > 0);
            }
        }
        return map;
    }

    @Override
    public Result<Boolean> getValue(Integer variable) {
        int index = indexOfVariable(variable);
        if (index < 0) {
            return Result.empty();
        }
        int value = get(index);
        return value == 0 ? Result.empty() : Result.of(value > 0);
    }

    @Override
    public String print() {
        return toValue().print();
    }

    public BooleanAssignment(Assignment assignment, VariableMap map) {
        super(new int[assignment.size()]);
        int i = 0;
        for (var keyValue : assignment.getAll().entrySet()) {
            String key = keyValue.getKey();

            Result<Integer> index = map.get(key);
            if (index.isEmpty()) {
                throw new IllegalArgumentException("Variable " + key + " does not exist in the variable map.");
            }
            elements[i++] = index.get();
        }
    }

    @Override
    public String toString() {
        return String.format("BooleanAssignment%s", Arrays.toString(elements));
    }

    @Override
    public BooleanAssignment clone() {
        return new BooleanAssignment(this);
    }

    @Override
    public BooleanAssignment toAssignment() {
        return this;
    }

    @Override
    public BooleanClause toClause() {
        return new BooleanClause(Arrays.copyOf(elements, elements.length));
    }

    @Override
    public BooleanSolution toSolution() {
        return new BooleanSolution(IntStream.of(elements).map(Math::abs).max().orElse(0), elements);
    }

    public BooleanAssignment toVariables() {
        return new BooleanAssignment(IntStream.of(elements).map(Math::abs).toArray());
    }

    public BooleanSolution toSolution(int variableCount) {
        if (variableCount < 0) {
            throw new IllegalArgumentException(
                    String.format("Variable count must be positive, but was %d.", variableCount));
        }
        return new BooleanSolution(variableCount, elements);
    }

    @Override
    public BooleanAssignment negate() {
        return new BooleanAssignment(negateInts());
    }

    public BooleanAssignment addAll(IntegerList integers) {
        return addAll(integers.get());
    }

    public BooleanAssignment retainAll(IntegerList integers) {
        return retainAll(integers.get());
    }

    public BooleanAssignment retainAllNegated(IntegerList integers) {
        return retainAllNegated(integers.get());
    }

    public BooleanAssignment retainAllVariables(IntegerList integers) {
        return retainAllVariables(integers.get());
    }

    public BooleanAssignment removeAll(IntegerList integers) {
        return removeAll(integers.get());
    }

    public BooleanAssignment removeAllVariables(IntegerList integers) {
        return removeAllVariables(integers.get());
    }

    public BooleanAssignment addAll(int... integers) {
        return new BooleanAssignment(addAllInts(integers));
    }

    public BooleanAssignment retainAll(int... integers) {
        return new BooleanAssignment(retainAllInts(integers));
    }

    public BooleanAssignment retainAllNegated(int... integers) {
        return new BooleanAssignment(retainAllNegatedInts(integers));
    }

    public BooleanAssignment retainAllVariables(int... integers) {
        return new BooleanAssignment(retainAllVariablesInts(integers));
    }

    public BooleanAssignment removeAll(int... integers) {
        return new BooleanAssignment(removeAllInts(integers));
    }

    public BooleanAssignment removeAllVariables(int... integers) {
        return new BooleanAssignment(removeAllVariablesInts(integers));
    }
}
