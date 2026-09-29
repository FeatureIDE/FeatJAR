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
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.textual.JavaSymbols;
import de.featjar.formula.structure.IFormula;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.stream.Stream;

public class PreprocessorAnalyzerCommand extends ACommand {

    public static enum Mode {
        FIND_DEAD_CODE,
        PRINT_SUPERFLUOUS_ANNOTATIONS
    }

    public static final Option<Mode> MODE_OPTION = Options.newEnumOption("mode", Mode.class)
            .setDefaultArgument(Mode.FIND_DEAD_CODE.name())
            .setDescription("Mode of operation");

    public static final Option<String> PREFIX_OPTION = Options.newOption("annotation-prefix", Options.StringParser)
            .setDefaultArgument("#")
            .setDescription("The prefix that precedes each annotation");

    public static final Option<Path> FEATURE_MODEL_OPTION =
            Options.newOption("feature-model", Options.ExistingPathParser).setDescription("Path to feature model file");

    @Override
    public int run(OptionParser optionParser) {
        Path in = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Path out = optionParser.getResult(OUTPUT_OPTION).orElse(null);
        Charset charset = StandardCharsets.UTF_8;
        String annotationPrefix = optionParser.getResult(PREFIX_OPTION).orElseThrow();

        PreprocessorAnalyzer preprocessor = new PreprocessorAnalyzer(annotationPrefix, JavaSymbols.INSTANCE);

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
                default:
                    return 1;
            }
        } catch (IOException | IllegalArgumentException e) {
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

    private Stream<String> detectDeadCode(
            Path in, Charset charset, PreprocessorAnalyzer preprocessor, OptionParser optionParser) throws IOException {
        Path featureModelPath = optionParser.getResult(FEATURE_MODEL_OPTION).orElseThrow();
        IFormula featureModel =
                IO.load(featureModelPath, FormulaFormats.getInstance()).orElseThrow();
        try (Stream<String> lines = Files.lines(in, charset)) {
            return preprocessor.findDeadCode(lines, featureModel).stream();
        }
    }

    private Stream<String> printSuperfluousAnnotations(
            Path in, Charset charset, PreprocessorAnalyzer preprocessor, OptionParser optionParser) throws IOException {
        Path featureModelPath = optionParser.getResult(FEATURE_MODEL_OPTION).orElseThrow();
        IFormula featureModel =
                IO.load(featureModelPath, FormulaFormats.getInstance()).orElseThrow();
        try (Stream<String> lines = Files.lines(in, charset)) {
            return preprocessor.findSuperfluousAnnotations(lines, featureModel).stream();
        }
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Finds dead code and superfluous annotations using SAT4J");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("preprocessor-sat4j");
    }
}
