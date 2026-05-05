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
package de.featjar.analysis.sat4j.solver.strategy;

import static org.sat4j.core.LiteralsUtils.negLit;
import static org.sat4j.core.LiteralsUtils.posLit;
import static org.sat4j.core.LiteralsUtils.var;

import org.sat4j.minisat.core.IPhaseSelectionStrategy;

public class FixedLiteralSelectionStrategy implements IPhaseSelectionStrategy {

    private static final long serialVersionUID = 1L;

    protected final int[] model;

    protected final int[] phase;

    public FixedLiteralSelectionStrategy(int[] model) {
        this.model = model;
        phase = new int[model.length + 1];
        reset(model.length + 1);
    }

    @Override
    public void updateVar(int p) {}

    @Override
    public void assignLiteral(int p) {
        final int var = var(p);
        if (model[var - 1] == 0) {
            phase[var] = p;
        }
    }

    @Override
    public void updateVarAtDecisionLevel(int q) {}

    @Override
    public void init(int nlength) {
        reset(nlength);
    }

    protected void reset(int nlength) {
        for (int i = 1; i < nlength; i++) {
            phase[i] = model[i - 1] > 0 ? posLit(i) : negLit(i);
        }
    }

    @Override
    public void init(int var, int p) {}

    @Override
    public int select(int var) {
        return phase[var];
    }
}
