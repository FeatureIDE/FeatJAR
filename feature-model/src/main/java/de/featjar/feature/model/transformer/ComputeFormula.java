/*
 * Copyright (C) 2026 FeatJAR-Development-Team
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

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.base.tree.visitor.ITreeVisitor.TraversalAction;
import de.featjar.base.tree.visitor.PostOrderVisitor;
import de.featjar.base.tree.visitor.TreeNodeReplacer;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.constraints.IAttributeAggregate;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.ILiteral;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Transforms a feature model into a boolean formula. Supports a simple way of
 * transforming cardinality features and a more complicated transformation.
 *
 * @author Klara Surmeier
 * @author Nermine Mansour
 * @author Malena Horstmann
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 * @author Sebastian Krieter
 */
public class ComputeFormula extends AComputation<IFormula> {
    protected static final Dependency<IFeatureModel> FEATURE_MODEL = Dependency.newDependency(IFeatureModel.class);

    private IFeatureModel featureModel;
    private FeatureToFormula featureToFormula;
    private ArrayList<IFormula> constraints;

    public ComputeFormula(IComputation<IFeatureModel> featureModel) {
        super(featureModel);
    }

    protected ComputeFormula(ComputeFormula other) {
        super(other);
    }

    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        featureModel = FEATURE_MODEL.get(dependencyList);
        featureToFormula = new FeatureToFormula();
        constraints = new ArrayList<>();

        featureToFormula.initFeatureNames(featureModel.getFeatures());

        createTreeConstraints();
        createCrossTreeConstraints();

