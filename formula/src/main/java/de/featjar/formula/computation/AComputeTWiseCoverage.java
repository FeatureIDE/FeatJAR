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
package de.featjar.formula.computation;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.IntegerList;
import de.featjar.base.data.Result;
import de.featjar.formula.CoverageStatistic;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.combination.ICombinationFilter;
import de.featjar.formula.combination.ICombinationSpecification;
import de.featjar.formula.combination.VariableCombinationSpecification.VariableCombinationSpecificationComputation;
import de.featjar.formula.index.SampleBitIndex;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Calculates statistics regarding t-wise feature coverage of a set of
 * solutions.
 *
 * @author Sebastian Krieter
 */
public abstract class AComputeTWiseCoverage extends AComputation<CoverageStatistic> {

    public static final Dependency<BooleanAssignmentList> SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);

    public static final Dependency<ICombinationSpecification> COMBINATION_SET =
            Dependency.newDependency(ICombinationSpecification.class);
    public static final Dependency<ICombinationFilter> EXCLUDE_INTERACTIONS =
            Dependency.newDependency(ICombinationFilter.class);
    public static final Dependency<ICombinationFilter> INCLUDE_INTERACTIONS =
            Dependency.newDependency(ICombinationFilter.class);

    public AComputeTWiseCoverage(IComputation<BooleanAssignmentList> sample, IComputation<?>... computations) {
        super(
                sample,
                sample.map(VariableCombinationSpecificationComputation::new),
                Computations.of(ICombinationFilter.of(false)),
                Computations.of(ICombinationFilter.of(true)),
                computations);
    }

    public AComputeTWiseCoverage(AComputeTWiseCoverage other) {
        super(other);
    }

    protected ArrayList<CoverageStatistic> statisticList = new ArrayList<>();
    protected ICombinationSpecification combinationSet;
    protected IntegerList tValues;
    protected ICombinationFilter excludeFilter;
    protected ICombinationFilter includeFilter;
    protected BooleanAssignmentList sample;

    protected final void init(List<Object> dependencyList) {
        initWithOriginalVariableMap(dependencyList);

        VariableMap referenceVariableMap = getReferenceVariableMap();
        VariableMap sampleVariableMap = sample.getVariableMap();

        if (!Objects.equals(referenceVariableMap, sampleVariableMap)) {
            FeatJAR.log().warning("Variable maps of given sample and reference are different.");
            VariableMap mergedVariableMap = new VariableMap(sampleVariableMap, referenceVariableMap);
            adaptToMergedVariableMap(mergedVariableMap);
        }
        combinationSet.remap(sample.getVariableMap());

        adaptVariableMap(dependencyList);
    }

    protected void initWithOriginalVariableMap(List<Object> dependencyList) {
        sample = SAMPLE.get(dependencyList).toSolutionList();
        combinationSet = COMBINATION_SET.get(dependencyList);
    }

    protected void adaptVariableMap(List<Object> dependencyList) {
        excludeFilter = EXCLUDE_INTERACTIONS.get(dependencyList).remap(sample.getVariableMap());
        includeFilter = INCLUDE_INTERACTIONS.get(dependencyList).remap(sample.getVariableMap());
    }

    protected void adaptToMergedVariableMap(VariableMap mergedVariableMap) {
        sample = sample.remap(mergedVariableMap);
    }

    protected VariableMap getReferenceVariableMap() {
        return combinationSet.variableMap();
    }

    @Override
    public Result<CoverageStatistic> compute(List<Object> dependencyList, Progress progress) {
        init(dependencyList);

        SampleBitIndex sampleIndex = new SampleBitIndex(sample);

        progress.setTotalSteps(combinationSet.loopCount());

        process(
                combinationSet,
                (statistic, interaction) -> {
                    checkCancel();
                    progress.incrementCurrentStep();
                    if (excludeFilter.test(interaction) || !includeFilter.test(interaction)) {
                        statistic.incNumberOfIgnoredElements();
                    } else {
                        if (sampleIndex.test(interaction)) {
                            statistic.incNumberOfCoveredElements();
                        } else {
                            countUncovered(interaction, statistic);
                        }
                    }
                },
                this::createStatistic);
        return Result.ofOptional(statisticList.stream() //
                .reduce((s1, s2) -> s1.merge(s2)));
    }

    protected CoverageStatistic createStatistic() {
        CoverageStatistic env = new CoverageStatistic();
        synchronized (statisticList) {
            statisticList.add(env);
        }
        return env;
    }

    protected void process(
            ICombinationSpecification combinationSet,
            BiConsumer<CoverageStatistic, int[]> consumer,
            Supplier<CoverageStatistic> environmentCreator) {
        combinationSet.forEachParallel(consumer, environmentCreator);
    }

    protected abstract void countUncovered(int[] uncoveredInteraction, CoverageStatistic statistic);
}
