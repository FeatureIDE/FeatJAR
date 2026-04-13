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
import de.featjar.base.tree.structure.ALeafNode;
import de.featjar.base.tree.structure.ITree;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A constant computation.
 * Always computes the same value.
 * The leaves of a computation tree are precisely its constant computations.
 *
 * @param <T> the type of the computed value
 * @author Elias Kuiter
 */
public class ComputeConstant<T> extends ALeafNode<IComputation<?>> implements IComputation<T> {
    protected final T value;

    /**
     * Creates a constant computation.
     *
     * @param value   the value
     */
    public ComputeConstant(T value) {
        super();
        this.value = Objects.requireNonNull(value, "constant computation of null not allowed");
    }

    @Override
    public Result<T> compute(List<Object> dependencyList, Progress progress) {
        return Result.of(value);
    }

    @Override
    public boolean equalsNode(IComputation<?> other) {
        return getClass() == other.getClass() && Objects.equals(value, ((ComputeConstant<?>) other).value);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(getClass(), value);
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        return new ComputeConstant<>(value);
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s, %s)", getClass().getSimpleName(), value.getClass().getSimpleName(), value);
    }

    @Override
    public Result<T> computeResult(boolean tryHitCache, boolean tryWriteCache, Supplier<Progress> progressSupplier) {
        return Result.of(value);
    }
}
