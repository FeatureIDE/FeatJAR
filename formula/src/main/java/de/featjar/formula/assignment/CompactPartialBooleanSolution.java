/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.assignment;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Boolean assignment based on the {@link BitSet} data structure.
 *
 * @author Sebastian Krieter
 */
public class CompactPartialBooleanSolution implements ISolution<Integer, Boolean> {

    private BitSet assignment;
    private BitSet defined;
    private int size;

    public CompactPartialBooleanSolution(int size) {
        this.size = size;
        assignment = new BitSet(size);
        defined = new BitSet(size);
    }

    public CompactPartialBooleanSolution(int size, BooleanAssignment assignment) {
        this.size = size;
        this.assignment = new BitSet(size);
        this.defined = new BitSet(size);
        for (int literal : assignment.get()) {
            if (literal != 0) {
                final int index = Math.abs(literal) - 1;
                this.assignment.set(index, literal > 0);
                this.defined.set(index);
            }
        }
    }

    public CompactPartialBooleanSolution(CompactPartialBooleanSolution solution) {
        size = solution.size();
        assignment = new BitSet(size);
        defined = new BitSet(size);
        assignment.xor(solution.assignment);
        defined.xor(solution.defined);
    }

    public int indexOf(int literal) {
        final int index = Math.abs(literal) - 1;
        return literal != 0 && index < size && defined.get(index) && assignment.get(index) == literal > 0 ? index : -1;
    }

    public int indexOfVariable(int variable) {
        if (variable <= 0) {
            throw new IllegalArgumentException(String.format("Variable ID must be larger than 0. Was %d", variable));
        }
        final int index = variable - 1;
        return index < size && defined.get(index) ? index : -1;
    }

    @Override
    public String toString() {
        return String.format("CompactBooleanAssignment[%s]", assignment.toString());
    }

    @Override
    public CompactPartialBooleanSolution clone() {
        return new CompactPartialBooleanSolution(this);
    }

    /**
     * Set all elements that are currently 0 to their index + 1.
     * @return this solution, does not return a copy
     */
    public CompactPartialBooleanSolution setUndefined() {
        assignment.and(defined);
        defined.set(0, size);
        return this;
    }

    /**
     * Set all elements that are currently 0 to -(their index + 1).
     * @return this solution, does not return a copy
     */
    public CompactPartialBooleanSolution unsetUndefined() {
        defined.flip(0, size);
        assignment.or(defined);
        defined.set(0, size);
        return this;
    }

    @Override
    public Map<Integer, Boolean> getAll() {
        Map<Integer, Boolean> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            if (defined.get(i)) {
                map.put(i + 1, assignment.get(i));
            }
        }
        return map;
    }

    @Override
    public String print() {
        return getAll().toString();
    }

    @Override
    public BooleanAssignment toAssignment() {
        int[] booleanAssignment = new int[defined.cardinality()];
        int index = 0;
        for (int i = 0; i < size; i++) {
            if (defined.get(i)) {
                booleanAssignment[index++] = assignment.get(i) ? i + 1 : -(i + 1);
            }
        }
        return new BooleanAssignment(booleanAssignment);
    }

    @Override
    public IClause<Integer, Boolean> toClause() {
        int[] booleanAssignment = new int[defined.cardinality()];
        int index = 0;
        for (int i = 0; i < size; i++) {
            if (defined.get(i)) {
                booleanAssignment[index++] = assignment.get(i) ? i + 1 : -(i + 1);
            }
        }
        return new BooleanClause(booleanAssignment);
    }

    @Override
    public ISolution<Integer, Boolean> toSolution() {
        int[] booleanAssignment = new int[size];
        for (int i = 0; i < size; i++) {
            booleanAssignment[i] = defined.get(i) ? assignment.get(i) ? i + 1 : -(i + 1) : 0;
        }
        return new BooleanSolution(booleanAssignment, false);
    }
}
