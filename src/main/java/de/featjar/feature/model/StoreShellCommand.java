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
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.AFormats;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.base.shell.IShellCommand;
import de.featjar.base.shell.Shell;
import de.featjar.base.shell.ShellSession;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentFormats;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.IFormula;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public void execute(ShellSession session, List<String> cmdParams) {
        save(session, cmdParams);
    }

    /**
     * Saves the given variable with the selected format.
     *
     * @param <T> the type of the format to be stored
     * @param session the shell session
     * @param cmdParams the command line arguments
     */
    private <T> void save(ShellSession session, List<String> cmdParams) {

        String variableName = setVariableName(cmdParams);

        session.get(variableName)
                .ifPresent(element -> {
                    Result<IFormat<T>> inputFormat = setFormat(session, variableName, cmdParams);
                    inputFormat
                            .ifPresent(format -> {
                                final Path path = Paths.get("" + variableName);
                                try {
                                    IO.save((T) element, path, format);
                                } catch (IOException e) {
                                    FeatJAR.log().error(e);
                                    return;
                                }
                                FeatJAR.log().message("Storing of " + variableName + " Successful");
                            })
                            .orElseLog(Verbosity.ERROR);
                })
                .orElseLog(Verbosity.ERROR);
    }

    private <T> Result<AFormats<T>> getFormatType(ShellSession session, final String variableName) {
        Result<Object> elementType = session.getType(variableName);
        AFormats<T> format = null;

        if (elementType.isEmpty()) {
            return Result.empty(elementType.getProblems());
        }

        Class<T> classType = (Class<T>) elementType.get();
        if (classType.isAssignableFrom(FeatureModel.class)) {
            format = (AFormats<T>) FeatureModelFormats.getInstance();
        } else if (classType.isAssignableFrom(IFormula.class)) {
            format = (AFormats<T>) FormulaFormats.getInstance();
        } else if (classType.isAssignableFrom(BooleanAssignment.class)) {
            format = (AFormats<T>) BooleanAssignmentFormats.getInstance();
        } else if (classType.isAssignableFrom(BooleanAssignmentList.class)) {
            format = (AFormats<T>) BooleanAssignmentListFormats.getInstance();
        } else if (classType.isAssignableFrom(BooleanAssignmentGroups.class)) {
            format = (AFormats<T>) BooleanAssignmentGroupsFormats.getInstance();
        } else {
            return Result.empty(
                    addProblem(Severity.ERROR, "Could not find the variable %s or its format", variableName));
        }
        return Result.of(format);
    }

    private String setVariableName(List<String> cmdParams) {
        String input = "";
        if (cmdParams.isEmpty()) {
            while (input.isBlank()) {
                input = Shell.readCommand("Enter the variable that you want to store:\n")
                        .orElse("");
            }
        } else {
            input = cmdParams.get(0);
        }
        return input;
    }

    private Problem addProblem(Severity severity, String message, Object... arguments) {
        return new Problem(String.format(message, arguments), severity);
    }

    private <T> Result<IFormat<T>> setFormat(ShellSession session, String variableName, List<String> cmdParams) {

        Result<AFormats<T>> formatType = getFormatType(session, variableName);

        if (formatType.isEmpty()) {
            return Result.empty(formatType.getProblems());
        }

        AFormats<T> formatExtensionPoint = formatType.get();
        List<IFormat<T>> possibleFormats = formatExtensionPoint.getExtensions().stream()
                .filter(f -> f.supportsWrite())
                .collect(Collectors.toList());
        IFormat<T> format = null;
        String input = "";

        if (cmdParams.isEmpty() || cmdParams.size() == 1) {
            FeatJAR.log().message("Select the format for the variable to store:\n");
            return selectFormat(possibleFormats);
        } else {
            input = cmdParams.get(1);
            final String caputerdInput = input;
            List<IFormat<T>> formats = possibleFormats.stream()
                    .filter(f -> f.getName().toLowerCase().startsWith(caputerdInput))
                    .collect(Collectors.toList());

            if (formats.isEmpty()) {
                Result<IFormat<T>> matchingExtension = formatExtensionPoint.getMatchingExtension(input);
                if (matchingExtension.isEmpty()) {
                    FeatJAR.log().warning("No format matched the name '%s'!: Choose your format", input);
                    return selectFormat(possibleFormats);
                }
                format = matchingExtension.get();
            } else {
                if (formats.get(0).getName().toLowerCase().matches(input)) {
                    format = formats.get(0);
                    return Result.of(format);
                }
                String choice = Shell.readCommand(
                                "Do you mean: " + formats.get(0).getName() + "? (ENTER)\n")
                        .orElse("");
                if (choice.isEmpty()) {
                    format = formats.get(0);
                } else {
                    return Result.empty(addProblem(Severity.ERROR, "No format matched the name '%s'!", choice));
                }
            }
        }
        return Result.of(format);
    }

    private <T> Result<IFormat<T>> selectFormat(List<IFormat<T>> possibleFormats) {
        for (int i = 0; i < possibleFormats.size(); i++) {
            FeatJAR.log().message("%d. %s", i, possibleFormats.get(i).getName());
        }

        String choice = Shell.readCommand("").orElse("");

        if (choice.isBlank()) {
            return Result.empty();
        }
        int parsedChoice;
        try {
            parsedChoice = Integer.parseInt(choice);
        } catch (NumberFormatException e) {
            return Result.empty(addProblem(Severity.ERROR, String.format("'%s' is no vaild number", choice), e));
        }

        for (int i = 0; i < possibleFormats.size(); i++) {
            if (i == parsedChoice) {
                return Result.of(possibleFormats.get(i));
            }
        }
        return Result.empty(addProblem(Severity.ERROR, "No format matched the choice '%s'!", choice));
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("store");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("<var> <format> - save seesion variables on your cuurent working directory");
    }
}
