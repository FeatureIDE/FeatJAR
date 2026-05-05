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

import de.featjar.base.io.format.IFormat;
import de.featjar.formula.VariableMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Stores multiple groups of {@link BooleanAssignmentList}.
 * The main purposes of this class is to provide an easy to write/read object for a corresponding {@link IFormat format}.
 *
 * @author Sebastian Krieter
 */
public class BooleanAssignmentGroups implements Iterable<BooleanAssignmentList> {

    protected final VariableMap variableMap;
    protected final List<BooleanAssignmentList> assignmentGroups;

    public BooleanAssignmentGroups(VariableMap variableMap, List<BooleanAssignmentList> assignmentGroups) {
        this.variableMap = variableMap;
        this.assignmentGroups = assignmentGroups;
    }

    public BooleanAssignmentGroups(VariableMap variableMap, BooleanAssignmentList assignmentGroup) {
        this.variableMap = variableMap;
        this.assignmentGroups = List.of(assignmentGroup);
    }

    public BooleanAssignmentGroups(BooleanAssignmentList assignmentGroup) {
        this.variableMap = assignmentGroup.getVariableMap();
        this.assignmentGroups = List.of(assignmentGroup);
    }

    public BooleanAssignmentGroups(VariableMap variableMap, BooleanAssignment... assignments) {
        this.variableMap = variableMap;
        this.assignmentGroups = List.of(new BooleanAssignmentList(variableMap, Arrays.asList(assignments)));
    }

    public VariableMap getVariableMap() {
        return variableMap;
    }

    public List<BooleanAssignmentList> getGroups() {
        return assignmentGroups;
    }

    public BooleanAssignmentList getFirstGroup() {
        return assignmentGroups.get(0);
    }

    public BooleanAssignmentList getMergedGroups() {
        return assignmentGroups.stream().reduce(new BooleanAssignmentList(variableMap), (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        });
    }

    public BooleanAssignmentList toClauseList() {
        return toClauseList(0);
    }

    public BooleanAssignmentList toSolutionList() {
        return toSolutionList(0);
    }

    public BooleanAssignmentList toClauseList(int groupIndex) {
        return assignmentGroups.get(groupIndex).toClauseList();
    }

    public BooleanAssignmentList toSolutionList(int groupIndex) {
        return assignmentGroups.get(groupIndex).toSolutionList();
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentGroups, variableMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BooleanAssignmentGroups other = (BooleanAssignmentGroups) obj;
        return Objects.equals(assignmentGroups, other.assignmentGroups)
                && Objects.equals(variableMap, other.variableMap);
    }

    @Override
    public String toString() {
        return "AssignmentGroup [map=" + variableMap + ", groups=" + assignmentGroups + "]";
    }

    @Override
    public Iterator<BooleanAssignmentList> iterator() {
        return assignmentGroups.iterator();
    }
}
