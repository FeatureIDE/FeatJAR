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
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model-assistance> for further information.
 */
package de.featjar.featureide;

import de.featjar.analysis.sat4j.computation.ComputeAtomicSetsSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeCompleteSample;
import de.featjar.analysis.sat4j.computation.ComputeCoreSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeSAT4JSolver;
import de.featjar.analysis.sat4j.computation.ComputeSatisfiableSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeSolutionsSAT4J;
import de.featjar.analysis.sat4j.computation.YASA;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.analysis.sat4j.solver.SAT4JSolver;
import de.featjar.analysis.sharpsat.computation.ComputeSolutionCountSharpSAT;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.io.text.DataTreeTextFormat;
import de.featjar.base.tree.DataTree;
import de.featjar.feature.configuration.Configuration;
import de.featjar.feature.configuration.computation.ComputeConfigurationFromAssignment;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureModelElementFilter;
import de.featjar.feature.model.computation.ComputeConstraintNumberOfAtoms;
import de.featjar.feature.model.computation.ComputeConstraintNumberOfConnectives;
import de.featjar.feature.model.computation.ComputeConstraintNumberOfDistinctVariables;
import de.featjar.feature.model.computation.ComputeFeatureTreeMaxDepth;
import de.featjar.feature.model.computation.ComputeFeatureTreeNumberOfBranches;
import de.featjar.feature.model.computation.ComputeFeatureTreeNumberOfGroups;
import de.featjar.feature.model.computation.ComputeFeatureTreeNumberOfLeaves;
import de.featjar.feature.model.computation.ComputeFeatureTreeNumberOfTopNodes;
import de.featjar.feature.model.transformer.ComputeFeatureModelSlice;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.combination.VariableCombinationSpecification.VariableCombinationSpecificationComputation;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convenience class for analyzing a given feature model.
 *
 * Use the methods of this class to get analysis result.
 *
 * @author Sebastian Krieter
 */
public class FeatureModelAnalyzer {
    // TODO Redundant Constraints
    // TODO Explanations

    private final IFeatureModel featureModel;
    private final IComputation<IFeatureModel> fmComputation;

    /**
     * New analyzer using the given feature model.
     * @param featureModel the feature model to use for analysis
     */
    public FeatureModelAnalyzer(IFeatureModel featureModel) {
        this.featureModel = featureModel;
        this.fmComputation = Computations.of(featureModel);
    }

    /**
     * {@return Some easy to compute statistical values of the feature model in form of a grouped tree}
     */
    public DataTree<Void> statistics() {
        DataTree<Void> data = DataTree.of("FeatureModelStatistics");
        DataTree<?> treeData = DataTree.of("FeatureTree");
        data.addChild(treeData);
        treeData.addChild(fmComputation.map(ComputeFeatureTreeMaxDepth::new).compute());
        treeData.addChild(
                fmComputation.map(ComputeFeatureTreeNumberOfBranches::new).compute());
        treeData.addChild(
                fmComputation.map(ComputeFeatureTreeNumberOfLeaves::new).compute());
        treeData.addChild(
                fmComputation.map(ComputeFeatureTreeNumberOfTopNodes::new).compute());
        treeData.addChild(
                fmComputation.map(ComputeFeatureTreeNumberOfGroups::new).compute());
        DataTree<?> constraintData = DataTree.of("CrossTreeConstraints");
        data.addChild(constraintData);
        constraintData.addChild(
                fmComputation.map(ComputeConstraintNumberOfAtoms::new).compute());
        constraintData.addChild(fmComputation
                .map(ComputeConstraintNumberOfDistinctVariables::new)
                .compute());
        constraintData.addChild(
                fmComputation.map(ComputeConstraintNumberOfConnectives::new).compute());
        return data;
    }

    /**
     * Prints the statistics from calling {@link #statistics()} to the console.
     */
    public void printStatistics() {
        FeatJAR.log().message(new DataTreeTextFormat().serialize(statistics()).get());
    }

    /**
     * Slices a feature model removing all features from the given list.
     *
     * @param featuresToRemove the features to remove
     * @return a new instance of a sliced feature model
     */
    public Result<IFeatureModel> slice(List<String> featuresToRemove) {
        return fmComputation
                .map(ComputeFeatureModelSlice::new)
                .set(
                        ComputeFeatureModelSlice.EXCLUDE_FEATURES,
                        IFeatureModelElementFilter.featuresByName(featuresToRemove))
                .computeResult();
    }

    /**
     * Slices a feature model keeping all features from the given list.
     *
     * @param featuresToKeep the features to keep
     * @return a new instance of a sliced feature model
     */
    public Result<IFeatureModel> project(List<String> featuresToKeep) {
        return fmComputation
                .map(ComputeFeatureModelSlice::new)
                .set(
                        ComputeFeatureModelSlice.INCLUDE_FEATURES,
                        IFeatureModelElementFilter.featuresByName(featuresToKeep))
                .computeResult();
    }

    /**
     * {@return an equivalent propositional formula from the feature model}
     */
    public Result<IFormula> toFormula() {
        return fmComputation.map(ComputeFormula::new).computeResult();
    }

