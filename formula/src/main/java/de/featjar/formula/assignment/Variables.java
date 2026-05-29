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

import de.featjar.base.data.IntegerList;
import de.featjar.formula.VariableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;

/**
 * A set of non-negative, non-zero integer-identified
 * {@link de.featjar.formula.structure.term.value.Variable variables}.
 * Implemented as a natural ordered list of indices to variables in
 * some unspecified {@link VariableMap}.
 * Elements are positive, non-zero and distinct.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Variables extends IntegerList {

    private static final long serialVersionUID = 8373185948106515764L;

    public Variables(int[] integers, boolean clean) {
        super(
                clean
                        ? Arrays.stream(integers)
                                .map(Math::abs)
                                .filter(v -> v > 0)
                                .distinct()
                                .toArray()
                        : integers);
        assert clean || Arrays.stream(integers).noneMatch(a -> a <= 0)
                : "contains non-positive.: " + Arrays.toString(integers);
        assert clean
                        || Arrays.stream(integers)
                                        .reduce((a, b) -> a != 0 && a < b ? b : 0)
                                        .orElse(1)
                                != 0
                : "unsorted: " + Arrays.toString(integers);
    }

    public Variables(int... integers) {
        this(integers, true);
    }

    public Variables(IntegerList integers) {
        this(integers.get(), true);
    }

    public Variables(Collection<Integer> integers) {
        super(integers.stream()
                .mapToInt(Math::abs)
                .filter(v -> v > 0)
                .distinct()
                .toArray());
    }

    public Variables(Variables variables) {
        super(variables);
    }

    /**
     * Changes the indices of this variables set to a new mapping.
     * This creates a copy of this set.
     * A call of this method is equivalent to a call of {@link #remap(VariableMap, VariableMap, boolean) adapt(newVariables, false);}.
     *
     * @param oldVariableMap the old variable map
     * @param newVariableMap the new variable map
     * @return the new variable set with changed mapping
     */
    public Variables remap(VariableMap oldVariableMap, VariableMap newVariableMap) {
        return remap(oldVariableMap, newVariableMap, false);
    }

    /**
     * Changes the indices of this variables set to a new mapping.
     * This creates a copy of this set.
     *
     * @param oldVariableMap the old variable map
     * @param newVariableMap the new variable map
     * @param integrateOldVariables whether variable names from the old variable map are added to the new variable map, if missing
     * @return the new variable set with changed mapping
     */
    public Variables remap(VariableMap oldVariableMap, VariableMap newVariableMap, boolean integrateOldVariables) {
        final int[] newElements = new int[elements.length];
        oldVariableMap.remap(elements, newElements, newVariableMap, integrateOldVariables);
        return new Variables(newElements);
    }

    public int indexOfLiteral(int literal) {
        return Arrays.binarySearch(elements, Math.abs(literal));
    }

    public final boolean containsLiteral(int literal) {
        return indexOfLiteral(literal) >= 0;
    }

    public final boolean containsAnyLiteral(int... literals) {
        return Arrays.stream(literals).anyMatch(l -> containsLiteral(l));
    }

    public final boolean containsAllLiterals(int... literals) {
        return Arrays.stream(literals).allMatch(l -> containsLiteral(l));
    }

    public final boolean containsNoneLiterals(int... literals) {
        return Arrays.stream(literals).noneMatch(l -> containsLiteral(l));
    }

    @Override
    public String toString() {
        return String.format("Variables%s", Arrays.toString(elements));
    }

    @Override
    public Variables clone() {
        return new Variables(this);
    }

    public BooleanAssignment toAssignment() {
        return new BooleanAssignment(Arrays.copyOf(elements, elements.length));
    }

    public BooleanClause toClause() {
        return new BooleanClause(Arrays.copyOf(elements, elements.length));
    }

    public BooleanSolution toSolution() {
        return new BooleanSolution(IntStream.of(elements).map(Math::abs).max().orElse(0), elements);
    }

    public BooleanSolution toSolution(int variableCount) {
        if (variableCount < 0) {
            throw new IllegalArgumentException(
                    String.format("Variable count must be positive, but was %d.", variableCount));
        }
        return new BooleanSolution(variableCount, elements);
    }

    public Variables addAll(Variables variables) {
        return new Variables(addAllInts(variables.elements));
    }

    public Variables retainAll(Variables variables) {
        return new Variables(retainAllInts(variables.elements));
    }

    public Variables removeAll(Variables variables) {
        return new Variables(removeAllInts(variables.elements));
    }
}
