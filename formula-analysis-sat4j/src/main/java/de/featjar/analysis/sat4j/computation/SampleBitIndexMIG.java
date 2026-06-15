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
package de.featjar.analysis.sat4j.computation;

import de.featjar.analysis.sat4j.solver.ModalImplicationGraph;
import de.featjar.base.data.ExpandableIntegerList;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import java.util.BitSet;

/**
 * Calculates statistics regarding t-wise feature coverage of a set of
 * solutions.
 *
 * @author Sebastian Krieter
 */
public class SampleBitIndexMIG {

    private BitSet[] bitSetReference;
    private int numberOfVariables;
    private int sampleSize;
    private VariableMap variableMap;

    private final ModalImplicationGraph mig;
    private final ExpandableIntegerList literalCount;

    public SampleBitIndexMIG(final VariableMap variableMap, ModalImplicationGraph mig) {
        this.variableMap = variableMap;
        numberOfVariables = variableMap.size();
        bitSetReference = new BitSet[2 * numberOfVariables + 1];

        sampleSize = 0;
        for (int j = 0; j < bitSetReference.length; j++) {
            bitSetReference[j] = new BitSet();
        }

        literalCount = new ExpandableIntegerList();
        this.mig = mig;
    }

    public SampleBitIndexMIG(SampleBitIndexMIG other) {
        this.variableMap = other.variableMap;
        numberOfVariables = other.numberOfVariables;
        bitSetReference = new BitSet[2 * numberOfVariables + 1];

        sampleSize = other.sampleSize;
        for (int j = 0; j < bitSetReference.length; j++) {
            bitSetReference[j] = (BitSet) other.bitSetReference[j].clone();
        }

        literalCount = new ExpandableIntegerList(other.literalCount);
        this.mig = other.mig;
    }

    public void readdConfiguration() {
        sampleSize++;
    }

    public int addConfiguration(BooleanAssignment config) {
        return addConfiguration(config.get());
    }

    public int addConfiguration(int... config) {
        int id = sampleSize++;
        literalCount.add(0);

        for (int l : config) {
            if (l != 0) {
                bitSetReference[index(l)].set(id);
                literalCount.set(id, literalCount.get(id) + 1);
            }
        }
        return id;
    }

    private int index(int l) {
        return numberOfVariables + l;
    }

    public void remove(int id) {
        clear(id);
        sampleSize--;
    }

    public void clear(int id) {
        for (int j = 0; j < bitSetReference.length; j++) {
            bitSetReference[j].clear(id);
        }
        literalCount.set(id, 0);
    }

    public void reset(int id, int[] literals) {
        clear(id);
        set(id, literals);
    }

    public void set(int id, int... literals) {
        int numLiterals = literalCount.get(id);
        for (int l : literals) {
            if (!bitSetReference[index(l)].get(id)) {
                bitSetReference[index(l)].set(id);
                numLiterals++;
                for (int strongL : mig.getStrongEdges()[ModalImplicationGraph.getVertexIndex(l)]) {
                    if (!bitSetReference[index(strongL)].get(id)) {
                        bitSetReference[index(strongL)].set(id);
                        numLiterals++;
                    }
                }
            }
        }
        literalCount.set(id, numLiterals);
    }

    public int getLiteralsCount(int id) {
        return literalCount.get(id);
    }

    public int[] getLiterals(int id) {
        int[] literals = new int[literalCount.get(id)];
        int index = 0;
        for (int i = 1; i <= numberOfVariables; i++) {
            if (bitSetReference[index(i)].get(id)) {
                literals[index++] = i;
            } else if (bitSetReference[index(-i)].get(id)) {
                literals[index++] = -i;
            }
        }
        return literals;
    }

    public int[] getConfiguration(int id) {
        int[] literals = new int[numberOfVariables];
        for (int i = 1; i <= numberOfVariables; i++) {
            if (bitSetReference[index(i)].get(id)) {
                literals[i - 1] = i;
            } else if (bitSetReference[index(-i)].get(id)) {
                literals[i - 1] = -i;
            }
        }
        return literals;
    }

    public int size() {
        return sampleSize;
    }

    public int highestID() {
        return literalCount.size();
    }

    public BitSet getInternalBitSet(int literal) {
        return bitSetReference[index(literal)];
    }

    public BitSet getNegatedBitSet(int... literals) {
        BitSet first = bitSetReference[index(-literals[0])];
        BitSet bitSet = (BitSet) first.clone();
        for (int k = 1; k < literals.length; k++) {
            bitSet.or(bitSetReference[index(-literals[k])]);
        }
        return bitSet;
    }

    public BitSet getBitSet(int... literals) {
        BitSet first = bitSetReference[index(literals[0])];
        BitSet bitSet = (BitSet) first.clone();
        for (int k = 1; k < literals.length; k++) {
            if (bitSet.isEmpty()) {
                return bitSet;
            }
            bitSet.and(bitSetReference[index(literals[k])]);
        }
        return bitSet;
    }

    public boolean test(int... literals) {
        switch (literals.length) {
            case 0:
                return false;
            case 1:
                return getInternalBitSet(literals[0]).cardinality() > 0;
            case 2:
                return bitSetReference[index(literals[0])].intersects(bitSetReference[index(literals[1])]);
            default:
                return !getBitSet(literals).isEmpty();
        }
    }

    public int[] propagate(int id, int... literals) {
        BitSet literalSet = new BitSet(2 * numberOfVariables + 1);
        int literalCount = 0;
        for (int l = 1; l <= numberOfVariables; l++) {
            if (bitSetReference[index(l)].get(id)) {
                literalSet.set(index(l));
                literalCount++;
            } else if (bitSetReference[index(-l)].get(id)) {
                literalSet.set(index(-l));
                literalCount++;
            }
        }
        for (int l : literals) {
            if (l != 0) {
                if (literalSet.get(index(-l))) {
                    return null;
                }
                if (!literalSet.get(index(l))) {
                    literalSet.set(index(l));
                    literalCount++;
                    for (int strongL : mig.getStrongEdges()[ModalImplicationGraph.getVertexIndex(l)]) {
                        if (literalSet.get(index(-strongL))) {
                            return null;
                        }
                        if (!literalSet.get(index(strongL))) {
                            literalSet.set(index(strongL));
                            literalCount++;
                        }
                    }
                }
            }
        }
        int[] literalArray = new int[literalCount];
        int i = 0;
        for (int literalIndex = literalSet.nextSetBit(0);
                literalIndex >= 0;
                literalIndex = literalSet.nextSetBit(literalIndex + 1)) {
            literalArray[i++] = literalIndex - numberOfVariables;
        }
        return literalArray;
    }

    public boolean isUndefined(int id, int literal) {
        if (bitSetReference[index(literal)].get(id)) {
            return false;
        } else if (bitSetReference[index(-literal)].get(id)) {
            return false;
        }
        return true;
    }

    public int countUndefined(int id, int[] literals) {
        int count = 0;
        for (int l : literals) {
            if (isUndefined(id, l)) {
                count++;
            }
        }
        return count;
    }
}
