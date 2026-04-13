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

public class MIGVisitorLight implements IMIGVisitor {

    private final ModalImplicationGraph mig;
    private final byte[] model;
    private final ExpandableIntegerList addedLiterals;

    public MIGVisitorLight(ModalImplicationGraph mig) {
        this.mig = mig;
        this.model = new byte[mig.size];
        for (int l : mig.core) {
            model[Math.abs(l) - 1] = encode(l);
        }
        addedLiterals = new ExpandableIntegerList((mig.size() - mig.core.length) / 8 + 1);
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
        while (!addedLiterals.isEmpty()) {
            final int l = addedLiterals.getLast();
            addedLiterals.removeLast();
            model[Math.abs(l) - 1] = 0;
        }
    }

    @Override
    public void reset(int keep) {
        while (addedLiterals.size() > keep) {
            int last = addedLiterals.getLast();
            model[Math.abs(last) - 1] = 0;
            addedLiterals.removeLast();
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
                } else if (setL1 != encode(strongL)) {
                    throw new RuntimeContradictionException();
                }
            }
        } else if (setL != encode(l)) {
            throw new RuntimeContradictionException();
        }
    }

    private byte encode(int l) {
        return (byte) ((l >>> 31) | 2);
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
