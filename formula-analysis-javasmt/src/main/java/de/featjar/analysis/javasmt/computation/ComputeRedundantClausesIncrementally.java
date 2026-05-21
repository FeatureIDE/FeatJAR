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
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Reference;
import java.util.ArrayList;
import java.util.List;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;

/**
 * Finds redundant clauses with respect to a given formula. This
 * analysis works by iteratively adding each clause to a solver. If a clause
 * is implied by the current formula, it is marked as redundant and is removed
 * from it. Otherwise it is kept as part of the formula for the
 * remaining analysis. Clauses are added in the same order a they appear in the
 * given clauses list.
 *
 * @author Sebastian Krieter
 * @author Klara Surmeier
 */
public class ComputeRedundantClausesIncrementally extends AJavaSMTAnalysis<List<IExpression>> {

    public ComputeRedundantClausesIncrementally(IComputation<? extends JavaSMTFormula> formula) {
        super(formula);
    }

    protected ComputeRedundantClausesIncrementally(ComputeRedundantClausesIncrementally other) {
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
                List<? extends IExpression> clausesToTest = expression.getChildren();

                // set formula of the solver to True
                BooleanFormulaManager currentBooleanFormulaManager = FORMULA.get(dependencyList)
                        .getContext()
                        .getFormulaManager()
                        .getBooleanFormulaManager();
                BooleanFormula initialClause = currentBooleanFormulaManager.makeTrue();
                solver.setFormula(initialClause);

                // create a list to hold the clauses of the formula, initially containing True
                List<BooleanFormula> clausesInFormula = new ArrayList<>();
                clausesInFormula.add(initialClause);

                // for each clause:
                for (IExpression clause : clausesToTest) {
                    // 1. negate clause
                    Not negatedClause = new Not((IFormula) clause);
                    BooleanFormula negatedSMTClause = translator.nodeToFormula(negatedClause);

                    // 2. save current formula
                    // BooleanFormula previousFormula = solver.getFormula();

                    // 3. add negated clause to formula
                    // add negated SMTClause to list and transform clauses in list to and, but rename the list!
                    // List<BooleanFormula> formulaAndNegatedClause = Arrays.asList(previousFormula, negatedSMTClause);
                    clausesInFormula.add(negatedSMTClause);
                    BooleanFormula currentFormula = translator.createAnd(clausesInFormula);
                    solver.setFormula(currentFormula);

                    // 4. solve:
                    Result<ValueAssignment> findSolution = solver.findSolution();
                    // true: add unnegated clause to current formula and set the combination of both as the new formula
                    if (findSolution.isPresent()) {
                        // add clause to the original list and create an And
                        clausesInFormula.remove(clausesInFormula.size() - 1);
                        BooleanFormula SMTClause = translator.nodeToFormula(clause);
                        clausesInFormula.add(SMTClause);
                        BooleanFormula newFormula = translator.createAnd(clausesInFormula);
                        solver.setFormula(newFormula);
                        // false: add clause to return list and set the originalFormula as the new formula
                    } else {
                        // set formula to originalList
                        clausesInFormula.remove(clausesInFormula.size() - 1);
                        redundantClauses.add(clause);
                        BooleanFormula newFormula = translator.createAnd(clausesInFormula);
                        solver.setFormula(newFormula);
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
