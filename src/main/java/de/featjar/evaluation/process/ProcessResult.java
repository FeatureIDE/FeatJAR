/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-evaluation.
 *
 * evaluation is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with evaluation. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-evaluation> for further information.
 */
package de.featjar.evaluation.process;

import de.featjar.base.data.Result;

public class ProcessResult<R> {

    public static final long INVALID_TIME = -1;

    private boolean terminatedInTime;
    private boolean noError;
    private long time = INVALID_TIME;
    private Result<R> result;

    public boolean isTerminatedInTime() {
        return terminatedInTime;
    }

    public void setTerminatedInTime(boolean terminatedInTime) {
        this.terminatedInTime = terminatedInTime;
    }

    public boolean isNoError() {
        return noError;
    }

    public void setNoError(boolean noError) {
        this.noError = noError;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Result<R> getResult() {
        return result == null ? Result.empty() : result;
    }

    public void setResult(Result<R> result) {
        this.result = result;
    }
}
