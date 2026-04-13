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
package de.featjar.analysis.javasmt.solver;

import de.featjar.base.data.Problem;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.predicate.ProblemFormula;
import java.util.List;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.QuantifiedFormulaManager;
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaVisitor;

public abstract class FormulaVisitor implements BooleanFormulaVisitor<IExpression> {
    protected final BooleanFormulaManager booleanFormulaManager;

    public FormulaVisitor(BooleanFormulaManager booleanFormulaManager) {
        this.booleanFormulaManager = booleanFormulaManager;
    }

    @Override
    public IExpression visitConstant(boolean value) {
        return new ProblemFormula(new Problem("unexpected constant"));
    }

    @Override
    public IExpression visitBoundVar(BooleanFormula var, int deBruijnIdx) {
        return new ProblemFormula(new Problem("unexpected bound var"));
    }

    @Override
    public IExpression visitNot(BooleanFormula operand) {
        return new ProblemFormula(new Problem("unexpected not"));
    }

    @Override
    public IExpression visitAnd(List<BooleanFormula> operands) {
        return new ProblemFormula(new Problem("unexpected and"));
    }

    @Override
    public IExpression visitOr(List<BooleanFormula> operands) {
        return new ProblemFormula(new Problem("unexpected or"));
    }

    @Override
    public IExpression visitXor(BooleanFormula operand1, BooleanFormula operand2) {
        return new ProblemFormula(new Problem("unexpected xor"));
    }

    @Override
    public IExpression visitEquivalence(BooleanFormula operand1, BooleanFormula operand2) {
        return new ProblemFormula(new Problem("unexpected equivalence"));
    }

    @Override
    public IExpression visitImplication(BooleanFormula operand1, BooleanFormula operand2) {
        return new ProblemFormula(new Problem("unexpected implication"));
    }

    @Override
    public IExpression visitIfThenElse(
            BooleanFormula condition, BooleanFormula thenFormula, BooleanFormula elseFormula) {
        return new ProblemFormula(new Problem("unexpected if-then-else"));
    }

    @Override
    public IExpression visitQuantifier(
            QuantifiedFormulaManager.Quantifier quantifier,
            BooleanFormula quantifiedAST,
            List<org.sosy_lab.java_smt.api.Formula> boundVars,
            BooleanFormula body) {
        return new ProblemFormula(new Problem("unexpected quantifier"));
    }

    @Override
    public IExpression visitAtom(BooleanFormula atom, FunctionDeclaration<BooleanFormula> funcDecl) {
        return new ProblemFormula(new Problem("unexpected atom"));
    }
}
