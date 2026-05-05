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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.data.combination.CombinationStream;
import de.featjar.base.data.combination.ISelection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class SingleLexicographicIteratorTest {

    @Test
    void streamSequential1() {
        int[] items = IntStream.range(0, 10).toArray();
        List<String> sSet = CombinationStream.stream(items, 1) //
                .map(c -> Arrays.toString(c.select())) //
                .collect(Collectors.toList());

        int[] expectedCombination = new int[1];
        Iterator<String> iterator = sSet.iterator();
        for (int i = 0; i < items.length; i++) {
            expectedCombination[0] = i;
            assertEquals(Arrays.toString(expectedCombination), iterator.next());
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    void streamSequential2() {
        int[] items = IntStream.range(0, 10).toArray();
        List<String> sSet = CombinationStream.stream(items, 2) //
                .map(c -> Arrays.toString(c.select())) //
                .collect(Collectors.toList());

        int[] expectedCombination = new int[2];
        Iterator<String> iterator = sSet.iterator();
        for (int i = 1; i < items.length; i++) {
            expectedCombination[1] = i;
            for (int j = 0; j < i; j++) {
                expectedCombination[0] = j;
                assertEquals(Arrays.toString(expectedCombination), iterator.next());
            }
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    void streamSequential3() {
        int[] items = IntStream.range(0, 10).toArray();
        List<int[]> sSet = CombinationStream.stream(items, 3) //
                .map(c -> c.createSelection()) //
                .collect(Collectors.toList());

        final int[] gray = Ints.grayCode(3);
        int[] expectedCombination = new int[3];
        Iterator<int[]> iterator = sSet.iterator();
        for (int i = 2; i < items.length; i++) {
            expectedCombination[2] = i;
            for (int j = 1; j < i; j++) {
                expectedCombination[1] = j;
                for (int k = 0; k < j; k++) {
                    expectedCombination[0] = k;
                    int[] next = iterator.next();
                    assertTrue(
                            Arrays.equals(expectedCombination, next),
                            Arrays.toString(expectedCombination) + " <-> " + Arrays.toString(next));
                    for (int g : gray) {
                        next[g] = -next[g];
                    }
                    assertTrue(
                            Arrays.equals(expectedCombination, next),
                            Arrays.toString(expectedCombination) + " <-> " + Arrays.toString(next));
                }
            }
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    void parallelAndSequenbtialContainSameTuples1() {
        streamParallelAndSequential(1, 20);
    }

    @Test
    void parallelAndSequenbtialContainSameTuples2() {
        streamParallelAndSequential(2, 20);
    }

    @Test
    void parallelAndSequenbtialContainSameTuples3() {
        streamParallelAndSequential(3, 20);
    }

    @Test
    void parallelStreamContainsAllTuples1() {
        streamParallel(1, 20);
    }

    @Test
    void parallelStreamContainsAllTuples2() {
        streamParallel(2, 20);
    }

    @Test
    void parallelStreamContainsAllTuples3() {
        streamParallel(3, 20);
    }

    private void streamParallel(int k, int n) {
        int[] items = IntStream.range(0, n).toArray();
        int size = (int) BinomialCalculator.computeBinomial(n, k);
        int[] counts = new int[size];
        Random random = new Random(1);
        CombinationStream.parallelStream(items, k).mapToLong(c -> c.index()).forEach(c -> {
            try {
                Thread.sleep((long) (20 * random.nextDouble()));
            } catch (Exception e) {
            }
            synchronized (counts) {
                counts[Math.toIntExact(c)]++;
            }
        });
        for (int i = 0; i < counts.length; i++) {
            assertEquals(1, counts[i], String.valueOf(i));
        }
    }

    private void streamParallelAndSequential(int t, int n) {
        int[] items = IntStream.range(0, n).toArray();
        List<String> pSet = CombinationStream.parallelStream(items, t)
                .map(ISelection::toString)
                .collect(Collectors.toList());
        List<String> sSet = CombinationStream.stream(items, t) //
                .map(ISelection::toString) //
                .collect(Collectors.toList());

        assertEquals(sSet.size(), pSet.size(), sSet + "\n!=\n" + pSet);
        assertTrue(new HashSet<>(pSet).containsAll(sSet), sSet + "\n!=\n" + pSet);
        assertTrue(new HashSet<>(sSet).containsAll(pSet), sSet + "\n!=\n" + pSet);
    }
}
