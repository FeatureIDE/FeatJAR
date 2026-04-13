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
/*

* Copyright (C) 2023 Sebastian Krieter
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
package de.featjar.analysis.sat4j.solver.strategy;

import static org.sat4j.core.LiteralsUtils.negLit;
import static org.sat4j.core.LiteralsUtils.posLit;

import java.util.Random;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;

/**
 * Random phase selection.
 *
 * @author Sebastian Krieter
 */
public class RandomSelectionStrategy implements IPhaseSelectionStrategy {

    private static final long serialVersionUID = 1L;

    public final Random RAND = new Random(123456789);

    @Override
    public void assignLiteral(int p) {}

    @Override
    public void init(int nlength) {}

    @Override
    public void init(int var, int p) {}

    @Override
    public int select(int var) {
        return RAND.nextBoolean() ? posLit(var) : negLit(var);
    }

    @Override
    public void updateVar(int p) {}

    @Override
    public void updateVarAtDecisionLevel(int q) {}

    @Override
    public String toString() {
        return "random phase selection";
    }
}
