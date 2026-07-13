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
package de.featjar.analysis.sat4j.cli;

import de.featjar.analysis.sat4j.computation.ComputeCoreSAT4J;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.IFormatSupplier;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.BooleanAssignmentListToVariableMap;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.assignment.conversion.IVariableNameFilter;
import de.featjar.formula.assignment.conversion.VariableMapToVariables;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.VariableMapFormats;
import de.featjar.formula.io.dimacs.BooleanAssignmentListDimacsFormat;
import de.featjar.formula.structure.IFormula;
import java.util.Optional;

/**
 * Computes core and dead variables for a given formula using SAT4J.
 *
 * @author Sebastian Krieter
 * @author Andreas Gerasimow
 */
public class CoreCommand extends ASAT4JAnalysisCommand<BooleanAssignmentList> {

    public static final Option<IFormatSupplier<IFormula>> INPUT_FORMAT =
            Options.newInputFormatOption(FormulaFormats.class);

    public static final Option<IFormat<BooleanAssignmentList>> OUTPUT_FORMAT = Options.newOutputFormatOption(
            BooleanAssignmentListFormats.class, new BooleanAssignmentListDimacsFormat().getName());

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Computes core and dead variables for a given formula using SAT4J.");
    }

    @Override
    protected IComputation<BooleanAssignmentList> newComputation(OptionList optionParser) {
        final ComputeBooleanClauseList clauseList = createCNFComputation(optionParser);

        final ComputeCoreSAT4J core = clauseList.map(ComputeCoreSAT4J::new);
        VariableMap ignoreFile = optionParser
                .getResult(IGNORE_VARIABLES)
                .mapResult(p -> IO.load(p, VariableMapFormats.getInstance()))
                .orElseLog(Verbosity.WARNING);
        if (ignoreFile != null) {
            core.set(
                    ComputeCoreSAT4J.AUXILLIARY_VARIABLES,
                    clauseList
                            .map(BooleanAssignmentListToVariableMap::new)
                            .map(VariableMapToVariables::new)
                            .set(
                                    VariableMapToVariables.INCLUDE,
                                    IVariableNameFilter.list(ignoreFile.getVariableNames())));
        }
        return core;
    }

    @Override
    protected IFormat<BooleanAssignmentList> getOuputFormat(OptionList optionParser) {
        return optionParser.get(OUTPUT_FORMAT);
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("core-sat4j");
    }
}
