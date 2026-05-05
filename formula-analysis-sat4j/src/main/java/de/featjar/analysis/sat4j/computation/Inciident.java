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

import de.featjar.analysis.IConfigurationTester;
import de.featjar.analysis.IConfigurationUpdater;
import de.featjar.analysis.sat4j.solver.ModalImplicationGraph;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.IntegerList;
import de.featjar.base.data.Result;
import de.featjar.base.data.combination.CombinationStream;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.index.SampleBitIndex;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Detect interactions from given set of configurations.
 *
 * @author Sebastian Krieter
 */
public class Inciident extends ASAT4JAnalysis.Solution<BooleanAssignment> {

    private static final class DefaultTester implements IConfigurationTester {
        @Override
        public Result<Integer> test(BooleanAssignment configuration) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VariableMap getVariableMap() {
            return null;
        }

        @Override
        public void setVariableMap(VariableMap variableMap) {}
    }

    public static final Dependency<Integer> T = Dependency.newDependency(Integer.class);
    public static final Dependency<BooleanAssignmentList> INITIAL_SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<ModalImplicationGraph> MIG = Dependency.newDependency(ModalImplicationGraph.class);

    public static final Dependency<IConfigurationTester> TESTER = Dependency.newDependency(IConfigurationTester.class);
    public static final Dependency<Integer> TESTING_LIMIT = Dependency.newDependency(Integer.class);

    public Inciident(IComputation<BooleanAssignmentList> clauseList, Object... computations) {
        super(
                clauseList,
                Computations.of(1),
                Computations.of(new BooleanAssignmentList((VariableMap) null)),
                new MIGBuilder(clauseList),
                Computations.of(new DefaultTester()),
                Computations.of(Integer.MAX_VALUE),
                computations);
    }

    protected Inciident(Inciident other) {
        super(other);
    }

    private IConfigurationUpdater updater;
    private IConfigurationTester tester;
    private ModalImplicationGraph mig;

    private int testLimit;
    private int tmax;
    private int variableCount;

    private SampleBitIndex succeedingConfs;
    private SampleBitIndex failingConfs;

    private int testingCounter;
    private int[] lastMerge;

    @Override
    public Result<BooleanAssignment> compute(List<Object> dependencyList, Progress progress) {
        testLimit = TESTING_LIMIT.get(dependencyList);
        testingCounter = 0;

        tester = TESTER.get(dependencyList);
        if (tester instanceof DefaultTester) {
            return Result.empty(new IllegalArgumentException("No tester specified!"));
        }

        BooleanAssignmentList booleanAssignmentList = BOOLEAN_CLAUSE_LIST.get(dependencyList);
        VariableMap variableMap = booleanAssignmentList.getVariableMap();
        tester.setVariableMap(variableMap);
        succeedingConfs = new SampleBitIndex(variableMap);
        failingConfs = new SampleBitIndex(variableMap);

        BooleanAssignmentList initialSample = INITIAL_SAMPLE.get(dependencyList);
        initialSample.forEach(this::test);

        if (failingConfs.size() == 0) {
            return Result.empty(new IllegalArgumentException("No failing configurations!"));
        }

        tmax = T.get(dependencyList);

        mig = MIG.get(dependencyList);

        updater = new RandomConfigurationUpdater(initialSample, RANDOM_SEED.get(dependencyList));

        lastMerge = null;

        @SuppressWarnings("unchecked")
        List<int[]>[] results = new List[tmax];
        BooleanAssignment[] mergedResults = new BooleanAssignment[tmax];
        for (int ti = 1; ti <= tmax; ++ti) {
            List<int[]> res = findT(ti);
            if (res != null) {
                mergedResults[ti - 1] = new BooleanAssignment(lastMerge);
                results[ti - 1] = res;
            }
        }

        int lastI = -1;

        loop:
        for (int i = tmax - 1; i >= 0; --i) {
            if (mergedResults[i] != null) {
                if (lastI == -1) {
                    lastI = i;
                } else {
                    final BooleanAssignment lastMergedResult = mergedResults[lastI];
                    final BooleanAssignment curMergedResult = mergedResults[i];
                    if (lastMergedResult.containsAll(curMergedResult)) {
                        if (!curMergedResult.containsAll(lastMergedResult)) {
                            final LinkedHashSet<int[]> exclude = new LinkedHashSet<>();
                            for (int[] r : results[lastI]) {
                                int[] nr = new int[r.length];
                                int nrIndex = 0;
                                for (int l : r) {
                                    if (!curMergedResult.contains(l)) {
                                        nr[nrIndex++] = l;
                                    }
                                }
                                if (nrIndex == 0) {
                                    continue loop;
                                }
                                nr = nrIndex == nr.length ? nr : Arrays.copyOf(nr, nrIndex);
                                exclude.add(nr);
                            }
                            final BooleanSolution complete = updater.complete(
                                            List.of(curMergedResult.get()), exclude, null)
                                    .orElse(null);
                            if (complete != null && test(complete)) {
                                break loop;
                            }
                        }
                        lastI = i;
                    } else {
                        break loop;
                    }
                }
            }
        }

        final List<int[]> result = lastI == -1 ? null : results[lastI];
        return isPotentialInteraction(result)
                ? Result.of(new BooleanAssignment(
                        IntegerList.mergeInt(result.stream().collect(Collectors.toList()))))
                : Result.empty();
    }

