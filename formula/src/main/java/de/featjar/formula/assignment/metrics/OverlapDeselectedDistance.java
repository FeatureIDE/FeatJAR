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

/**
 * Computes the Overlap distance between two literal arrays. Considers only
 * negative literals.
 *
 * @author Sebastian Krieter
 */
public class OverlapDeselectedDistance implements IDistanceFunction {

    @Override
    public double computeDistance(final int[] literals1, final int[] literals2) {
        double sum = 0;
        double sumA = 0;
        double sumB = 0;
        for (int k = 0; k < literals1.length; k++) {
            final int a = literals1[k] >>> (Integer.SIZE - 1);
            final int b = literals2[k] >>> (Integer.SIZE - 1);
            sumA += a;
            sumB += b;
            sum += a & b;
        }
        final double similarity = sum / Math.min(sumA, sumB);
        return 1 - similarity;
    }

    @Override
    public String getName() {
        return "OverlapDeselected";
    }
}
