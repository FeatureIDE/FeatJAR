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
package de.featjar.feature.model.io.dimacs;

import de.featjar.base.computation.Computations;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.VariableMap;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.dimacs.DimacsParser;
import de.featjar.formula.io.dimacs.DimacsSerializer;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes feature models in the DIMACS CNF format.
 *
 * @author Sebastian Krieter
 */
public class DimacsFeatureModelFormat implements IFormat<IFeatureModel> {

    @Override
    public Result<String> serialize(IFeatureModel featureModel) {
        Reference formula = Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .set(ComputeCNFFormula.IS_STRICT, true)
                .compute();
        VariableMap variableMap = new VariableMap(formula.getVariableMap().keySet());
        return Result.of(DimacsSerializer.serialize(
                variableMap, formula.getExpression().getChildren(), c -> writeClause(c, variableMap)));
    }

    private static int[] writeClause(IExpression clause, VariableMap variableMap) {
        int[] literals = new int[clause.getChildrenCount()];
        int i = 0;
        for (final IExpression child : clause.getChildren()) {
            final Literal l = (Literal) child;
            final int index = variableMap.get(l.getExpression().getName()).orElseThrow();
            literals[i++] = l.isPositive() ? index : -index;
        }
        return literals;
    }

    @Override
    public Result<IFeatureModel> parse(AInputMapper inputMapper) {
        final DimacsParser parser = new DimacsParser();
        parser.setReadingVariableDirectory(true);
        try {
            Pair<VariableMap, List<int[]>> parsingResult = parser.parse(inputMapper);
            VariableMap variableMap = parsingResult.getKey();

            FeatureModel featureModel = new FeatureModel();
            for (String variableName : variableMap.getVariableNames()) {
                IFeatureTree featureNode = featureModel.addFeatureTreeRoot(featureModel.addFeature(variableName));
                featureNode.mutate().makeOptional();
            }

            for (int[] clauseLiterals : parsingResult.getValue()) {
                List<Literal> literals = new ArrayList<>(clauseLiterals.length);
                for (int l : clauseLiterals) {
                    String variableName = variableMap
                            .get(Math.abs(l))
                            .orElseThrow(p -> new IllegalArgumentException("No mapping for literal " + l));
                    literals.add(new Literal(l > 0, variableName));
                }
                featureModel.addConstraint(new Or(literals));
            }

            return Result.of(featureModel);
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e, e.getErrorOffset()));
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public String getName() {
        return "DIMACS";
    }

    @Override
    public String getFileExtension() {
        return "dimacs";
    }
}
