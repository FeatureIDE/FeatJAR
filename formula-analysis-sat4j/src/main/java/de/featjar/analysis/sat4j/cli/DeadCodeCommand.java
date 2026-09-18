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

import de.featjar.analysis.sat4j.computation.ComputeSatisfiableSAT4J;
import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.computation.Computations;
import de.featjar.base.io.IO;
import de.featjar.composition.Preprocessor;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.textual.JavaSymbols;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

public class DeadCodeCommand extends ACommand {

    public static final Option<Path> FEATURE_MODEL_OPTION = Options.newOption("feature-model", Options.PathParser)
            .setDescription("Path to feature model file")
            .setValidator(Options.PathValidator);

    public static final Option<String> PREFIX_OPTION = Options.newOption("annotation-prefix", Options.StringParser)
            .setDefaultArgument("#")
            .setDescription("The prefix that precedes each annotation");

    @Override
    public int run(OptionList optionParser) {
        try {
            IFormula featureModel = IO.load(
                            optionParser.getResult(FEATURE_MODEL_OPTION).orElseThrow(), FormulaFormats.getInstance())
                    .orElseThrow();
            new Preprocessor(optionParser.getResult(PREFIX_OPTION).orElseThrow(), JavaSymbols.INSTANCE)
                    .findDeadCode(
                            Files.readAllLines(
                                    optionParser.getResult(INPUT_OPTION).orElseThrow())
                                    .stream(),
                            consistentWith(featureModel))
                    .forEach(FeatJAR.log()::plainMessage);
            return 0;
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
    }

    /**
     * {@return a test whether a presence condition can be true in some configuration of the feature model}
     */
    public static Predicate<IFormula> consistentWith(IFormula featureModel) {
        IFormula model = featureModel instanceof Reference reference ? reference.getExpression() : featureModel;
        return condition -> Computations.of((IFormula) new And(model, condition))
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSatisfiableSAT4J::new)
                .compute();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints code blocks that can never be included for a given feature model");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("dead-code");
    }
}
