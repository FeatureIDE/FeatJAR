/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.composition.cli;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.composition.Preprocessor;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.io.textual.CPPAssignmentFormat;
import de.featjar.formula.io.textual.JavaSymbols;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PreprocessorCommand extends ACommand {

    public static enum Mode {
        PROCESS,
        PRINT_VARIABLES,
        PRINT_ANNOTATIONS
    }

    public static enum MissingVariables {
        IGNORE,
        TRUE,
        FALSE
    }

    public static final Option<Path> CONFIGURATION_OPTION = Option.newOption("configuration", Option.PathParser)
            .setDescription("Path to configuration file")
            .setValidator(Option.PathValidator);

    public static final Option<Mode> MODE_OPTION = Option.newEnumOption("mode", Mode.class)
            .setDefaultValue(Mode.PROCESS)
            .setDescription("Mode of operation");

    public static final Option<MissingVariables> MISSING_VARIABLES_OPTION = Option.newEnumOption(
                    "missing-variables", MissingVariables.class)
            .setDefaultValue(MissingVariables.IGNORE)
            .setDescription("How to deal with variables in the processed file that do not appear in the given config");

    public static final Option<String> PREFIX_OPTION = Option.newOption("annotation-prefix", Option.StringParser)
            .setDefaultValue("#")
            .setDescription("The prefix that precedes each annotation");

    @Override
    public int run(OptionList optionParser) {
        Path in = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Path out = optionParser.getResult(OUTPUT_OPTION).orElse(null);
        Charset charset = StandardCharsets.UTF_8;
        String annotationPrefix = optionParser.getResult(PREFIX_OPTION).orElseThrow();

        Preprocessor preprocessor = new Preprocessor(annotationPrefix, JavaSymbols.INSTANCE);

        Mode mode = optionParser.getResult(MODE_OPTION).orElseThrow();

        Stream<String> stream = null;
        try {
            switch (mode) {
                case PROCESS:
                    stream = preprocess(
                            in,
                            out,
                            optionParser.getResult(CONFIGURATION_OPTION).orElseThrow(),
                            optionParser.getResult(MISSING_VARIABLES_OPTION).orElseThrow(),
                            charset,
                            preprocessor);
                    break;
                case PRINT_VARIABLES:
                    stream = printVariableNames(in, charset, preprocessor);
                    break;
                case PRINT_ANNOTATIONS:
                    stream = printAnnotations(in, charset, preprocessor);
                    break;
                default:
                    return 1;
            }
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return 1;
        }

        if (out != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    out, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                stream.forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        FeatJAR.log().error(e);
                    }
                });
            } catch (IOException e) {
                FeatJAR.log().error(e);
                return 1;
            }
        } else {
            stream.forEach(FeatJAR.log()::plainMessage);
        }
        return 0;
    }

    private Stream<String> preprocess(
            Path in,
            Path out,
            Path assignmentPath,
            MissingVariables missingVariables,
            Charset charset,
            Preprocessor preprocessor)
            throws IOException {
        Result<Assignment> parsedAssignment = IO.load(assignmentPath, new CPPAssignmentFormat());
        if (parsedAssignment.isEmpty()) {
            FeatJAR.log().problems(parsedAssignment);
            return Stream.empty();
        }

        final Assignment assignment;
        switch (missingVariables) {
            case FALSE:
                assignment = addMissingVariablesToAssignment(
                        parsedAssignment.get(),
                        preprocessor.extractVariableNames(Files.lines(in, charset)),
                        Boolean.FALSE);
                break;
            case IGNORE:
                assignment = addMissingVariablesToAssignment(
                        parsedAssignment.get(), preprocessor.extractVariableNames(Files.lines(in, charset)), null);
                break;
            case TRUE:
                assignment = addMissingVariablesToAssignment(
                        parsedAssignment.get(),
                        preprocessor.extractVariableNames(Files.lines(in, charset)),
                        Boolean.TRUE);
                break;
            default:
                throw new IllegalStateException(String.valueOf(missingVariables));
        }

        return preprocessor.preprocess(Files.lines(in, charset), assignment);
    }

    private Assignment addMissingVariablesToAssignment(
            Assignment orgAssignment, List<String> extractVariableNames, Object value) throws IOException {
        LinkedHashMap<String, Object> variableValuePairs = new LinkedHashMap<>(orgAssignment.getAll());
        for (String variableName : extractVariableNames) {
            if (!variableValuePairs.containsKey(variableName)) {
                variableValuePairs.put(variableName, value);
            }
        }
        return new Assignment(variableValuePairs);
    }

    private Stream<String> printVariableNames(Path in, Charset charset, Preprocessor preprocessor) throws IOException {
        return preprocessor.extractVariableNames(Files.lines(in, charset)).stream();
    }

    private Stream<String> printAnnotations(Path in, Charset charset, Preprocessor preprocessor) throws IOException {
        return preprocessor.extractAnnotations(Files.lines(in, charset)).stream();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Preprocesses files with annotations");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("preprocessor");
    }
}
