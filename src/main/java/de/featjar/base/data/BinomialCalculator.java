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

/**
 * Computes binomial coefficients and factorial.
 *
 * @author Sebastian Krieter
 */
// TODO Improve performance and memory efficiency
public class BinomialCalculator {

    private final long[][] binomial;
    private final long[] factorial;

    private final int maxK, maxN;

    public BinomialCalculator(int maxK, int maxN) {
        this.maxK = maxK;
        this.maxN = maxN;
        binomial = new long[maxN + 1][maxK + 1];
        factorial = new long[maxK + 1];
    }

    public long factorial(int k) {
        synchronized (factorial) {
            long f = factorial[k];
            if (f == 0) {
                f = 1;
                for (int i = 2; i <= k; i++) {
                    f *= i;
                }
                factorial[k] = f;
            }
            return f;
        }
    }

    public long binomial() {
        return binomial(maxN, maxK);
    }

    public long binomial(int n, int k) {
        synchronized (binomial) {
            long b = binomial[n][k];
            if (b == 0) {
                b = computeBinomial(n, k);
                binomial[n][k] = b;
            }
            return b;
        }
    }

    public int[] combination(long index, int[] combination) {
        return combination(index, combination, 0);
    }

    public int[] combination(long index, int[] combination, int offset) {
        for (int i = maxK; i > 0; i--) {
            if (index <= 0) {
                combination[i - 1 + offset] = i - 1;
            } else {
                final int p = (int) Math.ceil(Math.pow(index * factorial(i), (1.0 / i)));
                for (int j = p; j <= maxN; j++) {
                    if (binomial(j, i) > index) {
                        combination[i - 1] = j - 1;
                        index -= binomial(j - 1, i);
                        break;
                    }
                }
            }
        }
        return combination;
    }

    public long index(int[] c) {
        long index = 0;
        for (int i = 0; i < maxK; i++) {
            index += binomial(c[i], i + 1);
        }
        return index;
    }

    public static long computeBinomial(int n, int k) {
        if (n < k) {
            return 0;
        }
        if (k > (n - k)) {
            k = n - k;
        }

        long b = k == 0 ? 1 : n;
        for (int i = 1; i < k; i++) {
            b = Math.multiplyExact(b, n - i) / (i + 1);
        }
        return b;
    }

    public static long computeFactorial(int k) {
        long f = 1;
        for (int i = 2; i <= k; i++) {
            f = Math.multiplyExact(f, i);
        }
        return f;
    }
}
