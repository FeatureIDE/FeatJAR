/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.analysis;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import java.time.Duration;

/**
 * Solves problems expressed as logical formulas.
 * Is capable of performing various basic analyses.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface ISolver {
    Duration getTimeout();

    void setTimeout(Duration timeout);

    boolean isTimeoutOccurred();

    default <T> Result<T> createResult(T result) {
        return createResult(Result.of(result));
    }

    default <T> Result<T> createResult(Result<T> result) {
        return createResult(result, null);
    }

    default <T> Result<T> createResult(T result, String timeoutExplanation) {
        return createResult(Result.of(result), timeoutExplanation);
    }

    default <T> Result<T> createResult(Result<T> result, String timeoutExplanation) {
        return isTimeoutOccurred() ? result.nullify(getTimeoutProblem(timeoutExplanation)) : result;
    }

    static Problem getTimeoutProblem(String timeoutExplanation) {
        return new Problem(
                "solver timeout occurred" + (timeoutExplanation != null ? ", " + timeoutExplanation : ""),
                Problem.Severity.WARNING);
    }
}
