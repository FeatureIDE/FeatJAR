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
import de.featjar.base.data.Problem;
import de.featjar.base.io.IO;
import de.featjar.composition.PreprocessorStream;
import de.featjar.composition.PreprocessorStream.Line;
import de.featjar.composition.Style;
import de.featjar.composition.cli.PreprocessorCommand.AnnotationStyle;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.IFormula;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class PreprocessorAnalyzerCommand extends ACommand {

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
        Style style = optionParser.getResult(STYLE_OPTION).orElseThrow().getStyle();
        style = style.withPrefix(optionParser.getResult(PREFIX_OPTION).orElse(style.getPrefix()));

        Path featureModelPath = optionParser.getResult(FEATURE_MODEL_OPTION).orElseThrow();
        IFormula featureModel =
                IO.load(featureModelPath, FormulaFormats.getInstance()).orElseThrow();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer();
        try (Stream<Line> stream = PreprocessorStream.create(in, style)) {
            Stream<Line> lineStream = stream;
            lineStream = analyzer.detectCoreCode(lineStream, featureModel, new Assignment());
            lineStream = analyzer.detectDeadCode(lineStream, featureModel, new Assignment());
            lineStream = analyzer.detectSuperfluous(lineStream, featureModel, new Assignment());

            return writeStream(
                    optionParser, lineStream.flatMap(Line::getProblems).map(Problem::toString));
        } catch (IOException | IllegalArgumentException e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
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
