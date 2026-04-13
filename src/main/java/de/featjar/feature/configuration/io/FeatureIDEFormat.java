/*
 * Copyright (C) 2026 FeatJAR-Development-Team
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
package de.featjar.feature.configuration.io;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.feature.configuration.Configuration;
import de.featjar.feature.configuration.Configuration.Selection;
import de.featjar.feature.configuration.Configuration.SelectionNotPossibleException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended configuration format for FeatureIDE projects.<br> Lists all features and indicates the manual and automatic selection.
 *
 * @author Sebastian Krieter
 */
public class FeatureIDEFormat implements IFormat<Configuration> {

    private static final String NEWLINE = System.lineSeparator();

    /**
     * Parses a String representation of a FeatureIDE Format into a Configuration.
     *
     * @param inputmapper the input mapper
     * @return Configuration inside the Result wrapper
     */
    @Override
    public Result<Configuration> parse(AInputMapper inputmapper) {
        Configuration configuration = new Configuration();
        List<Problem> warnings = new ArrayList<>();

        String line = null;
        int lineNumber = 1;
        try (BufferedReader reader = inputmapper.get().getReader(); ) {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    Boolean manual = null;
                    Boolean automatic = null;
                    try {
                        switch (Integer.parseInt(line.substring(0, 1))) {
                            case 0:
                                manual = Boolean.FALSE;
                                break;
                            case 1:
                                manual = Boolean.TRUE;
                                break;
                            case 2:
                                break;
                            default:
                                warnings.add(new ParseProblem(line, Severity.WARNING, lineNumber));
                                break;
                        }
                        switch (Integer.parseInt(line.substring(1, 2))) {
                            case 0:
                                automatic = Boolean.FALSE;
                                break;
                            case 1:
                                automatic = Boolean.TRUE;
                                break;
                            case 2:
                                break;
                            default:
                                warnings.add(new ParseProblem(line, Severity.WARNING, lineNumber));
                                break;
                        }
                    } catch (final NumberFormatException e) {
                        warnings.add(new ParseProblem(e, lineNumber));
                    }

                    final String name = line.substring(2);

                    final Result<Selection<?>> feature = configuration.getSelection(name);
                    if (feature.isEmpty()) {
                        warnings.add(new ParseProblem(name, Severity.ERROR, lineNumber));
                    } else {
                        try {
                            feature.get().setManual(manual);
                            feature.get().setAutomatic(automatic);
                        } catch (final SelectionNotPossibleException e) {
                            warnings.add(new ParseProblem(e, lineNumber));
                        }
                    }
                }
                lineNumber++;
            }
        } catch (final IOException e) {
            warnings.add(new Problem(e));
        }

        return Result.of(configuration, warnings);
    }

    /**
     * Returns the String representation of a Configuration in the FeatureIDE Format.
     *
     * @param configuration the object
     * @return String representation of the Configuration inside the Result wrapper
     */
    @Override
    public Result<String> serialize(Configuration configuration) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(
                "# Lists all features from the model with manual (first digit) and automatic (second digit) selection");
        buffer.append(NEWLINE);
        buffer.append("# 0 = deselected, 1 = selected, 2 = undefined");
        buffer.append(NEWLINE);

        for (final String name : configuration.getVariableMap().getVariableNames()) {
            Selection<?> selection = configuration.get(name);
            buffer.append(Integer.toString(getSelectionCode((Boolean) selection.getManual())));
            buffer.append(Integer.toString(getSelectionCode((Boolean) selection.getAutomatic())));
            buffer.append(name);
            buffer.append(NEWLINE);
        }

        return Result.of(buffer.toString());
    }

    private int getSelectionCode(Boolean selection) {
        if (selection == null) {
            return 2;
        } else if (selection == Boolean.TRUE) {
            return 1;
        } else if (selection == Boolean.FALSE) {
            return 0;
        } else {
            return 3;
        }
    }

    @Override
    public String getFileExtension() {
        return ".config";
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public String getName() {
        return "FeatureIDE-Internal";
    }
}
