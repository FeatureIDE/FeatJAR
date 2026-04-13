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
import de.featjar.analysis.sat4j.solver.IMIGVisitor;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.analysis.sat4j.solver.MIGVisitorInt;
import de.featjar.analysis.sat4j.solver.ModalImplicationGraph;
import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.analysis.sat4j.solver.SAT4JSolver;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.BinomialCalculator;
import de.featjar.base.data.ExpandableIntegerList;
import de.featjar.base.data.Result;
import de.featjar.base.data.combination.CombinationStream;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanClause;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * YASA sampling algorithm. Generates configurations for a given propositional
 * formula such that t-wise feature coverage is achieved.
 *
 * @author Sebastian Krieter
 */
public class YASALegacy extends ASAT4JAnalysis<BooleanAssignmentList> {

    public static final Dependency<BooleanAssignment> VARIABLES = Dependency.newDependency(BooleanAssignment.class);

    public static final Dependency<Integer> T = Dependency.newDependency(Integer.class);

    public static final Dependency<Integer> CONFIGURATION_LIMIT = Dependency.newDependency(Integer.class);
    public static final Dependency<BooleanAssignmentList> INITIAL_SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);

    public static final Dependency<ModalImplicationGraph> MIG = Dependency.newDependency(ModalImplicationGraph.class);

    public static final Dependency<Boolean> ALLOW_CHANGE_TO_INITIAL_SAMPLE = Dependency.newDependency(Boolean.class);
    public static final Dependency<Boolean> INITIAL_SAMPLE_COUNTS_TOWARDS_CONFIGURATION_LIMIT =
            Dependency.newDependency(Boolean.class);

    public static final Dependency<Integer> ITERATIONS = Dependency.newDependency(Integer.class);
    public static final Dependency<Integer> INTERNAL_SOLUTION_LIMIT = Dependency.newDependency(Integer.class);

    public YASALegacy(IComputation<BooleanAssignmentList> clauseList) {
        super(
                clauseList,
                clauseList.flatMapResult(YASALegacy.class, "variables", l -> {
                    return Result.of(l.getVariableMap().getVariables());
                }),
                Computations.of(2),
                Computations.of(Integer.MAX_VALUE),
                Computations.of(new BooleanAssignmentList((VariableMap) null)),
                new MIGBuilder(clauseList),
                Computations.of(Boolean.TRUE),
                Computations.of(Boolean.TRUE),
                Computations.of(1),
                Computations.of(100_000));
    }

    protected YASALegacy(YASALegacy other) {
        super(other);
    }

    /**
     * Converts a set of single literals into a grouped expression list.
     *
     * @param literalSet the literal set
     * @return a grouped expression list (can be used as an input for the
     *         configuration generator).
     */
    public static List<List<BooleanClause>> convertLiterals(BooleanAssignment literalSet) {
        final List<List<BooleanClause>> arrayList = new ArrayList<>(literalSet.size());
        for (final Integer literal : literalSet.get()) {
            final List<BooleanClause> clauseList = new ArrayList<>(1);
            clauseList.add(new BooleanClause(literal));
            arrayList.add(clauseList);
        }
        return arrayList;
    }

    private class PartialConfiguration extends BooleanSolution {
        private static final long serialVersionUID = 1464084516529934929L;

        private final int id;
        private final boolean allowChange;

        private IMIGVisitor visitor;
        private ArrayList<BooleanSolution> solverSolutions;

        public PartialConfiguration(int id, boolean allowChange, ModalImplicationGraph mig, int... newliterals) {
            super(new int[variableCount], false);
            this.id = id;
            this.allowChange = allowChange;
            visitor = new MIGVisitorInt(mig, elements);
            solverSolutions = new ArrayList<>();
            visitor.propagate(newliterals);
        }

        public void initSolutionList() {
            solutionLoop:
            for (BooleanSolution solution : randomSample) {
                final int[] solverSolutionLiterals = solution.get();
                for (int j = 0; j < visitor.getAddedLiteralCount(); j++) {
                    final int l = visitor.getAddedLiterals()[j];
                    if (solverSolutionLiterals[Math.abs(l) - 1] != l) {
                        continue solutionLoop;
                    }
                }
                solverSolutions.add(solution);
            }
        }

        public void updateSolutionList(int lastIndex) {
            if (!isComplete()) {
                for (int i = lastIndex; i < visitor.getAddedLiteralCount(); i++) {
                    final int newLiteral = visitor.getAddedLiterals()[i];
                    final int k = Math.abs(newLiteral) - 1;
                    for (int j = solverSolutions.size() - 1; j >= 0; j--) {
                        final int[] solverSolutionLiterals =
                                solverSolutions.get(j).get();
                        if (solverSolutionLiterals[k] != newLiteral) {
                            final int last = solverSolutions.size() - 1;
                            Collections.swap(solverSolutions, j, last);
                            solverSolutions.remove(last);
                        }
                    }
                }
            }
        }

        public int setLiteral(int... literals) {
            final int oldModelCount = visitor.getAddedLiteralCount();
            visitor.propagate(literals);
            return oldModelCount;
        }

        public void clear() {
            solverSolutions = null;
        }

        public boolean isComplete() {
            return visitor.getAddedLiteralCount() == variableCount;
        }

        public int countLiterals() {
            return visitor.getAddedLiteralCount();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    private int iterations, internalConfigurationLimit, t;
    private BooleanAssignment literals;

    private ArrayDeque<BooleanSolution> randomSample;
    private List<PartialConfiguration> bestSample;
    private List<PartialConfiguration> currentSample;

    private ArrayList<PartialConfiguration> candidateConfiguration;
    private ArrayList<ExpandableIntegerList> currentSampleIndices;
    private ExpandableIntegerList[] selectedSampleIndices;
    private BitSet[] bestSampleIndices;
    private PartialConfiguration newConfiguration;
    private int curSolutionId;
    private boolean overLimit;

    protected int maxSampleSize, variableCount;
    protected boolean allowChangeToInitialSample, initialSampleCountsTowardsConfigurationLimit;

    protected BooleanAssignment variables;

    protected SAT4JSolutionSolver solver;
    protected VariableMap variableMap;
    protected Random random;
    protected ModalImplicationGraph mig;

    // TODO change to SampleBitIndex
    protected BooleanAssignmentList initialSample;

    @Override
    public final Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        maxSampleSize = CONFIGURATION_LIMIT.get(dependencyList);
        if (maxSampleSize < 0) {
            throw new IllegalArgumentException(
                    "Configuration limit must be greater than 0. Value was " + maxSampleSize);
        }

        initialSample = INITIAL_SAMPLE.get(dependencyList);

        random = new Random(RANDOM_SEED.get(dependencyList));

        allowChangeToInitialSample = ALLOW_CHANGE_TO_INITIAL_SAMPLE.get(dependencyList);
        initialSampleCountsTowardsConfigurationLimit =
                INITIAL_SAMPLE_COUNTS_TOWARDS_CONFIGURATION_LIMIT.get(dependencyList);

        variableMap = BOOLEAN_CLAUSE_LIST.get(dependencyList).getVariableMap();
        variableCount = variableMap.size();

        variables = VARIABLES.get(dependencyList);

        solver = createSolver(dependencyList);
        solver.setSelectionStrategy(ISelectionStrategy.random(random));

        if (initialSampleCountsTowardsConfigurationLimit) {
            maxSampleSize = Math.max(maxSampleSize, maxSampleSize + initialSample.size());
        }

        iterations = ITERATIONS.get(dependencyList);
        if (iterations == 0) {
            throw new IllegalArgumentException("Iterations must not equal 0.");
        }
        if (iterations < 0) {
            iterations = Integer.MAX_VALUE;
        }

        internalConfigurationLimit = INTERNAL_SOLUTION_LIMIT.get(dependencyList);
        if (internalConfigurationLimit < 0) {
            throw new IllegalArgumentException(
                    "Internal solution limit must be greater than 0. Value was " + internalConfigurationLimit);
        }

        randomSample = new ArrayDeque<>(internalConfigurationLimit);

        mig = MIG.get(dependencyList);

        t = T.get(dependencyList);
        BooleanAssignment filteredVariables = variables.removeAllVariables(
                Arrays.stream(mig.getCore()).map(Math::abs).toArray());
        literals = new BooleanAssignment(filteredVariables.addAll(filteredVariables.negate()));

        progress.setTotalSteps(iterations * BinomialCalculator.computeBinomial(literals.size(), t));

        buildCombinations(progress);

        if (!overLimit && iterations > 1) {
            rebuildCombinations(progress);
        }

        return finalizeResult();
    }

    @Override
    public Result<BooleanAssignmentList> getIntermediateResult() {
        return finalizeResult();
    }

    private Result<BooleanAssignmentList> finalizeResult() {
        if (bestSample != null) {
            BooleanAssignmentList result = new BooleanAssignmentList(variableMap, bestSample.size());
            for (int j = bestSample.size() - 1; j >= 0; j--) {
                result.add(autoComplete(bestSample.get(j)));
            }
            return Result.of(result);
        } else {
            return Result.empty();
        }
    }

    private void buildCombinations(Progress monitor) {
        initSample();

        selectedSampleIndices = new ExpandableIntegerList[t];
        initRun();

        CombinationStream.stream(literals.get(), t).forEach(combination -> {
            int[] combinationLiterals = combination.select();
            checkCancel();
            monitor.incrementCurrentStep();

            if (isCovered(combinationLiterals, currentSampleIndices)) {
                return;
            }
            if (isCombinationInvalidMIG(combinationLiterals)) {
                return;
            }

            try {
                if (isCombinationValidSample(combinationLiterals)) {
                    if (tryCover(combinationLiterals)) {
                        return;
                    }
                } else {
                    if (isCombinationInvalidSAT(combinationLiterals)) {
                        return;
                    }
                }

                if (tryCoverWithSat(combinationLiterals)) {
                    return;
                }
                newConfiguration(combinationLiterals);
            } finally {
                candidateConfiguration.clear();
                newConfiguration = null;
            }
        });
        setBestSolutionList();
    }

    private void rebuildCombinations(Progress monitor) {
        if (iterations > 1) {
            int solutionCount = bestSample.size();
            bestSampleIndices = new BitSet[2 * variableCount + 1];
            for (int j = 1; j <= variableCount; j++) {
                BitSet negIndices = new BitSet(solutionCount);
                BitSet posIndices = new BitSet(solutionCount);
                for (int i = 0; i < solutionCount; i++) {
                    BooleanSolution config = bestSample.get(i);
                    int l = config.get(j - 1);
                    if (l != 0) {
                        if (l < 0) {
                            negIndices.set(i);
                        } else {
                            posIndices.set(i);
                        }
                    }
                }
                bestSampleIndices[variableCount - j] = negIndices;
                bestSampleIndices[j + variableCount] = posIndices;
            }
        }

        for (int j = 1; j < iterations; j++) {
            checkCancel();
            initSample();
            initRun();
            CombinationStream.stream(literals.shuffle(random).get(), t).forEach(combination -> {
                int[] combinationLiterals = combination.select();
                checkCancel();
                monitor.incrementCurrentStep();
                if (isCovered(combinationLiterals, currentSampleIndices)) {
                    return;
                }
                if (!isCovered(combinationLiterals, bestSampleIndices)) {
                    return;
                }
                try {
                    if (tryCoverWithoutMIG(combinationLiterals)) {
                        return;
                    }
                    if (tryCoverWithSat(combinationLiterals)) {
                        return;
                    }
                    newConfiguration(combinationLiterals);
                } finally {
                    candidateConfiguration.clear();
                    newConfiguration = null;
                }
            });
            setBestSolutionList();
        }
    }

    private void setBestSolutionList() {
        if (bestSample == null || bestSample.size() > currentSample.size()) {
            bestSample = currentSample;
        }
    }

    private void initSample() {
        curSolutionId = 0;
        overLimit = false;
        currentSample = new ArrayList<>();
        final int indexSize = 2 * variableCount;
        currentSampleIndices = new ArrayList<>(indexSize);
        for (int i = 0; i < indexSize; i++) {
            currentSampleIndices.add(new ExpandableIntegerList());
        }
        for (BooleanAssignment config : initialSample) {
            if (currentSample.size() < maxSampleSize) {
                PartialConfiguration initialConfiguration =
                        new PartialConfiguration(curSolutionId++, allowChangeToInitialSample, mig, config.get());
                if (allowChangeToInitialSample) {
                    initialConfiguration.initSolutionList();
                }
                if (initialConfiguration.isComplete()) {
                    initialConfiguration.clear();
                }
                currentSample.add(initialConfiguration);
                for (int i = 0; i < initialConfiguration.visitor.getAddedLiteralCount(); i++) {
                    ExpandableIntegerList indexList = currentSampleIndices.get(ModalImplicationGraph.getVertexIndex(
                            initialConfiguration.visitor.getAddedLiterals()[i]));
                    indexList.add(initialConfiguration.id);
                }
            } else {
                overLimit = true;
            }
        }
    }

    private void initRun() {
        newConfiguration = null;
        candidateConfiguration = new ArrayList<>();
        Collections.sort(currentSample, (a, b) -> b.countLiterals() - a.countLiterals());
    }

    private boolean isCovered(int[] combinationLiterals, ArrayList<ExpandableIntegerList> indexedSolutions) {
        if (t < 2) {
            return !indexedSolutions
                    .get(ModalImplicationGraph.getVertexIndex(combinationLiterals[0]))
                    .isEmpty();
        }
        for (int i = 0; i < t; i++) {
            final ExpandableIntegerList indexedSolution =
                    indexedSolutions.get(ModalImplicationGraph.getVertexIndex(combinationLiterals[i]));
            if (indexedSolution.size() == 0) {
                return false;
            }
            selectedSampleIndices[i] = indexedSolution;
        }
        Arrays.sort(selectedSampleIndices, (a, b) -> a.size() - b.size());
        final int[] ix = new int[variableCount - 1];

        final ExpandableIntegerList i0 = selectedSampleIndices[0];
        final int[] ia0 = i0.getInternalArray();
        loop:
        for (int i = 0; i < i0.size(); i++) {
            int id0 = ia0[i];
            for (int j = 1; j < t; j++) {
                final ExpandableIntegerList i1 = selectedSampleIndices[j];
                int binarySearch = Arrays.binarySearch(i1.getInternalArray(), ix[j - 1], i1.size(), id0);
                if (binarySearch < 0) {
                    ix[j - 1] = -binarySearch - 1;
                    continue loop;
                } else {
                    ix[j - 1] = binarySearch;
                }
            }
            return true;
        }
        return false;
    }

    private BitSet combinedIndex(final int size, int[] literals, BitSet[] bitSets) {
        BitSet first = bitSets[literals[0] + size];
        BitSet bitSet = new BitSet(first.size());
        bitSet.xor(first);
        for (int k = 1; k < literals.length; k++) {
            bitSet.and(bitSets[literals[k] + size]);
        }
        return bitSet;
    }

    private boolean isCovered(int[] combinationLiterals, BitSet[] indexedSolutions) {
        if (t == 1) {
            return !indexedSolutions[combinationLiterals[0] + variableCount].isEmpty();
        }

        return !combinedIndex(variableCount, combinationLiterals, indexedSolutions)
                .isEmpty();
    }

    private void select(PartialConfiguration solution, int[] combinationLiterals) {
        final int lastIndex = solution.setLiteral(combinationLiterals);
        for (int i = lastIndex; i < solution.visitor.getAddedLiteralCount(); i++) {
            ExpandableIntegerList indexList = currentSampleIndices.get(
                    ModalImplicationGraph.getVertexIndex(solution.visitor.getAddedLiterals()[i]));
            final int idIndex = Arrays.binarySearch(indexList.getInternalArray(), 0, indexList.size(), solution.id);
            if (idIndex < 0) {
                indexList.add(solution.id, -(idIndex + 1));
            }
        }
        solution.updateSolutionList(lastIndex);
    }

    private boolean tryCover(int[] literals) {
        return newConfiguration == null ? tryCoverWithoutMIG(literals) : tryCoverWithMIG(literals);
    }

    private boolean tryCoverWithoutMIG(int[] literals) {
        configLoop:
        for (final PartialConfiguration configuration : currentSample) {
            if (configuration.allowChange && !configuration.isComplete()) {
                final int[] literals2 = configuration.get();
                for (int i = 0; i < literals.length; i++) {
                    final int l = literals[i];
                    if (literals2[Math.abs(l) - 1] == -l) {
                        continue configLoop;
                    }
                }
                if (isSelectionPossibleSol(configuration, literals)) {
                    select(configuration, literals);
                    change(configuration);
                    return true;
                }
                candidateConfiguration.add(configuration);
            }
        }
        return false;
    }

    private boolean tryCoverWithMIG(int[] combinationLiterals) {
        configLoop:
        for (final PartialConfiguration configuration : currentSample) {
            if (configuration.allowChange && !configuration.isComplete()) {
                final int[] literals2 = configuration.get();
                for (int i = 0; i < newConfiguration.visitor.getAddedLiteralCount(); i++) {
                    final int l = newConfiguration.visitor.getAddedLiterals()[i];
                    if (literals2[Math.abs(l) - 1] == -l) {
                        continue configLoop;
                    }
                }
                if (isSelectionPossibleSol(configuration, combinationLiterals)) {
                    select(configuration, combinationLiterals);
                    change(configuration);
                    return true;
                }
                candidateConfiguration.add(configuration);
            }
        }
        return false;
    }

    private void addToCandidateList(int[] literals) {
        if (newConfiguration != null) {
            configLoop:
            for (final PartialConfiguration configuration : currentSample) {
                if (configuration.allowChange && !configuration.isComplete()) {
                    final int[] literals2 = configuration.get();
                    for (int i = 0; i < newConfiguration.visitor.getAddedLiteralCount(); i++) {
                        final int l = newConfiguration.visitor.getAddedLiterals()[i];
                        if (literals2[Math.abs(l) - 1] == -l) {
                            continue configLoop;
                        }
                    }
                    candidateConfiguration.add(configuration);
                }
            }
        } else {
            configLoop:
            for (final PartialConfiguration configuration : currentSample) {
                if (configuration.allowChange && !configuration.isComplete()) {
                    final int[] literals2 = configuration.get();
                    for (int i = 0; i < literals.length; i++) {
                        final int l = literals[i];
                        if (literals2[Math.abs(l) - 1] == -l) {
                            continue configLoop;
                        }
                    }
                    candidateConfiguration.add(configuration);
                }
            }
        }
    }

    private void change(final PartialConfiguration configuration) {
        if (configuration.isComplete()) {
            configuration.clear();
        }
        Collections.sort(currentSample, (a, b) -> b.countLiterals() - a.countLiterals());
    }

    private boolean isCombinationInvalidMIG(int[] literals) {
        try {
            newConfiguration = new PartialConfiguration(curSolutionId++, true, mig, literals);
        } catch (RuntimeContradictionException e) {
            return true;
        }
        return false;
    }

    private boolean isCombinationValidSample(int[] literals) {
        for (final BooleanSolution s : randomSample) {
            if (!s.containsAnyNegated(literals)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCombinationInvalidSAT(int[] literals) {
        final int orgAssignmentLength = solver.getAssignment().size();
        try {
            if (newConfiguration != null) {
                for (int i = 0; i < newConfiguration.visitor.getAddedLiteralCount(); i++) {
                    solver.getAssignment().add(newConfiguration.visitor.getAddedLiterals()[i]);
                }
            } else {
                for (int i = 0; i < literals.length; i++) {
                    solver.getAssignment().add(literals[i]);
                }
            }
            Result<Boolean> hasSolution = solver.hasSolution();
            if (hasSolution.isPresent()) {
                if (hasSolution.get()) {
                    BooleanSolution e = addSolverSolution();

                    addToCandidateList(literals);
                    PartialConfiguration compatibleConfiguration = null;
                    for (PartialConfiguration c : candidateConfiguration) {
                        if (!c.containsAnyNegated(e)) {
                            if (compatibleConfiguration == null) {
                                compatibleConfiguration = c;
                            } else {
                                c.solverSolutions.add(e);
                            }
                        }
                    }
                    if (compatibleConfiguration != null) {
                        select(compatibleConfiguration, literals);
                        compatibleConfiguration.solverSolutions.add(e);
                        change(compatibleConfiguration);
                        return true;
                    }
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } finally {
            solver.getAssignment().clear(orgAssignmentLength);
        }
    }

    private boolean tryCoverWithSat(int[] combinationLiterals) {
        for (PartialConfiguration configuration : candidateConfiguration) {
            if (trySelectSat(configuration, combinationLiterals)) {
                change(configuration);
                return true;
            }
        }
        return false;
    }

    private void newConfiguration(int[] literals) {
        if (currentSample.size() < maxSampleSize) {
            if (newConfiguration == null) {
                newConfiguration = new PartialConfiguration(curSolutionId++, true, mig, literals);
            }
            newConfiguration.initSolutionList();
            currentSample.add(newConfiguration);
            change(newConfiguration);
            for (int i = 0; i < newConfiguration.visitor.getAddedLiteralCount(); i++) {
                ExpandableIntegerList indexList = currentSampleIndices.get(ModalImplicationGraph.getVertexIndex(
                        newConfiguration.visitor.getAddedLiterals()[i]));
                indexList.add(newConfiguration.id);
            }
        } else {
            overLimit = true;
        }
    }

    private BooleanSolution autoComplete(PartialConfiguration configuration) {
        if (configuration.allowChange && !configuration.isComplete()) {
            if (configuration.solverSolutions != null && configuration.solverSolutions.size() > 0) {
                final int[] configuration2 =
                        configuration.solverSolutions.get(0).get();
                System.arraycopy(configuration2, 0, configuration.get(), 0, configuration.size());
                configuration.clear();
            } else {
                final int orgAssignmentSize = setUpSolver(configuration);
                try {
                    Result<Boolean> hasSolution = solver.hasSolution();
                    if (hasSolution.isPresent()) {
                        if (hasSolution.get()) {
                            final int[] internalSolution = solver.getInternalSolution();
                            System.arraycopy(internalSolution, 0, configuration.get(), 0, configuration.size());
                            configuration.clear();
                        } else {
                            throw new RuntimeContradictionException();
                        }
                    } else {
                        throw new RuntimeTimeoutException();
                    }
                } finally {
                    solver.getAssignment().clear(orgAssignmentSize);
                }
            }
        }
        return new BooleanSolution(configuration.get(), false);
    }

    private boolean isSelectionPossibleSol(PartialConfiguration configuration, int[] literals) {
        for (BooleanSolution configuration2 : configuration.solverSolutions) {
            if (!configuration2.containsAnyNegated(literals)) {
                return true;
            }
        }
        return false;
    }

    private boolean trySelectSat(PartialConfiguration configuration, final int[] combinationLiterals) {
        final int oldModelCount = configuration.visitor.getAddedLiteralCount();
        try {
            configuration.visitor.propagate(combinationLiterals);
        } catch (RuntimeException e) {
            configuration.visitor.reset(oldModelCount);
            return false;
        }

        final int orgAssignmentSize = setUpSolver(configuration);
        try {
            if (newConfiguration != null) {
                for (int i = 0; i < newConfiguration.visitor.getAddedLiteralCount(); i++) {
                    int l = newConfiguration.visitor.getAddedLiterals()[i];
                    if (configuration.get()[Math.abs(l) - 1] == 0) {
                        solver.getAssignment().add(l);
                    }
                }
            } else {
                for (int i = 0; i < combinationLiterals.length; i++) {
                    int l = combinationLiterals[i];
                    if (configuration.get()[Math.abs(l) - 1] == 0) {
                        solver.getAssignment().add(l);
                    }
                }
            }
            Result<Boolean> hasSolution = solver.hasSolution();
            if (hasSolution.isPresent()) {
                if (hasSolution.get()) {
                    final BooleanSolution e = addSolverSolution();
                    for (int i = oldModelCount; i < configuration.visitor.getAddedLiteralCount(); i++) {
                        ExpandableIntegerList indexList = currentSampleIndices.get(ModalImplicationGraph.getVertexIndex(
                                configuration.visitor.getAddedLiterals()[i]));
                        final int idIndex = Arrays.binarySearch(
                                indexList.getInternalArray(), 0, indexList.size(), configuration.id);
                        if (idIndex < 0) {
                            indexList.add(configuration.id, -(idIndex + 1));
                        }
                    }
                    configuration.updateSolutionList(oldModelCount);
                    configuration.solverSolutions.add(e);
                    return true;
                } else {
                    configuration.visitor.reset(oldModelCount);
                }
            } else {
                configuration.visitor.reset(oldModelCount);
            }
        } finally {
            solver.getAssignment().clear(orgAssignmentSize);
        }
        return false;
    }

    @Override
    protected SAT4JSolver newSolver(BooleanAssignmentList clauseList) {
        return new SAT4JSolutionSolver(clauseList);
    }

    private BooleanSolution addSolverSolution() {
        if (randomSample.size() == internalConfigurationLimit) {
            randomSample.removeFirst();
        }
        final int[] solution = solver.getInternalSolution();
        final BooleanSolution e = new BooleanSolution(Arrays.copyOf(solution, solution.length), false);
        randomSample.add(e);
        solver.shuffleOrder(random);
        return e;
    }

    private int setUpSolver(PartialConfiguration configuration) {
        final int orgAssignmentSize = solver.getAssignment().size();
        for (int i = 0; i < configuration.visitor.getAddedLiteralCount(); i++) {
            solver.getAssignment().add(configuration.visitor.getAddedLiterals()[i]);
        }
        return orgAssignmentSize;
    }
}
