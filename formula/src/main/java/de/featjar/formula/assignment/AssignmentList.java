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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A list of {@link Assignment assignments}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class AssignmentList implements IAssignmentList<Assignment> {
    protected final List<Assignment> literalLists;

    public AssignmentList() {
        literalLists = new ArrayList<>();
    }

    public AssignmentList(int size) {
        literalLists = new ArrayList<>(size);
    }

    public AssignmentList(Collection<Assignment> literalLists) {
        this.literalLists = new ArrayList<>(literalLists);
    }

    public AssignmentList(AssignmentList other) {
        this(other.getAll());
    }

    @Override
    public String toString() {
        return String.format("ValueAssignmentList[%s]", print());
    }

    @Override
    public List<Assignment> getAll() {
        return literalLists;
    }

    @Override
    public AssignmentList toClauseList() {
        return this;
    }

    @Override
    public AssignmentList toSolutionList() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentList that = (AssignmentList) o;
        return Objects.equals(literalLists, that.literalLists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(literalLists);
    }

    @Override
    public AssignmentList toAssignmentList() {
        return this;
    }
}
