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
import de.featjar.base.log.Log.Verbosity;
import de.featjar.base.shell.AbortException;
import de.featjar.base.shell.Shell;
import de.featjar.base.shell.ShellSession;
import de.featjar.base.tree.structure.ITree;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Prints the content of given shell session variables.
 *
 * @author Niclas Kleinert
 */
public class PrintShellCommand implements IShellCommand {

    @Override
    public void execute(ShellSession session, LinkedList<String> cmdParams) throws AbortException {
        if (cmdParams.isEmpty()) {
            session.printAll();
            Shell.message("Enter variable name(s):");
            cmdParams = Shell.readText()
                    .map(c -> Arrays.stream(c.split("\\s+")).collect(Collectors.toCollection(LinkedList::new)))
                    .orElseGet(LinkedList::new);
        }

        cmdParams.stream().filter(name -> !name.isBlank()).forEach(name -> {
            session.getValue(name.toLowerCase())
                    .ifPresent(m -> {
                        FeatJAR.log().message(name + ":");
                        printMap(m);
                    })
                    .orElseLog(Verbosity.ERROR);
        });
    }

    private void printMap(Object v) {
        if (v instanceof ITree<?>) {
            FeatJAR.log().message(((ITree<?>) v).print());
        } else {
            FeatJAR.log().message(v);
        }
        FeatJAR.log().message("");
    }

    public Optional<String> getShortName() {
        return Optional.of("print");
    }

    public Optional<String> getDescription() {
        return Optional.of("print the content of variables");
    }
}
