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

import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.CoverageStatistic;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.index.SampleBitIndex;
import java.util.List;

/**
 * Calculates statistics regarding t-wise feature coverage of a set of
 * solutions.
 *
 * @author Sebastian Krieter
 */
public class ComputeRelativeTWiseCoverage extends AComputeTWiseCoverage {
    public static final Dependency<BooleanAssignmentList> REFERENCE_SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);

    public ComputeRelativeTWiseCoverage(IComputation<BooleanAssignmentList> sample) {
        super(sample, Computations.of(new BooleanAssignmentList(null, 0)));
    }

    public ComputeRelativeTWiseCoverage(ComputeRelativeTWiseCoverage other) {
        super(other);
    }

    private BooleanAssignmentList referenceSample;
    private SampleBitIndex referenceIndex;

    @Override
    protected void initWithOriginalVariableMap(List<Object> dependencyList) {
        super.initWithOriginalVariableMap(dependencyList);
        referenceSample = REFERENCE_SAMPLE.get(dependencyList).toSolutionList();
    }

    @Override
    protected VariableMap getReferenceVariableMap() {
        return referenceSample.getVariableMap();
    }

    @Override
    protected void adaptToMergedVariableMap(VariableMap mergedVariableMap) {
        super.adaptToMergedVariableMap(mergedVariableMap);
        referenceSample = referenceSample.remap(mergedVariableMap);
    }

    @Override
    protected void adaptVariableMap(List<Object> dependencyList) {
        super.adaptVariableMap(dependencyList);
        referenceIndex = new SampleBitIndex(referenceSample);
    }

    @Override
    protected void countUncovered(int[] uncoveredInteraction, CoverageStatistic statistic) {
        if (referenceIndex.test(uncoveredInteraction)) {
            statistic.incNumberOfUncoveredElements();
        } else {
            statistic.incNumberOfInvalidElements();
        }
    }
}
