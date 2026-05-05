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
import de.featjar.base.data.IntegerList;
import de.featjar.formula.VariableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;

/**
 * A (partial) Boolean solution; that is, a conjunction of literals. Implemented
 * as a sorted list of indices. Often holds output of a SAT {@link ISolver}.
 * Indices are ordered such that the array index {@code i - 1} either holds
 * {@code -i}, {@code 0}, or {@code i}. That is, the largest occurring index
 * mandates the minimum length of the underlying array. The same index may not
 * occur multiple times, but indices may be 0 for partial solutions.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class BooleanSolution extends BooleanAssignment implements ISolution<Integer, Boolean> {

    private static final long serialVersionUID = -6609620937538213007L;

    public BooleanSolution(int... integers) {
        this(integers, true);
    }

    public BooleanSolution(int[] integers, boolean sort) {
        super(integers);
        assert sort
                        || Arrays.stream(integers)
                                        .map(Math::abs) //
                                        .max()
                                        .orElse(0)
                                <= integers.length //
                : String.format(
                        "Max index %d is larger than number of elements %d. Elements = %s",
                        Arrays.stream(integers).map(Math::abs).max().orElse(0),
                        integers.length,
                        Arrays.toString(integers));
        assert sort
                        || Arrays.stream(integers)
                                        .map(Math::abs) //
                                        .reduce(0, (a, b) -> b == 0 ? a + 1 : a + 1 == b ? b : -2)
                                == integers.length
                : "Unsorted: " + Arrays.toString(integers);
        if (sort) sort();
    }

    public BooleanSolution(Collection<Integer> integers) {
        super(integers);
        assert integers.stream().mapToInt(a -> a).map(Math::abs).max().getAsInt() == integers.size();
        sort();
    }

    public BooleanSolution(int variableCount, int[] integers) {
        super(new int[variableCount]);
        Arrays.stream(integers)
                .filter(integer -> integer != 0)
                .forEach(integer -> elements[Math.abs(integer) - 1] = integer);
    }

    public BooleanSolution(BooleanSolution booleanSolution) {
        super(booleanSolution);
    }

    protected void sort() {
        hashCodeValid = false;
        final int[] sortedIntegers = new int[elements.length];
        Arrays.stream(elements)
                .filter(integer -> integer != 0)
                .forEach(integer -> sortedIntegers[Math.abs(integer) - 1] = integer);
        System.arraycopy(sortedIntegers, 0, elements, 0, elements.length);
    }

    public int countConflicts(int... integers) {
        return (int) Arrays.stream(integers)
                .filter(integer -> indexOf(-integer) >= 0)
                .count();
    }

    public boolean conflictsWith(int... integers) {
        return Arrays.stream(integers).anyMatch(integer -> indexOf(-integer) >= 0);
    }

    public static int[] removeConflicts(int[] integers1, int[] integers2) {
        if (integers1.length != integers2.length) {
            throw new IllegalArgumentException(
                    String.format("Arguments have different lengths (%d != %d)", integers1.length, integers2.length));
        }
        int[] integers = new int[integers1.length];
        for (int i = 0; i < integers1.length; i++) {
            final int x = integers1[i];
            final int y = integers2[i];
            integers[i] = x != y ? 0 : x;
        }
        return integers;
    }

    public static void removeConflictsInplace(int[] integers1, int[] integers2) {
        if (integers1.length != integers2.length) {
            throw new IllegalArgumentException(
                    String.format("Arguments have different lengths (%d != %d)", integers1.length, integers2.length));
        }
        for (int i = 0; i < integers1.length; i++) {
            final int x = integers1[i];
            final int y = integers2[i];
            integers1[i] = x != y ? 0 : x;
        }
    }

    public int[] removeConflicts(int... literals) {
        return removeConflicts(elements, literals);
    }

    @Override
    public int indexOf(int literal) {
        final int index = Math.abs(literal) - 1;
        return literal != 0 && index < size() && elements[index] == literal ? index : -1;
    }

    @Override
    public int indexOfVariable(int variable) {
        if (variable <= 0) {
            throw new IllegalArgumentException(String.format("Variable ID must be larger than 0. Was %d", variable));
        }
        final int index = variable - 1;
        return index < size() && elements[index] != 0 ? index : -1;
    }

    @Override
    public int[] indicesOf(int integer) {
        return new int[] {indexOf(integer)};
    }

    @Override
    public int[] indicesOfVariable(int variable) {
        return new int[] {indexOfVariable(variable)};
    }

    @Override
    public ValueSolution toValue() {
        return VariableMap.toValue(this);
    }

    @Override
    public String toString() {
        return String.format("BooleanSolution%s", Arrays.toString(elements));
    }

    @Override
    public BooleanSolution toSolution() {
        return this;
    }

    @Override
    public BooleanSolution toSolution(int variableCount) {
        return variableCount == elements.length ? this : super.toSolution(variableCount);
    }

    @Override
    public BooleanAssignment toAssignment() {
        return new BooleanAssignment(IntStream.of(elements).filter(l -> l != 0).toArray());
    }

    @Override
    public BooleanClause toClause() {
        return new BooleanClause(IntStream.of(elements).filter(l -> l != 0).toArray());
    }

    @Override
    public BooleanSolution clone() {
        return new BooleanSolution(this);
    }

    @Override
    public BooleanSolution remap(VariableMap oldVariableMap, VariableMap newVariableMap) {
        return remap(oldVariableMap, newVariableMap, false);
    }

    @Override
    public BooleanSolution remap(
            VariableMap oldVariableMap, VariableMap newVariableMap, boolean integrateOldVariables) {
        if (integrateOldVariables) {
            throw new IllegalArgumentException("Dynamic extension of variable map not allowed for BooleanSolution.");
        }
        int[] newElements = new int[newVariableMap.size()];
        for (int i = 0; i < elements.length; i++) {
            int newLiteral = oldVariableMap.remap(i + 1, newVariableMap, false);
            int oldLiteral = elements[i];
            newElements[newLiteral - 1] = oldLiteral == 0 ? 0 : oldLiteral > 0 ? newLiteral : -newLiteral;
        }
        return new BooleanSolution(newElements);
    }

    @Override
    public BooleanSolution negate() {
        return new BooleanSolution(negateInts(), false);
    }

    public BooleanSolution addAll(IntegerList integers) {
        return addAll(integers.get());
    }

    public BooleanSolution retainAll(IntegerList integers) {
        return retainAll(integers.get());
    }

    public BooleanSolution retainAllNegated(IntegerList integers) {
        return retainAllNegated(integers.get());
    }

    public BooleanSolution retainAllVariables(IntegerList integers) {
        return retainAllVariables(integers.get());
    }

    public BooleanSolution removeAll(IntegerList integers) {
        return removeAll(integers.get());
    }

    public BooleanSolution removeAllVariables(IntegerList integers) {
        return removeAllVariables(integers.get());
    }

    public BooleanSolution addAll(int... integers) {
        return new BooleanSolution(addAllInts(integers));
    }

    public BooleanSolution retainAll(int... integers) {
        return new BooleanSolution(retainAllInts(integers));
    }

    public BooleanSolution retainAllNegated(int... integers) {
        return new BooleanSolution(retainAllNegatedInts(integers));
    }

    public BooleanSolution retainAllVariables(int... integers) {
        return new BooleanSolution(retainAllVariablesInts(integers));
    }

    public BooleanSolution removeAll(int... integers) {
        return new BooleanSolution(removeAllInts(integers));
    }

    public BooleanSolution removeAllVariables(int... integers) {
        return new BooleanSolution(removeAllVariablesInts(integers));
    }

    /**
     * Set all elements that are currently 0 to their index + 1.
     * @return this solution, does not return a copy
     */
    public BooleanSolution setUndefined() {
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] == 0) {
                elements[i] = i + 1;
            }
        }
        return this;
    }

    /**
     * Set all elements that are currently 0 to -(their index + 1).
     * @return this solution, does not return a copy
     */
    public BooleanSolution unsetUndefined() {
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] == 0) {
                elements[i] = -(i + 1);
            }
        }
        return this;
    }
}
