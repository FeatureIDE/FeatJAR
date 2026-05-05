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

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.ValuedBooleanAssignment;
import de.featjar.formula.assignment.ValuedBooleanAssignmentList;
import de.featjar.formula.index.SampleBitIndex;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sorts a sample by a ranked list of assignments.
 *
 * @author Sebastian Krieter
 */
public class ComputeRankedSample extends AComputation<BooleanAssignmentList> {

    public static final Dependency<BooleanAssignmentList> SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<ValuedBooleanAssignmentList> RANK_VALUES =
            Dependency.newDependency(ValuedBooleanAssignmentList.class);
    public static final Dependency<Boolean> OPTIMIZE = Dependency.newDependency(Boolean.class);

    public ComputeRankedSample(IComputation<BooleanAssignmentList> sample) {
        super(sample, Computations.of(new ValuedBooleanAssignmentList()), Computations.of(Boolean.FALSE));
    }

    @Override
    public final Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList sample = SAMPLE.get(dependencyList);
        ValuedBooleanAssignmentList rankValues = RANK_VALUES.get(dependencyList);
        boolean optimize = OPTIMIZE.get(dependencyList);

        progress.setTotalSteps(rankValues.size());

        SampleBitIndex index = new SampleBitIndex(sample);
        Collections.sort(rankValues, Comparator.comparing(ValuedBooleanAssignment::getValue));
        boolean[] used = new boolean[sample.size()];
        BooleanAssignmentList sortedList = new BooleanAssignmentList(sample.getVariableMap());

        int size = rankValues.size();
        for (int i = 0; i < size; i++) {
            BitSet bs = index.getBitSet(rankValues.get(i).get());
            int k = bs.nextSetBit(0);
            if (k < 0) {
                FeatJAR.log().warning("Combination not found in sample: ", rankValues.get(i));
                progress.incrementCurrentStep();
            } else {
                if (optimize) {
                    i++;
                    for (; i < size; i++) {
                        index.updateBitSet(bs, rankValues.get(i).get());
                        int l = bs.nextSetBit(k);
                        progress.incrementCurrentStep();
                        if (l >= 0) {
                            k = l;
                        } else {
                            i--;
                            break;
                        }
                    }
                }
                if (!used[k]) {
                    sortedList.add(sample.get(k));
                    used[k] = true;
                }
            }
            progress.incrementCurrentStep();
        }

        for (int i = 0; i < used.length; i++) {
            if (!used[i]) {
                sortedList.add(sample.get(i));
            }
        }

        return Result.of(sortedList);
    }
}
