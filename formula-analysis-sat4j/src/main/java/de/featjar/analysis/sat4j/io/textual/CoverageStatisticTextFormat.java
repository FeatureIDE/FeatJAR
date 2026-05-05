/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j.io.textual;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.CoverageStatistic;

/**
 * Serializes an arbitrary object as text, as it is returned by {@link Object#toString()}.
 *
 * @author Sebastian Krieter
 */
public class CoverageStatisticTextFormat implements IFormat<CoverageStatistic> {

    private boolean coverageOnly;
    private boolean countOnly;

    public CoverageStatisticTextFormat(boolean coverageOnly, boolean countOnly) {
        this.coverageOnly = coverageOnly;
        this.countOnly = countOnly;
    }

    @Override
    public String getName() {
        return "CoverageStatistic";
    }

    @Override
    public String getFileExtension() {
        return "txt";
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    public boolean isCoverageOnly() {
        return coverageOnly;
    }

    public void setCoverageOnly(boolean coverageOnly) {
        this.coverageOnly = coverageOnly;
    }

    public boolean isCountOnly() {
        return countOnly;
    }

    public void setCountOnly(boolean countOnly) {
        this.countOnly = countOnly;
    }

    @Override
    public Result<String> serialize(CoverageStatistic statistic) {
        return Result.of(
                coverageOnly
                        ? countOnly
                                ? statistic.coverage() + "\n" + statistic.covered() + "\n" + statistic.uncovered()
                                        + "\n" + statistic.invalid() + "\n" + statistic.ignored()
                                : String.valueOf(statistic.coverage())
                        : countOnly
                                ? statistic.covered() + "\n" + statistic.uncovered() + "\n" + statistic.invalid() + "\n"
                                        + statistic.ignored()
                                : statistic.print());
    }
}
