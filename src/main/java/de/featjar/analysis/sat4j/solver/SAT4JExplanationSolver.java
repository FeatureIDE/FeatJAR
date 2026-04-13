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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.xplain.Xplain;

/**
 * Implements a MUS {@link de.featjar.analysis.ISolver} using Sat4J.
 *
 * <br>
 * <br>
 * Sat4J only support the extraction of one minimal unsatisfiable subset, thus
 * {@link #getAllMinimalUnsatisfiableSubsets()} only returns one solution.
 *
 * <br>
 * <br>
 * Note: The usage of a solver to solve expression and to find minimal
 * unsatisfiable subset should be divided into two task because the native
 * solver for the MUS extractor are substantially slower in solving
 * satisfiability requests. If for solving the usage of the {@link SAT4JSolutionSolver}
 * is recommended.
 *
 * @author Joshua Sprey
 * @author Sebastian Krieter
 */
public class SAT4JExplanationSolver extends SAT4JSolver {
    public SAT4JExplanationSolver(BooleanAssignmentList clauseList) {
        super(clauseList, false);
    }

    @Override
    protected Xplain<ISolver> newInternalSolver() {
        return new Xplain<>(SolverFactory.newDefault());
    }

    public Result<List<BooleanAssignment>> getMinimalUnsatisfiableSubset() {
        if (hasSolution().equals(Result.of(true))) {
            return Result.empty(new IllegalStateException("Problem is satisfiable"));
        }
        try {
            return Result.of(IntStream.of(((Xplain<?>) internalSolver).minimalExplanation()) //
                    .mapToObj(index -> getClauseList().get(index)) //
                    .collect(Collectors.toList()));
        } catch (final TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    public Result<List<List<BooleanAssignment>>> getAllMinimalUnsatisfiableSubsets() {
        return Result.of(
                Collections.singletonList(getMinimalUnsatisfiableSubset().get()));
    }
}
