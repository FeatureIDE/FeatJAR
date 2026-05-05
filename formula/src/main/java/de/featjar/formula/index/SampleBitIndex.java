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
package de.featjar.formula.index;

import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.BitSet;

/**
 * Stores assignments in a way that makes it easy to see which assignments share one or more given literals.
 *
 * @author Sebastian Krieter
 */
public class SampleBitIndex implements ISampleIndex {

    private BitSet[] bitSetReference;
    private int numberOfVariables;
    private int sampleSize;
    private VariableMap variableMap;

    /**
     * Creates a new index based on the number of variables in the given map.
     * @param variableMap the variable map
     */
    public SampleBitIndex(final VariableMap variableMap) {
        this.variableMap = variableMap;
        numberOfVariables = variableMap.size();
        bitSetReference = new BitSet[2 * numberOfVariables + 1];

        sampleSize = 0;
        for (int j = 0; j < bitSetReference.length; j++) {
            bitSetReference[j] = new BitSet();
        }
    }

    /**
     * Creates a new index based on the number of variables in the given map.
     * Reserves space for a given number of assignments.
     * Use this constructor, if the number of assignments is already known upon creating this index.
     * Otherwise use {@link #SampleBitIndex(VariableMap)} instead.
     *
     * @param variableMap the variable map
     * @param numberOfInitialConfigs the number of assignments
     */
    public SampleBitIndex(final VariableMap variableMap, int numberOfInitialConfigs) {
        this.variableMap = variableMap;
        numberOfVariables = variableMap.size();
        bitSetReference = new BitSet[2 * numberOfVariables + 1];

        sampleSize = 0;
        for (int j = 0; j < bitSetReference.length; j++) {
            bitSetReference[j] = new BitSet(numberOfInitialConfigs);
        }
    }

    /**
     * Creates a new index based on the number of variables in the given sample's variable map.
     * Adds all assignments of the given sample to this index.
     *
     * @param sample a list of assignments
     */
    public SampleBitIndex(BooleanAssignmentList sample) {
        this(sample.getVariableMap(), sample.size());
        sample.forEach(this::addConfiguration);
    }

    public void addConfiguration(BooleanAssignment config) {
        addConfiguration(config.get());
    }

    public void addConfiguration(int[] config) {
        int i = sampleSize++;

        for (int l : config) {
            if (l != 0) {
                bitSetReference[numberOfVariables + l].set(i);
            }
        }
    }

    /**
     * Add an empty configuration to this index.
     * @return the id of the added configuration.
     */
    public int addEmptyConfiguration() {
        return sampleSize++;
    }

    /**
     * Updates the values for the assignment with the given id.
     * @param id the id of the assignment to update
     * @param config the new values
     */
    public void update(int id, BooleanAssignment config) {
        update(id, config.get());
    }

    /**
     * Updates the values for the assignment with the given id.
     * @param id the id of the assignment to update
     * @param config the new values
     */
    public void update(int id, int[] config) {
        for (int l : config) {
            update(id, l);
        }
    }
    /**
     * Updates a value for the assignment with the given id.
     * @param id the id of the assignment to update
     * @param literal the new value
     */
    public void update(int id, int literal) {
        bitSetReference[numberOfVariables - literal].clear(id);
        bitSetReference[numberOfVariables + literal].set(id, literal != 0);
    }

    /**
     * Defines a value for the assignment with the given id.
     * This method assumes that the value was previously undefined.
     * @param id the id of the assignment to update
     * @param literal the new value
     */
    public void set(int id, int literal) {
        assert !bitSetReference[numberOfVariables - literal].get(id);
        bitSetReference[numberOfVariables + literal].set(id);
    }

    /**
     * Removes a value for the given variable in the assignment with the given id.
     * @param id the id of the assignment to update
     * @param variable the variable for which to remove a value
     */
    public void clear(int id, int variable) {
        bitSetReference[numberOfVariables - variable].clear(id);
        bitSetReference[numberOfVariables + variable].clear(id);
    }

    /**
     * Removes all values for the assignment with the given id.
     * @param id the id of the assignment to clear
     */
    public void clear(int id) {
        for (int j = 0; j < bitSetReference.length; j++) {
            bitSetReference[j].clear(id);
        }
    }