    private List<int[]> computePotentialInteractions(int t) {
        int[] commonLiterals = new BooleanAssignment(IntStream.rangeClosed(-variableCount, variableCount)
                        .filter(l -> l != 0)
                        .filter(l -> failingConfs.size(l) == failingConfs.size())
                        .toArray())
                .removeAllInts(mig.getCore());

        if (commonLiterals.length < t) {
            return List.of(commonLiterals);
        }

        Stream<int[]> stream = CombinationStream.parallelStream(commonLiterals, t) //
                .map(combo -> combo.select());
        List<int[]> interactions;
        if (lastMerge != null) {
            BooleanAssignment lastLiterals = new BooleanAssignment(lastMerge).toClause();
            if (lastLiterals.containsAll(commonLiterals)) {
                return null;
            }
            interactions = stream //
                    .filter(literals -> !succeedingConfs.test(literals)) //
                    .filter(literals -> !lastLiterals.containsAll(literals)) //
                    .map(literals -> Arrays.copyOf(literals, literals.length)) //
                    .collect(Collectors.toList());
            interactions.add(lastMerge);
        } else {
            interactions = stream //
                    .filter(literals -> !succeedingConfs.test(literals)) //
                    .map(literals -> Arrays.copyOf(literals, literals.length)) //
                    .collect(Collectors.toList());
        }
        return interactions;
    }

    private List<int[]> findT(int t) {
        if (lastMerge != null && lastMerge.length <= t) {
            lastMerge = null;
        }

        List<int[]> curInteractionList = computePotentialInteractions(t);
        if (curInteractionList == null) {
            return null;
        }

        while (curInteractionList.size() > 1 //
                && testingCounter < testLimit) {
            BooleanSolution bestConfig =
                    updater.complete(null, null, curInteractionList).orElse(null);
            if (bestConfig == null) {
                break;
            }

            Map<Boolean, List<int[]>> partitions = group(curInteractionList, bestConfig);
            List<int[]> include = partitions.get(Boolean.TRUE);
            List<int[]> exclude = partitions.get(Boolean.FALSE);
            int diff = Math.abs(include.size() - exclude.size());
            int lastDiff = diff;

            while (diff > 1) {
                BooleanSolution config;
                if (include.size() > exclude.size()) {
                    config = updater.complete(null, exclude, include).orElse(null);
                    if (config == null) {
                        break;
                    }
                    partitions = group(include, config);
                    assert partitions.get(Boolean.FALSE) != null;
                    assert partitions.get(Boolean.TRUE) != null;
                    diff = Math.abs(
                            (exclude.size() + partitions.get(Boolean.FALSE).size())
                                    - partitions.get(Boolean.TRUE).size());
                    if (diff >= lastDiff) {
                        break;
                    }
                    exclude.addAll(partitions.get(Boolean.FALSE));
                    include = partitions.get(Boolean.TRUE);
                } else {
                    config = updater.complete(include, null, exclude).orElse(null);
                    if (config == null) {
                        break;
                    }
                    partitions = group(exclude, config);
                    assert partitions.get(Boolean.FALSE) != null;
                    assert partitions.get(Boolean.TRUE) != null;
                    diff = Math.abs(
                            (include.size() + partitions.get(Boolean.TRUE).size())
                                    - partitions.get(Boolean.FALSE).size());
                    if (diff >= lastDiff) {
                        break;
                    }
                    include.addAll(partitions.get(Boolean.TRUE));
                    exclude = partitions.get(Boolean.FALSE);
                }
                lastDiff = diff;
                bestConfig = config;
            }

            final boolean pass = test(bestConfig);
            curInteractionList = pass ? exclude : include;
            if (lastMerge != null && pass == bestConfig.containsAll(lastMerge)) {
                lastMerge = null;
            }
        }

        if (curInteractionList.isEmpty()) {
            return null;
        } else {
            lastMerge = IntegerList.mergeInt(curInteractionList);
            return curInteractionList;
        }
    }

    private Map<Boolean, List<int[]>> group(List<int[]> list, final BooleanSolution newConfig) {
        return list.stream()
                .collect(Collectors.groupingByConcurrent(
                        i -> newConfig.containsAll(i), Collectors.toCollection(ArrayList::new)));
    }

    private boolean test(BooleanAssignment solution) {
        testingCounter++;
        Result<Integer> testResult = tester.test(solution);
        if (testResult.orElseThrow() == 0) {
            succeedingConfs.addConfiguration(solution);
            return true;
        } else {
            failingConfs.addConfiguration(solution);
            return false;
        }
    }

    private boolean isPotentialInteraction(List<int[]> interactions) {
        if (interactions == null) {
            return false;
        }
        final BooleanSolution testConfig =
                updater.complete(interactions, null, null).orElse(null);
        if (testConfig == null || test(testConfig)) {
            return false;
        }
        int[] exclude = IntegerList.mergeInt(interactions);
        final BooleanSolution inverseConfig =
                updater.complete(null, List.of(exclude), null).orElse(null);
        return inverseConfig == null || test(inverseConfig);
    }
}
