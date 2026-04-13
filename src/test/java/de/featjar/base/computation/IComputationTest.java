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

import static de.featjar.base.computation.Computations.async;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.FeatJAR;
import de.featjar.base.FeatJAR.Configuration;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.log.CallerFormatter;
import de.featjar.base.log.Log;
import de.featjar.base.log.TimeStampFormatter;
import de.featjar.base.log.VerbosityFormatter;
import de.featjar.base.tree.structure.ITree;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class IComputationTest {
    @Test
    void simpleComputation() {
        {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.get().get());
            assertFalse(FeatJAR.cache().has(computation));
        }
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());

        FeatJAR.run(fj -> {
            IComputation<Integer> computation = Computations.of(42).flatMapResult(getClass(), "42", i -> Result.of(i));
            assertEquals(42, computation.get().get());
            assertFalse(FeatJAR.cache().has(computation));
            assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());
        });
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());

        FeatJAR.run(fj -> {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.get().get());
            assertFalse(FeatJAR.cache().has(computation));
            assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());
        });
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());

        {
            IComputation<Integer> computation = Computations.of(42).flatMapResult(getClass(), "42", i -> Result.of(i));
            assertEquals(42, computation.get().get());
            assertFalse(FeatJAR.cache().has(computation));
        }
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());

        final Configuration configuration = new Configuration();
        configuration
                .logConfig
                .logToSystemErr(Log.Verbosity.ERROR, Log.Verbosity.WARNING)
                .logToSystemOut(Log.Verbosity.MESSAGE, Log.Verbosity.INFO)
                .addFormatter(new TimeStampFormatter())
                .addFormatter(new VerbosityFormatter())
                .addFormatter(new CallerFormatter());
        configuration.cacheConfig.setCachePolicy(Cache.CachePolicy.CACHE_TOP_LEVEL);

        FeatJAR.run(configuration, fj -> {
            IComputation<Integer> computation = Computations.of(42).flatMapResult(getClass(), "42", i -> Result.of(i));
            assertEquals(42, computation.get().get());
            assertTrue(FeatJAR.cache().has(computation));
            assertFalse(FeatJAR.cache().getCachedComputations().isEmpty());
        });
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());

        FeatJAR.run(configuration, fj -> {
            IComputation<Integer> computation = Computations.of(42);
            assertEquals(42, computation.get().get());
            assertFalse(FeatJAR.cache().has(computation));
            assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());
        });
        assertTrue(FeatJAR.cache().getCachedComputations().isEmpty());
    }

    static class ComputeIsEven extends AComputation<Boolean> {
        protected static Dependency<Integer> INPUT = Dependency.newDependency(Integer.class);

        public ComputeIsEven(IComputation<Integer> input) {
            super(input);
        }

        protected ComputeIsEven(ComputeIsEven other) {
            super(other);
        }

        @Override
        public Result<Boolean> compute(List<Object> dependencyList, Progress progress) {
            return Result.of(INPUT.get(dependencyList) % 2 == 0);
        }
    }

    @Test
    void chainedComputation() {
        IComputation<Integer> computation = Computations.of(42);
        IComputation<Boolean> isEvenComputation = new ComputeIsEven(async(42));
        assertTrue(isEvenComputation.get().get());
        assertTrue(computation.map(ComputeIsEven::new).get().get());
        assertTrue(computation.map(ComputeIsEven::new).get().get());
    }

    static class ComputeIsParity extends AComputation<Boolean> {
        @Override
        public ITree<IComputation<?>> cloneNode() {
            return null;
        }

        enum Parity {
            EVEN,
            ODD
        }

        protected static Dependency<Integer> INPUT = Dependency.newDependency(Integer.class);
        protected static Dependency<Parity> PARITY = Dependency.newDependency(Parity.class);

        public ComputeIsParity(IComputation<Integer> input, IComputation<Parity> parity) {
            super(input, parity);
        }

        @Override
        public Result<Boolean> compute(List<Object> dependencyList, Progress progress) {
            boolean c = Math.abs(INPUT.get(dependencyList)) % 2 == 0;
            boolean d = Math.abs(INPUT.get(dependencyList)) % 2 == 1;
            boolean b = PARITY.get(dependencyList) == Parity.EVEN ? c : d;
            return Result.of(b);
        }
    }

    @Test
    void computationWithArguments() {
        IComputation<Integer> computation = Computations.of(42);
        assertTrue(new ComputeIsParity(computation, async(ComputeIsParity.Parity.EVEN))
                .get()
                .get());
        assertFalse(new ComputeIsParity(computation, async(ComputeIsParity.Parity.ODD))
                .get()
                .get());
        assertTrue(computation
                .map(c -> new ComputeIsParity(c, async(ComputeIsParity.Parity.EVEN)))
                .get()
                .get());
    }

    @Test
    void allOfSimple() {
        Pair<Integer, Integer> r =
                Computations.of(Computations.of(1), Computations.of(2)).get().get();
        assertEquals(1, r.getKey());
        assertEquals(2, r.getValue());
    }

    @Test
    void allOfComplex() {
        IComputation<Integer> c1 = Computations.of(42);
        IComputation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = Computations.of(c1, c2).get().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }

    @Test
    void allOfSleep() {
        IComputation<Integer> c1 = new AComputation<>() {
            @Override
            public Result<Integer> compute(List<Object> dependencyList, Progress progress) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return Result.of(42);
            }

            @Override
            public ITree<IComputation<?>> cloneNode() {
                return null;
            }
        };
        IComputation<Boolean> c2 = c1.map(ComputeIsEven::new);
        Pair<Integer, Boolean> r = Computations.of(c1, c2).get().get();
        assertEquals(42, r.getKey());
        assertEquals(true, r.getValue());
    }

    <T> void testCaching(Supplier<IComputation<T>> computationSupplier) {
        // computationSupplier.get().get()
        // cache should have changed
        // computationSupplier.get().get()
        // cache should not have changed
    }

    static class WaitCompute extends AComputation<Object> {
        private static final Dependency<?> INPUT = Dependency.newDependency();
        private boolean completed;

        public WaitCompute(IComputation<Object> input) {
            super(input);
        }

        protected WaitCompute(WaitCompute other) {
            super(other);
        }

        @Override
        public Result<Object> compute(List<Object> dependencyList, Progress progress) {
            Object x = INPUT.get(dependencyList);
            try {
                Thread.sleep(10000);
                completed = true;
            } catch (InterruptedException e) {
            }
            return Result.of(x);
        }
    }

    static class WaitCompute2 extends AComputation<Object> {
        private static final Dependency<?> INPUT = Dependency.newDependency();

        public WaitCompute2(IComputation<Object> input) {
            super(input);
        }

        protected WaitCompute2(WaitCompute other) {
            super(other);
        }

        @Override
        public Result<Object> compute(List<Object> dependencyList, Progress progress) {
            Object x = INPUT.get(dependencyList);
            checkCancel();
            return Result.of(x);
        }
    }

    @Test
    void futureCanBeCanceled() {
        WaitCompute computation1 = new WaitCompute(Computations.of(125));
        FutureResult<Object> computeFutureResult =
                computation1.map(WaitCompute::new).computeFutureResult();
        computeFutureResult.cancelAfter(Duration.ofMillis(10));
        assertNull(computeFutureResult.get().orElse(null));
        assertFalse(computation1.completed);
    }

    @Test
    void futureDoesNotCompleteWhenCanceled() {
        WaitCompute computation1 = new WaitCompute(Computations.of(125));
        FutureResult<Object> computeFutureResult =
                computation1.map(WaitCompute2::new).computeFutureResult();
        computeFutureResult.cancel();
        assertNull(computeFutureResult.get().orElse(null));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        assertFalse(computation1.completed);
    }
}
