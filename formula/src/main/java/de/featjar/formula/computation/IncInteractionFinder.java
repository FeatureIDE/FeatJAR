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

import de.featjar.analysis.IConfigurationTester;
import de.featjar.analysis.IConfigurationUpdater;
import de.featjar.base.data.IntegerList;
import de.featjar.base.data.combination.CombinationStream;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Detect interactions from given set of configurations.
 *
 * @author Sebastian Krieter
 */
// TODO convert to computation
public class IncInteractionFinder {

    protected IConfigurationUpdater updater;
    private IConfigurationTester verifier;
    private BooleanAssignment core;

    protected int configurationVerificationLimit = Integer.MAX_VALUE;

    protected List<BooleanSolution> succeedingConfs;
    protected List<BooleanSolution> failingConfs;

    protected int verifyCounter;
    protected int[] lastMerge;

    public void reset() {
        succeedingConfs = new ArrayList<>();
        failingConfs = new ArrayList<>();
    }

    public void setUpdater(IConfigurationUpdater updater) {
        this.updater = updater;
    }

    public void setVerifier(IConfigurationTester verifier) {
        this.verifier = verifier;
    }

    public void setCore(BooleanAssignment core) {
        this.core = core;
    }

    public void setConfigurationVerificationLimit(int configurationVerificationLimit) {
        this.configurationVerificationLimit = configurationVerificationLimit;
    }

    public void addConfigurations(BooleanAssignmentList configurations) {
        configurations.forEach(this::verify);
    }

    public List<BooleanAssignment> find(int tmax) {
        if (failingConfs.isEmpty()) {
            return null;
        }
        verifyCounter = 0;
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
                            if (complete != null && verify(complete)) {
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
                ? List.of(new BooleanAssignment(
                        IntegerList.mergeInt(result.stream().collect(Collectors.toList()))))
                : null;
    }

    public List<BooleanSolution> getSample() {
        ArrayList<BooleanSolution> sample = new ArrayList<>(succeedingConfs.size() + failingConfs.size());
        sample.addAll(succeedingConfs);
        sample.addAll(failingConfs);
        return sample;
    }

    public int getVerifyCounter() {
        return verifyCounter;
    }

    protected List<int[]> computePotentialInteractions(int t) {
        final Iterator<BooleanSolution> iterator = failingConfs.iterator();
        BooleanAssignment failingLiterals = iterator.next();
        while (iterator.hasNext()) {
            failingLiterals = new BooleanAssignment(iterator.next().retainAll(failingLiterals.get()));
        }
        if (core != null) {
            failingLiterals = new BooleanAssignment(failingLiterals.removeAll(core.get()));
        }

        final int[] commonLiterals = failingLiterals.toAssignment().get();
        if (commonLiterals.length < t) {
            return List.of(commonLiterals);
        }

        Stream<int[]> stream = CombinationStream.parallelStream(commonLiterals, t) //
                .map(combo -> combo.select());
        List<int[]> interactions;
        if (lastMerge != null) {
            BooleanAssignment lastLiterals = new BooleanAssignment(lastMerge);
            if (lastLiterals.containsAll(failingLiterals)) {
                return null;
            }
            interactions = stream //
                    .filter(literals -> !lastLiterals.containsAll(literals)) //
                    .filter(literals -> !isCovered(literals)) //
                    .map(literals -> Arrays.copyOf(literals, literals.length)) //
                    .collect(Collectors.toList());
            interactions.add(lastMerge);
        } else {
            interactions = stream //
                    .filter(literals -> !isCovered(literals)) //
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
                && verifyCounter < configurationVerificationLimit) {
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

            final boolean pass = verify(bestConfig);
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

    private boolean isCovered(int[] combo) {
        for (BooleanSolution configuration : succeedingConfs) {
            if (configuration.containsAll(combo)) {
                return true;
            }
        }
        return false;
    }

    protected Map<Boolean, List<int[]>> group(List<int[]> list, final BooleanSolution newConfig) {
        return list.stream()
                .collect(Collectors.groupingByConcurrent(
                        i -> newConfig.containsAll(i), Collectors.toCollection(ArrayList::new)));
    }

    protected boolean verify(BooleanAssignment solution) {
        verifyCounter++;
        if (verifier.test(solution).valueEquals(0)) {
            succeedingConfs.add(solution.toSolution());
            return true;
        } else {
            failingConfs.add(solution.toSolution());
            return false;
        }
    }

    protected boolean isPotentialInteraction(List<int[]> interactions) {
        if (interactions == null) {
            return false;
        }
        final BooleanSolution testConfig =
                updater.complete(interactions, null, null).orElse(null);
        if (testConfig == null || verify(testConfig)) {
            return false;
        }
        int[] exclude = IntegerList.mergeInt(interactions);
        final BooleanSolution inverseConfig =
                updater.complete(null, List.of(exclude), null).orElse(null);
        return inverseConfig == null || verify(inverseConfig);
    }
}
