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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.tascalate.concurrent.CompletableTask;
import net.tascalate.concurrent.DependentPromise;
import net.tascalate.concurrent.PromiseOrigin;

/**
 * A result that will become available in the future.
 * A {@link FutureResult} combines a {@link java.util.concurrent.Future} (which allows for asynchronous
 * calculations) with a {@link Result} (which tracks errors).
 * It wraps a {@link Result} that is not available yet, but will become available in the future.
 * It can be converted into a {@link Result} by calling {@link #get()}, which blocks until the result is available.
 * It can be chained with other calculations by calling {@link #thenFromResult(BiFunction)}}.
 * Once the result is available, it is cached indefinitely and can be retrieved with {@link #get()}.
 *
 * @param <T> the type of the result
 * @author Elias Kuiter
 */
public class FutureResult<T> implements Supplier<Result<T>> {
    protected final DependentPromise<Result<T>> promise;

    protected final Progress progress;

    /**
     * Creates a future result completed with a given result.
     *
     * @param result the result
     * @param progress the progress object for the computation
     */
    public FutureResult(Result<T> result, Progress progress) {
        this(DependentPromise.from(CompletableTask.completed(result, getExecutor())), progress);
    }

    /**
     * Creates a future result from a given promise.
     *
     * @param promise the promise
     * @param progress the progress object for the computation
     */
    public FutureResult(DependentPromise<Result<T>> promise, Progress progress) {
        this.promise = promise;
        this.progress = progress;
    }

    public static Executor getExecutor() {
        return FeatJAR.cache().getConfiguration().executor;
    }

    private static <T> Result<T> compute(IComputation<T> computation, List<Object> args, Progress progress) {
        if (Thread.interrupted()) {
            throw new CancellationException();
        }
        try (progress) {
            FeatJAR.progress().track(progress);
            return computation.compute(args, progress);
        }
    }

    /**
     * {@return a future result from given computation that resolves when all dependencies are resolved}
     *
     * @param computation the computation
     * @param tryHitCache whether to try to read from the cache
     * @param tryWriteCache whether to try to write to the cache
     * @param progressSupplier creates a {@link Progress} for each future result
     */
    @SuppressWarnings("unchecked")
    public static <U, T extends List<Object>> FutureResult<U> compute(
            IComputation<U> computation,
            boolean tryHitCache,
            boolean tryWriteCache,
            Supplier<Progress> progressSupplier) {
        Progress progress = progressSupplier.get();
        progress.setName(computation.toString());

        if (computation instanceof ComputeConstant) {
            return new FutureResult<>(
                    DependentPromise.from(
                            CompletableTask.submit(() -> compute(computation, List.of(), progress), getExecutor()),
                            PromiseOrigin.ALL),
                    progress);
        }

        if (tryHitCache) {
            Result<FutureResult<U>> cacheHit = FeatJAR.cache().tryHit(computation);
            if (cacheHit.isPresent()) {
                Result<U> result = cacheHit.get().getPromise().getNow(Result.<U>empty());
                if (result.isPresent()) {
                    return new FutureResult<>(
                            DependentPromise.from(CompletableTask.completed(result, getExecutor()), PromiseOrigin.ALL),
                            progress);
                }
            }
        }

        DependentPromise<Result<U>> promise;
        if (!computation.hasChildren()) {
            promise = DependentPromise.from(
                    CompletableTask.submit(() -> compute(computation, List.of(), progress), getExecutor()),
                    PromiseOrigin.ALL);
        } else {
            DependentPromise<List<Object>> allOf = null;
            for (IComputation<?> child : computation.getChildren()) {
                if (allOf == null) {
                    allOf = compute(child, tryHitCache, tryWriteCache, progressSupplier)
                            .getPromise()
                            .thenApplyAsync(
                                    r -> {
                                        List<Object> list = new ArrayList<>();
                                        list.add(r);
                                        return list;
                                    },
                                    getExecutor(),
                                    true);
                } else {
                    allOf = allOf.thenCombineAsync(
                            compute(child, tryHitCache, tryWriteCache, progressSupplier)
                                    .getPromise(),
                            (a, b) -> {
                                List<Object> list = (List<Object>) a;
                                list.add(b);
                                return list;
                            },
                            getExecutor(),
                            PromiseOrigin.ALL);
                }
            }
            promise = allOf.thenApplyAsync(
                    list -> compute(
                            computation,
                            computation
                                    .mergeResults(list.stream()
                                            .map(r -> (Result<Object>) r)
                                            .collect(Collectors.toList()))
                                    .get(),
                            progress),
                    getExecutor(),
                    true);
        }

        FutureResult<U> futureResult = new FutureResult<>(promise, progress);
        if (tryWriteCache) {
            // TODO write only result to cache not futureResult
            FeatJAR.cache().tryWrite(computation, futureResult);
        }
        return futureResult;
    }

