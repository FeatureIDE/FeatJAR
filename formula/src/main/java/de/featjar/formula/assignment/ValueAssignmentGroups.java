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
import java.util.List;
import java.util.Objects;

/**
 * Stores multiple groups of {@link ValueAssignmentList}.
 * The main purposes of this class is to provide an easy to write/read object for a corresponding {@link IFormat format}.
 *
 * @author Sebastian Krieter
 */
public class ValueAssignmentGroups {

    protected final VariableMap variableMap;
    protected final List<? extends ValueAssignmentList> assignmentGroups;

    public ValueAssignmentGroups(VariableMap variableMap, List<? extends ValueAssignmentList> assignmentGroups) {
        this.variableMap = variableMap;
        this.assignmentGroups = assignmentGroups;
    }

    public ValueAssignmentGroups(ValueAssignmentList assignmentGroup) {
        this.variableMap = assignmentGroup.getVariableMap();
        this.assignmentGroups = List.of(assignmentGroup);
    }

    public ValueAssignmentGroups(VariableMap variableMap, ValueAssignment... assignments) {
        this.variableMap = variableMap;
        ValueAssignmentList firstGroup = new ValueAssignmentList(variableMap);
        for (ValueAssignment assignment : assignments) {
            firstGroup.add(assignment);
        }
        this.assignmentGroups = List.of(firstGroup);
    }

    public VariableMap getVariableMap() {
        return variableMap;
    }

    public List<? extends ValueAssignmentList> getGroups() {
        return assignmentGroups;
    }

    public ValueAssignmentList getFirstGroup() {
        return assignmentGroups.get(0);
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
        ValueAssignmentGroups other = (ValueAssignmentGroups) obj;
        return Objects.equals(assignmentGroups, other.assignmentGroups)
                && Objects.equals(variableMap, other.variableMap);
    }

    @Override
    public String toString() {
        return "AssignmentGroup [map=" + variableMap + ", groups=" + assignmentGroups + "]";
    }
}
