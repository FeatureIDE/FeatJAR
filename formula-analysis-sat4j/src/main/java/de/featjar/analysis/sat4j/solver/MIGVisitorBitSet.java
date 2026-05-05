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
import java.util.BitSet;

public class MIGVisitorBitSet implements IMIGVisitor {

    private final ModalImplicationGraph mig;
    private final BitSet model;
    private final ExpandableIntegerList addedLiterals;

    public MIGVisitorBitSet(ModalImplicationGraph mig) {
        this.mig = mig;
        this.model = new BitSet();
        for (int l : mig.core) {
            model.set(encode(l));
        }
        addedLiterals = new ExpandableIntegerList((mig.size - mig.core.length) / 8 + 1);
    }

    public MIGVisitorBitSet(MIGVisitorBitSet other) {
        this.mig = other.mig;
        this.model = (BitSet) other.model.clone();
        this.addedLiterals = new ExpandableIntegerList(other.addedLiterals);
    }

    private static int encode(int l) {
        return ((l ^ (l >> 31)) << 1) - (l >> 31);
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
                setLiteral(l);
            }
        }
    }

    private boolean setLiteral(int l) {
        int index = encode(l);
        if (model.get(index)) {
            return false;
        }
        if (model.get(encode(-l))) {
            throw new RuntimeContradictionException();
        }
        model.set(index);
        addedLiterals.add(l);
        return true;
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
        reset(0);
    }

    @Override
    public void reset(int keep) {
        while (addedLiterals.size() > keep) {
            model.clear(encode(addedLiterals.getLast()));
            addedLiterals.removeLast();
        }
    }

    private void processLiteral(int l) {
        if (setLiteral(l)) {
            for (int strongL : mig.strong[ModalImplicationGraph.getVertexIndex(l)]) {
                setLiteral(strongL);
            }
        }
    }

    @Override
    public boolean isUndefined(int literal) {
        return !model.get(encode(literal)) && !model.get(encode(-literal));
    }

    @Override
    public int countUndefined(int[] literals) {
        int count = 0;
        for (int l : literals) {
            if (isUndefined(l)) {
                count++;
            }
        }
        return count;
    }
}
