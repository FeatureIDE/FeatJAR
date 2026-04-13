/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.computation;

import de.featjar.base.data.Result;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A computation that maps one value to another.
 * As functions cannot be reliably checked for equality or hashed, an identifier must be explicitly passed.
 * The caller must guarantee that this identifier is unique.
 *
 * @param <T> the type of the mapped value
 * @param <U> the type of the mapped result
 * @author Elias Kuiter
 */
public class ComputeFunction<T, U> extends AComputation<U> {
    protected static final Dependency<?> INPUT = Dependency.newDependency();
    protected final Class<?> klass;
    protected final String scope;
    protected final Function<T, Result<U>> function;

    /**
     * Creates a function computation.
     *
     * @param input    the input computation
     * @param klass    the calling class
     * @param scope    the calling scope
     * @param function the mapper function
     */
    public ComputeFunction(IComputation<T> input, Class<?> klass, String scope, Function<T, Result<U>> function) {
        super(input);
        this.klass = klass;
        this.scope = scope;
        this.function = function;
    }

    protected ComputeFunction(ComputeFunction<T, U> other) {
        super(other);
        this.klass = other.klass;
        this.scope = other.scope;
        this.function = other.function;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Result<U> compute(List<Object> dependencyList, Progress progress) {
        return function.apply((T) INPUT.getValue(dependencyList));
    }

    @Override
    public boolean equalsNode(IComputation<?> other) {
        return super.equalsNode(other)
                && Objects.equals(klass, ((ComputeFunction<?, ?>) other).klass)
                && Objects.equals(scope, ((ComputeFunction<?, ?>) other).scope);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), klass, scope);
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s)", super.toString(), klass.getSimpleName(), scope);
    }
}
