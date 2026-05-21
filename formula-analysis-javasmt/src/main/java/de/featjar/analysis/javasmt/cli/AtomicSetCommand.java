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

import de.featjar.analysis.javasmt.computation.ComputeAtomicSet;
import de.featjar.analysis.javasmt.computation.ComputeJavaSMTFormula;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;
import java.util.Optional;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

public class AtomicSetCommand extends AJavasmtAnalysisCommand<List<List<Variable>>> {

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes atomic sets.");
    }

    @Override
    public IComputation<List<List<Variable>>> newAnalysis(
            OptionList optionParser, IComputation<? extends IFormula> formula) {
        return formula.map(ComputeJavaSMTFormula::new)
                .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                .map(ComputeAtomicSet::new);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("atomic-sets-javasmt");
    }
}
