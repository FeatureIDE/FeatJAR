/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.cli;

import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.IFormatSupplier;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.IFormula;
import java.util.Optional;

/**
 * Reads and writes {@link BooleanAssignmentGroups a set of assignments} in different formats.
 *
 * @author Knut Köhnlein
 * @author Kilian Hüppe
 * @author Andreas Gerasimow
 * @author Sebastian Krieter
 */
public class ConvertFormulaFormatCommand extends ACommand {

    public static final Option<IFormatSupplier<IFormula>> INPUT_FORMAT =
            Options.newInputFormatOption(FormulaFormats.class);

    public static final Option<IFormat<IFormula>> OUTPUT_FORMAT =
            Options.newOutputFormatOption(FormulaFormats.class, null);

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Convert formula file into another format.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("convert-formula");
    }

    @Override
    public int run(OptionList optionParser) {
        return writeResult(
                optionParser,
                readFromInput(optionParser, optionParser.get(INPUT_FORMAT)),
                optionParser.get(OUTPUT_FORMAT));
    }
}
