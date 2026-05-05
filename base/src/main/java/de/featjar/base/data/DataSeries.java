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

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * Utility for data series.
 *
 * @author Valentin Laubsch
 * @author Sebastian Krieter
 */
public final class DataSeries {
    private DataSeries() {}

    /** Returns {@code true} if the given object is a numeric series:
     * 		primitive number array or List<Number> (empty list counts as series).
     * @param potentialSeries the object in question
     * @return {@code true} if the given object is a numeric series:, {@code false} otherwise
     */
    public static boolean isSeries(Object potentialSeries) {
        if (potentialSeries == null) return false;
        if (potentialSeries instanceof double[]
                || potentialSeries instanceof int[]
                || potentialSeries instanceof long[]
                || potentialSeries instanceof float[]) {
            return true;
        }
        if (potentialSeries instanceof List<?>) {
            List<?> l = (List<?>) potentialSeries;
            if (l.isEmpty()) return true; // treat empty as a valid (empty) series
            for (Object e : l) {
                if (!(e instanceof Number)) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Converts any supported series (double[]/int[]/long[]/float[]/List<Number>) to a double[].
     *
     * @param potentialSeries the object to treat as a series
     * @return a double array containing cast values or an empty array, if unknown data type.
     */
    public static double[] toSeries(Object potentialSeries) {
        if (potentialSeries instanceof double[]) return (double[]) potentialSeries; // no copy on purpose
        if (potentialSeries instanceof int[]) {
            int[] a = (int[]) potentialSeries;
            double[] d = new double[a.length];
            for (int i = 0; i < a.length; i++) d[i] = a[i];
            return d;
        }
        if (potentialSeries instanceof long[]) {
            long[] a = (long[]) potentialSeries;
            double[] d = new double[a.length];
            for (int i = 0; i < a.length; i++) d[i] = a[i];
            return d;
        }
        if (potentialSeries instanceof float[]) {
            float[] a = (float[]) potentialSeries;
            double[] d = new double[a.length];
            for (int i = 0; i < a.length; i++) d[i] = a[i];
            return d;
        }
        if (potentialSeries instanceof List<?>) {
            List<?> l = (List<?>) potentialSeries;
            double[] d = new double[l.size()];
            for (int i = 0; i < l.size(); i++) {
                Object e = l.get(i);
                d[i] = (e instanceof Number) ? ((Number) e).doubleValue() : Double.NaN;
            }
            return d;
        }
        return new double[0];
    }

    public static String toReadableString(Object v) {
        if (v instanceof int[]) return Arrays.toString((int[]) v);
        if (v instanceof double[]) return Arrays.toString((double[]) v);
        if (v instanceof long[]) return Arrays.toString((long[]) v);
        if (v instanceof float[]) return Arrays.toString((float[]) v);
        if (v instanceof List<?>) return toReadableString(toSeries(v));
        return String.valueOf(v);
    }

    public static double avg(double[] a) {
        return DoubleStream.of(a).average().orElse(0);
    }

    public static double median(double[] a) {
        if (a.length == 0) return 0.0;
        double[] c = Arrays.copyOf(a, a.length);
        Arrays.sort(c);
        int n = c.length;
        int half = n / 2;
        return (n % 2 == 1) ? c[half] : (c[half - 1] + c[half]) / 2.0;
    }

    public static double min(double[] a) {
        return DoubleStream.of(a).min().orElse(0);
    }

    public static double max(double[] a) {
        return DoubleStream.of(a).max().orElse(0);
    }
}
