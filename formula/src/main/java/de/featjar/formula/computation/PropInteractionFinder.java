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
import de.featjar.base.data.combination.CombinationStream;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanClause;
import de.featjar.formula.assignment.BooleanSolution;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Calculates statistics regarding t-wise feature coverage of a set of
 * solutions.
 *
 * @author Sebastian Krieter
 */
// TODO convert to computation
public class PropInteractionFinder {

    private IConfigurationUpdater updater;
    private IConfigurationTester verifier;
    private BooleanAssignment core;

    private List<BooleanSolution> succeedingConfs;
    private List<BooleanSolution> failingConfs;

    private int verifyCounter;

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

    public void addConfigurations(List<BooleanSolution> configurations) {
        configurations.forEach(this::verify);
    }

    public List<BooleanAssignment> find(int t) {
        int[] tempLiterals = failingConfs.get(0).get();
        int[] variantLiterals = new int[tempLiterals.length - core.size()];
        int variantLiteralsIndex = 0;
        for (int i = 0; i < tempLiterals.length; i++) {
            int l = tempLiterals[i];
            if (l != 0) {
                variantLiterals[variantLiteralsIndex++] = Math.abs(l);
            }
        }

        final int n = variantLiterals.length;
        final int t2 = (n < t) ? n : t;
        final int pow = (int) Math.pow(2, t2);

        ArrayList<int[]> interactions = new ArrayList<>();

        boolean[][] masks = new boolean[pow][t2];
        for (int i = 0; i < masks.length; i++) {
            boolean[] p = masks[i];
            for (int j = 0; j < t2; j++) {
                p[j] = (i >> j & 1) == 0;
            }
        }

        IntStream.range(0, pow) //
                .forEach(maskIndex -> {
                    boolean[] mask = new boolean[t2];
                    for (int j = 0; j < t2; j++) {
                        mask[j] = (maskIndex >> j & 1) == 0;
                    }
                    int[] literals = new int[t2];

                    CombinationStream.stream(variantLiterals, t2).forEach(combo -> {
                        for (int k = 0; k < t2; k++) {
                            final int l = variantLiterals[(int) combo.selectionIndices()[k]];
                            literals[k] = mask[k] ? l : -l;
                        }

                        int succ = 0;
                        int fail = 0;
                        for (BooleanSolution config : failingConfs) {
                            if (config.containsAll(literals)) {
                                fail++;
                            }
                        }
                        for (BooleanSolution config : succeedingConfs) {
                            if (config.containsAll(literals)) {
                                succ++;
                            }
                        }
                        if (fail == 0 && succ == 0) {
                            BooleanSolution config = updater.complete(List.of(literals), null, null)
                                    .orElse(null);
                            if (config != null) {
                                if (verify(config)) {
                                    succ++;
                                    loop:
                                    for (int[] interaction : interactions) {
                                        for (int i = 0; i < t2; i++) {
                                            if (config.indexOf(interaction[i]) < 0) {
                                                continue loop;
                                            }
                                        }
                                        interaction[t2 + 1]++;
                                    }
                                } else {
                                    fail++;
                                    loop:
                                    for (int[] interaction : interactions) {
                                        for (int i = 0; i < t2; i++) {
                                            if (config.indexOf(interaction[i]) < 0) {
                                                continue loop;
                                            }
                                        }
                                        interaction[t2]++;
                                    }
                                }
                            }
                            config = updater.complete(null, List.of(literals), null)
                                    .orElse(null);
                            if (config != null) {
                                if (verify(config)) {
                                    loop:
                                    for (int[] interaction : interactions) {
                                        for (int i = 0; i < t2; i++) {
                                            if (config.indexOf(interaction[i]) < 0) {
                                                continue loop;
                                            }
                                        }
                                        interaction[t2 + 1]++;
                                    }
                                } else {
                                    loop:
                                    for (int[] interaction : interactions) {
                                        for (int i = 0; i < t2; i++) {
                                            if (config.indexOf(interaction[i]) < 0) {
                                                continue loop;
                                            }
                                        }
                                        interaction[t2]++;
                                    }
                                }
                            }
                        }
                        if (fail != 0 || succ != 0) {
                            int[] interaction = Arrays.copyOf(literals, literals.length + 2);
                            interaction[t2] = fail;
                            interaction[t2 + 1] = succ;
                            interactions.add(interaction);
                        }
                    });
                });

        interactions.stream().forEach(a -> a[t2] = support(a[t2], a[t2 + 1]));
        Collections.sort(interactions, Comparator.comparing(a -> a[t2]));
        double max = interactions.get(0)[t2];
        List<BooleanClause> collect = interactions.stream()
                .takeWhile(a -> (a[t2] / max) > 0)
                .map(this::ll)
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < collect.size(); i++) {
            collect.set(
                    i,
                    updater.update(collect.get(i))
                            .map(l -> l.toClause().removeAll(core))
                            .orElse(null));
        }

