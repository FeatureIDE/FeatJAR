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

import de.featjar.formula.VariableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A list of Boolean assignments.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class BooleanAssignmentList implements IAssignmentList<BooleanAssignment>, IBooleanRepresentation {

    /**
     * Compares list of clauses by the number of literals.
     */
    public static class AscendingLengthComparator implements Comparator<BooleanAssignmentList> {
        @Override
        public int compare(BooleanAssignmentList o1, BooleanAssignmentList o2) {
            return addLengths(o1) - addLengths(o2);
        }

        protected int addLengths(BooleanAssignmentList o) {
            int count = 0;
            for (final BooleanAssignment literalSet : o.assignments) {
                count += literalSet.get().length;
            }
            return count;
        }
    }

    /**
     * Compares list of clauses by the number of literals.
     */
    public static class DescendingClauseListLengthComparator implements Comparator<BooleanAssignmentList> {
        @Override
        public int compare(BooleanAssignmentList o1, BooleanAssignmentList o2) {
            return addLengths(o2) - addLengths(o1);
        }

        protected int addLengths(BooleanAssignmentList o) {
            int count = 0;
            for (final BooleanAssignment literalSet : o.assignments) {
                count += literalSet.get().length;
            }
            return count;
        }
    }

    protected VariableMap variableMap;
    protected final List<BooleanAssignment> assignments;

    public BooleanAssignmentList(VariableMap variableMap) {
        this.variableMap = variableMap;
        assignments = new ArrayList<>();
    }

    public BooleanAssignmentList(VariableMap variableMap, int size) {
        this.variableMap = variableMap;
        assignments = new ArrayList<>(size);
    }

    public BooleanAssignmentList(VariableMap variableMap, Collection<? extends BooleanAssignment> assignments) {
        this.variableMap = variableMap;
        this.assignments = new ArrayList<>(assignments);
    }

    public BooleanAssignmentList(VariableMap variableMap, Stream<? extends BooleanAssignment> assignments) {
        this.variableMap = variableMap;
        this.assignments = assignments.collect(Collectors.toCollection(ArrayList::new));
    }

    public BooleanAssignmentList(VariableMap variableMap, BooleanAssignment... assignments) {
        this.variableMap = variableMap;
        this.assignments = new ArrayList<>(List.of(assignments));
    }

    public BooleanAssignmentList(BooleanAssignmentList other) {
        this(other.variableMap, other.getAll());
    }

    public VariableMap getVariableMap() {
        return variableMap;
    }

    @Override
    public List<BooleanAssignment> getAll() {
        return assignments;
    }

    @Override
    public BooleanAssignmentList toAssignmentList() {
        return new BooleanAssignmentList(
                variableMap,
                assignments.stream().map(BooleanAssignment::toAssignment).collect(Collectors.toList()));
    }

    @Override
    public BooleanAssignmentList toClauseList() {
        return new BooleanAssignmentList(
                variableMap,
                assignments.stream().map(BooleanAssignment::toClause).collect(Collectors.toList()));
    }

    @Override
    public BooleanAssignmentList toSolutionList() {
        return new BooleanAssignmentList(
                variableMap,
                assignments.stream().map(a -> a.toSolution(variableMap.size())).collect(Collectors.toList()));
    }

    /**
     * Changes the {@link VariableMap variable map} and calls {@link BooleanAssignment#remap(VariableMap, VariableMap, boolean)} for every assignment in this list.
     * This does not create a copy of this list, but directly changes each assignment.
     * A call of this method is equivalent to a call of {@link #remap(VariableMap, boolean) adapt(newVariables, false);}.
     *
     * @param newVariables the new variable map
     * @return this list
     */
    public BooleanAssignmentList remap(VariableMap newVariables) {
        return remap(newVariables, false);
    }

    /**
     * Changes the {@link VariableMap variable map} and calls {@link BooleanAssignment#remap(VariableMap, VariableMap, boolean)} for every assignment in this list.
     * This creates a copy of this list.
     *
     * @param newVariables the new variable map
     * @param integrateOldVariables whether variable names from the old variable map are added to the new variable map, if missing
     * @return the new list
     */
    public BooleanAssignmentList remap(VariableMap newVariables, boolean integrateOldVariables) {
        if (variableMap == newVariables) {
            return this;
        }
        BooleanAssignmentList newList = new BooleanAssignmentList(newVariables);
        for (BooleanAssignment assignment : assignments) {
            newList.add(assignment.remap(variableMap, newVariables, integrateOldVariables));
        }
        return newList;
    }

    @Override
    public BooleanAssignmentList clone() {
        return new BooleanAssignmentList(
                variableMap, assignments.stream().map(BooleanAssignment::clone).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanAssignmentList that = (BooleanAssignmentList) o;
        return Objects.equals(assignments, that.assignments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignments);
    }

    @Override
    public ValueAssignmentList toValue() {
        return VariableMap.toValue(this);
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (BooleanAssignment assignment : assignments) {
            sb.append(assignment.print());
            sb.append('\n');
        }
        int length = sb.length();
        if (length > 0) {
            sb.setLength(length - 1);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format(
                "BooleanAssignmentList[%s\n,%s]",
                variableMap != null ? variableMap.toString() : "null",
                stream().map(a -> Arrays.toString(a.get())).collect(Collectors.joining(";\n")));
    }
}
