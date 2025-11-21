/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ddnnife.
 *
 * formula-analysis-ddnnife is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ddnnife is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ddnnife. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/formula-analysis-ddnnife> for further information.
 */
package de.featjar.analysis.ddnnife.cli;

import de.featjar.analysis.AAnalysisCommand;
import de.featjar.analysis.ddnnife.computation.ComputeDdnnifeWrapper;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.IO;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.conversion.ComputeBooleanRepresentation;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.IFormula;

public abstract class ADdnnifeAnalysisCommand<T> extends AAnalysisCommand<T> {

    /**
     * Option for setting the seed for the pseudo random generator.
     */
    public static final Option<Long> RANDOM_SEED_OPTION = Option.newOption("seed", Option.LongParser) //
            .setDescription("Seed for the pseudo random generator") //
            .setDefaultValue(1L);

    protected IFormula inputFormula;
    protected VariableMap variableMap;

    @Override
    protected IComputation<T> newComputation(OptionList optionParser) {
        inputFormula = optionParser
                .getResult(INPUT_OPTION)
                .flatMap(p -> IO.load(p, FormulaFormats.getInstance()))
                .orElseThrow();
        return newAnalysis(
                optionParser,
                Computations.of(inputFormula)
                        .map(ComputeNNFFormula::new)
                        .map(ComputeCNFFormula::new)
                        .map(ComputeBooleanRepresentation::new)
                        .peekResult(getClass(), "variableMap", clauseList -> variableMap = clauseList.getVariableMap())
                        .map(ComputeDdnnifeWrapper::new));
    }

    protected abstract IComputation<T> newAnalysis(OptionList optionParser, ComputeDdnnifeWrapper formula);
}
