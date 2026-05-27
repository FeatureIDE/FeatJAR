/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt.computation;

import de.featjar.analysis.javasmt.solver.FormulaToJavaSMT;
import de.featjar.analysis.javasmt.solver.JavaSMTFormula;
import de.featjar.analysis.javasmt.solver.JavaSMTSolver;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.ValueAssignment;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import java.util.ArrayList;
import java.util.List;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;

/**
 * Finds redundant clauses with respect to a given formula. This
 * analysis works by iteratively removing clauses from the formula.
 * If a clause is implied by the current formula, it is redundant and
 * added to the result.
 *
 * @author Sebastian Krieter
 * @author Klara Surmeier
 */
public class ComputeRedundantClauses extends AJavaSMTAnalysis<List<IExpression>> {
    public ComputeRedundantClauses(IComputation<? extends JavaSMTFormula> formula) {
        super(formula);
    }

    protected ComputeRedundantClauses(ComputeRedundantClausesIncrementally other) {
        super(other);
    }

    @Override
    public Result<List<IExpression>> compute(List<Object> dependencyList, Progress progress) {
        Result<JavaSMTSolver> solverResult = getCompatibleSolver(
                dependencyList, Solvers.Z3, Solvers.SMTINTERPOL, Solvers.PRINCESS, Solvers.MATHSAT5);
        if (solverResult.isEmpty()) {
            return solverResult.nullify();
        }
        JavaSMTSolver solver = solverResult.get();

        List<IExpression> redundantClauses = new ArrayList<>();

        // access originalFormula
        IExpression originalFormula = FORMULA.get(dependencyList).getOriginalFormula();
        FormulaToJavaSMT translator = FORMULA.get(dependencyList).getTranslator();
        // check for structure: Reference and And with children
        if (originalFormula instanceof Reference) {
            IExpression expression = originalFormula.getChildren().get(0);
            if (expression instanceof And) {
                // get children clauses of And
                List<? extends IExpression> clausesToTest = expression.getChildren();

                // transform them to SMT clauses
                List<BooleanFormula> SMTClausesToTest = new ArrayList<>();
                for (IExpression clause : clausesToTest) {
                    BooleanFormula SMTClause = translator.nodeToFormula(clause);
                    SMTClausesToTest.add(SMTClause);
                }

                // iterate through size of children clauses list
                for (int i = 0; i < SMTClausesToTest.size(); i++) {
                    // negate the clause at the current index
                    BooleanFormula currentClause = SMTClausesToTest.get(i);
                    BooleanFormula currentNegatedClause = translator.createNot(currentClause);
                    SMTClausesToTest.set(i, currentNegatedClause);

                    // set the list of clauses to the new solver formula and solve
                    BooleanFormula currentFormula = translator.createAnd(SMTClausesToTest);
                    solver.setFormula(currentFormula);
                    Result<ValueAssignment> findSolution = solver.findSolution();
                    if (findSolution.isPresent()) {
                        // restore SMTClausesToTest
                        SMTClausesToTest.set(i, currentClause);
                    } else {
                        // restore SMTClausesToTest and add clause to redundantClauses
                        SMTClausesToTest.set(i, currentClause);
                        redundantClauses.add(clausesToTest.get(i));
                    }
                }
            } else {
                return Result.empty(new UnsupportedOperationException("Formula's structure is not supported."));
            }
        } else {
            return Result.empty(
                    new UnsupportedOperationException("Formula's structure is not supported. Reference is missing."));
        }

        return Result.ofNullable(redundantClauses);
    }
}
