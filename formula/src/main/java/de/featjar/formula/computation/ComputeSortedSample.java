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
package de.featjar.formula.computation;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.ValuedBooleanAssignment;
import de.featjar.formula.assignment.ValuedBooleanAssignmentList;
import de.featjar.formula.index.SampleBitIndex;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * Sorts a sample by a ranked list of assignments.
 *
 * @author Sebastian Krieter
 */
public class ComputeSortedSample extends AComputation<BooleanAssignmentList> {

    private static class SortedListElement implements Comparable<SortedListElement> {
        private final BooleanAssignment booleanAssignment;
        private long rank;

        public SortedListElement(BooleanAssignment booleanAssignment, long score) {
            this.booleanAssignment = booleanAssignment;
            this.rank = score;
        }

        @Override
        public int compareTo(SortedListElement o) {
            return (rank < o.rank) ? -1 : ((rank == o.rank) ? 0 : 1);
        }
    }

    public static final Dependency<BooleanAssignmentList> SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<ValuedBooleanAssignmentList> SORTING_VALUES =
            Dependency.newDependency(ValuedBooleanAssignmentList.class);

    public ComputeSortedSample(IComputation<BooleanAssignmentList> sample) {
        super(sample, Computations.of(new ValuedBooleanAssignmentList()));
    }

    @Override
    public final Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList sample = SAMPLE.get(dependencyList);
        ValuedBooleanAssignmentList sortingValues = SORTING_VALUES.get(dependencyList);

        progress.setTotalSteps(sortingValues.size());

        SampleBitIndex index = new SampleBitIndex(sample);
        long maxScore = sortingValues.stream()
                .mapToLong(ValuedBooleanAssignment::getValue)
                .max()
                .orElse(0);
        List<SortedListElement> sortedList = new ArrayList<>();
        for (BooleanAssignment a : sample) {
            sortedList.add(new SortedListElement(a, 0));
        }

        for (ValuedBooleanAssignment a : sortingValues) {
            long weightedDelta = maxScore - a.getValue();
            BitSet bs = index.getBitSet(a.get());
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                SortedListElement sortedListElement = sortedList.get(i);
                sortedListElement.rank = sortedListElement.rank + weightedDelta;
                if (i == Integer.MAX_VALUE) {
                    break;
                }
            }
            progress.incrementCurrentStep();
        }

        Collections.sort(sortedList);

        return Result.of(new BooleanAssignmentList(
                sample.getVariableMap(), sortedList.stream().map(e -> e.booleanAssignment)));
    }
}
