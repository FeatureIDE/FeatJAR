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

import de.featjar.analysis.ISolver;
import de.featjar.base.data.Result;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A list of assignments.
 * Represents a list of assignments (e.g., {@link IClause clauses} or {@link ISolution solutions}) in a {@link ISolver}.
 * For a propositional implementation, see {@link BooleanAssignmentList},
 * for a first-order implementation.
 * If {@code T} refers to a {@link IClause} type, the list usually represents a conjunctive normal form (CNF).
 * If {@code T} refers to a {@link ISolution} type, the list usually represents a disjunctive normal form (DNF).
 *
 * @param <T> the type of the assignment
 * @author Elias Kuiter
 */
public interface IAssignmentList<T extends IAssignment<?, ?>> extends Iterable<T> {
    /**
     * {@return a list of all assignments in this assignment list}
     * The default implementations of the other methods assume that this list is mutable.
     * If it is not, the other methods must be overridden accordingly.
     */
    List<T> getAll();

    /**
     * {@return true iff all assignments in the given list match an assignment in this list}
     * @param other the other assignment
     */
    default boolean containsOtherAssignments(IAssignmentList<T> other) {
        List<T> thisAssignments = getAll();
        return other.stream().allMatch(otherAssignment -> thisAssignments.stream()
                .anyMatch(thisAssignment -> thisAssignment.containsOtherAssignment(otherAssignment)));
    }

    @Override
    default Iterator<T> iterator() {
        return getAll().iterator();
    }

    /**
     * {@return the number of assignments in this assignment list}
     */
    default int size() {
        return getAll().size();
    }

    /**
     * {@return whether this assignment list is empty}
     */
    default boolean isEmpty() {
        return getAll().isEmpty();
    }

    /**
     * {@return a stream of assignments in this assignment list}
     */
    default Stream<T> stream() {
        return getAll().stream();
    }

    /**
     * {@return a string representation of all assignments in this list}
     *
     * @see IAssignment#print()
     */
    default String print() {
        return stream().map(IAssignment::print).collect(Collectors.joining(";\n"));
    }

    /**
     * {@return the assignment at the given index in this assignment list, if any. Throws exception if otherwise.}
     *
     * @param index the index
     *
     * @throws IndexOutOfBoundsException if the index was < 0 or >= {@link #size()}
     */
    default T get(int index) {
        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException(index);
        return getAll().get(index);
    }

    /**
     * {@return the assignment at the first index in this assignment list, if any. Return null otherwise.}
     */
    default T getFirst() {
        return isEmpty() ? null : getAll().get(0);
    }

    /**
     * Replaces the assignment at the given index in this list with the given assignment.
     *
     * @param index the index
     * @param assignment the assignment
     */
    default void set(int index, T assignment) {
        if (index < 0 || index > size()) throw new IllegalArgumentException();
        getAll().set(index, assignment);
    }

    /**
     * Adds the given assignment at the given index to this assignment list.
     *
     * @param index the index
     * @param assignment the assignment
     */
    default void add(int index, T assignment) {
        if (index < 0 || index > size()) throw new IllegalArgumentException();
        getAll().add(index, assignment);
    }

    /**
     * Adds the given assignment to the end of this assignment list.
     *
     * @param assignment the assignment
     */
    default void add(T assignment) {
        add(size(), assignment);
    }

    /**
     * Adds the given assignments to the end of this assignment list.
     *
     * @param assignments the assignments
     */
    default void addAll(Collection<? extends T> assignments) {
        getAll().addAll(assignments);
    }

    /**
     * Adds the given assignments to the end of this assignment list.
     *
     * @param assignments the assignments
     */
    default void addAll(IAssignmentList<? extends T> assignments) {
        addAll(assignments.getAll());
    }

    /**
     * Removes the assignment at the given index in this assignment list.
     *
     * @param index the index
     * @return the removed assignment, if any
     */
    default Result<T> remove(int index) {
        if (index < 0 || index >= size()) return Result.empty();
        return Result.of(getAll().remove(index));
    }

    /**
     * Removes the last assignment in this assignment list.
     *
     * @return the removed assignment, if any
     */
    default Result<T> remove() {
        return remove(size() - 1);
    }

    /**
     * Removes all assignments in this assignment list.
     */
    default void clear() {
        getAll().clear();
    }

    /**
     * {@return an assignment list with the same contents of this assignment list}
     */
    IAssignmentList<?> toAssignmentList();

    /**
     * {@return a clause list with the same contents of this assignment list}
     */
    IAssignmentList<?> toClauseList();

    /**
     * {@return a solution list with the same contents of this assignment list}
     */
    IAssignmentList<?> toSolutionList();
}
