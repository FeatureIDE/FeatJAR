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
package de.featjar.analysis.sat4j.solver;

import de.featjar.analysis.RuntimeContradictionException;
import de.featjar.base.data.ExpandableIntegerList;
import java.util.Arrays;

public class MIGVisitorByte implements IMIGVisitor {

    private final ModalImplicationGraph mig;
    private final int[] clauseCounts;
    private final byte[] model;
    private final ExpandableIntegerList addedLiterals;

    public MIGVisitorByte(ModalImplicationGraph mig) {
        this.mig = mig;
        this.model = new byte[mig.size];
        for (int l : mig.core) {
            model[Math.abs(l) - 1] = encode(l);
        }
        addedLiterals = new ExpandableIntegerList((mig.size() - mig.core.length) / 8 + 1);
        clauseCounts = Arrays.copyOf(mig.clauseLengths, mig.clauseLengths.length);
    }

    @Override
    public int[] getAddedLiterals() {
        return addedLiterals.getInternalArray();
    }

    @Override
    public int getAddedLiteralCount() {
        return addedLiterals.size();
    }

    @Override
    public void propagate(int... literals) throws RuntimeContradictionException {
        for (int l : literals) {
            if (l != 0) {
                processLiteral(l);
            }
        }
    }

    @Override
    public void setLiterals(int... literals) throws RuntimeContradictionException {
        for (int l : literals) {
            if (l != 0) {
                int index = Math.abs(l) - 1;
                byte setL = model[index];
                byte newL = encode(l);
                if (setL == 0) {
                    model[index] = newL;
                    addedLiterals.add(l);
                } else if (setL != newL) {
                    throw new RuntimeContradictionException();
                }
            }
        }
    }

    @Override
    public boolean isContradiction(int... literals) {
        final int oldModelCount = getAddedLiteralCount();
        try {
            propagate(literals);
            return false;
        } catch (RuntimeContradictionException e) {
            return true;
        } finally {
            reset(oldModelCount);
        }
    }

    @Override
    public void reset() {
        for (int i = 0, count = addedLiterals.size(); i < count; i++) {
            final int l = addedLiterals.getLast();
            addedLiterals.removeLast();
            model[Math.abs(l) - 1] = 0;
            for (int clauseCountIndex : mig.clauseLengthsIndices[ModalImplicationGraph.getVertexIndex(l)]) {
                clauseCounts[clauseCountIndex] = mig.clauseLengths[clauseCountIndex];
            }
        }
    }

    @Override
    public void reset(int keep) {
        int addedLiteralCount = addedLiterals.size();
        for (int i = 0; i < addedLiteralCount; i++) {
            for (int clauseCountIndex :
                    mig.clauseLengthsIndices[ModalImplicationGraph.getVertexIndex(addedLiterals.get(i))]) {
                clauseCounts[clauseCountIndex] = mig.clauseLengths[clauseCountIndex];
            }
        }
        for (int i = keep; i < addedLiteralCount; i++) {
            model[Math.abs(addedLiterals.getLast()) - 1] = 0;
            addedLiterals.removeLast();
        }
        for (int i = 0; i < keep; i++) {
            for (int j : mig.clauseLengthsIndices[ModalImplicationGraph.getVertexIndex(addedLiterals.get(i))]) {
                --clauseCounts[j];
            }
        }
    }

    private void processLiteral(int l) {
        final int varIndex = Math.abs(l) - 1;
        final int setL = model[varIndex];
        if (setL == 0) {
            model[varIndex] = encode(l);
            addedLiterals.add(l);

            final int i = ModalImplicationGraph.getVertexIndex(l);

            for (int strongL : mig.strong[i]) {
                final int varIndex1 = Math.abs(strongL) - 1;
                final int setL1 = model[varIndex1];
                if (setL1 == 0) {
                    model[varIndex1] = encode(strongL);
                    addedLiterals.add(strongL);
                    processWeak(ModalImplicationGraph.getVertexIndex(strongL));
                } else if (setL1 != encode(strongL)) {
                    throw new RuntimeContradictionException();
                }
            }

            processWeak(i);
        } else if (setL != encode(l)) {
            throw new RuntimeContradictionException();
        }
    }

    private byte encode(int l) {
        return (byte) ((l >>> 31) | 2);
    }

    private void processWeak(final int index) {
        final int[] clauseCountIndexList = mig.clauseLengthsIndices[index];
        weakLoop:
        for (int j = 0; j < clauseCountIndexList.length; j++) {
            final int clauseCountIndex = clauseCountIndexList[j];
            final int count = --clauseCounts[clauseCountIndex];
            if (count <= 1) {
                if (count == 1) {
                    int clauseIndex = mig.clauseIndices[index][j];
                    for (int end = clauseIndex + mig.clauseLengths[clauseCountIndex], k = clauseIndex; k < end; k++) {
                        final int newL = mig.clauses[k];
                        int varIndex = Math.abs(newL) - 1;
                        final int modelL = model[varIndex];
                        if (modelL == 0 || modelL == encode(newL)) {
                            processLiteral(newL);
                            continue weakLoop;
                        }
                    }
                }
                throw new RuntimeContradictionException();
            }
        }
    }

    @Override
    public boolean isUndefined(int literal) {
        return model[Math.abs(literal) - 1] == 0;
    }

    @Override
    public int countUndefined(int[] literals) {
        int count = 0;
        for (int j : literals) {
            if (model[Math.abs(j) - 1] == 0) {
                count++;
            }
        }
        return count;
    }
}
