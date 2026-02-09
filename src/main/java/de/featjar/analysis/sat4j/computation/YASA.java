/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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

import de.featjar.analysis.RuntimeContradictionException;
import de.featjar.analysis.RuntimeTimeoutException;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.analysis.sat4j.solver.MIGVisitorBitSet;
import de.featjar.analysis.sat4j.solver.MIGVisitorByte;
import de.featjar.analysis.sat4j.solver.ModalImplicationGraph;
import de.featjar.analysis.sat4j.solver.SAT4JAssignment;
import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.analysis.sat4j.solver.SAT4JSolver;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.combination.VariableCombinationSpecification.VariableCombinationSpecificationComputation;
import de.featjar.formula.index.SampleBitIndex;
import java.time.Duration;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * YASA sampling algorithm. Generates configurations for a given propositional
 * formula such that t-wise feature coverage is achieved.
 *
 * @author Sebastian Krieter
 */
public class YASA extends ATWiseSampleComputation {

    private static class PartialConfiguration {
        private int id;
        private final boolean allowChange;
        private final MIGVisitorBitSet visitor;

        public PartialConfiguration(int id, boolean allowChange, ModalImplicationGraph mig, int... newliterals) {
            this.id = id;
            this.allowChange = allowChange;
            visitor = new MIGVisitorBitSet(mig);
            if (allowChange) {
                visitor.propagate(newliterals);
            } else {
                visitor.setLiterals(newliterals);
            }
        }

        public PartialConfiguration(PartialConfiguration other, int id) {
            this.id = id;
            this.allowChange = other.allowChange;
            visitor = new MIGVisitorBitSet(other.visitor);
        }

        public int setLiteral(int... literals) {
            final int oldModelCount = visitor.getAddedLiteralCount();
            visitor.propagate(literals);
            return oldModelCount;
        }

        public int countLiterals() {
            return visitor.getAddedLiteralCount();
        }
    }

