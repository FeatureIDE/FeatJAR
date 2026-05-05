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

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.formula.VariableMap;
import de.featjar.formula.io.textual.ValueAssignmentListFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * To be used when neither CNF nor DNF semantics are associated with an assignment list.
 *
 * @author Elias Kuiter
 */
public class ValueAssignmentList implements IAssignmentList<ValueAssignment>, IValueRepresentation {
    protected final List<ValueAssignment> literalLists;
    protected final VariableMap variableMap;

    public ValueAssignmentList(VariableMap variableMap) {
        literalLists = new ArrayList<>();
        this.variableMap = variableMap;
    }

    public ValueAssignmentList(VariableMap variableMap, int size) {
        literalLists = new ArrayList<>(size);
        this.variableMap = variableMap;
    }

    public ValueAssignmentList(VariableMap variableMap, Collection<? extends ValueAssignment> literalLists) {
        this.literalLists = new ArrayList<>(literalLists);
        this.variableMap = variableMap;
    }

    public ValueAssignmentList(ValueAssignmentList other) {
        this(other.variableMap, other.getAll());
    }

    @Override
    public List<ValueAssignment> getAll() {
        return literalLists;
    }

    @Override
    public ValueAssignmentList toAssignmentList() {
        return new ValueAssignmentList(
                variableMap,
                literalLists.stream().map(ValueAssignment::toAssignment).collect(Collectors.toList()));
    }

    @Override
    public ValueAssignmentList toClauseList() {
        return new ValueAssignmentList(
                variableMap,
                literalLists.stream().map(ValueAssignment::toClause).collect(Collectors.toList()));
    }

    @Override
    public ValueAssignmentList toSolutionList() {
        return new ValueAssignmentList(
                variableMap,
                literalLists.stream().map(ValueAssignment::toSolution).collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueAssignmentList that = (ValueAssignmentList) o;
        return Objects.equals(literalLists, that.literalLists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(literalLists);
    }

    public String print() {
        try {
            return IO.print(this, new ValueAssignmentListFormat());
        } catch (IOException e) {
            return e.toString();
        }
    }

    public VariableMap getVariableMap() {
        return variableMap;
    }

    @Override
    public Result<BooleanAssignmentList> toBoolean() {
        return VariableMap.toBoolean(this);
    }

    @Override
    public String toString() {
        return String.format("ValueAssignmentList[%s]", print());
    }
}
