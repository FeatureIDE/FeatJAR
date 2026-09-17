/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.cli;

import de.featjar.base.FeatJAR;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PrintHelpCommand extends ACommand {

    @Override
    public int run(OptionList optionParser) {
        List<ICommand> commands = FeatJAR.extensionPoint(Commands.class).getExtensions();
        if (commands.isEmpty()) {
            FeatJAR.log()
                    .plainMessage(String.format(
                            "No commands are available. You can register commands in an extensions.xml file when building %s.",
                            FeatJAR.LIBRARY_NAME));
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                            "Usage: java -jar %s [<command> | --command <classpath>] [--<flag> | --<option> <value>]...\n\n",
                            FeatJAR.LIBRARY_NAME))
                    .append("General options:\n");
            printOptions(sb, Options.getAllOptions(FeatJAROptions.class));
            printOptions(sb, Options.getAllOptions(LogOptions.class));
            FeatJAR.log().plainMessage(sb.toString());
        }
        return 0;
    }

    private void printOptions(StringBuilder sb, List<Option<?>> options) {
        for (Option<?> option : options) {
            sb.append(String.format(
                    "\t%s %s\n", //
                    option.getArgumentName(), //
                    option.getArgumentPlaceHolder()));
            option.getDescription()
                    .ifPresent(
                            description -> sb.append("\t\t").append(description).append("\n"));
            option.getPossibleArguments().ifPresent(possibleArguments -> sb.append(
                            "\t\tpossible: " + possibleArguments.stream().collect(Collectors.joining("|")))
                    .append("\n"));
            option.getDefaultArgument().ifPresent(defaultValue -> sb.append("\t\tdefault:  " + defaultValue)
                    .append("\n"));
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints usage information and general options.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("help");
    }
}