        return Result.of(new Reference(new And(constraints), featureToFormula.getVariables()));
    }

    private void createTreeConstraints() {
        createTreeChildrenConstraints(featureModel.getPseudoRoot(), null, new ArrayDeque<>());
    }

    /**
     * Recursively traverses a feature tree with cardinality features and adds the
     * tree constraints for every node.
     *
     * @param parentNode       from which to start the traversal
     * @param parentName       name of the parent
     * @param cardinalityNames name of the parents that are cardinality features
     */
    private void createTreeChildrenConstraints(
            IFeatureTree parentNode, String parentName, ArrayDeque<String> cardinalityNames) {
        IFormula parentLiteral = parentName == null ? True.INSTANCE : featureToFormula.getFeatureFormula(parentName);

        String cardinalityPrefix =
                cardinalityNames.isEmpty() ? null : cardinalityNames.stream().collect(Collectors.joining("."));

        for (IFeatureTree child : parentNode.getChildren()) {
            int upperBound = child.getFeatureCardinalityUpperBound();
            int lowerBound = child.getFeatureCardinalityLowerBound();
            IFeature feature = child.getFeature();
            String featureName = getFeatureName(child);

            if (upperBound > 1) {
                IFormula previousLiteral = null;

                for (int i = 1; i <= upperBound; i++) {
                    String featureNameInstance = getFeatureName(featureName, cardinalityPrefix, i);

                    IFormula currentLiteral = featureToFormula.createFeatureFormula(feature, featureNameInstance);
                    if (parentLiteral != True.INSTANCE && i == 1) {
                        constraints.add(new Implies(currentLiteral, parentLiteral));
                    }
                    if (previousLiteral != null) {
                        constraints.add(new Implies(currentLiteral, previousLiteral));
                    }
                    previousLiteral = currentLiteral;
                }

                for (int i = 1; i <= lowerBound; i++) {
                    IFormula featureNameInstance =
                            featureToFormula.getFeatureFormula(getFeatureName(featureName, cardinalityPrefix, i));
                    if (parentLiteral == True.INSTANCE) {
                        constraints.add(featureNameInstance);
                    } else {
                        constraints.add(new Implies(parentLiteral, featureNameInstance));
                    }
                }
            } else {
                String featureNameInstance = getFeatureName(featureName, cardinalityPrefix, 0);

                IFormula currentLiteral = featureToFormula.createFeatureFormula(feature, featureNameInstance);
                if (parentLiteral != True.INSTANCE) {
                    constraints.add(new Implies(currentLiteral, parentLiteral));
                }
                if (lowerBound > 0) {
                    if (parentLiteral == True.INSTANCE) {
                        constraints.add(currentLiteral);
                    } else {
                        constraints.add(new Implies(parentLiteral, currentLiteral));
                    }
                }
            }
        }

        handleGroups(parentLiteral, parentNode, cardinalityPrefix);

        for (IFeatureTree child : parentNode.getChildren()) {
            int upperBound = child.getFeatureCardinalityUpperBound();
            String featureName = getFeatureName(child);

            if (upperBound > 1) {
                for (int i = 1; i <= upperBound; i++) {
                    String featureNameInstance = getFeatureName(featureName, cardinalityPrefix, i);
                    cardinalityNames.addFirst(getFeatureName(featureName, null, i));
                    createTreeChildrenConstraints(child, featureNameInstance, cardinalityNames);
                    cardinalityNames.removeFirst();
                }
            } else {
                String featureNameInstance = getFeatureName(featureName, cardinalityPrefix, 0);
                createTreeChildrenConstraints(child, featureNameInstance, cardinalityNames);
            }
        }
    }

    private String getFeatureName(String featureName, String cardinalitySufix, int instanceIndex) {
        return featureName
                + ((instanceIndex > 0) ? "_" + instanceIndex : "")
                + ((cardinalitySufix != null) ? "." + cardinalitySufix : "");
    }

    /**
     * Adds group constraints (or, alternative, cardinality) for a given node.
     *
     * @param parentLiteral literal name of the node
     * @param parentNode
     * @param prefix        the name prefix for feature underneath a cardinality
     *                      feature
     */
    private void handleGroups(IFormula parentLiteral, IFeatureTree parentNode, String prefix) {
        for (Pair<Group, List<IFeatureTree>> featureGroup : parentNode.getGroupedChildren()) {
            Group group = featureGroup.getKey();
            if (group != null && !group.isAnd()) {
                ArrayList<IFormula> groupLiterals =
                        new ArrayList<>(featureGroup.getValue().size());
                for (IFeatureTree childNode : featureGroup.getValue()) {
                    String featureName = getFeatureName(childNode);
                    int upperBound = childNode.getFeatureCardinalityUpperBound();
                    if (upperBound > 1) {
                        for (int i = 1; i <= upperBound; i++) {
                            groupLiterals.add(
                                    featureToFormula.getFeatureFormula(getFeatureName(featureName, prefix, i)));
                        }
                    } else {
                        groupLiterals.add(featureToFormula.getFeatureFormula(getFeatureName(featureName, prefix, 0)));
                    }
                }

                if (group.isOr()) {
                    constraints.add(new Implies(parentLiteral, new Or(groupLiterals)));
                } else if (group.isAlternative()) {
                    constraints.add(new Implies(parentLiteral, new Choose(1, groupLiterals)));
                } else {
                    int lowerBound = group.getLowerBound();
                    int upperBound = group.getUpperBound();
                    if (lowerBound > 0) {
                        if (upperBound != Range.OPEN) {
                            constraints.add(
                                    new Implies(parentLiteral, new Between(lowerBound, upperBound, groupLiterals)));
                        } else {
                            constraints.add(new Implies(parentLiteral, new AtLeast(lowerBound, groupLiterals)));
                        }
                    } else {
                        if (upperBound != Range.OPEN) {
                            constraints.add(new Implies(parentLiteral, new AtMost(upperBound, groupLiterals)));
                        }
                    }
                }
            }
        }
    }

    private String getFeatureName(IFeatureTree node) {
        return node.getFeature().getName().orElse("???");
    }

    private class CardinalityExpander {
        private IFormula formula;
        private HashMap<String, Variable> variableNames;
        private HashMap<Variable, Variable> variableRemap;
        private List<ILiteral> cardinalityParents;
        private List<String> cardinalityParentNames;
        private HashMap<String, List<String>> cardinalityMap;
        private ArrayList<IFormula> expandedFormulas;
        private ArrayList<List<ILiteral>> conditionList;

        private CardinalityExpander(
                IFormula formula,
                HashMap<String, Variable> variableNames,
                HashMap<String, List<String>> cardinalityMap) {
            this.formula = formula;
            this.variableNames = variableNames;
            this.cardinalityMap = cardinalityMap;
            expandedFormulas = new ArrayList<>();
            conditionList = new ArrayList<>();
            cardinalityParentNames = new ArrayList<>(cardinalityMap.size());
            cardinalityParents = new ArrayList<>(cardinalityMap.size());
            for (Entry<String, List<String>> cardinalityEntry : cardinalityMap.entrySet()) {
                cardinalityParentNames.add(cardinalityEntry.getKey());
                cardinalityParents.add(null);
            }
            variableRemap = new HashMap<>();
            for (Entry<String, Variable> variableEntry : variableNames.entrySet()) {
                variableRemap.put(variableEntry.getValue(), variableEntry.getValue());
            }
        }

        public Pair<ArrayList<List<ILiteral>>, ArrayList<IFormula>> expand() {
            combineRecursive(0);
            return new Pair<>(conditionList, expandedFormulas);
        }

        private void combineRecursive(int depth) {
            if (cardinalityParentNames.size() <= depth) {
                IFormula clonedFormula = Trees.clone(formula);
                Trees.traverse(
                        clonedFormula,
                        new TreeNodeReplacer<>(n -> (n instanceof Variable) ? variableRemap.get(n) : null));
                conditionList.add(new ArrayList<>(cardinalityParents));
                expandedFormulas.add(clonedFormula);
                return;
            }

            String cardinalityParentName = cardinalityParentNames.get(depth);

            List<String> cardinalityFeatureNames = cardinalityMap.get(cardinalityParentName);
            List<String> parentNames = featureToFormula.getNamesPerFeature(cardinalityParentName);

            for (int i = 0; i < parentNames.size(); i++) {
                cardinalityParents.set(depth, featureToFormula.getFeatureFormula(parentNames.get(i)));

                for (String cardinalityFeatureName : cardinalityFeatureNames) {
                    String featureName = featureToFormula
                            .getNamesPerFeature(cardinalityFeatureName)
                            .get(i);

                    Variable orgVariable = variableNames.get(cardinalityFeatureName);
                    Variable newVariable =
                            featureToFormula.getFeatureFormula(featureName).getVariable();
                    variableRemap.put(orgVariable, newVariable);
                }
                combineRecursive(depth + 1);
            }
        }
    }

    private void createCrossTreeConstraints() {
        for (IConstraint constraint : featureModel.getConstraints()) {
            HashMap<String, List<String>> cardinalityMap = new HashMap<>();
            HashMap<String, Variable> variables = new HashMap<>();
            IFormula formula = constraint.getFormula();
            formula.getVariableStream().distinct().forEach(v -> {
                String name = v.getName();
                variables.put(name, v);
                featureModel
                        .getFeatureTreeNodeStream(name)
                        .flatMap(IFeatureTree::pathToRoot)
                        .filter(IFeatureTree::isMultiple)
                        .findFirst()
                        .map(this::getFeatureName)
                        .ifPresent(firstCardinalityParentName -> cardinalityMap
                                .computeIfAbsent(firstCardinalityParentName, k -> new ArrayList<>())
                                .add(name));
            });

            IFormula clonedFormula = Trees.clone(formula);
            Trees.traverse(clonedFormula, new PostOrderVisitor<>(this::translateAggregates));

            if (cardinalityMap.isEmpty()) {
                constraints.add(clonedFormula);
            } else {
                Pair<ArrayList<List<ILiteral>>, ArrayList<IFormula>> expandedFormulas =
                        new CardinalityExpander(clonedFormula, variables, cardinalityMap).expand();
                constraints.add(new Or(expandedFormulas.getSecond()));
            }
        }
    }

    private TraversalAction translateAggregates(List<IFormula> path) {
        final IExpression expression = ITreeVisitor.getCurrentNode(path);

        if (expression instanceof IAttributeAggregate) {
            final Result<IFormula> parent = ITreeVisitor.getParentNode(path);
            if (parent.isEmpty()) {
                return TraversalAction.FAIL;
            }
            Result<IExpression> result =
                    ((IAttributeAggregate) expression).translate(featureModel.getFeatures(), featureToFormula);
            if (result.isEmpty()) {
                return TraversalAction.FAIL;
            }
            parent.get().replaceChild(expression, result.get());
        }
        return TraversalAction.CONTINUE;
    }
}