    /**
     * {@return the value of the given variable in the assignment with the given id}
     * @param id the id of the assignment
     * @param variable the variable for which to get the value
     */
    public int get(int id, int variable) {
        if (bitSetReference[numberOfVariables + variable].get(id)) {
            return variable;
        } else if (bitSetReference[numberOfVariables - variable].get(id)) {
            return -variable;
        }
        return 0;
    }

    /**
     * {@return a bitset representing the ids of all assignments that contains the given values}
     * @param literals the values
     */
    public BitSet getBitSet(int... literals) {
        BitSet bitSet = (BitSet) bitSetReference[numberOfVariables + literals[0]].clone();
        for (int k = 1; k < literals.length; k++) {
            bitSet.and(bitSetReference[numberOfVariables + literals[k]]);
        }
        return bitSet;
    }

    /**
     * Modifies a given bitset to only represent assignments that also contain the given values.
     * @param bitSet the original bitset
     * @param literals the values
     * @return the modified bitset (no copy)
     */
    public BitSet updateBitSet(BitSet bitSet, int... literals) {
        for (int k = 0; k < literals.length; k++) {
            bitSet.and(bitSetReference[numberOfVariables + literals[k]]);
        }
        return bitSet;
    }

    /**
     * {@return the internal bitset (no copy) for a given literal which represents all assignments containing this literal}
     * @param literal the literal for which to get the bitset
     */
    public BitSet getInternalBitSet(int literal) {
        return bitSetReference[numberOfVariables + literal];
    }

    /**
     * {@return a bitset representing the ids of all assignments that contain any of the given values' complements}
     * @param literals the values
     */
    public BitSet getNegatedBitSet(int... literals) {
        BitSet bitSet = (BitSet) bitSetReference[numberOfVariables - literals[0]].clone();
        for (int k = 1; k < literals.length; k++) {
            bitSet.or(bitSetReference[numberOfVariables - literals[k]]);
        }
        return bitSet;
    }
    /**
     * {@return a bitset representing the ids of all assignments that contains the given values}
     * @param literals the values
     * @param n the number of values to consider
     */
    public BitSet getBitSet(int[] literals, int n) {
        if (n <= 0) {
            return new BitSet();
        }
        BitSet bitSet = (BitSet) bitSetReference[numberOfVariables + literals[0]].clone();
        for (int k = 1; k < n; k++) {
            bitSet.and(bitSetReference[numberOfVariables + literals[k]]);
            if (bitSet.isEmpty()) {
                return bitSet;
            }
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
                return bitSetReference[numberOfVariables + literals[0]].intersects(
                        bitSetReference[numberOfVariables + literals[1]]);
            default:
                return !getBitSet(literals).isEmpty();
        }
    }

    public int index(int... literals) {
        return (literals.length == 1 ? getInternalBitSet(literals[0]) : getBitSet(literals)).nextSetBit(0);
    }

    public int size(int... literals) {
        return literals.length == 1
                ? getInternalBitSet(literals[0]).cardinality()
                : getBitSet(literals).cardinality();
    }

    // TODO rename
    public int size() {
        return sampleSize;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    public int[] getConfiguration(int id) {
        int[] model = new int[numberOfVariables];
        for (int i = 1; i <= numberOfVariables; i++) {
            if (bitSetReference[numberOfVariables + i].get(id)) {
                model[i - 1] = i;
            } else if (bitSetReference[numberOfVariables - i].get(id)) {
                model[i - 1] = -i;
            }
        }
        return model;
    }

    @Override
    public SampleBitIndex remap(VariableMap newVariableMap) {
        int newNumberOfVariables = variableMap.size();
        BitSet[] newBitSetReference = new BitSet[2 * newNumberOfVariables + 1];

        for (int index = 1; index <= numberOfVariables; index++) {
            int remapedIndex = variableMap.remap(index, newVariableMap, true);
            newBitSetReference[newNumberOfVariables + remapedIndex] = bitSetReference[numberOfVariables + index];
            newBitSetReference[newNumberOfVariables - remapedIndex] = bitSetReference[numberOfVariables - index];
        }
        numberOfVariables = newNumberOfVariables;
        bitSetReference = newBitSetReference;
        variableMap = newVariableMap;
        return this;
    }
}
