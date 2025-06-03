/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.AFormats;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.base.shell.IShellCommand;
import de.featjar.base.shell.Shell;
import de.featjar.base.shell.ShellSession;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.structure.IFormula;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The abstract class for any shell command that loads formats into the shell.
 *
 * @author Niclas Kleinert
 */
public abstract class ALoadShellCommand implements IShellCommand {

    /**
     * {@return a format name that is used for prompts}
     */
    protected abstract Optional<String> getFormatName();

    /**
     * {@return the default format name that is used for the auto naming}
     */
    protected abstract Optional<String> getDefaultName();

    protected String setPath(List<String> cmdParams) {
        return cmdParams.size() > 1
                ? cmdParams.get(1)
                : Shell.readCommand("\nEnter a path to load a "
                                + getFormatName().orElse("") + " or leave blank to abort:\n")
                        .orElse("");
    }

    protected String setVariableName(ShellSession session, List<String> cmdParams) {
        return cmdParams.size() > 0
                ? cmdParams.get(0)
                : Shell.readCommand("\nChoose a name for your "
                                + getFormatName().orElse("")
                                + " or enter for default ("
                                + getDefaultName().orElse("") + (session.getSize() + 1)
                                + "):\n")
                        .orElse(getDefaultName().orElse("") + (session.getSize() + 1));
    }

    private Result<String> resolveKeyDoubling(String key, ShellSession session) {
        while (session.containsKey(key)) {
            FeatJAR.log().info("This session already contains a Variable with that name: \n");
            session.printSingleELement(key);
            String choice = Shell.readCommand("Overwrite " + key + " ? (y)es, (r)ename, (a)bort")
                    .orElse("")
                    .toLowerCase();
            if (Objects.equals("y", choice)) {
                session.remove(choice)
                        .ifPresent(e -> FeatJAR.log().message("Removing of " + e + " successful"))
                        .orElseLog(Verbosity.ERROR);
                break;
            } else if (Objects.equals("r", choice)) {
                key = Shell.readCommand("Enter another vaiable name: ").orElse("");
            } else if (Objects.equals("a", choice)) {
                FeatJAR.log().message("Aborted\n");
                return Result.empty();
            }
        }
        return Result.of(key);
    }

    private <T> void loadFormat(Result<T> result, String key, ShellSession session) {

        Result<String> newKey = resolveKeyDoubling(key, session);

        if (newKey.isEmpty()) {
            return;
        } else {
            key = newKey.get();
        }

        if (result.isPresent()) {
            if (FeatureModel.class.isAssignableFrom(result.get().getClass())) {
                session.put(key, (FeatureModel) result.get(), FeatureModel.class);

            } else if (IFormula.class.isAssignableFrom(result.get().getClass())) {
                session.put(key, (IFormula) result.get(), IFormula.class);

            } else if (BooleanAssignment.class.isAssignableFrom(result.get().getClass())) {
                session.put(
                        key,
                        ((BooleanAssignmentGroups) result.get()).getFirstGroup().getFirst(),
                        BooleanAssignment.class);

            } else if (BooleanAssignmentList.class.isAssignableFrom(result.get().getClass())) {
                session.put(key, ((BooleanAssignmentGroups) result.get()).getFirstGroup(), BooleanAssignmentList.class);

            } else if (BooleanAssignmentGroups.class.isAssignableFrom(
                    result.get().getClass())) {
                session.put(key, (BooleanAssignmentGroups) result.get(), BooleanAssignmentGroups.class);
            }
            FeatJAR.log().message(key + " successfully loaded\n");
        } else {
            printFormatIssue(result, key);
        }
    }

    private <T> void printFormatIssue(Result<T> result, String key) {
        String wrongFormat = "Possible formats:";
        String input = Problem.printProblems(result.getProblems());

        if (input.contains(wrongFormat)) {
            int firstIndex = input.indexOf(wrongFormat) + wrongFormat.length();
            int SecondIndex = input.lastIndexOf("]") + 1;
            String message = input.substring(0, firstIndex);
            String possibleFormats = input.substring(firstIndex, SecondIndex);
            possibleFormats = possibleFormats.replace(",", ",\n");

            FeatJAR.log().error(message + "\n");
            FeatJAR.log().error(possibleFormats);
        } else {
            FeatJAR.log()
                    .error("Could not load file '" + result.getProblems().get(0).getMessage() + "' for variable '" + key
                            + "'");
            FeatJAR.log().problems(result.getProblems(), Verbosity.DEBUG);
        }
    }

    protected <T> void parseArguments(ShellSession session, List<String> cmdParams, AFormats<T> format) {
        String name = setVariableName(session, cmdParams);
        String path = setPath(cmdParams);

        if (path.isBlank()) {
            FeatJAR.log()
                    .debug("No correct path for '%s' specified", getFormatName().orElse(""));
            return;
        }
        loadFormat(IO.load(Paths.get(path), format), name, session);
    }
}