    /**
     * {@return this future result's promise}
     */
    public DependentPromise<Result<T>> getPromise() {
        return promise;
    }

    /**
     * {@return this future result's progress}
     */
    public Progress getProgress() {
        if (getPromise().isDone()) return Progress.completed(progress.getCurrentStep());
        return progress;
    }

    /**
     * {@return a future result that composes this future result with the given function}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> then(BiFunction<T, Progress, U> fn) {
        return thenFromResult(mapArgumentAndReturnValue(fn));
    }

    /**
     * {@return a future result that composes this future result with the given function that returns a result}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> thenResult(BiFunction<T, Progress, Result<U>> fn) {
        return thenFromResult(mapArgument(fn));
    }

    /**
     * {@return a future result that composes this future result with the given function that operates on results}
     *
     * @param fn  the function
     * @param <U> the type of the future result
     */
    public <U> FutureResult<U> thenFromResult(BiFunction<Result<T>, Progress, Result<U>> fn) {
        Progress progress = new Progress();
        return new FutureResult<>(
                promise.thenApplyAsync(
                        tResult -> {
                            try {
                                Result<U> uResult = fn.apply(tResult, progress);
                                return (tResult == null)
                                        ? uResult
                                        : uResult.addProblemInformation(tResult.getProblems());
                            } catch (Exception e) {
                                return (tResult == null) ? Result.empty() : tResult.nullify(e);
                            }
                        },
                        getExecutor()),
                progress);
    }

    protected static <T, U> BiFunction<Result<T>, Progress, Result<U>> mapArgument(
            BiFunction<T, Progress, Result<U>> fn) {
        return (tResult, progress) -> tResult.isPresent() ? fn.apply(tResult.get(), progress) : Result.empty();
    }

    protected static <T, U> BiFunction<Result<T>, Progress, Result<U>> mapArgumentAndReturnValue(
            BiFunction<T, Progress, U> fn) {
        return (tResult, progress) ->
                tResult.isPresent() ? Result.ofNullable(fn.apply(tResult.get(), progress)) : Result.empty();
    }

    /**
     * {@return this future result's result}
     * Blocks synchronously until the result is available.
     */
    public Result<T> get() {
        try {
            return promise.get();
        } catch (InterruptedException | ExecutionException | CancellationException e) {
            return Result.empty(e);
        }
    }

    /**
     * {@return this future result's result}
     * Blocks synchronously until the result is available or until the timeout is reached.
     *
     * @param timeout the timeout
     */
    public Result<T> get(Duration timeout) {
        try {
            return promise.orTimeout(timeout).get();
        } catch (InterruptedException | ExecutionException | CancellationException e) {
            return Result.empty(e);
        }
    }

    /**
     * Cancels the execution of this future result's promise when a given duration has passed.
     * Discards any partially computed result.
     *
     * @param duration the duration
     */
    public void cancelAfter(Duration duration) {
        promise.orTimeout(duration);
    }

    /**
     * Cancels the execution of this future result's promise.
     * Discards any partially computed result.
     */
    public void cancel() {
        promise.cancel(true);
    }

    /**
     * Runs a function when a given duration has passed and the promise is not resolved yet.
     *
     * @param duration the duration
     * @param fn the function
     */
    public void peekAfter(Duration duration, Runnable fn) {
        Result<T> result = Result.empty(new Problem("timeout occurred"));
        promise.onTimeout(result, duration, false).thenApplyAsync(r -> {
            if (result.equals(r)) {
                fn.run();
            }
            return null;
        });
    }

    /**
     * Runs a function regularly at a given interval until the promise is resolved.
     *
     * @param interval the interval
     * @param fn the function
     */
    public void peekEvery(Duration interval, Runnable fn) {
        peekAfter(interval, () -> {
            fn.run();
            peekEvery(interval, fn);
        });
    }
}
