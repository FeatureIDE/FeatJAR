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
import de.featjar.base.data.Result;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.base.shell.AbortException;
import de.featjar.base.shell.Shell;
import de.featjar.base.shell.ShellSession;
import de.featjar.base.shell.ShellSession.StoredElement;
import de.featjar.base.shell.VariableTypes;
import de.featjar.base.shell.type.IVariableType;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The abstract class for any shell command that loads formats into the shell.
 *
 * @author Niclas Kleinert
 * @author Sebastian Krieter
 */
public class LoadShellCommand implements IShellCommand {

    public void execute(ShellSession session, LinkedList<String> cmdParams) {
        try {
            IVariableType<?> loadType = getLoadType(cmdParams);
            String path = getPath(cmdParams);
            String name = getVariableName(loadType, session, cmdParams);
            load(session, loadType, path, name);
        } catch (AbortException e) {
        }
    }

    private <T> void load(ShellSession session, IVariableType<T> loadType, String path, String name) {
        Result<?> load = loadType.load(Paths.get(path));
        if (load.isPresent()) {
            if (session.put(name, load.get(), loadType)) {
                FeatJAR.log().info("Loaded %s", name);
            }
        } else {
            FeatJAR.log().error("Object could not be loaded");
            FeatJAR.log().problems(load);
        }
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("load");
    }

    @Override
    public Optional<String> getDescription() {
        List<IVariableType<?>> loadTypes = VariableTypes.getInstance().getExtensions();
        return Optional.of(
                "load one of " + loadTypes.stream().map(t -> t.getName()).collect(Collectors.joining(", ")));
    }

    private IVariableType<?> getLoadType(LinkedList<String> cmdParams) throws AbortException {
        List<IVariableType<?>> loadTypes = VariableTypes.getInstance().getExtensions();

        if (!cmdParams.isEmpty()) {
            String input = cmdParams.removeFirst().toLowerCase();
            Optional<IVariableType<?>> match = loadTypes.stream()
                    .filter(t -> Objects.equals(input, t.getName().toLowerCase()))
                    .findFirst();
            if (match.isPresent()) {
                return match.get();
            }
            FeatJAR.log().warning("No type matched your input.");
        }
        return selectLoadType(loadTypes);
    }

    private IVariableType<?> selectLoadType(List<IVariableType<?>> possibleLoadType) throws AbortException {
        FeatJAR.log().message("Select type to load:");
        for (int i = 0; i < possibleLoadType.size(); i++) {
            FeatJAR.log().message("%d. %s", i, possibleLoadType.get(i).getName());
        }
        while (true) {
            String choice = Shell.readResponse().orElse("");
            if (choice.isBlank()) {
                throw new AbortException();
            }
            try {
                int parsedChoice = Integer.parseInt(choice);
                if (parsedChoice >= 0 && parsedChoice < possibleLoadType.size()) {
                    return possibleLoadType.get(parsedChoice);
                }
            } catch (NumberFormatException e) {
            }
            FeatJAR.log().error("'%s' is no vaild choice", choice);
        }
    }

    private String getPath(LinkedList<String> cmdParams) throws AbortException {
        if (!cmdParams.isEmpty()) {
            return cmdParams.removeFirst();
        } else {
            Shell.message("Enter path:");
            String path = Shell.readText().orElse("");
            if (path.isBlank()) {
                throw new AbortException();
            }
            return path;
        }
    }

    private String getVariableName(IVariableType<?> loadType, ShellSession session, LinkedList<String> cmdParams)
            throws AbortException {
        String defaultName = loadType.getName() + (session.getSize() + 1);

        String name;
        if (!cmdParams.isEmpty()) {
            name = cmdParams.removeFirst();
        } else {
            Shell.message("Choose a name or leave blank for default (%s):", defaultName);
            name = Shell.readText().orElse(defaultName);
        }

        for (Result<StoredElement<?>> value = session.getElement(name);
                value.isPresent();
                value = session.getElement(name)) {
            Shell.message(
                    "This session already contains a variable with that name of type %s.",
                    value.get().getTypeName());
            Shell.message("Overwrite %s? (y)es/(r)ename/(a)bort", name);
            String choice = Shell.readResponse().orElse("").toLowerCase();
            if (Objects.equals("y", choice)) {
                String oldKey = name;
                session.remove(oldKey)
                        .ifPresent(e -> FeatJAR.log().message("Removed old %s", oldKey))
                        .orElseLog(Verbosity.ERROR);
                break;
            } else if (Objects.equals("r", choice)) {
                Shell.message("Choose a name or leave blank for default (%s):", defaultName);
                name = Shell.readText().orElse(defaultName);
            } else if (Objects.equals("a", choice)) {
                throw new AbortException();
            }
        }

        if (name.isBlank()) {
            throw new AbortException();
        }
        return name;
    }
}
