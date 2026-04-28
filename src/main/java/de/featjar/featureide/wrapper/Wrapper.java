/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model-assistance.
 *
 * feature-model-assistance is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model-assistance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model-assistance. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-featureide-wrapper> for further information.
 */
package de.featjar.featureide.wrapper;

import de.featjar.analysis.sat4j.computation.ComputeAtomicSetsSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeCoreSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeSAT4JSolver;
import de.featjar.analysis.sat4j.computation.ComputeSatisfiableSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeSolutionsSAT4J;
import de.featjar.analysis.sat4j.solver.SAT4JSolver;
import de.featjar.analysis.sharpsat.computation.ComputeSolutionCountSharpSAT;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.IntegerList;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NonBooleanLiteral;
import de.featjar.formula.structure.term.value.Variable;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Wrapper {
    // TODO Redundant Constraints
    // TODO Store Configs
    // TODO Slice
    // TODO Explanations
    // TODO Statistics

    public static Result<IFeatureModel> loadFeatureModel(Path path) {
        return IO.load(path, FeatureModelFormats.getInstance());
    }

    public static void storeFeatureModel(IFeatureModel featureModel, Path path) throws IOException {
        IO.save(
                featureModel,
                path,
                FeatureModelFormats.getInstance().getFormatByName("UVL").orElseThrow());
    }

    public static Result<List<Pair<List<IFeature>, List<IFeature>>>> loadConfigurations(
            Path path, IFeatureModel featureModel) {
        return IO.load(path, BooleanAssignmentListFormats.getInstance())
                .map(list -> convertToFeatureList(list, featureModel));
    }

    public static void storeConfigurations(
            List<Pair<List<IFeature>, List<IFeature>>> configurations, IFeatureModel featureModel, Path path) {
        throw new UnsupportedOperationException();
    }

    public static void statistics(IFeatureModel featureModel) {
        throw new UnsupportedOperationException();
    }

    public static Result<IFeatureModel> slice(
            IFeatureModel featureModel, List<String> featuresToKeep, List<String> featuresToRemove) {
        throw new UnsupportedOperationException();
    }

    public static Result<IFormula> createCNF(IFeatureModel featureModel) {
        return Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .computeResult();
    }

    public static IFormula createLiteral(IFeature feature) {
        Class<?> type = feature.getType();
        Variable variable = new Variable(feature.getName().get(), type);
        if (type == Boolean.class) {
            return new Literal(variable);
        } else {
            return new NonBooleanLiteral(variable);
        }
    }

    public static Result<SAT4JSolver> createSat4JSolver(IFeatureModel featureModel) {
        return Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSAT4JSolver::new)
                .computeResult();
    }

    public static Result<SAT4JSolver> createSat4JSolver(IFormula cnf) {
        return Computations.of(cnf)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSAT4JSolver::new)
                .computeResult();
    }

    public static Result<List<Pair<List<IFeature>, List<IFeature>>>> atomicSets(IFeatureModel featureModel) {
        return Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeAtomicSetsSAT4J::new)
                .computeResult()
                .map(a -> convertToFeatureList(a, featureModel));
    }

    public static Result<Boolean> isVoid(IFeatureModel featureModel) {
        return Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSatisfiableSAT4J::new)
                .computeResult();
    }

    public static Result<List<IFeature>> core(IFeatureModel featureModel) {
        return Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeCoreSAT4J::new)
                .computeResult()
                .map(a -> convertToFeatureList(a.getFirst(), a.getVariableMap(), featureModel)
                        .getFirst());
    }

    public static Result<List<IFeature>> dead(IFeatureModel featureModel) {
        return Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeCoreSAT4J::new)
                .computeResult()
                .map(a -> convertToFeatureList(a.getFirst(), a.getVariableMap(), featureModel)
                        .getSecond());
    }

    // TODO implement
    public static Result<List<IConstraint>> redundant(IFeatureModel featureModel) {
        throw new UnsupportedOperationException();
    }

    public static Result<List<Pair<List<IFeature>, List<IFeature>>>> allConfigurations(IFeatureModel featureModel) {
        return Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSolutionsSAT4J::new)
                .computeResult()
                .map(a -> convertToFeatureList(a, featureModel));
    }

    public static Result<BigInteger> numberOfConfigurations(IFeatureModel featureModel) {
        return Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeSolutionCountSharpSAT::new)
                .computeResult();
    }

    private static List<Pair<List<IFeature>, List<IFeature>>> convertToFeatureList(
            BooleanAssignmentList assignments, IFeatureModel featureModel) {
        return assignments.stream()
                .map(assignment -> convertToFeatureList(assignment, assignments.getVariableMap(), featureModel))
                .collect(Collectors.toList());
    }

    private static Pair<List<IFeature>, List<IFeature>> convertToFeatureList(
            IntegerList values, VariableMap variableMap, IFeatureModel featureModel) {
        return new Pair<>(
                convertToFeatureList(values.stream().filter(literal -> literal > 0), variableMap, featureModel),
                convertToFeatureList(values.stream().filter(literal -> literal < 0), variableMap, featureModel));
    }

    private static List<IFeature> convertToFeatureList(
            IntStream filter, VariableMap variableMap, IFeatureModel featureModel) {
        return filter.mapToObj(v ->
                        variableMap.get(v).mapResult(featureModel::getFeature).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
