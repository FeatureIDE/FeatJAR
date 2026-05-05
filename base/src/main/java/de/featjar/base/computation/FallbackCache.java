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
import java.util.ArrayList;
import java.util.List;

/**
 * Caches nothing.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class FallbackCache extends Cache {

    public FallbackCache() {
        super();
        configuration = new Configuration();
    }

    public <T> Result<FutureResult<T>> tryHit(IComputation<T> computation) {
        return Result.empty();
    }

    public <T> void tryWrite(IComputation<T> computation, FutureResult<T> futureResult) {}

    public <T> boolean has(IComputation<T> computation) {
        return false;
    }

    public <T> Result<FutureResult<T>> get(IComputation<T> computation) {
        return Result.empty();
    }

    public <T> boolean put(IComputation<T> computation, FutureResult<T> futureResult) {
        return false;
    }

    public <T> boolean remove(IComputation<T> computation) {
        return false;
    }

    public void clear() {}

    public Long getNumberOfHits(IComputation<?> computation) {
        return 0L;
    }

    public Result<Double> getProgress(IComputation<?> computation) {
        return Result.empty();
    }

    public List<IComputation<?>> getCachedComputations() {
        return new ArrayList<>();
    }
}
