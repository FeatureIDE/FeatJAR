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

/**
 * Adjacency list implementation based on arrays. Intended to use for faster
 * traversion.
 *
 * @author Sebastian Krieter
 */
public class ModalImplicationGraph {

    final int size;

    final int[] core;

    final int[][] strong;

    final int[][] clauseIndices;
    final int[] clauses;

    // TODO join both index arrays
    final int[][] clauseLengthsIndices;
    final int[] clauseLengths;

    public static int getVertexIndex(int literal) {
        return literal < 0 ? (-literal - 1) << 1 : ((literal - 1) << 1) + 1;
    }

    public ModalImplicationGraph(
            int size,
            int[] core,
            int[][] strong,
            int[][] clauseIndices,
            int[] clauses,
            int[][] clauseLengthIndices,
            int[] clauseLength) {
        this.size = size;
        this.core = core;
        this.strong = strong;
        this.clauseIndices = clauseIndices;
        this.clauses = clauses;
        this.clauseLengthsIndices = clauseLengthIndices;
        this.clauseLengths = clauseLength;
    }

    public int[] getCore() {
        return core;
    }

    public int[][] getStrongEdges() {
        return strong;
    }

    public int size() {
        return size;
    }
}
