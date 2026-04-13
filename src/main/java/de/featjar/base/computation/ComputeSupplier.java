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
import java.util.function.Supplier;

/**
 * A computation that uses a supplier to create a value.
 * As supplier cannot be reliably checked for equality or hashed, an identifier must be explicitly passed.
 * The caller must guarantee that this identifier is unique.
 *
 * @param <T> the type of the mapped result
 * @author Elias Kuiter
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public class ComputeSupplier<T> extends AComputation<T> {
    protected final Class<?> klass;
    protected final String scope;
    protected final Supplier<Result<T>> supplier;

    /**
     * Creates a supplier computation.
     *
     * @param klass    the calling class
     * @param scope    the calling scope
     * @param supplier the supplier
     */
    public ComputeSupplier(Class<?> klass, String scope, Supplier<Result<T>> supplier) {
        super();
        this.klass = klass;
        this.scope = scope;
        this.supplier = supplier;
    }

    protected ComputeSupplier(ComputeSupplier<T> other) {
        super(other);
        this.klass = other.klass;
        this.scope = other.scope;
        this.supplier = other.supplier;
    }

    @Override
    public Result<T> compute(List<Object> dependencyList, Progress progress) {
        return supplier.get();
    }

    @Override
    public boolean equalsNode(IComputation<?> other) {
        return super.equalsNode(other)
                && Objects.equals(klass, ((ComputeSupplier<?>) other).klass)
                && Objects.equals(scope, ((ComputeSupplier<?>) other).scope);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), klass, scope);
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s)", super.toString(), klass.getSimpleName(), scope);
    }

    @Override
    public Result<T> computeResult(boolean tryHitCache, boolean tryWriteCache, Supplier<Progress> progressSupplier) {
        return supplier.get();
    }
}
