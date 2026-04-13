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

import java.util.stream.IntStream;

/**
 * Utility methods for int arrays.
 */
public final class Ints {

    private Ints() {}

    /**
     * Produces an array of size 2^t containing at each position the index at which a circular gray code of size t flips a bit.
     * @param t the size of the gray code
     * @return the index array
     */
    public static int[] grayCode(int t) {
        final int[] gray = IntStream.rangeClosed(1, 1 << t)
                .map(Integer::numberOfTrailingZeros)
                .toArray();
        gray[gray.length - 1]--;
        return gray;
    }

    /**
     * {@return a new int array with consecutive numbers from 1 to newSize without the numbers from the given list}
     * @param list the elements to remove
     * @param newSize the length of the new array
     */
    public static int[] invertedList(IntegerList list, final int newSize) {
        int[] invertedList = IntStream.rangeClosed(1, newSize).toArray();
        for (int e : list.elements) {
            invertedList[Math.abs(e) - 1] = 0;
        }
        return IntStream.of(invertedList).filter(i -> i != 0).toArray();
    }
}
