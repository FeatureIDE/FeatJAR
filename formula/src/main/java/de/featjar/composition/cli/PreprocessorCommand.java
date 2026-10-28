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
import de.featjar.base.cli.OptionParser;
import de.featjar.base.cli.Options;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.tree.Trees;
import de.featjar.composition.PreprocessorStream;
import de.featjar.composition.Style;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.io.textual.CPPAssignmentFormat;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.ExpressionSerializer.Notation;
import de.featjar.formula.io.textual.ShortSymbols;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class PreprocessorCommand extends ACommand {

    public static enum Mode {
        CHECK,
        PROCESS,
        PRINT_VARIABLES,
        PRINT_ANNOTATIONS,
        PRINT_PRESENCE_CONDITIONS,
    }

    public static enum AnnotationStyle {
        CPP(Style.CPP),
        ANTENNA(Style.ANTENNA),
        MUNGE(Style.MUNGE);

        private final Style style;

        private AnnotationStyle(Style style) {
            this.style = style;
        }

        public Style getStyle() {
            return style;
        }
    }

    public static final Option<Path> CONFIGURATION_OPTION =
            Options.newOption("configuration", Options.ExistingPathParser).setDescription("Path to configuration file");

    public static final Option<Path> FEATURE_MODEL_OPTION =
            Options.newOption("feature-model", Options.ExistingPathParser).setDescription("Path to feature model file");

    public static final Option<Mode> MODE_OPTION = Options.newEnumOption("mode", Mode.class)
            .setDefaultArgument(Mode.CHECK.name())
            .setDescription("Mode of operation");

    public static final Option<PreprocessorStream.UndeterminedMode> MISSING_VARIABLES_OPTION = Options.newEnumOption(
                    "undetermined", PreprocessorStream.UndeterminedMode.class)
            .setDefaultArgument(PreprocessorStream.UndeterminedMode.WARNING.name())
            .setDescription("How to deal with undetermined variables");

    public static final Option<AnnotationStyle> STYLE_OPTION = Options.newEnumOption(
                    "annotation-style", AnnotationStyle.class)
            .setDefaultArgument(AnnotationStyle.CPP.name())
            .setDescription("The syntax of the annotations");

    public static final Option<String> PREFIX_OPTION = Options.newOption("annotation-prefix", Options.StringParser)
            .setDescription("The prefix that precedes each annotation (overrides the prefix of the annotation style)");

    public static final Option<String> OLD_FEATURE_NAME_OPTION = Options.newOption(
                    "old-feature-name", Options.StringParser)
            .setDescription("Current feature name to rename in annotations");

    public static final Option<String> NEW_FEATURE_NAME_OPTION = Options.newOption(
                    "new-feature-name", Options.StringParser)
            .setDescription("New feature name to use in annotations");

    @Override
    public int run(OptionParser optionParser) {
        Path in = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Style style = optionParser.getResult(STYLE_OPTION).orElseThrow().getStyle();
        Result<String> annotationPrefix = optionParser.getResult(PREFIX_OPTION);
        if (annotationPrefix.isPresent()) {
            style = style.withPrefix(annotationPrefix.get());
        }
        Mode mode = optionParser.getResult(MODE_OPTION).orElseThrow();

        try (PreprocessorStream preprocessor = PreprocessorStream.create(in, style)) {
            Stream<String> stream =
                    switch (mode) {
                        case CHECK -> preprocessor.mapToProblems().map(ParseProblem::toString);
                        case PROCESS -> {
                            Assignment parsedAssignment = IO.load(
                                            optionParser
                                                    .getResult(CONFIGURATION_OPTION)
                                                    .orElseThrow(),
                                            new CPPAssignmentFormat())
                                    .orElseThrow();
                            yield preprocessor
                                    .process(
                                            parsedAssignment,
                                            optionParser
                                                    .getResult(MISSING_VARIABLES_OPTION)
                                                    .orElseThrow())
                                    .mapToString();
                        }
                        case PRINT_VARIABLES -> preprocessor.mapToVariableNames();
                        case PRINT_ANNOTATIONS -> preprocessor.keepAnnotations().mapToString();
                        case PRINT_PRESENCE_CONDITIONS -> {
                            ExpressionSerializer serializer = new ExpressionSerializer();
                            serializer.setNotation(Notation.INFIX);
                            serializer.setSymbols(ShortSymbols.INSTANCE);
                            yield preprocessor.mapToPresenceConditions().map(pc -> Trees.traverse(pc, serializer)
                                    .get());
                        }
                        default -> throw new IllegalStateException(String.valueOf(mode));
                    };
            writeStream(optionParser, stream);
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
        return FeatJAR.EXIT_SUCCESS;
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
