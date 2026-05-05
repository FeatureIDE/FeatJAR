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
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Assigns values to {@link de.featjar.formula.structure.term.value.Variable variables}.
 * Represents an {@link IClause} or an {@link ISolution} in an {@link ISolver}.
 * For a propositional implementation, see {@link BooleanAssignment},
 * for a first-order implementation, see {@link ValueAssignment}.
 *
 * @param <T> the index type of the variables
 * @param <R> the value type of the variables
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IAssignment<T, R> {
    /**
     * {@return an ordered map of all variable-value pairs in this assignment}
     * The default implementations of the other methods assume that this map is unmutable.
     * Undefined variables (e.g., for partial assignments) are omitted.
     */
    Map<T, R> getAll();

    /**
     * {@return true iff all entries of the given assignment are also present in this assignment}
     * @param other the other assignment
     */
    default boolean containsOtherAssignment(IAssignment<?, ?> other) {
        Map<T, R> thisAssignment = getAll();
        return other.getAll().entrySet().stream()
                .allMatch(e -> Objects.equals(e.getValue(), thisAssignment.get(e.getKey())));
    }

    /**
     * {@return the number of variable-value pairs in this assignment}
     */
    default int size() {
        return getAll().size();
    }

    /**
     * {@return whether this assignment is empty}
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@return a stream of variable-value pairs in this assignment}
     */
    default Stream<Pair<T, R>> streamValues() {
        return getAll().entrySet().stream().map(Pair::of);
    }

    /**
     * {@return the value assigned to the given variable, if any}
     *
     * @param variable the variable
     */
    default Result<R> getValue(T variable) {
        return Result.ofNullable(getAll().get(variable));
    }

    /**
     * {@return an assignment with the same contents of this assignment}
     */
    IAssignment<T, R> toAssignment();

    /**
     * {@return a clause with the same contents of this assignment}
     */
    IClause<T, R> toClause();

    /**
     * {@return a solution with the same contents of this assignment}
     */
    ISolution<T, R> toSolution();

    /**
     * {@return a string representation of all mappings in this assignment}
     */
    String print();
}
