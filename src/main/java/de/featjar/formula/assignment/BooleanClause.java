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

/**
 * A Boolean clause; that is, a disjunction of literals. Implemented as a sorted
 * list of indices. Often used as input to a SAT {@link ISolver}. Indices are
 * ordered naturally; that is, in ascending order, so negative indices come
 * before positive indices. The same index may occur multiple times, but no
 * element may be 0.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class BooleanClause extends BooleanAssignment implements IClause<Integer, Boolean> {

    private static final long serialVersionUID = -684031217097515442L;

    public BooleanClause(int... integers) {
        this(integers, true);
        assert Arrays.stream(integers).noneMatch(a -> a == 0) : "contains zero: " + Arrays.toString(integers);
    }

    public BooleanClause(int[] integers, boolean sort) {
        super(integers);
        assert Arrays.stream(integers).noneMatch(a -> a == 0) : "contains zero: " + Arrays.toString(integers);
        assert sort
                        || Arrays.stream(integers)
                                        .reduce((a, b) -> a != 0 && a < b ? b : 0)
                                        .orElse(1)
                                != 0
                : "unsorted: " + Arrays.toString(integers);
        if (sort) sort();
    }

    public BooleanClause(Collection<Integer> integers) {
        super(integers);
        assert integers.stream().noneMatch(a -> a == 0) : "contains zero: " + integers.toString();
        sort();
    }

    public BooleanClause(BooleanClause booleanClause) {
        super(booleanClause);
    }

    protected void sort() {
        hashCodeValid = false;
        Arrays.sort(elements);
    }

    @Override
    public int[] getNonZeroValuesInts() {
        assert Arrays.stream(elements).noneMatch(a -> a == 0) : "contains zero: " + Arrays.toString(elements);
        return copyInts();
    }

    @Override
    public int countNonZero() {
        assert Arrays.stream(elements).noneMatch(a -> a == 0) : "contains zero: " + Arrays.toString(elements);
        return size();
    }

    @Override
    public int countNegatives() {
        return -(Arrays.binarySearch(elements, 0) + 1);
    }

    @Override
    public int countPositives() {
        return elements.length + (Arrays.binarySearch(elements, 0) + 1);
    }

    @Override
    public int[] getPositiveValuesInts() {
        return Arrays.copyOfRange(elements, elements.length - countPositives(), elements.length);
    }

    @Override
    public int[] getNegativeValuesInts() {
        return Arrays.copyOfRange(elements, 0, countNegatives());
    }

    @Override
    public int indexOf(int integer) {
        return Arrays.binarySearch(elements, integer);
    }

    @Override
    public int[] indicesOf(int integer) {
        return new int[] {Arrays.binarySearch(elements, integer)};
    }

    @Override
    public int[] negateInts() {
        final int[] inverse = new int[elements.length];
        final int highestIndex = inverse.length - 1;
        for (int i = 0; i < inverse.length; i++) {
            inverse[highestIndex - i] = -elements[i];
        }
        return inverse;
    }

    @Override
    public BooleanClause negate() {
        return new BooleanClause(negateInts(), false);
    }

    @Override
    public ValueClause toValue() {
        return VariableMap.toValue(this);
    }

    @Override
    public String toString() {
        return String.format("BooleanClause%s", Arrays.toString(elements));
    }

    @Override
    public BooleanAssignment toAssignment() {
        return this;
    }

    @Override
    public BooleanClause toClause() {
        return this;
    }

    @Override
    public BooleanClause clone() {
        return new BooleanClause(this);
    }

    @Override
    public BooleanClause remap(VariableMap oldVariableMap, VariableMap newVariableMap) {
        return (BooleanClause) super.remap(oldVariableMap, newVariableMap);
    }

    @Override
    public BooleanClause remap(VariableMap oldVariableMap, VariableMap newVariableMap, boolean integrateOldVariables) {
        final int[] newElements = new int[elements.length];
        oldVariableMap.remap(elements, newElements, newVariableMap, integrateOldVariables);
        return new BooleanClause(newElements);
    }

    @Override
    public BooleanClause addAll(IntegerList integers) {
        return new BooleanClause(addAllInts(integers.get()));
    }

    @Override
    public BooleanClause retainAll(IntegerList integers) {
        return new BooleanClause(retainAllInts(integers.get()));
    }

    @Override
    public BooleanClause retainAllVariables(IntegerList integers) {
        return new BooleanClause(retainAllVariablesInts(integers.get()));
    }

    @Override
    public BooleanClause removeAll(IntegerList integers) {
        return new BooleanClause(removeAllInts(integers.get()));
    }

    @Override
    public BooleanClause removeAllVariables(IntegerList integers) {
        return new BooleanClause(removeAllVariablesInts(integers.get()));
    }
}