    /**
     * The input formula in CNF.
     */
    public static final Dependency<BooleanAssignmentList> BOOLEAN_CLAUSE_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);
    /**
     * An assignment to assume in addition to the input formula.
     */
    public static final Dependency<BooleanAssignment> ASSUMED_ASSIGNMENT =
            Dependency.newDependency(BooleanAssignment.class);
    /**
     * A list of CNF clauses to assume in addition to the input formula.
     */
    public static final Dependency<BooleanAssignmentList> ASSUMED_CLAUSE_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);
    /**
     * The internal SAT timeout. No timeout per default.
     */
    public static final Dependency<Duration> SAT_TIMEOUT = Dependency.newDependency(Duration.class);
    /**
     * The MIG to use. Will be computed if none is provided.
     */
    public static final Dependency<ModalImplicationGraph> MIG = Dependency.newDependency(ModalImplicationGraph.class);

    /**
     * Number of iterations for decreasing the sample size.
     */
    public static final Dependency<Integer> ITERATIONS = Dependency.newDependency(Integer.class);
    /**
     * The maximum number of solution for internal caching.
     */
    public static final Dependency<Integer> INTERNAL_SOLUTION_LIMIT = Dependency.newDependency(Integer.class);
    /**
     * Whether to use an incremental approach for t values.
     */
    public static final Dependency<Boolean> INCREMENTAL_T = Dependency.newDependency(Boolean.class);

    /**
     * Constructs a new YASA computation.
     * @param clauseList the computation of the input formula
     */
    public YASA(IComputation<BooleanAssignmentList> clauseList) {
        super(
                clauseList.map(VariableCombinationSpecificationComputation::new),
                clauseList,
                Computations.of(new BooleanAssignment()),
                Computations.of(new BooleanAssignmentList(null, 0)),
                Computations.of(Duration.ZERO),
                new MIGBuilder(clauseList),
                Computations.of(1),
                Computations.of(65_536),
                Computations.of(Boolean.FALSE));
    }

    private int iterations, randomConfigurationLimit, curSolutionId, randomSampleIdsIndex;
    private boolean incrementalT;
    private List<PartialConfiguration> currentSample;
    private SampleBitIndex bestSampleIndex, currentSampleIndex, randomSampleIndex;

    private SAT4JSolutionSolver solver;
    private ModalImplicationGraph mig;

    @Override
    public Result<BooleanAssignmentList> computeSample(List<Object> dependencyList, Progress progress) {
        iterations = ITERATIONS.get(dependencyList);
        if (iterations < 0) {
            iterations = Integer.MAX_VALUE;
        }

        randomConfigurationLimit = INTERNAL_SOLUTION_LIMIT.get(dependencyList);
        if (randomConfigurationLimit < 0) {
            throw new IllegalArgumentException(
                    "Internal solution limit must be greater than 0. Value was " + randomConfigurationLimit);
        }

        incrementalT = INCREMENTAL_T.get(dependencyList);
        randomSampleIndex = new SampleBitIndex(variableMap);

        mig = MIG.get(dependencyList);

        BooleanAssignmentList clauseList = BOOLEAN_CLAUSE_LIST.get(dependencyList);
        BooleanAssignment assumedAssignment = ASSUMED_ASSIGNMENT.get(dependencyList);
        BooleanAssignmentList assumedClauseList = ASSUMED_CLAUSE_LIST.get(dependencyList);
        assumedClauseList = assumedClauseList.remap(clauseList.getVariableMap());

        Duration timeout = SAT_TIMEOUT.get(dependencyList);

        solver = new SAT4JSolutionSolver(clauseList);
        SAT4JSolver.initializeSolver(solver, clauseList, assumedAssignment, assumedClauseList, timeout);

        solver.setSelectionStrategy(ISelectionStrategy.original());

        initialFixedSample = checkInitialSample(initialFixedSample);
        initialVariableSample = checkInitialSample(initialVariableSample);

        solver.setSelectionStrategy(ISelectionStrategy.random(random));

        progress.setTotalSteps((iterations + 1) * combinationSets.loopCount());

        buildCombinations(progress);
        rebuildCombinations(progress);

        return finalizeResult();
    }

    @Override
    public Result<BooleanAssignmentList> getIntermediateResult() {
        return finalizeResult();
    }

    private Result<BooleanAssignmentList> finalizeResult() {
        currentSample = null;
        currentSampleIndex = null;
        if (bestSampleIndex != null) {
            BooleanAssignmentList result = new BooleanAssignmentList(variableMap, bestSampleIndex.size());
            int initialSize = initialFixedSample.size();
            for (int j = 0; j < initialSize; j++) {
                result.add(new BooleanSolution(bestSampleIndex.getConfiguration(j), false));
            }
            for (int j = bestSampleIndex.size() - 1; j >= initialSize; j--) {
                result.add(new BooleanSolution(bestSampleIndex.getConfiguration(j), false));
            }
            return Result.of(result);
        } else {
            return Result.empty();
        }
    }

    private void buildCombinations(Progress monitor) {
        curSolutionId = 0;
        currentSample = null;
        currentSampleIndex = new SampleBitIndex(variableMap);
        for (BooleanAssignment config : initialFixedSample) {
            currentSampleIndex.addConfiguration(config);
        }
        for (BooleanAssignment config : initialVariableSample) {
            currentSampleIndex.addConfiguration(config);
        }

        combinationSets.forEach(combinationLiterals -> {
            checkCancel();
            monitor.incrementCurrentStep();

            if (!currentSampleIndex.test(combinationLiterals)
                    && includeFilter.test(combinationLiterals)
                    && !excludeFilter.test(combinationLiterals)
                    && !isCombinationInvalidMIG(combinationLiterals)) {
                newRandomConfiguration(combinationLiterals);
            }
        });
        bestSampleIndex = currentSampleIndex;
    }

    private boolean isCombinationInvalidMIG(int[] literals) {
        try {
            MIGVisitorByte visitor = new MIGVisitorByte(mig);
            visitor.propagate(literals);
        } catch (RuntimeContradictionException e) {
            return true;
        }
        return false;
    }

    private void newRandomConfiguration(final int[] fixedLiterals) {
        int orgAssignmentSize = setUpSolver(fixedLiterals);
        try {
            Result<Boolean> hasSolution = solver.hasSolution();
            if (hasSolution.isPresent()) {
                if (hasSolution.get()) {
                    int[] solution = solver.getInternalSolution();
                    currentSampleIndex.addConfiguration(solution);
                    if (randomSampleIndex.size() < randomConfigurationLimit) {
                        randomSampleIndex.addConfiguration(solution);
                    }
                    solver.shuffleOrder(random);
                }
            } else {
                throw new RuntimeTimeoutException();
            }
        } finally {
            solver.getAssignment().clear(orgAssignmentSize);
        }
    }

    private void rebuildCombinations(Progress monitor) {
        int maxT = combinationSets.maxT();
        int minT = incrementalT ? 1 : maxT;
        List<PartialConfiguration> bestSample = null;
        List<PartialConfiguration> oldSample = null;

        for (int j = 0; j < iterations; j++) {
            if (bestSample != null) {
                Collections.sort(bestSample, Comparator.comparingInt(c -> c.countLiterals()));
                oldSample = bestSample.subList(0, ((int) (0.7 * bestSample.size())));
                bestSample = null;
            }

            curSolutionId = 0;
            currentSample = new ArrayList<>();
            currentSampleIndex = new SampleBitIndex(variableMap);

            for (BooleanAssignment config : initialFixedSample) {
                newConfiguration(config.get(), false);
            }
            for (BooleanAssignment config : initialVariableSample) {
                newConfiguration(config.get(), true);
            }

            if (oldSample != null) {
                for (PartialConfiguration config : oldSample) {
                    if (currentSample.size() >= maxSampleSize) {
                        break;
                    }
                    config.id = curSolutionId++;
                    currentSample.add(config);
                    currentSampleIndex.addEmptyConfiguration();
                    updateIndex(config, 0);
                    for (int l : mig.getCore()) {
                        currentSampleIndex.update(config.id, l);
                    }
                }
            }

            combinationSets.shuffleElements(random);
            for (int t = minT; t <= maxT; t++) {
                combinationSets.reduceTTo(t).forEach(combinationLiterals -> {
                    checkCancel();
                    monitor.incrementCurrentStep();

                    if (!currentSampleIndex.test(combinationLiterals)
                            && bestSampleIndex.test(combinationLiterals)
                            && includeFilter.test(combinationLiterals)
                            && !excludeFilter.test(combinationLiterals)
                            && !tryCoverInExistingSolution(combinationLiterals)) {
                        newConfiguration(combinationLiterals, true);
                    }
                });
            }
            if (bestSampleIndex.size() > currentSampleIndex.size()) {
                bestSampleIndex = currentSampleIndex;
                bestSample = currentSample;
            }
        }
    }

    private void updateIndex(PartialConfiguration solution, int firstLiteralToConsider) {
        int addedLiteralCount = solution.visitor.getAddedLiteralCount();
        int[] addedLiterals = solution.visitor.getAddedLiterals();
        for (int i = firstLiteralToConsider; i < addedLiteralCount; i++) {
            currentSampleIndex.update(solution.id, addedLiterals[i]);
        }
    }

    private boolean tryCoverInExistingSolution(int[] literals) {
        List<PartialConfiguration> selectionCandidates = new ArrayList<>();

        BitSet negatedBitSet = currentSampleIndex.getNegatedBitSet(literals);
        int nextBit = negatedBitSet.nextClearBit(0);
        while (nextBit < currentSampleIndex.size()) {
            PartialConfiguration configuration = currentSample.get(nextBit);
            if (canBeModified(configuration)) {
                selectionCandidates.add(configuration);
            }
            nextBit = negatedBitSet.nextClearBit(nextBit + 1);
        }

        if (selectionCandidates.isEmpty()) {
            return false;
        }

        Collections.sort(
                selectionCandidates,
                Comparator.<PartialConfiguration>comparingInt(c -> c.visitor.countUndefined(literals))
                        .thenComparingInt(c -> -c.countLiterals()));

        BitSet literalBitSet = randomSampleIndex.getBitSet(literals);
        if (!literalBitSet.isEmpty()) {
            for (PartialConfiguration configuration : selectionCandidates) {
                BitSet configurationBitSet = randomSampleIndex.getBitSet(
                        configuration.visitor.getAddedLiterals(), configuration.visitor.getAddedLiteralCount());
                configurationBitSet.and(literalBitSet);
                if (!configurationBitSet.isEmpty()) {
                    updateIndex(configuration, configuration.setLiteral(literals));
                    return true;
                }
            }
        }

        for (PartialConfiguration configuration : selectionCandidates) {
            if (trySelectSat(configuration, literals)) {
                return true;
            }
        }
        return false;
    }

    private void newConfiguration(int[] literals, boolean allowChange) {
        if (currentSample.size() < maxSampleSize) {
            PartialConfiguration newConfiguration =
                    new PartialConfiguration(curSolutionId++, allowChange, mig, literals);
            currentSample.add(newConfiguration);
            currentSampleIndex.addEmptyConfiguration();
            updateIndex(newConfiguration, 0);
            for (int l : mig.getCore()) {
                currentSampleIndex.update(newConfiguration.id, l);
            }
        }
    }

    private boolean canBeModified(PartialConfiguration configuration) {
        return configuration.allowChange && configuration.visitor.getAddedLiteralCount() != variableCount;
    }

    private BooleanAssignmentList checkInitialSample(BooleanAssignmentList initialSample) {
        BooleanAssignmentList checkedInitialSample = new BooleanAssignmentList(variableMap);
        for (BooleanAssignment config : initialSample) {
            if (checkInitialConfiguration(config)) {
                checkedInitialSample.add(config);
            } else {
                FeatJAR.log().warning("Initial configuration is invalid and will be skipped:\n" + config);
            }
        }
        return checkedInitialSample;
    }

    private boolean checkInitialConfiguration(BooleanAssignment configuration) {
        final int orgAssignmentSize = setUpSolver(configuration.get());
        try {
            return solver.hasSolution().orElse(Boolean.FALSE);
        } finally {
            solver.getAssignment().clear(orgAssignmentSize);
        }
    }

    private boolean trySelectSat(PartialConfiguration configuration, final int[] literals) {
        int addedLiteralCount = configuration.visitor.getAddedLiteralCount();
        final int oldModelCount = addedLiteralCount;
        try {
            configuration.visitor.propagate(literals);
        } catch (RuntimeException e) {
            configuration.visitor.reset(oldModelCount);
            return false;
        }

        final int orgAssignmentSize = setUpSolver(configuration);
        try {
            Result<Boolean> hasSolution = solver.hasSolution();
            if (hasSolution.isPresent()) {
                if (hasSolution.get()) {
                    updateIndex(configuration, oldModelCount);
                    randomSampleIdsIndex = (randomSampleIdsIndex + 1) % randomConfigurationLimit;
                    final int[] solution = solver.getInternalSolution();
                    randomSampleIndex.update(randomSampleIdsIndex, solution);
                    solver.shuffleOrder(random);
                    return true;
                } else {
                    configuration.visitor.reset(oldModelCount);
                }
            } else {
                throw new RuntimeTimeoutException();
            }
        } finally {
            solver.getAssignment().clear(orgAssignmentSize);
        }
        return false;
    }

    private int setUpSolver(PartialConfiguration configuration) {
        return setUpSolver(configuration.visitor.getAddedLiterals(), configuration.visitor.getAddedLiteralCount());
    }

    private int setUpSolver(int[] configuration) {
        return setUpSolver(configuration, configuration.length);
    }

    private int setUpSolver(int[] elements, int size) {
        SAT4JAssignment assignment = solver.getAssignment();
        final int orgAssignmentSize = assignment.size();
        for (int i = 0; i < size; i++) {
            assignment.add(elements[i]);
        }
        return orgAssignmentSize;
    }
}