        //		ArrayList<LiteralList> merge = new ArrayList<>();
        //		for (LiteralList in : collect) {
        //			boolean merged = false;
        //			for (int i = 0; i < merge.size(); i++) {
        //				LiteralList config = updater.complete(null, null, List.of(in.getLiterals(), merge.get(i).getLiterals()))
        //						.orElse(null);
        //				if (config == null) {
        //					merge.set(i, LiteralList.merge(List.of(in, merge.get(i))));
        //					merged = true;
        //				}
        //			}
        //			if (!merged) {
        //				merge.add(in);
        //			}
        //		}
        //
        //		for (int i = 0; i < merge.size(); i++) {
        //			merge.set(i, updater.update(merge.get(i)).orElse(null));
        //		}
        for (int i = 0; i < collect.size(); i++) {
            BooleanClause clause = collect.get(i);
            for (int j = i + 1; j < collect.size(); j++) {
                if (collect.get(j).containsAll(clause)) {
                    collect.set(i, null);
                    break;
                }
            }
        }
        Collections.sort(collect, Comparator.comparing(a -> a.size()));
        //		Collections.reverse(collect);
        //		for (int i = 0; i < collect.size(); i++) {
        //			for (int j = i + 1; j < collect.size(); j++) {
        //				if (collect.get(i).containsAll(collect.get(j))) {
        //					collect.set(i, null);
        //					break;
        //				}
        //			}
        //		}
        //		for (int i = 0; i < collect.size(); i++) {
        //			for (int j = i + 1; j < collect.size(); j++) {
        //				if (collect.get(j).containsAll(collect.get(i))) {
        //					collect.set(i, null);
        //					break;
        //				}
        //			}
        //		}

        ArrayList<int[]> merge2 = new ArrayList<>();
        for (BooleanClause literals : collect) {
            if (literals != null) {
                int succ = 0;
                int fail = 0;
                for (BooleanSolution config : failingConfs) {
                    if (config.containsAll(literals)) {
                        fail++;
                    }
                }
                for (BooleanSolution config : succeedingConfs) {
                    if (config.containsAll(literals)) {
                        succ++;
                    }
                }
                int[] interaction = Arrays.copyOf(literals.get(), literals.size() + 1);
                interaction[literals.size()] = support(fail, succ);
                merge2.add(interaction);
            }
        }

        Collections.sort(merge2, Comparator.comparing(a -> a[a.length - 1]));
        double max2 = merge2.get(0)[t2];
        return merge2.stream()
                .takeWhile(a -> (a[t2] / max2) > 0.5)
                .map(this::ll2)
                .collect(Collectors.toList());
    }

    private BooleanClause ll(int[] a) {
        return new BooleanClause(Arrays.copyOf(a, a.length - 2));
    }

    private BooleanAssignment ll2(int[] a) {
        return new BooleanAssignment(Arrays.copyOf(a, a.length - 1));
    }

    private int support(int fail, int succ) {
        int propFail = fail * succeedingConfs.size();
        int propSucc = succ * failingConfs.size();
        return propSucc - propFail;
    }

    private boolean verify(BooleanSolution solution) {
        verifyCounter++;
        BooleanSolution nonCore = solution.removeAll(core);
        if (verifier.test(solution).valueEquals(0)) {
            succeedingConfs.add(nonCore);
            return true;
        } else {
            failingConfs.add(nonCore);
            return false;
        }
    }

    public int getVerifyCounter() {
        return verifyCounter;
    }
}
