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

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import de.featjar.formula.io.csv.BooleanAssignmentGroupsCSVFormat;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reads and writes {@link BooleanAssignmentGroups a set of assignments} in different formats.
 *
 * @author Knut & Kilian
 * @author Sebastian Krieter
 */
public class ConvertAssignmentFormatCommand extends ACommand {

    public static final Option<String> INPUT_FORMAT = Option.newStringEnumOption(
                    "input-format",
                    BooleanAssignmentGroupsFormats.getInstance().getExtensions().stream()
                            .filter(IFormat::supportsParse)
                            .map(IFormat::getName)
                            .collect(Collectors.toList()))
            .setDescription("Format of the input. If not specified, tries to auto detect.");

    public static final Option<String> OUTPUT_FORMAT = Option.newStringEnumOption(
                    "output-format",
                    BooleanAssignmentGroupsFormats.getInstance().getExtensions().stream()
                            .filter(IFormat::supportsWrite)
                            .map(IFormat::getName)
                            .collect(Collectors.toList()))
            .setDefaultValue(new BooleanAssignmentGroupsCSVFormat().getName())
            .setDescription("Format of the output");

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
        Result<IFormat<BooleanAssignmentGroups>> userInputFormat = optionParser
                .getResult(INPUT_FORMAT)
                .mapOptional(BooleanAssignmentGroupsFormats.getInstance()::getFormatByName);
        Result<BooleanAssignmentGroups> parseResult = userInputFormat.isPresent()
                ? readFromInput(optionParser, userInputFormat.get())
                : readFromInput(optionParser, BooleanAssignmentGroupsFormats.getInstance());
        if (parseResult.isEmpty()) {
            FeatJAR.log().problems(parseResult, Verbosity.ERROR);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
        try {
            writeToOutput(
                    parseResult.get(),
                    BooleanAssignmentGroupsFormats.getInstance()
                            .getFormatByName(optionParser.get(OUTPUT_FORMAT))
                            .orElseThrow(),
                    optionParser);
            return 0;
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_WRITING_RESULT;
        }
    }
}
