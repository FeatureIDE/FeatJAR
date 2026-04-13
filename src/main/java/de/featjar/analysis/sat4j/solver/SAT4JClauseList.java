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

import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanClause;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;

/**
 * ... This class breaks the Liskov principle, as it only allows appending
 * clauses at the end (i.e., implementing an assumption stack) and does not
 * allow for meaningful cloning due to being tied to a solver instance.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class SAT4JClauseList extends BooleanAssignmentList {
    protected final SAT4JSolver solver;
    protected final LinkedList<IConstr> addedConstraints = new LinkedList<>();

    public SAT4JClauseList(SAT4JSolver solver, BooleanAssignmentList other) {
        super(other);
        this.solver = solver;
        assignments.forEach(this::addConstraint);
    }

    @Override
    public void add(int index, BooleanAssignment clause) {
        if (index != assignments.size()) throw new UnsupportedOperationException();
        super.add(index, clause);
    }

    @Override
    public Result<BooleanAssignment> remove(int index) {
        if (index != assignments.size()) throw new UnsupportedOperationException();
        return super.remove(index);
    }

    protected void addConstraint(BooleanAssignment booleanClause) {
        addConstraint(booleanClause.get());
    }

    protected void addConstraint(int... integers) {
        try {
            addedConstraints.add(solver.internalSolver.addClause(new VecInt(Arrays.copyOf(integers, integers.length))));
        } catch (ContradictionException e) {
            solver.trivialContradictionFound = true;
        }
    }

    @Override
    public void add(BooleanAssignment clause) {
        addConstraint(clause.get());
        super.add(clause);
    }

    public void add(int... integers) {
        add(new BooleanClause(integers));
    }

    @Override
    public void addAll(Collection<? extends BooleanAssignment> clauses) {
        final ArrayList<IConstr> constraints = new ArrayList<>();
        for (final BooleanAssignment clause : clauses) {
            try {
                constraints.add(
                        solver.internalSolver.addClause(new VecInt(Arrays.copyOf(clause.get(), clause.size()))));
            } catch (ContradictionException e) {
                for (final IConstr constraint : constraints) {
                    solver.internalSolver.removeConstr(constraint);
                }
                solver.trivialContradictionFound = true;
            }
        }
        addedConstraints.addAll(constraints);
        super.addAll(clauses);
    }

    @Override
    public Result<BooleanAssignment> remove() {
        if (addedConstraints.size() > 0) {
            final IConstr lastConstraint = addedConstraints.pop();
            solver.internalSolver.removeConstr(lastConstraint);
        }
        return super.remove();
    }

    @Override
    public void clear() {
        while (addedConstraints.size() > 0) remove();
        super.clear();
    }
}