    /**
     * {@return an equivalent propositional formula in CNF from the feature model}
     */
    public Result<IFormula> toCNF() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .computeResult();
    }

    /**
     * {@return the equivalent propositional formula in CNF from the feature model in the internal boolean assignment representation}
     */
    public Result<BooleanAssignmentList> toClauseList() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .computeResult();
    }

    /**
     * {@return a Sat4J solver using an equivalent propositional formula from the feature model}
     */
    public Result<SAT4JSolver> toSat4JSolver() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSAT4JSolver::new)
                .computeResult();
    }

    /**
     * {@return a list of atomic sets. Each element in the list consists of a list of positive (first) and negative features (second)}
     */
    public Result<List<Pair<List<String>, List<String>>>> atomicSets() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeAtomicSetsSAT4J::new)
                .map(ComputeConfigurationFromAssignment::new)
                .computeResult()
                .map(a -> a.stream()
                        .map(c -> new Pair<>(c.getSelected(), c.getDeselected()))
                        .collect(Collectors.toList()));
    }

    /**
     * {@return whether the feature model is void}
     */
    public Result<Boolean> isVoid() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSatisfiableSAT4J::new)
                .computeResult()
                .map(sat -> !sat);
    }

    /**
     * {@return whether the feature model is satisfiable}
     */
    public Result<Boolean> isSatisfiable() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSatisfiableSAT4J::new)
                .computeResult();
    }

    /**
     * {@return all core features of the feature model}
     */
    public Result<List<String>> core() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeCoreSAT4J::new)
                .map(ComputeConfigurationFromAssignment::new)
                .computeResult()
                .map(c -> c.get(0).getSelected());
    }

    /**
     * {@return all dead features of the feature model}
     */
    public Result<List<String>> dead() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeCoreSAT4J::new)
                .map(ComputeConfigurationFromAssignment::new)
                .computeResult()
                .map(c -> c.get(0).getDeselected());
    }

    /**
     * {@return converts a list of feature names to the corresponding feature objects from the feature model}
     */
    public List<IFeature> toFeature(List<String> names) {
        return names.stream().map(this::toFeature).collect(Collectors.toList());
    }

    /**
     * {@return converts a feature name to the corresponding feature object from the feature model}
     */
    public IFeature toFeature(String name) {
        return featureModel.getFeature(name).orElseThrow();
    }

    /**
     * {@return the list of redundant constraints of the feature model, if any}
     */
    public Result<List<IConstraint>> redundant() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return all valid configurations of the feature model}
     *
     * This method uses an inefficient Sat4J-based implementation. It may run into a timeout.
     */
    public Result<List<Configuration>> allConfigurations() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSolutionsSAT4J::new)
                .set(ComputeSolutionsSAT4J.SELECTION_STRATEGY, ISelectionStrategy.NonParameterStrategy.ORIGINAL)
                .set(ComputeSolutionsSAT4J.FORBID_DUPLICATES, Boolean.TRUE)
                .map(ComputeConfigurationFromAssignment::new)
                .computeResult();
    }

    /**
     * {@return a list of valid random configurations of the feature model}
     *
     * This method uses a Sat4J-based implementation and is **not** uniformly distributed.
     * The result may contain duplicate configurations.
     *
     * @param numberOfConfigurations the number of configuration in the final list
     * @param randomSeed a random seed. Using the same seed on the same feature model will result in the same list to be created
     */
    public Result<List<Configuration>> randomConfigurations(int numberOfConfigurations, long randomSeed) {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSolutionsSAT4J::new)
                .set(ComputeSolutionsSAT4J.SELECTION_STRATEGY, ISelectionStrategy.NonParameterStrategy.FAST_RANDOM)
                .set(ComputeSolutionsSAT4J.FORBID_DUPLICATES, Boolean.FALSE)
                .set(ComputeSolutionsSAT4J.LIMIT, numberOfConfigurations)
                .set(ComputeSolutionsSAT4J.RANDOM_SEED, randomSeed)
                .map(ComputeConfigurationFromAssignment::new)
                .computeResult();
    }

    /**
     * {@return a list of configurations with 100% t-wise interaction coverage}
     * This method is deterministic. The same feature model will always produce the same list of configurations.
     *
     * @param t the value of t
     */
    public Result<List<Configuration>> twiseConfigurations(int t) {
        ComputeBooleanClauseList clauseList = fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new);
        return clauseList
                .map(YASA::new)
                .set(YASA.ITERATIONS, 1)
                .set(
                        YASA.COMBINATION_SET,
                        clauseList
                                .map(VariableCombinationSpecificationComputation::new)
                                .set(VariableCombinationSpecificationComputation.T, t))
                .map(ComputeCompleteSample::new)
                .set(ComputeCompleteSample.BOOLEAN_CLAUSE_LIST, clauseList)
                .set(ComputeCompleteSample.SELECTION_STRATEGY, ISelectionStrategy.NonParameterStrategy.NEGATIVE)
                .map(ComputeConfigurationFromAssignment::new)
                .computeResult();
    }

    /**
     * {@return the number of valid configurations of the feature model}
     * This method uses the tool sharpsat. It may run into a timeout.
     */
    public Result<BigInteger> numberOfValidConfigurations() {
        return fmComputation
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeSolutionCountSharpSAT::new)
                .computeResult();
    }
}
