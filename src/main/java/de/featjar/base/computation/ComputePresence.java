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
 * Computes whether the supplied input is present.
 *
 * @param <T> the type of the input
 * @author Elias Kuiter
 */
public class ComputePresence<T> extends AComputation<Boolean> {
    protected static final Dependency<?> INPUT = Dependency.newDependency();

    public ComputePresence(IComputation<T> input) {
        super(input);
    }

    protected ComputePresence(ComputePresence<T> other) {
        super(other);
    }

    @Override
    public Result<List<Object>> mergeResults(List<? extends Result<?>> results) {
        return Result.mergeAllNullable(results, ArrayList::new);
    }

    @Override
    public Result<Boolean> compute(List<Object> dependencyList, Progress progress) {
        return Result.of(INPUT.getValue(dependencyList) != null);
    }
}
