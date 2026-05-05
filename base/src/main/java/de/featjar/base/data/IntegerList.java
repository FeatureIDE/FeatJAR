/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * An unordered list of integers. Subclasses implement specific interpretations
 * of these integers (e.g., as an index into a {@link RangeMap}). Negative and
 * zero integers are allowed.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class IntegerList implements Serializable {

    private static final long serialVersionUID = -8440039489675429479L;

    public static final class DescendingLengthComparator implements Comparator<IntegerList>, Serializable {
        private static final long serialVersionUID = -6244438443507884424L;

        @Override
        public int compare(IntegerList o1, IntegerList o2) {
            return o2.elements.length - o1.elements.length;
        }
    }

    public static final class AscendingLengthComparator implements Comparator<IntegerList>, Serializable {
        private static final long serialVersionUID = 2117836682884275173L;

        @Override
        public int compare(IntegerList o1, IntegerList o2) {
            return o1.elements.length - o2.elements.length;
        }
    }

    protected final int[] elements;
    protected boolean hashCodeValid;
    protected int hashCode;

    /**
     * Creates a new integer list from a given array of integers. To ensure
     * performance, the array is not copied, so it must not be modified.
     *
     * @param array the array
     */
    public IntegerList(int... array) {
        this.elements = array;
    }

    /**
     * Creates a new integer list from a given collection of integers.
     *
     * @param collection the collection
     */
    public IntegerList(Collection<Integer> collection) {
        this(collection.stream().mapToInt(Integer::intValue).toArray());
    }

    /**
     * Creates a new integer list by copying a given integer list.
     *
     * @param integerList the integer list
     */
    public IntegerList(IntegerList integerList) {
        elements = Arrays.copyOf(integerList.elements, integerList.elements.length);
        hashCodeValid = integerList.hashCodeValid;
        hashCode = integerList.hashCode;
    }

    public static int[] merge(Collection<? extends IntegerList> integerLists) {
        return integerLists.stream()
                .flatMapToInt(l -> Arrays.stream(l.elements))
                .distinct()
                .toArray();
    }

    public static int[] mergeInt(Collection<int[]> integerLists) {
        return integerLists.stream()
                .flatMapToInt(l -> Arrays.stream(l))
                .distinct()
                .toArray();
    }

    /**
     * {@return a copy of this integer list's integers} The returned array may be
     * modified.
     */
    public final int[] copyInts() {
        return copyOfRangeInts(0, elements.length);
    }

    /**
     * {@return a copy of this integer list's integers in a given range} The
     * returned array may be modified.
     *
     * @param range the range
     */
    public final int[] copyOfRangeInts(Range range) {
        int lowerBound = range.getLowerBound();
        int upperBound = range.getUpperBound();
        return copyOfRangeInts(
                lowerBound != Range.OPEN ? lowerBound : 0, upperBound != Range.OPEN ? upperBound : elements.length);
    }

    /**
     * {@return a copy of this integer list's integers in a given range} The
     * returned array may be modified.
     *
     * @param start the start index
     * @param end   the end index
     */
    public final int[] copyOfRangeInts(int start, int end) {
        return Arrays.copyOfRange(elements, start, end);
    }

    /**
     * {@return the absolute values of this integer list's integers} The returned
     * array may be modified.
     */
    public final int[] getAbsoluteValuesInts() {
        return Arrays.stream(elements).map(Math::abs).toArray();
    }

    /**
     * {@return the positive values in this integer list's integers} The returned
     * array may be modified.
     */
    public int[] getPositiveValuesInts() {
        return Arrays.stream(elements).filter(integer -> integer > 0).toArray();
    }

    /**
     * {@return the negative values in this integer list's integers} The returned
     * array may be modified.
     */
    public int[] getNegativeValuesInts() {
        return Arrays.stream(elements).filter(integer -> integer < 0).toArray();
    }

    /**
     * {@return the non-zero values in this integer list's integers} The returned
     * array may be modified.
     */
    public int[] getNonZeroValuesInts() {
        return Arrays.stream(elements).filter(integer -> integer != 0).toArray();
    }

    /**
     * {@return the union of this integer list with the given integers} No
     * duplicated are created.
     *
     * @param integers the integers
     */
    public final int[] addAllInts(int... integers) {
        boolean[] intersectionMarker = new boolean[elements.length];
        int count = 0;
        for (int integer : integers) {
            final int[] indices = indicesOf(integer);
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                if (index >= 0 && !intersectionMarker[index]) {
                    count++;
                    intersectionMarker[index] = true;
                }
            }
        }

        int[] newArray = new int[elements.length + integers.length - count];
        int j = 0;
        for (int i = 0; i < elements.length; i++) {
            if (!intersectionMarker[i]) {
                newArray[j++] = elements[i];
            }
        }
        System.arraycopy(integers, 0, newArray, j, integers.length);
        assert Arrays.stream(elements).allMatch(e -> Arrays.stream(newArray).anyMatch(i -> i == e));
        assert Arrays.stream(integers).allMatch(e -> Arrays.stream(newArray).anyMatch(i -> i == e));
        return newArray;
    }

    /**
     * {@return the intersection of this integer list with the given integers}
     *
     * @param integers the integers
     */
    public final int[] retainAllInts(int... integers) {
        boolean[] intersectionMarker = new boolean[elements.length];
        int count = 0;
        for (int integer : integers) {
            final int[] indices = indicesOf(integer);
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
                        == Arrays.stream(integers).anyMatch(i -> i == e));
        return newArray;
    }

    /**
     * {@return the difference of this integer list and the given integers}
     *
     * @param integers the integers
     */
    public final int[] removeAllInts(int... integers) {
        boolean[] intersectionMarker = new boolean[elements.length];
        int count = 0;
        for (int integer : integers) {
            final int[] indices = indicesOf(integer);
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
                        ^ Arrays.stream(integers).anyMatch(i -> i == e));
        return newArray;
    }

    /**
     * {@return a new integer list containing the negated values of this integer
     * list}
     */
    public int[] negateInts() {
        return Arrays.stream(elements).map(integer -> -integer).toArray();
    }

    /**
     * {@return a copy of this integer list's integers} The returned array may be
     * modified.
     */
    public IntegerList copy() {
        return new IntegerList(copyInts());
    }

    /**
     * {@return a copy of this integer list's integers in a given range} The
     * returned array may be modified.
     *
     * @param range the range
     */
    public IntegerList copyOfRange(Range range) {
        return new IntegerList(copyOfRangeInts(range));
    }

    /**
     * {@return a copy of this integer list's integers in a given range} The
     * returned array may be modified.
     *
     * @param start the start index
     * @param end   the end index
     */
    public IntegerList copyOfRange(int start, int end) {
        return new IntegerList(copyOfRangeInts(start, end));
    }

    /**
     * {@return the absolute values of this integer list's integers} The returned
     * array may be modified.
     */
    public IntegerList getAbsoluteValues() {
        return new IntegerList(getAbsoluteValuesInts());
    }

    /**
     * {@return the positive values in this integer list's integers} The returned
     * array may be modified.
     */
    public IntegerList getPositiveValues() {
        return new IntegerList(getPositiveValuesInts());
    }

    /**
     * {@return the negative values in this integer list's integers} The returned
     * array may be modified.
     */
    public IntegerList getNegativeValues() {
        return new IntegerList(getNegativeValuesInts());
    }

    /**
     * {@return the non-zero values in this integer list's integers} The returned
     * array may be modified.
     */
    public IntegerList getNonZeroValues() {
        return new IntegerList(getNonZeroValuesInts());
    }

    /**
     * {@return the union of this integer list with the given integers} No
     * duplicated are created.
     *
     * @param integers the integers
     */
    public IntegerList addAll(int... integers) {
        return new IntegerList(addAllInts(integers));
    }

    /**
     * {@return the intersection of this integer list with the given integers}
     *
     * @param integers the integers
     */
    public IntegerList retainAll(int... integers) {
        return new IntegerList(retainAllInts(integers));
    }

    /**
     * {@return the difference of this integer list and the given integers}
     *
     * @param integers the integers
     */
    public IntegerList removeAll(int... integers) {
        return new IntegerList(removeAllInts(integers));
    }

    /**
     * {@return a new integer list containing the negated values of this integer
     * list}
     */
    public IntegerList negate() {
        return new IntegerList(negateInts());
    }

    /**
     * {@return the value at the given index of this integer list} To ensure
     * performance, no {@link Result} is created, so the index should be checked for
     * validity beforehand.
     *
     * @param index the index
     * @throws IndexOutOfBoundsException when the index is invalid
     */
    public final int get(int index) {
        return elements[index];
    }

    public final boolean contains(int element) {
        return indexOf(element) >= 0;
    }

    public final boolean containsNegated(int element) {
        return indexOf(-element) >= 0;
    }

    /**
     * {@return whether this integer list contains any of the given integers}
     *
     * @param integers the integers
     */
    public final boolean containsAny(int... integers) {
        return Arrays.stream(integers).anyMatch(this::contains);
    }

    /**
     * {@return whether this integer list contains any of the given integers in
     * negated form}
     *
     * @param integers the integers
     */
    public final boolean containsAnyNegated(int... integers) {
        return Arrays.stream(integers).anyMatch(this::containsNegated);
    }

    /**
     * {@return whether this integer list contains all of the given integers}
     *
     * @param integers the integers
     */
    public final boolean containsAll(int... integers) {
        return Arrays.stream(integers).allMatch(this::contains);
    }

    /**
     * {@return whether this integer list contains all of the given integers in
     * negated form}
     *
     * @param integers the integers
     */
    public final boolean containsAllNegated(int... integers) {
        return Arrays.stream(integers).allMatch(this::containsNegated);
    }

    /**
     * {@return whether this integer list and the given integers are disjoint}
     *
     * @param integers the integers
     */
    public final boolean containsNone(int... integers) {
        return Arrays.stream(integers).noneMatch(this::contains);
    }
    /**
     * {@return whether this integer list and the given integers are disjoint}
     *
     * @param integers the integers
     */
    public final boolean containsNoneNegated(int... integers) {
        return Arrays.stream(integers).noneMatch(this::containsNegated);
    }

    /**
     * {@return whether this integer list contains any integer in the given integer
     * list}
     *
     * @param integers another integer list
     */
    public final boolean containsAny(IntegerList integers) {
        return containsAny(integers.elements);
    }

    /**
     * {@return whether this integer list contains any negated integer in the given
     * integer list}
     *
     * @param integers another integer list
     */
    public final boolean containsAnyNegated(IntegerList integers) {
        return containsAnyNegated(integers.elements);
    }

    /**
     * {@return whether this integer list contains all integers in the given integer
     * list}
     *
     * @param integers another integer list
     */
    public final boolean containsAll(IntegerList integers) {
        return containsAll(integers.elements);
    }

    /**
     * {@return whether this integer list contains all negated integers in the given
     * integer list}
     *
     * @param integers another integer list
     */
    public final boolean containsAllNegated(IntegerList integers) {
        return containsAllNegated(integers.elements);
    }

    /**
     * {@return whether this integer list contains no integer in the given integer
     * list}
     *
     * @param integers another integer list
     */
    public final boolean containsNone(IntegerList integers) {
        return containsNone(integers.elements);
    }

    /**
     * {@return whether this integer list contains no negated integer in the given
     * integer list}
     *
     * @param integers another integer list
     */
    public final boolean containsNoneNegated(IntegerList integers) {
        return containsNoneNegated(integers.elements);
    }

    /**
     * {@return the first index of the given integer in this integer list} To ensure
     * performance, no {@link Result} is created. Instead, a negative number is
     * returned when the integer is not contained.
     *
     * @param integer the integer
     */
    public int indexOf(int integer) {
        return IntStream.range(0, elements.length)
                .filter(i -> elements[i] == integer)
                .findFirst()
                .orElse(-1);
    }

    /**
     * {@return all indices of the given integer in this integer list} To ensure
     * performance, no {@link Result} is created. Instead, a negative number is
     * returned when the integer is not contained.
     *
     * @param integer the integer
     */
    public int[] indicesOf(int integer) {
        return IntStream.range(0, elements.length)
                .filter(i -> elements[i] == integer)
                .toArray();
    }

    /**
     * {@return the number of positive values in this integer list's integers}
     */
    public int countPositives() {
        return (int) Arrays.stream(elements).filter(integer -> integer > 0).count();
    }

    /**
     * {@return the number of negative values in this integer list's integers}
     */
    public int countNegatives() {
        return (int) Arrays.stream(elements).filter(integer -> integer < 0).count();
    }

    /**
     * {@return the number of non-zero values in this integer list's integers}
     */
    public int countNonZero() {
        return (int) Arrays.stream(elements).filter(integer -> integer != 0).count();
    }

    /**
     * {@return the number of integers in this integer list}
     */
    public final int size() {
        return elements.length;
    }

    /**
     * {@return whether this integer list is empty}
     */
    public final boolean isEmpty() {
        return elements.length == 0;
    }

    /**
     * {@return the number of elements in the given array that are also contained in
     * this integer list.}
     *
     * @param integers the integers
     */
    public final int sizeOfIntersection(int... integers) {
        return (int) Arrays.stream(integers).filter(this::contains).count();
    }

    /**
     * {@return the number of elements in the given array that are not contained in
     * this integer list.}
     *
     * @param integers the integers
     */
    public final int sizeOfDisjoint(int... integers) {
        return (int) Arrays.stream(integers).filter(i -> !contains(i)).count();
    }

    /**
     * {@return this integer list's elements as an IntStream}
     */
    public final IntStream stream() {
        return IntStream.of(elements);
    }

    /**
     * {@return this integer list's elements} The returned array must not be
     * modified.
     */
    public final int[] get() {
        return elements;
    }

    @Override
    public int hashCode() {
        return hashCodeValid ? hashCode : (hashCode = Arrays.hashCode(elements));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        return Arrays.equals(elements, ((IntegerList) obj).elements);
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }
}
