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
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import de.featjar.formula.io.csv.BooleanAssignmentGroupsCSVFormat;
import java.util.Optional;

/**
 * Reads and writes {@link BooleanAssignmentGroups a set of assignments} in different formats.
 *
 * @author Knut & Kilian
 * @author Sebastian Krieter
 */
public class ConvertAssignmentFormatCommand extends ACommand {

    public static final Option<IFormatSupplier<BooleanAssignmentGroups>> INPUT_FORMAT =
            Options.newInputFormatOption(BooleanAssignmentGroupsFormats.class);

    public static final Option<IFormat<BooleanAssignmentGroups>> OUTPUT_FORMAT = Options.newOutputFormatOption(
            BooleanAssignmentGroupsFormats.class, new BooleanAssignmentGroupsCSVFormat().getName());

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Convert configuration groups file into another format.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("convert-configuration");
    }

    @Override
    public int run(OptionList optionParser) {
        return writeResult(
                optionParser,
                readFromInput(optionParser, optionParser.getResult(INPUT_FORMAT).get()),
                optionParser.get(OUTPUT_FORMAT));
    }
}
