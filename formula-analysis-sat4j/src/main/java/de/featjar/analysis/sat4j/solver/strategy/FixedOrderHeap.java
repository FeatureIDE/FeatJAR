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
package de.featjar.analysis.sat4j.solver.strategy;

import static org.sat4j.core.LiteralsUtils.negLit;
import static org.sat4j.core.LiteralsUtils.posLit;
import static org.sat4j.core.LiteralsUtils.var;

import java.util.Random;
import java.util.function.Function;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.specs.ISolver;

/**
 * Modified variable order for {@link ISolver}.<br>
 * Initializes the used heap in a certain order.
 *
 * @author Sebastian Krieter
 */
public class FixedOrderHeap extends VarOrderHeap {

    public static class GeneralSelectionStrategy implements IPhaseSelectionStrategy {

        private static final long serialVersionUID = 1L;

        public static final int SELECT_ORG = 0;
        public static final int SELECT_NEGATIVE = 1;
        public static final int SELECT_POSITIVE = 2;
        public static final int SELECT_RANDOM = 3;

        protected int[] selectionMask;

        protected int[] phase;

        public final Random RAND = new Random(123456789);

        public GeneralSelectionStrategy(int[] selectionMask) {
            this.selectionMask = selectionMask;
            phase = new int[selectionMask.length + 1];
        }

        @Override
        public void init(int nlength) {
            assert phase.length == nlength;
            for (int i = 1; i < nlength; i++) {
                phase[i] = selectionMask[i - 1] == SELECT_POSITIVE ? posLit(i) : negLit(i);
            }
        }

        @Override
        public void init(int var, int p) {
            this.phase[var] = p;
        }

        @Override
        public int select(int var) {
            return switch (selectionMask[var - 1]) {
                case SELECT_NEGATIVE -> negLit(var);
                case SELECT_POSITIVE -> posLit(var);
                case SELECT_RANDOM -> RAND.nextBoolean() ? posLit(var) : negLit(var);
                default -> phase[var];
            };
        }

        @Override
        public void assignLiteral(int p) {
            phase[var(p)] = p;
        }

        @Override
        public void updateVar(int p) {}

        @Override
        public void updateVarAtDecisionLevel(int p) {}
    }

    private static final long serialVersionUID = 1L;
    private int[] order;
    private boolean[] orderMask;

    private static int[] createSelectionMask(boolean[] orderMask, Function<Integer, Integer> sup) {
        int[] selectionMask = new int[orderMask.length];
        for (int i = 0; i < orderMask.length; i++) {
            selectionMask[i] = orderMask[i] ? GeneralSelectionStrategy.SELECT_ORG : sup.apply(i);
        }
        return selectionMask;
    }

    public FixedOrderHeap(int[] order, boolean[] orderMask, Function<Integer, Integer> sup) {
        super(new GeneralSelectionStrategy(createSelectionMask(orderMask, sup)));
        this.order = order;
        this.orderMask = orderMask != null ? orderMask : new boolean[order.length];
    }

    public FixedOrderHeap(IPhaseSelectionStrategy strategy, int[] order, boolean[] orderMask) {
        super(strategy);
        this.order = order;
        this.orderMask = orderMask != null ? orderMask : new boolean[order.length];
    }

    public FixedOrderHeap(IPhaseSelectionStrategy strategy, int[] order) {
        this(strategy, order, null);
    }

    @Override
    public void init() {
        int nlength = lits.nVars() + 1;
        if (activity == null || activity.length < nlength) {
            activity = new double[nlength];
        }
        phaseStrategy.init(nlength);
        activity[0] = -1;
        heap = createHeap(activity);
        heap.setBounds(nlength);
        for (int x : order) {
            if (orderMask[x - 1]) {
                activity[x] = 0.0;
                if (lits.belongsToPool(x)) {
                    heap.insert(x);
                }
            }
        }
        for (int x : order) {
            if (!orderMask[x - 1]) {
                activity[x] = 1.0;
                if (lits.belongsToPool(x)) {
                    heap.insert(x);
                }
            }
        }
    }
}
