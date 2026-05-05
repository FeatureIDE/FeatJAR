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
package de.featjar.formula.assignment.metrics;

import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.List;

/**
 * Calculates a certain coverage for a given sample.
 *
 * @author Sebastian Krieter
 */
public interface ISampleMetric {

    double get(BooleanAssignmentList sample);

    default double[] get(List<BooleanAssignmentList> sampleList) {
        final double[] values = new double[sampleList.size()];
        int index = 0;
        for (final BooleanAssignmentList sample : sampleList) {
            values[index++] = get(sample);
        }
        return values;
    }

    String getName();
}
