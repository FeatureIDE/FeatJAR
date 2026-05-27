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
import de.featjar.analysis.javasmt.computation.ComputeMaximalVariableRange;
import de.featjar.analysis.javasmt.computation.ComputeMinimalVariableRange;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.term.value.Variable;
import java.util.Map;
import java.util.Optional;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

public class VariableRangesCommand extends AJavasmtAnalysisCommand<Map<Variable, Object>> {

    public static final Option<Boolean> MIN =
            Option.newFlag("min").setDescription("Finds the minimal value for each numerical variable in a Term.");

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes the minimal and maximal values for each numerical variable in a Term.");
    }

    @Override
    public IComputation<Map<Variable, Object>> newAnalysis(
            OptionList optionParser, IComputation<? extends IFormula> formula) {
        Boolean min = optionParser.get(MIN);
        if (min) {
            return formula.map(ComputeJavaSMTFormula::new)
                    .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                    .map(ComputeMinimalVariableRange::new);
        } else {
            return formula.map(ComputeJavaSMTFormula::new)
                    .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                    .map(ComputeMaximalVariableRange::new);
        }
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("variable-ranges-javasmt");
    }
}
