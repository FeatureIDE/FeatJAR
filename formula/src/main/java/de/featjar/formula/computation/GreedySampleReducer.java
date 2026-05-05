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

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.ValuedBooleanAssignment;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Removes solutions from a given sample without reducing the t-wise interaction coverage for a given t.
 *
 * @author Sebastian Krieter
 * @author Rahel Sundermann
 */
public class GreedySampleReducer extends AComputation<BooleanAssignmentList> {

    private static class Config extends BooleanAssignment {
        private static final long serialVersionUID = 6908616425377756720L;

        private double score = 0;
        private int interactionCount = 0;

        public Config(BooleanAssignment solution) {
            super(solution);
        }

        public synchronized void incScore(double s) {
            score += s;
            interactionCount++;
        }

        public synchronized void decScore(double s) {
            score -= s;
            interactionCount--;
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

    public static final Dependency<BooleanAssignmentList> SAMPLE =
            Dependency.newDependency(BooleanAssignmentList.class);

    public static final Dependency<Integer> T = Dependency.newDependency(Integer.class);

    private Config[] fieldConfigurations;
    private int n, t, t2;

    private LinkedHashSet<BooleanAssignment> reducedSample;
    private LinkedHashSet<ValuedBooleanAssignment> interactions;
    private BitSet mainIndex;
    private BitSet[] indices;

    public GreedySampleReducer(GreedySampleReducer other) {
        super(other);
    }

    public GreedySampleReducer(IComputation<BooleanAssignmentList> sampleComputation) {
        super(sampleComputation, new ComputeConstant<>(1));
    }

    private void generate(int first) {
        int[] elementIndices;
        boolean[] marker;
        elementIndices = new int[t];
        marker = new boolean[t];
        for (int i = 0; i < t - 1; i++) {
            elementIndices[i] = i;
        }
        elementIndices[t - 1] = first;

        int i = 0;
        for (; ; ) {
            int[] literals = new int[t];
            BitSet curIndices = (BitSet) mainIndex.clone();
            for (int k2 = 0; k2 < literals.length; k2++) {
                int var = elementIndices[k2] + 1;
                int l = marker[k2] ? -var : var;
                literals[k2] = l;
                curIndices.and(indices[l + n]);
            }
            int counter = curIndices.cardinality();
            if (counter > 1) {
                ValuedBooleanAssignment interaction = new ValuedBooleanAssignment(literals);
                interaction.setValue(counter);
                synchronized (interactions) {
                    interactions.add(interaction);
                }
            } else if (counter == 1) {
                BooleanAssignment config = fieldConfigurations[curIndices.nextSetBit(0)];
                synchronized (reducedSample) {
                    reducedSample.add(config);
                }
            }

            for (i = 0; i < t2; i++) {
                int index = elementIndices[i];
                if (index + 1 < elementIndices[i + 1]) {
                    if (marker[i]) {
                        elementIndices[i] = index + 1;
                    }
                    marker[i] = !marker[i];
                    for (int j = i - 1; j >= 0; j--) {
                        elementIndices[j] = j;
                        marker[j] = false;
                    }
                    break;
                } else {
                    if (marker[i]) {
                        marker[i] = false;
                        continue;
                    } else {
                        marker[i] = true;
                        for (int j = i - 1; j >= 0; j--) {
                            elementIndices[j] = j;
                            marker[j] = false;
                        }
                        break;
                    }
                }
            }
            if (i == t2) {
                if (marker[i]) {
                    break;
                } else {
                    marker[i] = true;
                    for (int j = i - 1; j >= 0; j--) {
                        elementIndices[j] = j;
                        marker[j] = false;
                    }
                }
            }
        }
    }

    @Override
    public Result<BooleanAssignmentList> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList sample = SAMPLE.get(dependencyList);
        if (sample.size() == 0) {
            return Result.of(sample);
        }
        n = sample.get(0).size();

        if (t > n) {
            throw new IllegalArgumentException(String.format("%d > %d", t, n));
        }
        t = T.get(dependencyList);
        t2 = t - 1;
        fieldConfigurations = new Config[sample.size()];
        int fi = 0;
        for (BooleanAssignment solution : sample) {
            fieldConfigurations[fi++] = new Config(solution);
        }
        reducedSample = new LinkedHashSet<>();
        interactions = new LinkedHashSet<>();
        mainIndex = new BitSet(fieldConfigurations.length);
        mainIndex.flip(0, fieldConfigurations.length);

        indices = new BitSet[2 * n + 1];
        for (int j = 1; j <= n; j++) {
            BitSet negIndices = new BitSet(fieldConfigurations.length);
            BitSet posIndices = new BitSet(fieldConfigurations.length);
            for (int i = 0; i < fieldConfigurations.length; i++) {
                BooleanAssignment config = fieldConfigurations[i];
                if (config.get(j - 1) < 0) {
                    negIndices.set(i);
                } else {
                    posIndices.set(i);
                }
            }
            indices[n - j] = negIndices;
            indices[j + n] = posIndices;
        }

        IntStream.range(t - 1, n).parallel().forEach(this::generate);

        for (int j = 0; j < fieldConfigurations.length; j++) {
            BooleanAssignment config = fieldConfigurations[j];
            if (reducedSample.contains(config)) {
                mainIndex.clear(j);
            }
        }

        List<ValuedBooleanAssignment> alreadyCoveredValuedBooleanAssignments = interactions.parallelStream()
                .filter(interaction -> {
                    if (reducedSample.stream().anyMatch(c -> c.containsAll(interaction))) {
                        return true;
                    } else {
                        int[] is = interaction.get();
                        BitSet curIndices = (BitSet) mainIndex.clone();
                        for (int k2 = 0; k2 < is.length; k2++) {
                            curIndices.and(indices[is[k2] + n]);
                        }
                        double s = 1.0 / interaction.getValue();
                        curIndices.stream()
                                .mapToObj(i -> fieldConfigurations[i])
                                .forEach(c -> c.incScore(s));
                        return false;
                    }
                })
                .collect(Collectors.toList());
        interactions.removeAll(alreadyCoveredValuedBooleanAssignments);

        while (!interactions.isEmpty()) {
            double bestScore = -1;
            int bestConfigIndex = -1;

            for (int j = mainIndex.nextSetBit(0); j >= 0; j = mainIndex.nextSetBit(j + 1)) {
                Config config = fieldConfigurations[j];
                if (config.score <= 0 || config.interactionCount == 0) {
                    mainIndex.clear(j);
                } else if (config.score > bestScore) {
                    bestScore = config.score;
                    bestConfigIndex = j;
                }
            }
            if (bestConfigIndex < 0) {
                break;
            }

            BooleanAssignment bestConfig = fieldConfigurations[bestConfigIndex];
            reducedSample.add(bestConfig);
            mainIndex.clear(bestConfigIndex);

            List<ValuedBooleanAssignment> coveredValuedBooleanAssignments = interactions.parallelStream()
                    .filter(interaction -> bestConfig.containsAll(interaction))
                    .peek(interaction -> {
                        int[] is = interaction.get();
                        BitSet curIndices = (BitSet) mainIndex.clone();
                        for (int k2 = 0; k2 < is.length; k2++) {
                            curIndices.and(indices[is[k2] + n]);
                        }
                        double s = 1.0 / interaction.getValue();
                        curIndices.stream()
                                .mapToObj(i -> fieldConfigurations[i])
                                .forEach(c -> c.decScore(s));
                    })
                    .collect(Collectors.toList());

            interactions.removeAll(coveredValuedBooleanAssignments);
        }
        return Result.of(new BooleanAssignmentList(sample.getVariableMap(), reducedSample));
    }
}
