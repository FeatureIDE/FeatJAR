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
package de.featjar.formula.assignment.conversion;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.IBooleanRepresentation;
import de.featjar.formula.assignment.IValueRepresentation;
import java.util.List;

/**
 * Converts an {@link IBooleanRepresentation} to an {@link IValueRepresentation}.
 * @param <T> the concrete type of the input IBooleanRepresentation
 * @param <U> the concrete type of the output IValueRepresentation
 * @author Elias Kuiter
 */
public class ValueRepresentationComputation<T extends IBooleanRepresentation, U extends IValueRepresentation>
        extends AComputation<U> {

    /**
     * The input dependency.
     */
    protected static final Dependency<?> BOOLEAN_REPRESENTATION = Dependency.newDependency();

    /**
     * Constructs a new computation with the given dependencies.
     * @param booleanRepresentation the computation of the boolean representation
     */
    public ValueRepresentationComputation(IComputation<T> booleanRepresentation) {
        super(booleanRepresentation);
    }

    /**
     * Copy constructor.
     * @param other the computation to copy
     */
    protected ValueRepresentationComputation(ValueRepresentationComputation<T, U> other) {
        super(other);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<U> compute(List<Object> dependencyList, Progress progress) {
        T t = (T) BOOLEAN_REPRESENTATION.get(dependencyList);
        return (Result<U>) t.toValue();
    }
}
