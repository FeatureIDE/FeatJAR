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

import de.featjar.base.extension.IExtension;
import de.featjar.base.shell.AbortException;
import de.featjar.base.shell.ShellCommands;
import de.featjar.base.shell.ShellSession;
import java.util.LinkedList;
import java.util.Optional;

/**
 * A shell command run within a {@link ShellCommands}
 *
 * @author Niclas Kleinert
 */
public interface IShellCommand extends IExtension {

    static Optional<String> getCommandDescriptionString(IShellCommand c) {
        Optional<String> shortName = c.getShortName();
        if (shortName.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> description = c.getDescription();
        if (description.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.format("\t%-10s - %s", shortName.get(), description.get()));
    }

    /**
     * Executes the shell command.
     *
     * @param session the storage location of all variables
     * @param cmdParams all arguments except the shell command
     */
    void execute(ShellSession session, LinkedList<String> cmdParams) throws AbortException;

    /**
     * {@return this command's short name, if any} The short name can be used to call this command from the CLI.
     */
    default Optional<String> getShortName() {
        return Optional.empty();
    }

    /**
     * {@return this command's description name, if any}
     */
    default Optional<String> getDescription() {
        return Optional.empty();
    }
}
