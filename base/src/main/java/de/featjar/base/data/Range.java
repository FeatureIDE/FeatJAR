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

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * A range of positive integers limited by a lower and upper bound.
 * Both bounds may be open, in which case they are not checked.
 *
 * @author Elias Kuiter
 */
public class Range implements Function<Integer, Boolean>, Cloneable {
    public static final int OPEN = -1;

    // TODO store as one int
    protected int lowerBound;
    protected int upperBound;

    protected Range(int lowerBound, int upperBound) {
        checkBounds(lowerBound, upperBound);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    protected static void checkBounds(int lowerBound, int upperBound) {
        if ((lowerBound < OPEN) || (upperBound < OPEN)) {
            throw new IllegalArgumentException(
                    String.format("invalid bounds %d, %d, negative values are not allowed", lowerBound, upperBound));
        }
        if (upperBound != OPEN && lowerBound > upperBound) {
            throw new IllegalArgumentException(
                    String.format("invalid bounds %d, %d, lower bound > upper bound", lowerBound, upperBound));
        }
    }

    public static Range copy(Range range) {
        return new Range(range.lowerBound, range.upperBound);
    }

    public static Range of(int lowerBound, int upperBound) {
        return new Range(lowerBound, upperBound);
    }

    public static Range open() {
        return new Range(OPEN, OPEN);
    }

    public static Range atLeast(int minimum) {
        return new Range(minimum, OPEN);
    }

    public static Range atMost(int maximum) {
        return new Range(OPEN, maximum);
    }

    public static Range exactly(int bound) {
        return new Range(bound, bound);
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public boolean isLowerBoundOpen() {
        return lowerBound == OPEN;
    }

    public void setLowerBound(Integer lowerBound) {
        checkBounds(lowerBound, upperBound);
        this.lowerBound = lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public boolean isUpperBoundOpen() {
        return upperBound == OPEN;
    }

    public void setUpperBound(int upperBound) {
        checkBounds(lowerBound, upperBound);
        this.upperBound = upperBound;
    }

    public boolean is(int lowerBound, int upperBound) {
        return this.lowerBound == lowerBound && this.upperBound == upperBound;
    }

    public boolean is(Range range) {
        return this.lowerBound == range.lowerBound && this.upperBound == range.upperBound;
    }

    public boolean isOpen() {
        return isLowerBoundOpen() || isUpperBoundOpen();
    }

    public void setBounds(int lowerBound, int upperBound) {
        checkBounds(lowerBound, upperBound);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public void setBounds(Range range) {
        this.lowerBound = range.lowerBound;
        this.upperBound = range.upperBound;
    }

    public boolean testLowerBound(int integer) {
        return lowerBound == OPEN || lowerBound <= integer;
    }

    public boolean testUpperBound(int integer) {
        return upperBound == OPEN || upperBound >= integer;
    }

    public boolean test(int integer) {
        return testLowerBound(integer) && testUpperBound(integer);
    }

    /**
     * {@return a finite integer stream for this range}
     */
    public Result<IntStream> stream() {
        return lowerBound != OPEN && upperBound != OPEN
                ? Result.of(IntStream.rangeClosed(lowerBound, upperBound))
                : Result.empty();
    }

    @Override
    public Boolean apply(Integer integer) {
        return test(integer);
    }

    @Override
    public String toString() {
        return String.format("Range[%d, %d]", lowerBound, upperBound);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Range range = (Range) o;
        return lowerBound == range.lowerBound && upperBound == range.upperBound;
    }

    @Override
    public Range clone() {
        return copy(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowerBound, upperBound);
    }

    public int getLargerBound() {
        return upperBound != OPEN ? upperBound : lowerBound;
    }
}
