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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PrintCommandsCommand extends ACommand {

    @Override
    public int run(OptionParser optionParser) {
        List<ICommand> commands = FeatJAR.extensionPoint(Commands.class).getExtensions();
        if (commands.isEmpty()) {
            FeatJAR.log()
                    .plainMessage(String.format(
                            "No commands are available. You can register commands in an extensions.xml file when building %s.",
                            FeatJAR.LIBRARY_NAME));
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("The following commands are available:\n");
            ArrayList<ICommand> commandList = new ArrayList<>(commands);
            Collections.sort(
                    commandList, Comparator.comparing(c -> c.getShortName().orElse("") + c.getIdentifier()));
            for (final ICommand c : commandList) {
                sb.append(String.format(
                        "\t%s: %s\n", //
                        c.getShortName().orElse(c.getIdentifier()), //
                        c.getDescription().orElse("")));
                sb.append(String.format("\t\t(Classpath: %s)\n", c.getIdentifier()));
            }
            FeatJAR.log().plainMessage(sb.toString());
        }
        return 0;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints all available commands.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("commands");
    }
}
