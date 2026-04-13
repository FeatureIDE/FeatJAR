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
package de.featjar.analysis.sat4j.computation;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.combination.ICombinationFilter;
import de.featjar.formula.combination.ICombinationSpecification;
import java.util.List;
import java.util.Random;

/**
 * YASA sampling algorithm. Generates configurations for a given propositional
 * formula such that t-wise feature coverage is achieved.
 *
 * @author Sebastian Krieter
 */
public abstract class ATWiseSampleComputation extends AComputation<BooleanAssignmentList> {

    public static final Dependency<ICombinationSpecification> COMBINATION_SET =
            Dependency.newDependency(ICombinationSpecification.class);

    public static final Dependency<ICombinationFilter> EXCLUDE_INTERACTIONS =
            Dependency.newDependency(ICombinationFilter.class);
    public static final Dependency<ICombinationFilter> INCLUDE_INTERACTIONS =
            Dependency.newDependency(ICombinationFilter.class);

    public static final Dependency<Integer> CONFIGURATION_LIMIT = Dependency.newDependency(Integer.class);
    public static final Dependency<BooleanAssignmentList> INITIAL_FIXED_SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<BooleanAssignmentList> INITIAL_VARIABLE_SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);

    public static final Dependency<Long> RANDOM_SEED = Dependency.newDependency(Long.class);

    public ATWiseSampleComputation(IComputation<ICombinationSpecification> combinationSet, Object... computations) {
        super(
                combinationSet,
                Computations.of(ICombinationFilter.of(false)),
                Computations.of(ICombinationFilter.of(true)),
                Computations.of(Integer.MAX_VALUE),
                Computations.of(new BooleanAssignmentList((VariableMap) null)),
                Computations.of(new BooleanAssignmentList((VariableMap) null)),
                Computations.of(1L),
                computations);
    }

    protected ATWiseSampleComputation(ATWiseSampleComputation other) {
        super(other);
    }

    protected int maxSampleSize, variableCount;

    protected ICombinationSpecification combinationSets;
    protected ICombinationFilter excludeFilter;
    protected ICombinationFilter includeFilter;

    protected VariableMap variableMap;

    protected Random random;

    // TODO change to SampleBitIndex
    protected BooleanAssignmentList initialFixedSample;
    protected BooleanAssignmentList initialVariableSample;

    @Override
    public final Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        maxSampleSize = CONFIGURATION_LIMIT.get(dependencyList);
        if (maxSampleSize < 0) {
            throw new IllegalArgumentException(
                    "Configuration limit must be greater than 0. Value was " + maxSampleSize);
        }

        random = new Random(RANDOM_SEED.get(dependencyList));

        combinationSets = COMBINATION_SET.get(dependencyList);
        variableMap = combinationSets.variableMap();

        initialFixedSample = INITIAL_FIXED_SAMPLE.get(dependencyList).remap(variableMap);
        initialVariableSample = INITIAL_VARIABLE_SAMPLE.get(dependencyList).remap(variableMap);

        excludeFilter = EXCLUDE_INTERACTIONS.get(dependencyList).remap(variableMap);
        includeFilter = INCLUDE_INTERACTIONS.get(dependencyList).remap(variableMap);

        variableCount = variableMap.size();

        return computeSample(dependencyList, progress);
    }

    public abstract Result<BooleanAssignmentList> computeSample(List<Object> dependencyList, Progress progress);
}
