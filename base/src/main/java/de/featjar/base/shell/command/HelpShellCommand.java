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
package de.featjar.base.shell.command;

import de.featjar.base.FeatJAR;
import de.featjar.base.shell.ShellCommands;
import de.featjar.base.shell.ShellSession;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Prints basic usage informations of the shell and all available shell commands.
 *
 * @author Niclas Kleinert
 */
public class HelpShellCommand implements IShellCommand {

    @Override
    public void execute(ShellSession session, LinkedList<String> cmdParams) {
        printBasicUsage();
        FeatJAR.log().message("");
        printAllCommands();
    }

    /**
     * Prints all {@link IShellCommand} that are registered at {@link ShellCommands}
     */
    public void printAllCommands() {
        FeatJAR.log().message("Supported commands:");
        ShellCommands.getInstance().getExtensions().stream()
                .map(IShellCommand::getCommandDescriptionString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .forEach(FeatJAR.log()::message);
    }

    /**
     * Prints basic usage informations of the shell.
     */
    private void printBasicUsage() {
        FeatJAR.log().message("Capitalization of COMMANDS is NOT taken into account");
        FeatJAR.log().message("You can cancel ANY command by pressing the (ESC) key");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("help");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("print all commads");
    }
}
