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
package de.featjar.base.io.graphviz;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Serializes computations to GraphViz DOT files.
 *
 * @author Elias Kuiter
 */
public class GraphVizComputationTreeFormat extends GraphVizTreeFormat<IComputation<?>> {
    protected boolean includeResults = true;

    public boolean isIncludeResults() {
        return includeResults;
    }

    public void setIncludeResults(boolean includeResults) {
        this.includeResults = includeResults;
    }

    @Override
    protected String getNodeOptions(IComputation<?> computation) {
        if (!includeResults) return options(option("label", shorten(computation.toString())));
        long numberOfHits = FeatJAR.cache().getNumberOfHits(computation);
        Result<?> result = computation.get();
        String resultString = Objects.toString(result.orElse(null));
        return options(
                option(
                        "label",
                        String.format(
                                "{%s|%s|%s|%s}",
                                shorten(computation.toString()),
                                result.map(Object::getClass)
                                        .map(Class::getSimpleName)
                                        .orElse(""),
                                shorten(resultString),
                                result.getProblems().stream()
                                        .map(Problem::toString)
                                        .collect(Collectors.joining(", ")))),
                option("xlabel", String.valueOf(numberOfHits)));
    }

    private static String shorten(String resultString) {
        return resultString.substring(0, Math.min(resultString.length(), 80));
    }

    @Override
    protected String getEdgeOptions(IComputation<?> parent, IComputation<?> child, int idx) {
        return options(option("label", String.valueOf(idx)));
    }
}
