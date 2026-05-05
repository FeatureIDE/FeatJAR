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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ATree;
import de.featjar.base.tree.structure.ITree;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * Primary implementation of {@link IComputation}.
 *
 * @param <T> the type of the computation result
 * @author Elias Kuiter
 */
public abstract class AComputation<T> extends ATree<IComputation<?>> implements IComputation<T> {

    /**
     * The cache object.
     */
    protected Cache cache = FeatJAR.cache();

    /**
     * Constructs a new computation.
     * @param dependencies the dependencies of this computation
     */
    protected AComputation(IComputation<?>... dependencies) {
        super(dependencies.length);
        final Integer size = Dependency.getDependencyCount(getClass());
        assert size == dependencies.length;
        setChildren(List.of(dependencies));
    }

    /**
     * Constructs a new computation.
     * @param dependencies1 the first dependencies of this computation
     * @param dependencies2 further dependencies of this computation
     */
    protected AComputation(List<IComputation<?>> dependencies1, IComputation<?>... dependencies2) {
        super(dependencies1.size() + dependencies2.length);
        final Integer size = Dependency.getDependencyCount(getClass());
        assert size == dependencies1.size() + dependencies2.length;
        ArrayList<IComputation<?>> computations = new ArrayList<>(size);
        computations.addAll(dependencies1);
        computations.addAll(List.of(dependencies2));
        setChildren(computations);
    }

    /**
     * Constructs a new computation.
     * @param dependencies the dependencies of this computation
     */
    protected AComputation(Object... dependencies) {
        super(dependencies.length);
        final Integer size = Dependency.getDependencyCount(getClass());
        ArrayList<IComputation<?>> computationList = new ArrayList<>(size);
        for (Object computation : dependencies) {
            unpackComputations(computationList, computation);
        }
        assert size == computationList.size();
        setChildren(computationList);
    }

    private void unpackComputations(List<IComputation<?>> computationList, Object element) {
        if (element instanceof IComputation<?>) {
            computationList.add((IComputation<?>) element);
        } else if (element instanceof Object[]) {
            for (Object computation : (Object[]) element) {
                unpackComputations(computationList, computation);
            }
        } else if (element instanceof List<?>) {
            for (Object computation : (List<?>) element) {
                unpackComputations(computationList, computation);
            }
        } else {
            if (element == null) {
                throw new RuntimeException("Argument is null");
            } else {
                throw new RuntimeException("Argument is not a computation: " + element.getClass());
            }
        }
    }

    /**
     * Copy constructor.
     * @param other the computation to copy
     */
    protected AComputation(AComputation<T> other) {
        super();
    }

    /**
     * Check whether this computation received a cancel request.
     * If so, this method throws a {@link CancellationException}.
     */
    protected final void checkCancel() {
        if (Thread.interrupted()) {
            throw new CancellationException();
        }
    }

    @Override
    public Result<T> computeResult(boolean tryHitCache, boolean tryWriteCache, Supplier<Progress> progressSupplier) {
        if (tryHitCache) {
            Result<FutureResult<T>> cacheHit = getCache().tryHit(this);
            if (cacheHit.isPresent()) {
                return cacheHit.get().get();
            }
        }
        List<Result<?>> results = getChildren().stream()
                .map(computation -> computation.computeResult(tryHitCache, tryWriteCache, progressSupplier))
                .collect(Collectors.toList());
        Progress progress = progressSupplier.get();
        progress.setName(toString());
        checkCancel();
        try {
            Result<T> result = mergeResults(results).mapResult(r -> {
                try (progress) {
                    FeatJAR.progress().track(progress);
                    return compute(r, progress);
                }
            });
            if (tryWriteCache) {
                getCache().tryWrite(this, new FutureResult<>(result, progress));
            }
            return result;
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    @Override
    public boolean equalsNode(IComputation<?> other) {
        return (getClass() == other.getClass());
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(getClass());
    }

    @Override
    public Cache getCache() {
        return cache;
    }

    /**
     * Sets the cache this computation should be stored and looked up in.
     *
     * @param cache the cache
     */
    public void setCache(Cache cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public <U> AComputation<T> setDependencyComputation(
            Dependency<U> dependency, IComputation<? extends U> computation) {
        replaceChild(dependency.getIndex(), computation);
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ITree<IComputation<?>> cloneNode() {
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends AComputation<T>> computationClass = (Class<? extends AComputation<T>>) getClass();
            return computationClass.getDeclaredConstructor(computationClass).newInstance(this);
        } catch (InstantiationException | NoSuchMethodException e) {
            FeatJAR.log().error(e);
            throw new UnsupportedOperationException(e);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            throw new RuntimeException(e);
        }
    }
}
