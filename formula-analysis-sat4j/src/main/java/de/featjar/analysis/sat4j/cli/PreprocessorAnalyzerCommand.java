/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j.cli;

import de.featjar.analysis.sat4j.PreprocessorAnalyzer;
import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionParser;
import de.featjar.base.cli.Options;
import de.featjar.base.io.IO;
import de.featjar.composition.Preprocessor;
import de.featjar.composition.cli.PreprocessorCommand.AnnotationStyle;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.textual.CPPAssignmentFormat;
import de.featjar.formula.structure.IFormula;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public class PreprocessorAnalyzerCommand extends ACommand {

    public static enum Mode {
        FIND_DEAD_CODE,
        PRINT_SUPERFLUOUS_ANNOTATIONS,
        PRINT_INCLUSIONS
    }

    public static final Option<Mode> MODE_OPTION = Options.newEnumOption("mode", Mode.class)
            .setDefaultArgument(Mode.FIND_DEAD_CODE.name())
            .setDescription("Mode of operation");

    public static final Option<String> PREFIX_OPTION = Options.newOption("annotation-prefix", Options.StringParser)
            .setDescription("The prefix that precedes each annotation (overrides the prefix of the annotation style)");

    public static final Option<AnnotationStyle> STYLE_OPTION = Options.newEnumOption(
                    "annotation-style", AnnotationStyle.class)
            .setDefaultArgument(AnnotationStyle.CPP.name())
            .setDescription("The syntax of the annotations (CPP, ANTENNA, or MUNGE)");

    public static final Option<Path> CONFIGURATION_OPTION =
            Options.newOption("configuration", Options.ExistingPathParser).setDescription("Path to configuration file");

    public static final Option<Path> FEATURE_MODEL_OPTION =
            Options.newOption("feature-model", Options.ExistingPathParser).setDescription("Path to feature model file");

    @Override
    public int run(OptionParser optionParser) {
        Path in = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Path out = optionParser.getResult(OUTPUT_OPTION).orElse(null);
        Charset charset = StandardCharsets.UTF_8;
        Preprocessor.Style style =
                optionParser.getResult(STYLE_OPTION).orElseThrow().getStyle();
        style = style.withPrefix(optionParser.getResult(PREFIX_OPTION).orElse(style.getPrefix()));
        PreprocessorAnalyzer preprocessor = new PreprocessorAnalyzer(style);

        Mode mode = optionParser.getResult(MODE_OPTION).orElseThrow();

        Stream<String> stream = null;
        try {
            switch (mode) {
                case FIND_DEAD_CODE:
                    stream = detectDeadCode(in, charset, preprocessor, optionParser);
                    break;
                case PRINT_SUPERFLUOUS_ANNOTATIONS:
                    stream = printSuperfluousAnnotations(in, charset, preprocessor, optionParser);
                    break;
                case PRINT_INCLUSIONS:
                    stream = printInclusions(in, charset, preprocessor, optionParser);
                    break;
                default:
                    return 1;
            }
        } catch (IOException | IllegalArgumentException e) {
            FeatJAR.log().error(e);
            return 1;
        }

        try (Stream<String> output = stream) {
            if (out != null) {
                try (BufferedWriter writer = Files.newBufferedWriter(
                        out, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    output.forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                            writer.flush();
                        } catch (IOException e) {
                            FeatJAR.log().error(e);
                        }
                    });
                }
            } else {
                output.forEach(FeatJAR.log()::plainMessage);
            }
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return 1;
        }
        return 0;
    }

    private Stream<String> detectDeadCode(
            Path in, Charset charset, PreprocessorAnalyzer preprocessor, OptionParser optionParser) throws IOException {
        IFormula featureModel = loadFeatureModel(optionParser);
        try (Stream<String> lines = Files.lines(in, charset)) {
            return preprocessor.findDeadCode(lines, featureModel).stream();
        }
    }

    private Stream<String> printInclusions(
            Path in, Charset charset, PreprocessorAnalyzer preprocessor, OptionParser optionParser) throws IOException {
        Path assignmentPath = optionParser.getResult(CONFIGURATION_OPTION).orElseThrow();
        Assignment assignment =
                IO.load(assignmentPath, new CPPAssignmentFormat()).orElseThrow();
        IFormula featureModel = loadFeatureModel(optionParser);
        Iterator<PreprocessorAnalyzer.Inclusion> inclusions;
        try (Stream<String> lines = Files.lines(in, charset)) {
            inclusions = preprocessor
                    .computeInclusions(lines, featureModel, assignment)
                    .iterator();
        }
        return Files.lines(in, charset).map(line -> String.format("%-9s %s", inclusions.next(), line));
    }

    private Stream<String> printSuperfluousAnnotations(
            Path in, Charset charset, PreprocessorAnalyzer preprocessor, OptionParser optionParser) throws IOException {
        IFormula featureModel = loadFeatureModel(optionParser);
        try (Stream<String> lines = Files.lines(in, charset)) {
            return preprocessor.findSuperfluousAnnotations(lines, featureModel).stream();
        }
    }

    private IFormula loadFeatureModel(OptionParser optionParser) {
        Path featureModelPath = optionParser.getResult(FEATURE_MODEL_OPTION).orElseThrow();
        return IO.load(featureModelPath, FormulaFormats.getInstance()).orElseThrow();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Finds dead code, superfluous annotations, and line inclusions using SAT4J");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("preprocessor-sat4j");
    }
}
