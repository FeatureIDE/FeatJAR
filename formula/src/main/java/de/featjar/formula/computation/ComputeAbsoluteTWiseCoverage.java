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

import de.featjar.base.computation.IComputation;
import de.featjar.formula.CoverageStatistic;
import de.featjar.formula.assignment.BooleanAssignmentList;

/**
 * Calculates statistics regarding t-wise feature coverage of a set of
 * solutions.
 *
 * @author Sebastian Krieter
 */
public class ComputeAbsoluteTWiseCoverage extends AComputeTWiseCoverage {
    public ComputeAbsoluteTWiseCoverage(IComputation<BooleanAssignmentList> sample) {
        super(sample);
    }

    public ComputeAbsoluteTWiseCoverage(ComputeAbsoluteTWiseCoverage other) {
        super(other);
    }

    @Override
    protected void countUncovered(int[] uncoveredInteraction, CoverageStatistic statistic) {
        statistic.incNumberOfUncoveredElements();
    }
}
