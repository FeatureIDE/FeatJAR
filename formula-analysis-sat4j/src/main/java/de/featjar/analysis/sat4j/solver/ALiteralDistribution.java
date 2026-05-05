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

import java.io.Serializable;
import java.util.Random;

/**
 * Uses a sample of configurations to achieve a phase selection that corresponds
 * to a uniform distribution of configurations in the configuration space.
 *
 * @author Sebastian Krieter
 */
public abstract class ALiteralDistribution implements Serializable {

    private static final long serialVersionUID = 5431855598362532000L;

    protected Random random = new Random(0);

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public abstract void reset();

    public abstract void set(int literal);

    public abstract void unset(int var);

    public abstract int getRandomLiteral(int var);
}
