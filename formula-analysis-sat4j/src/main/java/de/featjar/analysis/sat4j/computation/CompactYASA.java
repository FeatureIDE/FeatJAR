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

import de.featjar.analysis.RuntimeContradictionException;
import de.featjar.analysis.RuntimeTimeoutException;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * YASA sampling algorithm. Generates configurations for a given propositional
 * formula such that t-wise feature coverage is achieved.
 * This implementation uses mostly bit vectors to be more memory efficient.
 *
 * @author Sebastian Krieter
 */
public class CompactYASA extends ATWiseSampleComputation {

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
    public CompactYASA(IComputation<BooleanAssignmentList> clauseList) {
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

    private int iterations, randomConfigurationLimit, randomSampleIdsIndex;
    private boolean incrementalT;
    private SampleBitIndexMIG bestSampleIndex, randomSampleIndex;
    private SampleBitIndexMIG currentSampleIndex;

    private SAT4JSolutionSolver solver;
    private ModalImplicationGraph mig;

    private int changableIdThreshold;

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
        mig = MIG.get(dependencyList);

        randomSampleIndex = new SampleBitIndexMIG(variableMap, mig);

        changableIdThreshold = initialFixedSample.size();

        BooleanAssignmentList clauseList = BOOLEAN_CLAUSE_LIST.get(dependencyList);
        BooleanAssignment assumedAssignment = ASSUMED_ASSIGNMENT.get(dependencyList);
        BooleanAssignmentList assumedClauseList = ASSUMED_CLAUSE_LIST.get(dependencyList);
        assumedClauseList = assumedClauseList.remap(variableMap);

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
        if (bestSampleIndex != null) {
            BooleanAssignmentList result = new BooleanAssignmentList(variableMap, bestSampleIndex.size());
            int initialSize = initialFixedSample.size();
            for (int j = 0; j < initialSize; j++) {
                result.add(new BooleanSolution(bestSampleIndex.getConfiguration(j), false));
            }
            for (int j = bestSampleIndex.highestID() - 1; j >= initialSize; j--) {
                if (bestSampleIndex.getLiteralsCount(j) > 0) {
                    result.add(new BooleanSolution(bestSampleIndex.getConfiguration(j), false));
                }
            }
            return Result.of(result);
        } else {
            return Result.empty();
        }
    }

