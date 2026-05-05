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
import de.featjar.base.extension.AExtensionPoint;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.AFormats;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.base.shell.AbortException;
import de.featjar.base.shell.Shell;
import de.featjar.base.shell.ShellSession;
import de.featjar.base.shell.ShellSession.StoredElement;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stores loaded session variables in the current working directory. Offers
 * different types for storage depending on the format of the variable.
 *
 * @author Niclas Kleinert
 */
public class StoreShellCommand implements IShellCommand {

    public void execute(ShellSession session, LinkedList<String> cmdParams) {
        try {
            StoredElement<?> element = getVariable(session, cmdParams);
            Path path = getPath(cmdParams);
            IFormat<?> format = getFormat(element, cmdParams);
            store(element, format, path);
        } catch (Exception e) {
            FeatJAR.log().error(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void store(StoredElement<?> element, IFormat<T> format, Path path) throws IOException {
        IO.save((T) element.getElement(), path, format);
    }

    private Path getPath(List<String> cmdParams) throws AbortException {
        if (cmdParams.size() > 1) {
            String pathName = cmdParams.get(1);
            if (pathName != null) {
                return getPath(pathName);
            }
        }
        while (true) {
            Shell.message("Enter file path:");
            String pathName = Shell.readText().orElse("");
            if (pathName.isBlank()) {
                throw new AbortException();
            }
            return getPath(pathName);
        }
    }

    private Path getPath(String pathName) throws AbortException {
        try {
            return Path.of(pathName);
        } catch (InvalidPathException e) {
            throw new AbortException(e);
        }
    }

    private StoredElement<?> getVariable(ShellSession session, LinkedList<String> cmdParams) throws AbortException {
        if (!cmdParams.isEmpty()) {
            StoredElement<?> sessionVariable =
                    session.getElement(cmdParams.removeFirst()).orElseLog(Verbosity.ERROR);
            if (sessionVariable != null) {
                return sessionVariable;
            }
        }
        while (true) {
            Shell.message("Enter variable name:");
            String variableName = Shell.readText().orElse("");
            if (variableName.isBlank()) {
                throw new AbortException();
            }
            StoredElement<?> sessionVariable = session.getElement(variableName).orElseLog(Verbosity.ERROR);
            if (sessionVariable != null) {
                return sessionVariable;
            }
        }
    }

    private IFormat<?> getFormat(StoredElement<?> element, LinkedList<String> cmdParams) throws AbortException {
        List<IFormat<?>> possibleFormats = getFormats(element.getClassType());

        if (!cmdParams.isEmpty()) {
            String input = cmdParams.removeFirst().toLowerCase();
            List<IFormat<?>> formats = possibleFormats.stream()
                    .filter(f -> f.getName().toLowerCase().startsWith(input))
                    .collect(Collectors.toList());

            if (formats.isEmpty()) {
                FeatJAR.log().warning("No format matched the name '%s'!: Choose your format", input);
            } else {
                if (formats.get(0).getName().toLowerCase().matches(input)) {
                    return formats.get(0);
                }
                Shell.message("Do you mean: %s? (Y)es/(n)o", formats.get(0).getName());
                String choice = Shell.readResponse().orElse("");
                if (choice.isEmpty()) {
                    throw new AbortException();
                } else {
                    return formats.get(0);
                }
            }
        }
        return selectFormat(possibleFormats);
    }

    private List<IFormat<?>> getFormats(Class<?> variableType) throws AbortException {
        Optional<AExtensionPoint<?>> format = FeatJAR.getInstance().getExtensionManager().getExtensionPoints().stream()
                .filter(p -> p instanceof AFormats)
                .filter(p -> variableType.isAssignableFrom(((AFormats<?>) p).getType()))
                .findFirst();
        if (format.isEmpty()) {
            throw new AbortException("Could not find a suitable output format");
        }
        return ((AFormats<?>) format.get())
                .getExtensions().stream().filter(IFormat::supportsWrite).collect(Collectors.toList());
    }

    private IFormat<?> selectFormat(List<IFormat<?>> possibleFormats) throws AbortException {
        FeatJAR.log().message("Select the format for the variable to store:\n");
        for (int i = 0; i < possibleFormats.size(); i++) {
            FeatJAR.log().message("%d. %s", i, possibleFormats.get(i).getName());
        }
        while (true) {
            String choice = Shell.readResponse().orElse("");
            if (choice.isBlank()) {
                throw new AbortException();
            }
            try {
                int parsedChoice = Integer.parseInt(choice);
                if (parsedChoice >= 0 && parsedChoice < possibleFormats.size()) {
                    return possibleFormats.get(parsedChoice);
                }
            } catch (NumberFormatException e) {
            }
            FeatJAR.log().error("'%s' is no vaild choice", choice);
        }
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("store");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("save seesion variables on your cuurent working directory");
    }
}
