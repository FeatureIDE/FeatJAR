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
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Describes a deterministic (potentially complex or long-running) computation.
 * An {@link IComputation} does not contain the computation result itself, it only computes it on demand as a {@link Supplier}.
 * There are several modes for computing the result of an {@link IComputation}:
 * The computation mode can be either synchronous (e.g., {@link #computeResult()}) or asynchronous,
 * returning a {@link Result} or {@link FutureResult}, respectively.
 * An asynchronous {@link FutureResult} can be shared between different threads.
 * It can be waited for, it can be cancelled, and its progress can be tracked, so it is well-suited for a graphical user interface.
 * A synchronous {@link Result} is computed on the current thread in a blocking fashion.
 * Synchronous computation modes are well-suited for a command-line interface.
 * In addition, the computation mode can either leverage results stored in a {@link Cache} (e.g., the global cache in {@link FeatJAR#cache()}).
 * Caching computation modes are well-suited for implementing knowledge compilation, incremental analyses, and evolution operators.
 * Computations can depend on other computations by declaring a {@link Dependency} of type {@code T}.
 * Thus, every computation is a tree of computations, where the dependencies of the computation are its children.
 * When a child is {@link Result#empty(Problem...)}, this signals an unrecoverable error by default, this behavior can be overridden with {@link #mergeResults(List)}.
 * Thus, every required dependency must be set to a non-null value in the constructor, and every optional dependency must have a non-null default value.
 * To ensure the determinism required by caching, all parameters of a computation must be depended on (including sources of randomness).
 * Also, all used data structures must be deterministic (e.g., by using {@link de.featjar.base.data.Maps} and {@link de.featjar.base.data.Sets}).
 * Implementors should pass mandatory parameters in the constructor and optional parameters using dedicated setters.
 * This can be facilitated by using specializations of {@link IComputation}.
 * Though not necessary, it is recommended to implement this interface by subclassing {@link AComputation}, which provides a mechanism for declaring dependencies.
 * It is strongly discouraged to implement this interface anonymously to ensure correct caching
 * with {@link Cache.CachePolicy#CACHE_TOP_LEVEL} and correct hash code and equality computations.
 * To compose anonymous computations, consider using {@link ComputeFunction} instead.
 *
 * @param <T> the type of the computation result
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public interface IComputation<T> extends Supplier<Result<T>>, ITree<IComputation<?>> {
    /**
     * {@return the result of this computation for the given list of dependencies}
     * Implementations must be deterministic to guarantee proper caching:
     * That is, they may only depend on the given dependency list and must not use data structures with
     * nondeterministic access (e.g., prefer {@link java.util.LinkedHashMap} over {@link java.util.HashMap}).
     * The dependency list is guaranteed to contain a non-null object for each declared {@link Dependency},
     * provided that {@link #mergeResults(List)} is not overridden.
     * Consequently, when {@link Result#empty(Problem...)} is returned, any dependent computations return {@link Result#empty(Problem...)} as well.
     * The given {@link Progress} can be used to report progress tracking information to the backing {@link FutureResult}.
     * This progress can be inspected using {@link FutureResult#peekEvery(Duration, Runnable)} and {@link Cache#getProgress(IComputation)}.
     * This method must not be renamed, as {@link de.featjar.base.computation.Cache.CachePolicy#CACHE_TOP_LEVEL}
     * uses reflection based on its name to detect nested computations.
     *
     * @param dependencyList the dependency list
     * @param progress       the progress
     */
    Result<T> compute(List<Object> dependencyList, Progress progress);

    /**
     * {@return the current result object of the computation}
     * Can be used to check the intermediate state of a computation.
     * Computation may not implement this functionality.
     */
    default Result<T> getIntermediateResult() {
        return Result.empty();
    }

    /**
     * Start this computation.
     *
     * @return the result of this computation or throws an exception if not result could be computed
     *
     * @see #computeResult()
     */
    default T compute() {
        return computeResult().orElseThrow();
    }

    /**
     * Start this computation.
     *
     * @param progressSupplier the supplier of a progress object
     *
     * @return the result of this computation or throws an exception if not result could be computed
     *
     * @see #computeResult(Supplier)
     */
    default T compute(Supplier<Progress> progressSupplier) {
        return computeResult(progressSupplier).orElseThrow();
    }

    /**
     * {@return the (asynchronous) future result of this computation}
     * Implements an asynchronous mode of computation that does or does not use the {@link Cache}.
     * Leverages parallelism as permitted by the {@link java.util.concurrent.Executor} of {@link FutureResult}.
     * Allows for cancellation and {@link Progress} tracking.
     * Generally recommended over {@link #computeResult(boolean, boolean)} for complex computations.
     *
     * @param tryHitCache   whether the cache should be queried for the result
     * @param tryWriteCache whether the result should be stored in the cache
     */
    default FutureResult<T> computeFutureResult(boolean tryHitCache, boolean tryWriteCache) {
        return FutureResult.compute(this, tryHitCache, tryWriteCache, Progress::new);
    }

    /**
     * {@return the (asynchronous and cached) future result of this computation}
     *
     * @see #computeFutureResult(boolean, boolean)
     */
    default FutureResult<T> computeFutureResult() {
        return computeFutureResult(true, true);
    }

    /**
     * {@return the (asynchronous and uncached) future result of this computation}
     *
     * @see #computeFutureResult(boolean, boolean)
     */
    default FutureResult<T> computeUncachedFutureResult() {
        return computeFutureResult(false, false);
    }

    /**
     * {@return the (synchronous) result of this computation}
     * Implements a synchronous mode of computation that does or does not use the {@link Cache}.
     * Does not leverage parallelism and does not allow for cancellation.
     * Allows for {@link Progress} tracking when a suitable progress supplier is passed.
     * Recommended for debugging and when no parallelism overhead is desired (e.g., for simpler computations).
     *
     * @param tryHitCache      whether the cache should be queried for the result
     * @param tryWriteCache    whether the result should be stored in the cache
     * @param progressSupplier the progress supplier
     */
    Result<T> computeResult(boolean tryHitCache, boolean tryWriteCache, Supplier<Progress> progressSupplier);

    /**
     * {@return the (synchronous) result of this computation}
     * Progress tracking is enabled only when the cache is written to, which is the only way to access the progress.
     *
     * @param tryHitCache   whether the cache should be queried for the result
     * @param tryWriteCache whether the result should be stored in the cache
     * @see #computeResult(boolean, boolean, Supplier)
     */
    default Result<T> computeResult(boolean tryHitCache, boolean tryWriteCache) {
        return computeResult(tryHitCache, tryWriteCache, tryWriteCache ? Progress::new : () -> Progress.NULL);
    }

    /**
     * {@return the (synchronous and cached) result of this computation}
     *
     * @see #computeResult(boolean, boolean, Supplier)
     */
    default Result<T> computeResult() {
        return computeResult(true, true);
    }

    /**
     * {@return the result of this computation or, in case of a timeout or an error, an intermediate result, if available}
     * Blocks until the computation is finished or until the timeout is reached.
     *
     * @param tryHitCache   whether the cache should be queried for the result
     * @param tryWriteCache whether the result should be stored in the cache
     * @param timeout       the timeout
     *
     * @see #computeFutureResult(boolean, boolean)
     */
    default Result<T> computeResult(boolean tryHitCache, boolean tryWriteCache, Duration timeout) {
        try {
            return computeFutureResult(tryHitCache, tryWriteCache)
                    .getPromise()
                    .onTimeout(
                            () -> getIntermediateResult()
                                    .addProblemInformation(new Problem(new TimeoutException(
                                            String.format("Timeout of %ss was reached.", timeout.getSeconds())))),
                            timeout,
                            true)
                    .get();
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    /**
     * {@return the (cached) result of this computation or, in case of a timeout or an error, an intermediate result, if available}
     * Blocks until the computation is finished or until the timeout is reached.
     *
     * @param timeout the timeout
     *
     * @see #computeFutureResult(boolean, boolean)
     */
    default Result<T> computeResult(Duration timeout) {
        return computeResult(true, true, timeout);
    }

    /**
     * {@return the (cached) result of this computation. Uses the given progress supplier to create Progress instances for each computation.}
     *
     * @param progressSupplier the progress supplier
     *
     * @see #computeResult(boolean, boolean, Supplier)
     */
    default Result<T> computeResult(Supplier<Progress> progressSupplier) {
        return computeResult(true, true, progressSupplier);
    }

    /**
     * {@return the (synchronous and uncached) result of this computation}
     *
     * @see #computeResult(boolean, boolean, Supplier)
     */
    default Result<T> computeUncachedResult() {
        return computeResult(false, false);
    }

    /**
     * {@return the (synchronous and cached) result of this computation}
     * Emulates a synchronous mode of computation by internally awaiting the {@link FutureResult} of this computation.
     * In contrast to {@link #computeResult()}, this method leverages parallelism where possible (possibly with overhead).
     *
     * @see #computeFutureResult(boolean, boolean)
     */
    default Result<T> parallelComputeResult() {
        return computeFutureResult().get();
    }

    /**
     * {@return the (synchronous and uncached) result of this computation}
     * Emulates a synchronous mode of computation by internally awaiting the {@link FutureResult} of this computation.
     * In contrast to {@link #computeUncachedResult()}, this method leverages parallelism where possible (possibly with overhead).
     *
     * @see #computeFutureResult(boolean, boolean)
     */
    default Result<T> parallelComputeUncachedResult() {
        return computeUncachedFutureResult().get();
    }

    /**
     * {@return the cache this computation should be stored and looked up in}
     */
    default Cache getCache() {
        return FeatJAR.cache();
    }

    /**
     * {@return for a given list of results, a result of a dependency list}
     * If not overridden, merges a list of n non-empty results into a non-empty result of a dependency list of length n.
     * If any result in the given list is empty, returns an empty result.
     * To allow for (and detect) empty results, this can be overridden (e.g., in {@link ComputePresence}).
     *
     * @param results the results
     */
    default Result<List<Object>> mergeResults(List<? extends Result<?>> results) {
        return Result.mergeAll(results, ArrayList::new);
    }

    /**
     * {@return the (synchronous and cached) result of this computation}
     */
    @Override
    default Result<T> get() {
        return parallelComputeResult();
    }

    /**
     * {@return a stream containing the (synchronous and cached) result of this computation}
     * If the result is empty, the stream is empty as well.
     */
    default Stream<T> stream() {
        return Stream.generate(this).limit(1).filter(Result::isPresent).map(Result::get);
    }

    /**
     * {@return the value returned by a given function applied to this computation}
     * Typically, this returns a new computation composed with this computation.
     *
     * @param fn the function
     * @param <U> the return type of the given function
     */
    default <U> U map(Function<IComputation<T>, U> fn) {
        return fn.apply(this);
    }

    /**
     * {@return peeks at this computation with a given function}
     *
     * @param fn this computation
     * @param <U> the return type of the given function
     */
    @SuppressWarnings("unchecked")
    default <U extends IComputation<T>> IComputation<T> peek(Consumer<U> fn) {
        fn.accept((U) this);
        return this;
    }

    /**
     * {@return a computation that maps the result of this computation to another value}
     * To allow proper caching, a unique combination of the calling class and scope (e.g., a method name)
     * must be supplied.
     *
     * @param klass the calling class
     * @param scope the calling scope
     * @param fn    the function
     * @param <U>   the type of the mapped result
     */
    default <U> IComputation<U> mapResult(Class<?> klass, String scope, Function<T, U> fn) {
        return flatMapResult(klass, scope, t -> Result.ofNullable(fn.apply(t)));
    }

    /**
     * {@return a new computation that cast the result of another computation to a new type}
     * @param <U> the new type
     * @param newType the class object of the new type
     */
    default <U> IComputation<U> cast(Class<U> newType) {
        return mapResult(newType, "castTo", i -> newType.cast(i));
    }

    /**
     * {@return all dependencies of this computation}
     */
    default List<Dependency<?>> getDependencies() {
        return Dependency.getDependencyList(getClass());
    }

    /**
     * {@return a computation for the given dependency}
     *
     * @param <U> the type of the dependency
     * @param dependency the dependency
     */
    @SuppressWarnings("unchecked")
    default <U> Result<IComputation<U>> getDependencyComputation(Dependency<U> dependency) {
        return getChild(dependency.getIndex()).map(c -> (IComputation<U>) c);
    }

    /**
     * Adds a computation as a dependency of this computation.
     *
     * @param <U> The type of the dependency
     * @param dependency The dependency identifier
     * @param computation The computation to compute the actual value of the dependency
     *
     * @return This computation
     *
     * @see #set(Dependency, IComputation)
     */
    default <U> IComputation<T> setDependencyComputation(
            Dependency<U> dependency, IComputation<? extends U> computation) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a value as a dependency of this computation.<p>
     * This value is wrapped as a constant computation. Thus it behaves exactly as {@code setDependencyComputation(dependency, Computations.of(value))} would.
     *
     * @param <U> The type of the dependency
     * @param dependency The dependency identifier
     * @param value The value of the dependency
     *
     * @return This computation
     *
     * @see #set(Dependency, Object)
     */
    default <U> IComputation<T> setDependency(Dependency<U> dependency, U value) {
        return setDependencyComputation(dependency, Computations.of(value));
    }

    /**
     * Adds a value as a dependency of this computation.
     *
     * @param <U> The type of the dependency
     * @param dependency The dependency identifier
     * @param value The value of the dependency
     *
     * @return This computation
     */
    default <U> IComputation<T> set(Dependency<U> dependency, U value) {
        return setDependency(dependency, value);
    }

    /**
     * Adds a computation as a dependency of this computation.
     *
     * @param <U> The type of the dependency
     * @param dependency The dependency identifier
     * @param computation The computation to compute the actual value of the dependency
     *
     * @return This computation
     */
    default <U> IComputation<T> set(Dependency<U> dependency, IComputation<U> computation) {
        return setDependencyComputation(dependency, computation);
    }

    /**
     * {@return a computation that maps the result of this computation to another value}
     * To allow proper caching, a unique combination of the calling class and scope (e.g., a method name)
     * must be supplied.
     *
     * @param klass the calling class
     * @param scope the calling scope
     * @param fn    the function
     * @param <U>   the type of the mapped result
     */
    default <U> IComputation<U> flatMapResult(Class<?> klass, String scope, Function<T, Result<U>> fn) {
        return new ComputeFunction<>(this, klass, scope, fn);
    }

    /**
     * {@return a computation that peeks at the result of this computation with a given function}
     *
     * @param klass the calling class
     * @param scope the calling scope
     * @param fn the consumer function
     */
    default IComputation<T> peekResult(Class<?> klass, String scope, Consumer<T> fn) {
        return mapResult(klass, scope, t -> {
            fn.accept(t);
            return t;
        });
    }

    // TODO: serialization scheme. may require that all inputs (all dependencies) implement Serializable.
    default byte[] serialize() {
        return new byte[] {};
    }

    // TODO: validate whether a computation is sensible.
    //  maybe by encoding valid computations in a feature model, or some other way.
    default boolean validate() {
        return true;
    }

    // TODO: "magically" complete incomplete computation specifications with a suitable feature model (in a separate
    // module).
    //  it may also be nice to denote THE canonical best input for a computation (in a separate module).
}
