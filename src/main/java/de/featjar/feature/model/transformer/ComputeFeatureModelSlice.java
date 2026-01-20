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
package de.featjar.feature.model.transformer;

import de.featjar.analysis.sat4j.slice.CNFSlicer;
import de.featjar.analysis.sat4j.solver.SAT4JClauseList;
import de.featjar.analysis.sat4j.solver.SAT4JSolutionSolver;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureModelElement;
import de.featjar.feature.model.IFeatureModelElementFilter;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.PseudoFeatureTreeRoot;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Slices a feature model while preserving as much of its hierarchy and cross-tree constrains as possible.
 *
 * @author Sebastian Krieter
 */
public class ComputeFeatureModelSlice extends AComputation<IFeatureModel> {

    public static final Dependency<IFeatureModel> FEATURE_MODEL = Dependency.newDependency(IFeatureModel.class);
    public static final Dependency<IFeatureModelElementFilter> INCLUDE_FEATURES =
            Dependency.newDependency(IFeatureModelElementFilter.class);
    public static final Dependency<IFeatureModelElementFilter> EXCLUDE_FEATURES =
            Dependency.newDependency(IFeatureModelElementFilter.class);

    public ComputeFeatureModelSlice(IComputation<IFeatureModel> formula) {
        super(
                formula,
                Computations.of(IFeatureModelElementFilter.ALL),
                Computations.of(IFeatureModelElementFilter.NONE));
    }

    protected ComputeFeatureModelSlice(ComputeFormula other) {
        super(other);
    }

    @Override
    public Result<IFeatureModel> compute(List<Object> dependencyList, Progress progress) {
        IFeatureModel featureModel = FEATURE_MODEL.get(dependencyList);
        IFeatureModelElementFilter include = INCLUDE_FEATURES.get(dependencyList);
        IFeatureModelElementFilter exclude = EXCLUDE_FEATURES.get(dependencyList);

        Predicate<IFeatureModelElement> featureFilter = include.and(exclude.negate());

        BooleanAssignmentList cnf = Computations.of(featureModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .compute();

        int[] literalsToKeep = featureModel.getFeatures().stream()
                .filter(featureFilter)
                .map(IFeature::getName)
                .map(Result::get)
                .map(cnf.getVariableMap()::get)
                .filter(Result::isPresent)
                .mapToInt(Result::get)
                .toArray();

        BooleanAssignmentList slicedCnf = Computations.of(cnf)
                .map(CNFSlicer::new)
                .set(CNFSlicer.VARIABLES_TO_KEEP, new BooleanAssignment(literalsToKeep))
                .compute();

        IFeatureModel slicedModel = featureModel.clone();

        List<IFeatureTree> newRoots = new ArrayList<>(slicedModel.getRoots().size());
        for (IFeatureTree rootFeature : slicedModel.getRoots()) {
            PseudoFeatureTreeRoot pseudoRoot = new PseudoFeatureTreeRoot(slicedModel);
            pseudoRoot.addChild(rootFeature);
            pseudoRoot.postOrderStream().forEach(node -> {
                if (!featureFilter.test(node.getFeature())) {
                    node.mutate().removeFromTree();
                }
            });
            newRoots.addAll(pseudoRoot.detach());
        }

        Collection<IConstraint> constraints = new ArrayList<>(slicedModel.getConstraints());
        for (IConstraint constraint : constraints) {
            if (!constraint.getReferencedFeatures().stream().allMatch(featureFilter)) {
                slicedModel.mutate().removeConstraint(constraint);
            }
        }

        BooleanAssignmentList newCnf = Computations.of(slicedModel)
                .map(ComputeFormula::new)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .compute();

        SAT4JSolutionSolver solver = new SAT4JSolutionSolver(newCnf, false);
        SAT4JClauseList clauseList = solver.getClauseList();
        clauseList.addAll(newCnf);

        for (BooleanAssignment disjunction : slicedCnf.getAll()) {
            List<IFormula> clause = new ArrayList<>();
            for (int literal : disjunction.get()) {
                if (literal < 0) {
                    clause.add(new Literal(
                            false, cnf.getVariableMap().get(-literal).get()));
                } else {
                    clause.add(
                            new Literal(true, cnf.getVariableMap().get(literal).get()));
                }
            }
            if (solver.hasSolution(disjunction.negateInts()).orElse(Boolean.TRUE)) {
                clauseList.add(disjunction);
                slicedModel.mutate().addConstraint(new Or(clause));
            }
        }
        return Result.of(slicedModel);
    }
}
