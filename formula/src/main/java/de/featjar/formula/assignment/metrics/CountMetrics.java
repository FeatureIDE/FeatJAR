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

import java.util.List;
import java.util.function.DoubleSupplier;

public class CountMetrics extends AAggregatableMetrics {

    private final ICountFunction function;

    public CountMetrics(ICountFunction function) {
        this.function = function;
    }

    public static List<ISampleMetric> getAllAggregates(ICountFunction function) {
        return new CountMetrics(function).getAllAggregates();
    }

    @Override
    protected double[] computeValues() {
        final int size = sample.size();
        final double[] values = new double[size];
        for (int i = 0; i < (size - 1); i++) {
            values[i] = function.compute(sample.get(i));
        }
        return values;
    }

    @Override
    public ISampleMetric getAggregate(String name, DoubleSupplier aggregate) {
        return new DoubleMetric(function.getName() + "_count_" + name, aggregate);
    }
}
