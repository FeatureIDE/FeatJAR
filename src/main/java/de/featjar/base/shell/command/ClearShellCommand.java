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

import de.featjar.base.FeatJAR;
import de.featjar.base.shell.AbortException;
import de.featjar.base.shell.Shell;
import de.featjar.base.shell.ShellSession;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Deletes all variables from the entire shell session.
 *
 * @author Niclas Kleinert
 */
public class ClearShellCommand implements IShellCommand {

    @Override
    public void execute(ShellSession session, LinkedList<String> cmdParams) throws AbortException {
        Shell.message("Clearing entire session. Proceed? (y)es/(n)o");
        String choice = Shell.readResponse().orElse("").toLowerCase().trim();

        switch (choice) {
            case "y":
                session.clear();
                FeatJAR.log().info("Clearing successful");
                break;
            case "n":
                FeatJAR.log().info("Clearing aborted");
                break;
            default:
                FeatJAR.log().error("Invalid input");
                FeatJAR.log().info("Clearing aborted");
                break;
        }
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("clear");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("delete the entire session");
    }
}
