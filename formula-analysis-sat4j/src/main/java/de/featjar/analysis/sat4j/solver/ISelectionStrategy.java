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

import java.util.Random;

public interface ISelectionStrategy {

    enum NonParameterStrategy {
        ORIGINAL,
        NEGATIVE,
        POSITIVE,
        FAST_RANDOM,
    }

    enum Strategy {
        ORIGINAL,
        NEGATIVE,
        POSITIVE,
        FIXED,
        INVERSE_FIXED,
        FAST_RANDOM,
        UNIFORM_RANDOM,
    }

    Strategy strategy();

    class OriginalStrategy implements ISelectionStrategy {
        @Override
        public Strategy strategy() {
            return Strategy.ORIGINAL;
        }
    }

    class NegativeStrategy implements ISelectionStrategy {
        @Override
        public Strategy strategy() {
            return Strategy.NEGATIVE;
        }
    }

    class PositiveStrategy implements ISelectionStrategy {
        @Override
        public Strategy strategy() {
            return Strategy.POSITIVE;
        }
    }

    class FixedStrategy implements ISelectionStrategy {
        private final int[] model;

        public FixedStrategy(int[] model) {
            this.model = model;
        }

        @Override
        public Strategy strategy() {
            return Strategy.FIXED;
        }

        public int[] getModel() {
            return model;
        }
    }

    class InverseFixedStrategy implements ISelectionStrategy {
        private final int[] model;

        public InverseFixedStrategy(int[] model) {
            this.model = model;
        }

        @Override
        public Strategy strategy() {
            return Strategy.INVERSE_FIXED;
        }

        public int[] getModel() {
            return model;
        }
    }

    class FastRandomStrategy implements ISelectionStrategy {
        private final Random random;

        public FastRandomStrategy(Random random) {
            this.random = random;
        }

        @Override
        public Strategy strategy() {
            return Strategy.FAST_RANDOM;
        }

        public Random getRandom() {
            return random;
        }
    }

    class UniformRandomStrategy implements ISelectionStrategy {
        private final ALiteralDistribution dist;

        public UniformRandomStrategy(ALiteralDistribution dist) {
            this.dist = dist;
        }

        @Override
        public Strategy strategy() {
            return Strategy.UNIFORM_RANDOM;
        }

        public ALiteralDistribution getDist() {
            return dist;
        }
    }

    static OriginalStrategy original() {
        return new OriginalStrategy();
    }

    static NegativeStrategy negative() {
        return new NegativeStrategy();
    }

    static PositiveStrategy positive() {
        return new PositiveStrategy();
    }

    static FastRandomStrategy random(Random random) {
        return new FastRandomStrategy(random);
    }

    static FastRandomStrategy random() {
        return new FastRandomStrategy(new Random());
    }

    static FixedStrategy fixed(int[] model) {
        return new FixedStrategy(model);
    }

    static InverseFixedStrategy inverse(int[] model) {
        return new InverseFixedStrategy(model);
    }

    static UniformRandomStrategy uniform(ALiteralDistribution dist) {
        return new UniformRandomStrategy(dist);
    }
}
