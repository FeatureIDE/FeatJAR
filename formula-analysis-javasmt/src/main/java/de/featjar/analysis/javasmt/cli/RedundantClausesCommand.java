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
package de.featjar.analysis.javasmt.cli;

import de.featjar.analysis.javasmt.computation.ComputeJavaSMTFormula;
import de.featjar.analysis.javasmt.computation.ComputeRedundantClauses;
import de.featjar.analysis.javasmt.computation.ComputeRedundantClausesIncrementally;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.io.textual.ExpressionListStringFormat;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import java.util.List;
import java.util.Optional;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

public class RedundantClausesCommand extends AJavasmtAnalysisCommand<List<IExpression>> {

    public static final Option<Boolean> REMOVE = Option.newFlag("remove")
            .setDescription("Finds redundant clauses by iteratively removing clauses from the formula.");

    @Override
    public Optional<String> getDescription() {
        return Optional.of(
                "Computes redundant clauses either by iteratively adding clauses to the formula or by removing them.");
    }

    @Override
    public IComputation<List<IExpression>> newAnalysis(
            OptionList optionParser, IComputation<? extends IFormula> formula) {
        Boolean remove = optionParser.get(REMOVE);
        if (remove) {
            return formula.map(ComputeJavaSMTFormula::new)
                    .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                    .map(ComputeRedundantClauses::new);
        } else {
            return formula.map(ComputeJavaSMTFormula::new)
                    .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                    .map(ComputeRedundantClausesIncrementally::new);
        }
    }

    @Override
    protected IFormat<List<IExpression>> getOuputFormat(OptionList optionParser) {
        return new ExpressionListStringFormat();
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("redundant-clauses-javasmt");
    }
}
