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

import org.sat4j.specs.ISolver;

/**
 * Modified variable order for {@link ISolver}.<br>
 * Uses the {@link UniformRandomSelectionStrategy}.
 *
 * @author Sebastian Krieter
 */
public class FixedOrderHeap2 extends FixedOrderHeap {

    private static final long serialVersionUID = 1L;

    private final UniformRandomSelectionStrategy selectionStrategy;

    public FixedOrderHeap2(UniformRandomSelectionStrategy strategy, int[] order) {
        super(strategy, order);
        selectionStrategy = strategy;
    }

    @Override
    public void undo(int x) {
        super.undo(x);
        selectionStrategy.undo(x);
    }
}
