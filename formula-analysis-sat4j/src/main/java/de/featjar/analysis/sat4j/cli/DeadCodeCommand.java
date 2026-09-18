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
import de.featjar.base.tree.Trees;
import de.featjar.composition.Preprocessor;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.JavaSymbols;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.False;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            Preprocessor preprocessor =
                    new Preprocessor(optionParser.getResult(PREFIX_OPTION).orElseThrow(), JavaSymbols.INSTANCE);
            List<String> lines =
                    Files.readAllLines(optionParser.getResult(INPUT_OPTION).orElseThrow());
            detectDead(preprocessor, lines, featureModel).forEach(FeatJAR.log()::plainMessage);
            return 0;
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }
    }

    /**
     * {@return one message per block of code lines whose presence condition contradicts the feature model}
     */
    public static List<String> detectDead(Preprocessor preprocessor, List<String> lines, IFormula featureModel) {
        IFormula model = featureModel instanceof Reference reference ? reference.getExpression() : featureModel;
        List<IFormula> presence = preprocessor.computePresenceConditions(lines.stream());
        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setSymbols(JavaSymbols.INSTANCE);
        List<String> dead = new ArrayList<>();
        for (int start = 0; start < lines.size(); start++) {
            // annotation lines have the condition False; only the first line of each code block is checked
            if (presence.get(start) == False.INSTANCE || (start > 0 && presence.get(start - 1) != False.INSTANCE)) {
                continue;
            }
            int end = start;
            while (end + 1 < lines.size() && presence.get(end + 1) != False.INSTANCE) {
                end++;
            }
            boolean satisfiable = Computations.of((IFormula) new And(model, presence.get(start)))
                    .map(ComputeNNFFormula::new)
                    .map(ComputeCNFFormula::new)
                    .map(ComputeBooleanClauseList::new)
                    .map(ComputeSatisfiableSAT4J::new)
                    .compute();
            if (!satisfiable) {
                dead.add(String.format(
                        "Dead code at lines %d-%d: %s",
                        start + 1,
                        end + 1,
                        Trees.traverse(presence.get(start), serializer).orElseThrow()));
            }
        }
        return dead;
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
