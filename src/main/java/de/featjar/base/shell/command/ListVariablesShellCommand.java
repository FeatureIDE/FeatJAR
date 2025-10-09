/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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

import de.featjar.base.shell.ShellSession;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Deletes a given list of variables from the shell session.
 *
 * @author Niclas Kleinert
 */
public class ListVariablesShellCommand implements IShellCommand {

    @Override
    public void execute(ShellSession session, LinkedList<String> cmdParams) {
        session.printAll();
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("list");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("list session variables");
    }
}
