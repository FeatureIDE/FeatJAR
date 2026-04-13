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
package de.featjar.analysis.sat4j.slice;

import java.util.Iterator;

/**
 * Returns features dependent on the current clauses in the formula.
 *
 * @author Sebastian Krieter
 */
public class MinimumClauseHeuristic implements Iterator<DirtyFeature> {

    protected final DirtyFeature[] map;
    protected final int maxIndex;
    protected int curIndex = 0;
    protected int realCurIndex = 0;

    public MinimumClauseHeuristic(DirtyFeature[] map, int length) {
        this.map = map;
        maxIndex = length;
    }

    @Override
    public boolean hasNext() {
        return maxIndex != curIndex;
    }

    @Override
    public DirtyFeature next() {
        if (!hasNext()) {
            return null;
        }
        realCurIndex = getNextIndex();
        final DirtyFeature ret = map[realCurIndex];
        map[realCurIndex] = null;
        curIndex++;
        return ret;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return maxIndex - curIndex;
    }

    protected int getNextIndex() {
        DirtyFeature smallestFeature = map[1];
        int minIndex = 1;
        for (int i = 2; i < map.length; i++) {
            final DirtyFeature next = map[i];
            if ((smallestFeature == null)
                    || ((next != null) && ((smallestFeature.getClauseCount() - next.getClauseCount()) > 0))) {
                smallestFeature = next;
                minIndex = i;
            }
        }
        return minIndex;
    }
}
