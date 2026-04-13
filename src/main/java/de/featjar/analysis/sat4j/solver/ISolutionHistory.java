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

import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.*;

public interface ISolutionHistory extends Iterable<BooleanSolution> {
    // todo: store int[] instead of creating new solution instances
    List<BooleanSolution> getSolutionHistory();

    Result<BooleanSolution> getLastSolution();

    void setLastSolution(BooleanSolution solution);

    void addNewSolution(BooleanSolution solution);

    void clear();

    @Override
    default Iterator<BooleanSolution> iterator() {
        return getSolutionHistory().iterator();
    }

    class RememberLast implements ISolutionHistory {
        protected BooleanSolution lastSolution;

        @Override
        public List<BooleanSolution> getSolutionHistory() {
            return List.of(lastSolution);
        }

        @Override
        public Result<BooleanSolution> getLastSolution() {
            return Result.ofNullable(lastSolution);
        }

        @Override
        public void setLastSolution(BooleanSolution solution) {
            this.lastSolution = solution;
        }

        @Override
        public void addNewSolution(BooleanSolution solution) {
            setLastSolution(solution);
        }

        @Override
        public void clear() {
            lastSolution = null;
        }
    }

    class RememberUpTo extends RememberLast {
        protected int limit;
        protected final LinkedList<BooleanSolution> solutionHistory = new LinkedList<>();

        public RememberUpTo(int limit) {
            if (limit < 1) throw new IllegalArgumentException();
            this.limit = limit;
        }

        @Override
        public List<BooleanSolution> getSolutionHistory() {
            return solutionHistory;
        }

        @Override
        public void addNewSolution(BooleanSolution solution) {
            super.addNewSolution(solution);
            solutionHistory.addFirst(solution);
            if (solutionHistory.size() > limit) {
                solutionHistory.removeLast();
            }
        }

        @Override
        public void clear() {
            super.clear();
            solutionHistory.clear();
        }
    }
}
