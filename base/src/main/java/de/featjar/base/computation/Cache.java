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
import de.featjar.base.env.IBrowsable;
import de.featjar.base.env.StackTrace;
import de.featjar.base.extension.IInitializer;
import de.featjar.base.io.graphviz.GraphVizTreeFormat;
import de.featjar.base.tree.structure.ITree;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Caches computation results by storing a map of computations to their future results.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Cache implements IInitializer, IBrowsable<GraphVizTreeFormat<IComputation<?>>> {
    /**
     * Specifies which computation results a cache should contain.
     */
    public interface CachePolicy {
        /**
         * Caches no computation results.
         */
        CachePolicy CACHE_NONE = (computation, stackTrace) -> false;

        /**
         * Caches all computation results, even those nested in other computations.
         */
        CachePolicy CACHE_ALL = (computation, stackTrace) -> true;

        /**
         * Caches top-level computation results; that is, those not nested in other computations.
         * Nested computations are detected by checking if {@link IComputation#compute(List, Progress)} is already on the stack.
         */
        CachePolicy CACHE_TOP_LEVEL =
                (computation, stackTrace) -> !stackTrace.containsMethodCall(IComputation.class, "compute");

        /**
         * {@return whether the calling cache should store the given computation}
         *
         * @param computation the computation
         * @param stackTrace  the current stack trace
         */
        boolean shouldCache(IComputation<?> computation, StackTrace stackTrace);
    }

    /**
     * Configures a cache.
     */
    public static class Configuration {

        /**
         * The {@link CachePolicy} to use.
         */
        protected CachePolicy cachePolicy = CachePolicy.CACHE_NONE;

        /**
         * The {@link Executor} for computations.
         */
        protected Executor executor = Executors.newCachedThreadPool();

        /**
         * Configures the cache policy.
         *
         * @param cachePolicy the cache policy
         * @return this configuration
         */
        public Configuration setCachePolicy(CachePolicy cachePolicy) {
            this.cachePolicy = cachePolicy;
            return this;
        }

        /**
         * Configures the executor.
         *
         * @param executor the executor
         * @return this configuration
         */
        public Configuration setExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }
    }

    /**
     * This cache's configuration.
     */
    protected Configuration configuration;

    /**
     * A cache that maps computations to their future results.
     * A {@link IComputation} of type {@code T} should be mapped to a {@link FutureResult} of the same type {@code T}.
     */
    protected final Map<IComputation<?>, FutureResult<?>> computationMap = new ConcurrentHashMap<>();

    /**
     * Statistic for cache hits per computation.
     */
    protected final Map<IComputation<?>, Long> hitStatistics = new HashMap<>();

    /**
     * Creates a cache without configuration.
     */
    public Cache() {
        FeatJAR.log().debug("initializing cache");
    }

    /**
     * Creates a cache.
     *
     * @param configuration the configuration
     */
    public Cache(Cache.Configuration configuration) {
        this();
        setConfiguration(configuration);
    }

    /**
     * {@inheritDoc}
     * Clears this cache.
     */
    @Override
    public void close() {
        FeatJAR.log().debug("de-initializing cache");
        clear();
    }

    /**
     * {@return this cache's configuration}
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets this cache's configuration.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(Configuration configuration) {
        FeatJAR.log().debug("setting new cache configuration");
        this.configuration = configuration;
    }

    /**
     * {@return the future result stored in this cache for the given computation, if any}
     *
     * @param computation the computation
     * @param <T>         the type of the computation result
     */
    @SuppressWarnings("unchecked")
    public <T> Result<FutureResult<T>> tryHit(IComputation<T> computation) {
        FutureResult<T> futureResult;
        synchronized (computationMap) {
            futureResult = (FutureResult<T>) computationMap.get(computation);
            if (futureResult != null
                    && (futureResult.getPromise().isCancelled()
                            || futureResult.getPromise().isCompletedExceptionally())) {
                computationMap.put(computation, null);
                futureResult = null;
            }
        }
        if (futureResult != null) {
            //            FeatJAR.log().debug("cache hit for " + computation);
            synchronized (hitStatistics) {
                Long count = hitStatistics.get(computation);
                if (count == null) {
                    hitStatistics.put(computation, 1L);
                } else {
                    hitStatistics.put(computation, count + 1);
                }
            }
            return Result.of(futureResult);
        }
        FeatJAR.log().debug("cache miss for " + computation);
        return Result.empty();
    }

    /**
     * Stores the given future result for the given computation if the current {@link CachePolicy} agrees.
     *
     * @param computation  the computation
     * @param futureResult the future result
     * @param <T>          the type of the computation result
     */
    public <T> void tryWrite(IComputation<T> computation, FutureResult<T> futureResult) {
        if (configuration.cachePolicy.shouldCache(computation, new StackTrace())) {
            FeatJAR.log().debug("cache write for " + computation);
            put(computation, futureResult);
        }
    }

    /**
     * {@return whether the given computation has been cached in this cache}
     *
     * @param computation the computation
     * @param <T>         the type of the computation result
     */
    public <T> boolean has(IComputation<T> computation) {
        return computationMap.containsKey(computation);
    }

    /**
     * {@return the cached result of a given computation, if any}
     *
     * @param computation the computation
     * @param <T>         the type of the computation result
     */
    @SuppressWarnings("unchecked")
    public <T> Result<FutureResult<T>> get(IComputation<T> computation) {
        return Result.ofNullable((FutureResult<T>) computationMap.get(computation));
    }

    /**
     * Sets the cached result for a given computation, if not already cached.
     * Does nothing if the computation has already been cached.
     *
     * @param computation  the computation
     * @param futureResult the future result
     * @param <T>          the type of the computation result
     * @return whether the operation affected this cache
     */
    public <T> boolean put(IComputation<T> computation, FutureResult<T> futureResult) {
        if (has(computation)) // once set, immutable
        return false;
        computationMap.put(computation, futureResult);
        return true;
    }

    /**
     * Removes the cached result for a given computation, if already cached.
     * Does nothing if the computation has not already been cached.
     *
     * @param computation the computation
     * @param <T>         the type of the computation result
     * @return whether the operation affected this cache
     */
    public <T> boolean remove(IComputation<T> computation) {
        if (!has(computation)) return false;
        FeatJAR.log().debug("cache remove for " + computation);
        computationMap.remove(computation);
        return true;
    }

    /**
     * Removes all cached computation results.
     */
    public void clear() {
        FeatJAR.log().debug("clearing cache");
        computationMap.clear();
    }

    /**
     * {@return the number of cache hits for the given computation}
     *
     * @param computation the computation
     */
    public Long getNumberOfHits(IComputation<?> computation) {
        Long hits;
        synchronized (hitStatistics) {
            hits = hitStatistics.get(computation);
        }
        return Result.ofNullable(hits).orElse(0L);
    }

    /**
     * {@return the progress of the given computation}
     * The progress is equally weighted over all direct dependencies of the computation.
     * That is, if a computation with two direct dependencies has just started, the progress is 2/3.
     *
     * @param computation the computation
     */
    public Result<Double> getProgress(IComputation<?> computation) {
        List<Double> progresses = new ArrayList<>();
        get(computation).map(FutureResult::getProgress).map(Progress::get).ifPresent(progresses::add);
        progresses.addAll(computation.getChildren().stream()
                .map(this::getProgress)
                .filter(Result::isPresent)
                .map(Result::get)
                .collect(Collectors.toList()));
        if (progresses.isEmpty()) return Result.empty();
        return Result.of(progresses.stream().reduce(Double::sum).get() / progresses.size());
    }

    /**
     * {@return all computations in this cache}
     * Is sorted by hash code to guarantee determinism.
     */
    public List<IComputation<?>> getCachedComputations() {
        ArrayList<IComputation<?>> computations = new ArrayList<>(computationMap.keySet());
        computations.sort(Comparator.comparingInt(ITree::hashCodeTree));
        return computations;
    }

    /**
     * {@return a computation that depends on all computations in this cache}
     * Can be used to inspect the state of the cache.
     * If computed, guarantees quiescence (i.e., all running computations are done).
     */
    public IComputation<List<?>> getCacheComputation() {
        return Computations.allOf(getCachedComputations());
    }

    @Override
    public Result<URI> getBrowseURI(GraphVizTreeFormat<IComputation<?>> graphVizComputationTreeFormat) {
        graphVizComputationTreeFormat.setIncludeRoot(false);
        return getCacheComputation().getBrowseURI(graphVizComputationTreeFormat);
    }
}
