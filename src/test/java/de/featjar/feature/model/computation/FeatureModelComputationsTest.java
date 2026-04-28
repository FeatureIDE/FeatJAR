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
package de.featjar.feature.model.computation;

import static de.featjar.formula.structure.Expressions.constant;
import static de.featjar.formula.structure.Expressions.integerAdd;
import static de.featjar.formula.structure.Expressions.variable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.tree.DataTree;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree.IMutableFeatureTree;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.value.Constant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FeatureModelComputationsTest {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @SuppressWarnings("unused")
    public IFeatureModel createFeatureModel() {
        FeatureModel featureModel = new FeatureModel();
        IMutableFeatureTree root = featureModel
                .mutate()
                .addFeatureTreeRoot(featureModel.addFeature("root"))
                .mutate();

        // add Features (7)
        root.addFeatureBelow(featureModel.addFeature("a"));
        root.addFeatureBelow(featureModel.addFeature("b"));
        IMutableFeatureTree c =
                root.addFeatureBelow(featureModel.addFeature("c")).mutate();
        IMutableFeatureTree i = c.addFeatureBelow(featureModel.addFeature("i")).mutate();
        c.addFeatureBelow(featureModel.addFeature("k"));
        c.addFeatureBelow(featureModel.addFeature("o"));
        i.addFeatureBelow(featureModel.addFeature("x"));

        // define Features as literals
        Literal literalA = new Literal("a");
        Literal literalB = new Literal("b");
        Literal literalC = new Literal("c");
        Literal literalI = new Literal("i");
        Literal literalK = new Literal("k");
        Literal literalO = new Literal("o");
        Literal literalX = new Literal("x");

        // set some variables or literals
        literalO.setPositive(true);
        literalB.setPositive(false);

        // define terms
        ITerm termAdd = integerAdd(constant(42L), variable("a", Long.class));
        ITerm termAddLiteral = integerAdd(constant(42L), new Constant(2L));

        IFormula formula1 = new And(
                literalA,
                new Or(literalA, literalB, literalI),
                new Not(literalB),
                new Implies(literalK, literalO),
                new And(literalB),
                True.INSTANCE);

        IFormula formula2 = new Or(new Implies(formula1, literalO));
        IFormula formula3 = new Equals(termAdd, termAddLiteral);

        // add full formulas as constraints
        featureModel.addConstraint(formula1);
        featureModel.addConstraint(formula2);
        featureModel.addConstraint(formula3);
        return featureModel;
    }

    @Test
    public void constraintNumberOfAtomsIsCorrectlyComputed() {
        DataTree<Long> compute = Computations.of(createFeatureModel())
                .map(ComputeConstraintNumberOfAtoms::new)
                .compute();

        assertEquals(23L, compute.getValue().orElseThrow());
        assertEquals(3, compute.getChildren().size());
        assertEquals(9L, compute.getChildren().get(0).getValue().orElseThrow());
        assertEquals(10L, compute.getChildren().get(1).getValue().orElseThrow());
        assertEquals(4L, compute.getChildren().get(2).getValue().orElseThrow());
    }

    @Test
    public void constraintNumberOfConnectivesIsCorrectlyComputed() {
        DataTree<Long> compute = Computations.of(createFeatureModel())
                .map(ComputeConstraintNumberOfConnectives::new)
                .compute();

        assertEquals(12L, compute.getValue().orElseThrow());
        assertEquals(3, compute.getChildren().size());
        assertEquals(5L, compute.getChildren().get(0).getValue().orElseThrow());
        assertEquals(7L, compute.getChildren().get(1).getValue().orElseThrow());
        assertEquals(0L, compute.getChildren().get(2).getValue().orElseThrow());
    }

    @Test
    public void constraintNumberOfDistinctVariablesIsCorrectlyComputed() {
        DataTree<Long> compute = Computations.of(createFeatureModel())
                .map(ComputeConstraintNumberOfDistinctVariables::new)
                .compute();

        assertEquals(5L, compute.getValue().orElseThrow());
        assertEquals(3, compute.getChildren().size());
        assertEquals(5L, compute.getChildren().get(0).getValue().orElseThrow());
        assertEquals(5L, compute.getChildren().get(1).getValue().orElseThrow());
        assertEquals(1L, compute.getChildren().get(2).getValue().orElseThrow());
    }

    @Test
    public void featureTreeMaxDepthIsCorrectlyComputed() {
        DataTree<Integer> compute = Computations.of(createFeatureModel())
                .map(ComputeFeatureTreeMaxDepth::new)
                .compute();

        assertEquals(4, compute.getValue().orElseThrow());
        assertEquals(0, compute.getChildren().size());
    }

    @Test
    public void featureTreeNumberOfBranchesIsCorrectlyComputed() {
        DataTree<Double> compute = Computations.of(createFeatureModel())
                .map(ComputeFeatureTreeNumberOfBranches::new)
                .compute();

        assertEquals(2.0 + (1.0 / 3.0), compute.getValue().orElseThrow());
        assertEquals(0, compute.getChildren().size());
    }

    @Test
    public void featureTreeNumberOfGroupsIsCorrectlyComputed() {
        DataTree<Long> compute = Computations.of(createFeatureModel())
                .map(ComputeFeatureTreeNumberOfGroups::new)
                .compute();

        assertEquals(3L, compute.getValue().orElseThrow());
        assertEquals(1, compute.getChildren().size());
        assertEquals(3L, compute.getChildren().get(0).getValue().orElseThrow());
    }

    @Test
    public void featureTreeNumberOfLeavesIsCorrectlyComputed() {
        DataTree<Long> compute = Computations.of(createFeatureModel())
                .map(ComputeFeatureTreeNumberOfLeaves::new)
                .compute();

        assertEquals(5L, compute.getValue().orElseThrow());
        assertEquals(0, compute.getChildren().size());
    }

    @Test
    public void featureTreeNumberOfTopNodesIsCorrectlyComputed() {
        DataTree<Long> compute = Computations.of(createFeatureModel())
                .map(ComputeFeatureTreeNumberOfTopNodes::new)
                .compute();

        assertEquals(3L, compute.getValue().orElseThrow());
        assertEquals(0, compute.getChildren().size());
    }
}