    private void buildCombinations(Progress monitor) {
        currentSampleIndex = new SampleBitIndexMIG(variableMap, mig);
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
        bestSampleIndex = new SampleBitIndexMIG(currentSampleIndex);
        currentSampleIndex = null;
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
        SAT4JAssignment assignment = solver.getAssignment();
        final int orgAssignmentSize = assignment.size();
        for (int i = 0; i < fixedLiterals.length; i++) {
            assignment.add(fixedLiterals[i]);
        }
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
            assignment.clear(orgAssignmentSize);
        }
    }

    private void rebuildCombinations(Progress monitor) {
        int maxT = combinationSets.maxT();
        int minT = incrementalT ? 1 : maxT;

        for (int j = 0; j < iterations; j++) {
            if (currentSampleIndex == null) {
                currentSampleIndex = new SampleBitIndexMIG(variableMap, mig);
                for (BooleanAssignment config : initialFixedSample) {
                    currentSampleIndex.addConfiguration(config);
                }
                for (BooleanAssignment config : initialVariableSample) {
                    currentSampleIndex.addConfiguration(config);
                }
            } else {
                int n = (int) ((currentSampleIndex.size() - changableIdThreshold) * 0.7);
                List<Integer> ids = IntStream.range(changableIdThreshold, currentSampleIndex.highestID())
                        .filter(id -> currentSampleIndex.getLiteralsCount(id) > 0)
                        .boxed()
                        .sorted(Comparator.comparingInt(id -> currentSampleIndex.getLiteralsCount(id)))
                        .skip(n)
                        .collect(Collectors.toList());
                ids.forEach(id -> currentSampleIndex.clear(id));
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
                        newConfiguration(combinationLiterals);
                    }
                });
            }
            if (bestSampleIndex.size() > currentSampleIndex.size()) {
                bestSampleIndex = new SampleBitIndexMIG(currentSampleIndex);
            }
        }
        currentSampleIndex = null;
    }

    private boolean tryCoverInExistingSolution(int[] literals) {
        ArrayList<Integer> selectionCandidates = new ArrayList<>();

        BitSet negatedBitSet = currentSampleIndex.getNegatedBitSet(literals);
        int index = negatedBitSet.nextClearBit(0);
        while (index < currentSampleIndex.highestID()) {
            if (index >= changableIdThreshold) {
                int literalsCount = currentSampleIndex.getLiteralsCount(index);
                if (literalsCount > 0 && literalsCount < variableCount) {
                    selectionCandidates.add(index);
                }
            }
            index = negatedBitSet.nextClearBit(index + 1);
        }

        if (selectionCandidates.isEmpty()) {
            return false;
        }

        Collections.sort(
                selectionCandidates,
                Comparator.comparing(id -> currentSampleIndex.countUndefined((int) id, literals))
                        .thenComparing(id -> -currentSampleIndex.getLiteralsCount((int) id)));

        ArrayList<int[]> candidateLiterals = new ArrayList<>(selectionCandidates.size());
        for (Integer id : selectionCandidates) {
            candidateLiterals.add(currentSampleIndex.propagate(id, literals));
        }

        BitSet literalBitSet = randomSampleIndex.getBitSet(literals);
        if (!literalBitSet.isEmpty()) {
            for (int i = 0; i < candidateLiterals.size(); i++) {
                int[] literalSet = candidateLiterals.get(i);
                if (literalSet != null) {
                    candidateLiterals.add(literalSet);
                    BitSet configurationBitSet = randomSampleIndex.getBitSet(literalSet);
                    configurationBitSet.and(literalBitSet);
                    if (!configurationBitSet.isEmpty()) {
                        currentSampleIndex.set(selectionCandidates.get(i), literals);
                        return true;
                    }
                }
            }
        }

        for (int i = 0; i < candidateLiterals.size(); i++) {
            int[] literalSet = candidateLiterals.get(i);
            if (literalSet != null) {
                if (trySelectSat(literals)) {
                    currentSampleIndex.set(selectionCandidates.get(i), literals);
                    return true;
                }
            }
        }
        return false;
    }

    private void newConfiguration(int[] literals) {
        if (currentSampleIndex.size() < maxSampleSize) {
            int id = -1;
            for (int i = changableIdThreshold; i < currentSampleIndex.highestID(); i++) {
                if (currentSampleIndex.getLiteralsCount(i) == 0) {
                    id = i;
                    currentSampleIndex.readdConfiguration();
                    break;
                }
            }
            if (id == -1) {
                id = currentSampleIndex.addConfiguration();
                for (int l : mig.getCore()) {
                    currentSampleIndex.set(id, l);
                }
            }
            currentSampleIndex.set(id, literals);
        }
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
        return solver.hasSolution(configuration.get()).orElse(Boolean.FALSE);
    }

    private boolean trySelectSat(final int[] literals) {
        Result<Boolean> hasSolution = solver.hasSolution(literals);
        if (hasSolution.isPresent()) {
            if (hasSolution.get()) {
                final int[] solution = solver.getInternalSolution();
                if (randomSampleIndex.highestID() < randomConfigurationLimit) {
                    randomSampleIdsIndex = randomSampleIndex.addConfiguration(solution);
                } else {
                    randomSampleIdsIndex = (randomSampleIdsIndex + 1) % randomConfigurationLimit;
                    randomSampleIndex.reset(randomSampleIdsIndex, solution);
                }
                solver.shuffleOrder(random);
                return true;
            }
        } else {
            throw new RuntimeTimeoutException();
        }
        return false;
    }
}
