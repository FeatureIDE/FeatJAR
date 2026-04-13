/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j.solver;

import de.featjar.formula.assignment.BooleanAssignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Uses a sample of configurations to achieve a phase selection that corresponds
 * to a uniform distribution of configurations in the configuration space.
 *
 * @author Sebastian Krieter
 */
public class SampleDistribution extends ALiteralDistribution {

    private static final long serialVersionUID = -3902620512089122369L;

    private final ArrayList<BooleanAssignment> samples = new ArrayList<>();
    private int startIndex;

    private final byte[] model;

    public SampleDistribution(List<BooleanAssignment> sample) {
        samples.addAll(sample);
        startIndex = 0;
        model = new byte[sample.get(0).size()];
    }

    @Override
    public void reset() {
        Arrays.fill(model, (byte) 0);
        startIndex = 0;
    }

    @Override
    public void unset(int var) {
        final int index = var - 1;
        final byte sign = model[index];
        if (sign != 0) {
            model[index] = 0;
            final int literal = sign > 0 ? var : -var;
            for (int i = 0; i < startIndex; i++) {
                if (samples.get(i).get()[index] == -literal) {
                    Collections.swap(samples, i--, --startIndex);
                }
            }
        }
    }

    @Override
    public void set(int literal) {
        final int index = Math.abs(literal) - 1;
        if (model[index] == 0) {
            model[index] = (byte) (literal > 0 ? 1 : -1);
            for (int i = startIndex; i < samples.size(); i++) {
                if (samples.get(i).get()[index] == -literal) {
                    Collections.swap(samples, i, startIndex++);
                }
            }
        }
    }

    @Override
    public int getRandomLiteral(int var) {
        if (samples.size() > (startIndex + 1)) {
            return (random.nextInt((samples.size() - startIndex) + 2) < (getPositiveCount(var - 1) + 1)) ? var : -var;
        } else {
            return random.nextBoolean() ? var : -var;
        }
    }

    public int getPositiveCount(int index) {
        int sum = 0;
        for (final BooleanAssignment l : samples.subList(startIndex, samples.size())) {
            sum += (~l.get()[index]) >>> 31;
        }
        return sum;
    }

    public int getTotalCount() {
        return samples.size() + startIndex;
    }
}
