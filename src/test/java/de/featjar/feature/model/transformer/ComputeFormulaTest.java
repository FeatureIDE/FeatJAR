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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Attribute;
import de.featjar.base.data.Attributes;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureModel.IMutableFeatureModel;
import de.featjar.feature.model.IFeatureTree.IMutableFeatureTree;
import de.featjar.feature.model.constraints.AttributeAverage;
import de.featjar.feature.model.constraints.AttributeSum;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.IfThenElse;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.function.integer.IntegerDivide;
import de.featjar.formula.structure.term.value.Constant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * Test class for the variations of ComputeFormula. The simple translation of
 * cardinality features as well as the more complicated version is tested here.
 * Additionally the combination of attribute aggregates and feature
 * cardinalities.
 *
 * @author Klara Surmeier
 * @author Nermine Mansour
 * @author Malena Horstmann
 * @author Sebastian Krieter
 */
class ComputeFormulaTest {

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void deinit() {
        FeatJAR.deinitialize();
    }

    private void translateAndCompareFeatureModel(IFeatureModel featureModel, IFormula expected) {
        Result<IFormula> resultFormula =
                Computations.of(featureModel).map(ComputeFormula::new).computeResult();

        assertTrue(resultFormula.isPresent(), resultFormula.printProblems());
        assertEquals(Expressions.print(expected), Expressions.print(resultFormula.get()));
    }

    private IMutableFeatureTree below(
            IMutableFeatureModel featureModel, IMutableFeatureTree parentTreeNode, String name) {
        return parentTreeNode.addFeatureBelow(featureModel.addFeature(name)).mutate();
    }

    private static Literal positive(String name) {
        return new Literal(name);
    }

    private static Literal negative(String name) {
        return new Literal(false, name);
    }

    private static Implies implies(IFormula left, IFormula right) {
        return new Implies(left, right);
    }

    private static And and(IFormula... formulas) {
        return new And(formulas);
    }

    private static Or or(IFormula... formulas) {
        return new Or(formulas);
    }

    @Test
    void optionalRootIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.makeOptional();

        IMutableFeatureTree a = below(model, root, "A");
        IMutableFeatureTree b = below(model, root, "B");

        IFormula expected =
                new Reference(and(implies(positive("A"), positive("root")), implies(positive("B"), positive("root"))));

        translateAndCompareFeatureModel(model, expected);
    }

    @Test
    void crossTreeConstraintsIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.makeOptional();

        IMutableFeatureTree a = below(model, root, "A");
        IMutableFeatureTree b = below(model, root, "B");

        Not formula = new Not(
                new BiImplies(positive("A"), new Or(positive("B"), new Or(positive("root"), negative("root")))));
        model.addConstraint(formula);
        model.addConstraint(formula);

        IFormula expected = new Reference(and(
                implies(positive("A"), positive("root")), implies(positive("B"), positive("root")), formula, formula));

        translateAndCompareFeatureModel(model, expected);
    }

    @Test
    void andGroupIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.makeMandatory();
        root.toAndGroup();

        IMutableFeatureTree a = below(model, root, "A");
        a.toAndGroup();
        IMutableFeatureTree aa = below(model, a, "AA");
        aa.toAndGroup();
        IMutableFeatureTree ab = below(model, a, "AB");
        ab.toAndGroup();
        ab.makeMandatory();

        IMutableFeatureTree b = below(model, root, "B");
        b.toAndGroup();
        b.makeMandatory();
        IMutableFeatureTree ba = below(model, b, "BA");
        ba.toAndGroup();

        IMutableFeatureTree c = below(model, root, "C");
        c.toAndGroup();
        IMutableFeatureTree ca = below(model, c, "CA");
        ca.toAndGroup();
        IMutableFeatureTree cb = below(model, c, "CB");
        cb.toAndGroup();
        IMutableFeatureTree cba = below(model, cb, "CBA");
        cba.toAndGroup();
        cba.makeMandatory();
        IMutableFeatureTree cbb = below(model, cb, "CBB");
        cbb.toAndGroup();
        IMutableFeatureTree cbc = below(model, cb, "CBC");
        cbc.toAndGroup();
        IMutableFeatureTree cc = below(model, c, "CC");
        cc.toAndGroup();

        IFormula expected = new Reference(and(
                positive("root"),
                implies(positive("A"), positive("root")),
                implies(positive("B"), positive("root")),
                implies(positive("root"), positive("B")),
                implies(positive("C"), positive("root")),
                implies(positive("AA"), positive("A")),
                implies(positive("AB"), positive("A")),
                implies(positive("A"), positive("AB")),
                implies(positive("BA"), positive("B")),
                implies(positive("CA"), positive("C")),
                implies(positive("CB"), positive("C")),
                implies(positive("CC"), positive("C")),
                implies(positive("CBA"), positive("CB")),
                implies(positive("CB"), positive("CBA")),
                implies(positive("CBB"), positive("CB")),
                implies(positive("CBC"), positive("CB"))));

        translateAndCompareFeatureModel(model, expected);
    }

    @Test
    void featureCardinalityIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.makeOptional();
        root.toAndGroup();

        IMutableFeatureTree a = below(model, root, "A");
        a.toAndGroup();
        a.setFeatureCardinality(Range.of(0, 2));
        IMutableFeatureTree aa = below(model, a, "AA");
        IMutableFeatureTree ab = below(model, a, "AB");

        IFormula expected = new Reference(and(
                implies(positive("A_1"), positive("root")),
                implies(positive("A_2"), positive("A_1")),
                implies(positive("AA.A_1"), positive("A_1")),
                implies(positive("AB.A_1"), positive("A_1")),
                implies(positive("AA.A_2"), positive("A_2")),
                implies(positive("AB.A_2"), positive("A_2"))));

        translateAndCompareFeatureModel(model, expected);
    }

    @Test
    void nestedFeatureCardinalityIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.makeMandatory();
        root.toAndGroup();

        IMutableFeatureTree a = below(model, root, "A");
        a.toAndGroup();
        a.setFeatureCardinality(Range.of(0, 2));
        IMutableFeatureTree aa = below(model, a, "AA");
        IMutableFeatureTree ab = below(model, a, "AB");
        ab.makeMandatory();

        IMutableFeatureTree b = below(model, root, "B");

        IMutableFeatureTree c = below(model, root, "C");
        c.toAlternativeGroup();
        c.setFeatureCardinality(Range.of(1, 2));
        IMutableFeatureTree ca = below(model, c, "CA");
        IMutableFeatureTree cb = below(model, c, "CB");
        IMutableFeatureTree cba = below(model, cb, "CBA");
        cba.setFeatureCardinality(Range.of(3, 3));
        IMutableFeatureTree cbaa = below(model, cba, "CBAA");
        IMutableFeatureTree cbab = below(model, cba, "CBAB");

        IFormula expected = new Reference(and(
                positive("root"),
                implies(positive("A_1"), positive("root")),
                implies(positive("A_2"), positive("A_1")),
                implies(positive("B"), positive("root")),
                implies(positive("C_1"), positive("root")),
                implies(positive("C_2"), positive("C_1")),
                implies(positive("root"), positive("C_1")),
                implies(positive("AA.A_1"), positive("A_1")),
                implies(positive("AB.A_1"), positive("A_1")),
                implies(positive("A_1"), positive("AB.A_1")),
                implies(positive("AA.A_2"), positive("A_2")),
                implies(positive("AB.A_2"), positive("A_2")),
                implies(positive("A_2"), positive("AB.A_2")),
                implies(positive("CA.C_1"), positive("C_1")),
                implies(positive("CB.C_1"), positive("C_1")),
                implies(positive("C_1"), new Choose(1, positive("CA.C_1"), positive("CB.C_1"))),
                implies(positive("CBA_1.C_1"), positive("CB.C_1")),
                implies(positive("CBA_2.C_1"), positive("CBA_1.C_1")),
                implies(positive("CBA_3.C_1"), positive("CBA_2.C_1")),
                implies(positive("CB.C_1"), positive("CBA_1.C_1")),
                implies(positive("CB.C_1"), positive("CBA_2.C_1")),
                implies(positive("CB.C_1"), positive("CBA_3.C_1")),
                implies(positive("CBAA.CBA_1.C_1"), positive("CBA_1.C_1")),
                implies(positive("CBAB.CBA_1.C_1"), positive("CBA_1.C_1")),
                implies(positive("CBAA.CBA_2.C_1"), positive("CBA_2.C_1")),
                implies(positive("CBAB.CBA_2.C_1"), positive("CBA_2.C_1")),
                implies(positive("CBAA.CBA_3.C_1"), positive("CBA_3.C_1")),
                implies(positive("CBAB.CBA_3.C_1"), positive("CBA_3.C_1")),
                implies(positive("CA.C_2"), positive("C_2")),
                implies(positive("CB.C_2"), positive("C_2")),
                implies(positive("C_2"), new Choose(1, positive("CA.C_2"), positive("CB.C_2"))),
                implies(positive("CBA_1.C_2"), positive("CB.C_2")),
                implies(positive("CBA_2.C_2"), positive("CBA_1.C_2")),
                implies(positive("CBA_3.C_2"), positive("CBA_2.C_2")),
                implies(positive("CB.C_2"), positive("CBA_1.C_2")),
                implies(positive("CB.C_2"), positive("CBA_2.C_2")),
                implies(positive("CB.C_2"), positive("CBA_3.C_2")),
                implies(positive("CBAA.CBA_1.C_2"), positive("CBA_1.C_2")),
                implies(positive("CBAB.CBA_1.C_2"), positive("CBA_1.C_2")),
                implies(positive("CBAA.CBA_2.C_2"), positive("CBA_2.C_2")),
                implies(positive("CBAB.CBA_2.C_2"), positive("CBA_2.C_2")),
                implies(positive("CBAA.CBA_3.C_2"), positive("CBA_3.C_2")),
                implies(positive("CBAB.CBA_3.C_2"), positive("CBA_3.C_2"))));

        translateAndCompareFeatureModel(model, expected);
    }

    @Test
    void nestedFeatureCardinalityWithCrossTreeConstraintsIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();
        Attribute<Integer> xAttribute = Attributes.get("test", "x", Integer.class);
        Attribute<Integer> yAttribute = Attributes.get("test", "y", Integer.class);

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.makeMandatory();
        root.toAndGroup();

        IMutableFeatureTree a = below(model, root, "A");
        a.toAndGroup();
        a.setFeatureCardinality(Range.of(0, 2));
        a.getFeature().mutate().setAttributeValue(xAttribute, 1);
        IMutableFeatureTree aa = below(model, a, "AA");
        aa.getFeature().mutate().setAttributeValue(xAttribute, 2);
        IMutableFeatureTree ab = below(model, a, "AB");
        ab.makeMandatory();
        ab.getFeature().mutate().setAttributeValue(xAttribute, 3);

        IMutableFeatureTree b = below(model, root, "B");
        b.getFeature().mutate().setAttributeValue(xAttribute, 10);
        b.getFeature().mutate().setAttributeValue(yAttribute, 8);

        IMutableFeatureTree c = below(model, root, "C");
        c.toAlternativeGroup();
        c.setFeatureCardinality(Range.of(1, 2));
        c.getFeature().mutate().setAttributeValue(yAttribute, 7);
        IMutableFeatureTree ca = below(model, c, "CA");
        ca.getFeature().mutate().setAttributeValue(yAttribute, 5);
        IMutableFeatureTree cb = below(model, c, "CB");
        cb.getFeature().mutate().setAttributeValue(yAttribute, 4);
        IMutableFeatureTree cba = below(model, cb, "CBA");
        cba.setFeatureCardinality(Range.of(3, 3));
        cba.getFeature().mutate().setAttributeValue(yAttribute, 3);
        IMutableFeatureTree cbaa = below(model, cba, "CBAA");
        cbaa.getFeature().mutate().setAttributeValue(yAttribute, 2);
        IMutableFeatureTree cbab = below(model, cba, "CBAB");
        cbab.getFeature().mutate().setAttributeValue(yAttribute, 1);

        model.addConstraint(and(negative("AA"), negative("AB")));
        model.addConstraint(and(negative("AA"), negative("CBAB")));
        model.addConstraint(new Equals(new AttributeSum(xAttribute), new AttributeSum(yAttribute)));
        model.addConstraint(new LessThan(new AttributeAverage(xAttribute), new Constant(10L)));

        IFormula expected = new Reference(and(
                positive("root"),
                implies(positive("A_1"), positive("root")),
                implies(positive("A_2"), positive("A_1")),
                implies(positive("B"), positive("root")),
                implies(positive("C_1"), positive("root")),
                implies(positive("C_2"), positive("C_1")),
                implies(positive("root"), positive("C_1")),
                implies(positive("AA.A_1"), positive("A_1")),
                implies(positive("AB.A_1"), positive("A_1")),
                implies(positive("A_1"), positive("AB.A_1")),
                implies(positive("AA.A_2"), positive("A_2")),
                implies(positive("AB.A_2"), positive("A_2")),
                implies(positive("A_2"), positive("AB.A_2")),
                implies(positive("CA.C_1"), positive("C_1")),
                implies(positive("CB.C_1"), positive("C_1")),
                implies(positive("C_1"), new Choose(1, positive("CA.C_1"), positive("CB.C_1"))),
                implies(positive("CBA_1.C_1"), positive("CB.C_1")),
                implies(positive("CBA_2.C_1"), positive("CBA_1.C_1")),
                implies(positive("CBA_3.C_1"), positive("CBA_2.C_1")),
                implies(positive("CB.C_1"), positive("CBA_1.C_1")),
                implies(positive("CB.C_1"), positive("CBA_2.C_1")),
                implies(positive("CB.C_1"), positive("CBA_3.C_1")),
                implies(positive("CBAA.CBA_1.C_1"), positive("CBA_1.C_1")),
                implies(positive("CBAB.CBA_1.C_1"), positive("CBA_1.C_1")),
                implies(positive("CBAA.CBA_2.C_1"), positive("CBA_2.C_1")),
                implies(positive("CBAB.CBA_2.C_1"), positive("CBA_2.C_1")),
                implies(positive("CBAA.CBA_3.C_1"), positive("CBA_3.C_1")),
                implies(positive("CBAB.CBA_3.C_1"), positive("CBA_3.C_1")),
                implies(positive("CA.C_2"), positive("C_2")),
                implies(positive("CB.C_2"), positive("C_2")),
                implies(positive("C_2"), new Choose(1, positive("CA.C_2"), positive("CB.C_2"))),
                implies(positive("CBA_1.C_2"), positive("CB.C_2")),
                implies(positive("CBA_2.C_2"), positive("CBA_1.C_2")),
                implies(positive("CBA_3.C_2"), positive("CBA_2.C_2")),
                implies(positive("CB.C_2"), positive("CBA_1.C_2")),
                implies(positive("CB.C_2"), positive("CBA_2.C_2")),
                implies(positive("CB.C_2"), positive("CBA_3.C_2")),
                implies(positive("CBAA.CBA_1.C_2"), positive("CBA_1.C_2")),
                implies(positive("CBAB.CBA_1.C_2"), positive("CBA_1.C_2")),
                implies(positive("CBAA.CBA_2.C_2"), positive("CBA_2.C_2")),
                implies(positive("CBAB.CBA_2.C_2"), positive("CBA_2.C_2")),
                implies(positive("CBAA.CBA_3.C_2"), positive("CBA_3.C_2")),
                implies(positive("CBAB.CBA_3.C_2"), positive("CBA_3.C_2")),
                or(and(negative("AA.A_1"), negative("AB.A_1")), and(negative("AA.A_2"), negative("AB.A_2"))),
                or(
                        and(negative("AA.A_1"), negative("CBAB.CBA_1.C_1")),
                        and(negative("AA.A_1"), negative("CBAB.CBA_2.C_1")),
                        and(negative("AA.A_1"), negative("CBAB.CBA_3.C_1")),
                        and(negative("AA.A_1"), negative("CBAB.CBA_1.C_2")),
                        and(negative("AA.A_1"), negative("CBAB.CBA_2.C_2")),
                        and(negative("AA.A_1"), negative("CBAB.CBA_3.C_2")),
                        and(negative("AA.A_2"), negative("CBAB.CBA_1.C_1")),
                        and(negative("AA.A_2"), negative("CBAB.CBA_2.C_1")),
                        and(negative("AA.A_2"), negative("CBAB.CBA_3.C_1")),
                        and(negative("AA.A_2"), negative("CBAB.CBA_1.C_2")),
                        and(negative("AA.A_2"), negative("CBAB.CBA_2.C_2")),
                        and(negative("AA.A_2"), negative("CBAB.CBA_3.C_2"))),
                new Equals(
                        new IntegerAdd(
                                new IfThenElse(positive("A_1"), new Constant(1L), new Constant(0L)),
                                new IfThenElse(positive("A_2"), new Constant(1L), new Constant(0L)),
                                new IfThenElse(positive("AA.A_1"), new Constant(2L), new Constant(0L)),
                                new IfThenElse(positive("AA.A_2"), new Constant(2L), new Constant(0L)),
                                new IfThenElse(positive("AB.A_1"), new Constant(3L), new Constant(0L)),
                                new IfThenElse(positive("AB.A_2"), new Constant(3L), new Constant(0L)),
                                new IfThenElse(positive("B"), new Constant(10L), new Constant(0L))),
                        new IntegerAdd(
                                new IfThenElse(positive("B"), new Constant(8L), new Constant(0L)),
                                new IfThenElse(positive("C_1"), new Constant(7L), new Constant(0L)),
                                new IfThenElse(positive("C_2"), new Constant(7L), new Constant(0L)),
                                new IfThenElse(positive("CA.C_1"), new Constant(5L), new Constant(0L)),
                                new IfThenElse(positive("CA.C_2"), new Constant(5L), new Constant(0L)),
                                new IfThenElse(positive("CB.C_1"), new Constant(4L), new Constant(0L)),
                                new IfThenElse(positive("CB.C_2"), new Constant(4L), new Constant(0L)),
                                new IfThenElse(positive("CBA_1.C_1"), new Constant(3L), new Constant(0L)),
                                new IfThenElse(positive("CBA_2.C_1"), new Constant(3L), new Constant(0L)),
                                new IfThenElse(positive("CBA_3.C_1"), new Constant(3L), new Constant(0L)),
                                new IfThenElse(positive("CBA_1.C_2"), new Constant(3L), new Constant(0L)),
                                new IfThenElse(positive("CBA_2.C_2"), new Constant(3L), new Constant(0L)),
                                new IfThenElse(positive("CBA_3.C_2"), new Constant(3L), new Constant(0L)),
                                new IfThenElse(positive("CBAA.CBA_1.C_1"), new Constant(2L), new Constant(0L)),
                                new IfThenElse(positive("CBAA.CBA_2.C_1"), new Constant(2L), new Constant(0L)),
                                new IfThenElse(positive("CBAA.CBA_3.C_1"), new Constant(2L), new Constant(0L)),
                                new IfThenElse(positive("CBAA.CBA_1.C_2"), new Constant(2L), new Constant(0L)),
                                new IfThenElse(positive("CBAA.CBA_2.C_2"), new Constant(2L), new Constant(0L)),
                                new IfThenElse(positive("CBAA.CBA_3.C_2"), new Constant(2L), new Constant(0L)),
                                new IfThenElse(positive("CBAB.CBA_1.C_1"), new Constant(1L), new Constant(0L)),
                                new IfThenElse(positive("CBAB.CBA_2.C_1"), new Constant(1L), new Constant(0L)),
                                new IfThenElse(positive("CBAB.CBA_3.C_1"), new Constant(1L), new Constant(0L)),
                                new IfThenElse(positive("CBAB.CBA_1.C_2"), new Constant(1L), new Constant(0L)),
                                new IfThenElse(positive("CBAB.CBA_2.C_2"), new Constant(1L), new Constant(0L)),
                                new IfThenElse(positive("CBAB.CBA_3.C_2"), new Constant(1L), new Constant(0L)))),
                new LessThan(
                        new IntegerDivide(
                                new IntegerAdd(
                                        new IfThenElse(positive("A_1"), new Constant(1L), new Constant(0L)),
                                        new IfThenElse(positive("A_2"), new Constant(1L), new Constant(0L)),
                                        new IfThenElse(positive("AA.A_1"), new Constant(2L), new Constant(0L)),
                                        new IfThenElse(positive("AA.A_2"), new Constant(2L), new Constant(0L)),
                                        new IfThenElse(positive("AB.A_1"), new Constant(3L), new Constant(0L)),
                                        new IfThenElse(positive("AB.A_2"), new Constant(3L), new Constant(0L)),
                                        new IfThenElse(positive("B"), new Constant(10L), new Constant(0L))),
                                new IntegerAdd(
                                        new IfThenElse(positive("A_1"), new Constant(1L), new Constant(0L)),
                                        new IfThenElse(positive("A_2"), new Constant(1L), new Constant(0L)),
                                        new IfThenElse(positive("AA.A_1"), new Constant(1L), new Constant(0L)),
                                        new IfThenElse(positive("AA.A_2"), new Constant(1L), new Constant(0L)),
                                        new IfThenElse(positive("AB.A_1"), new Constant(1L), new Constant(0L)),
                                        new IfThenElse(positive("AB.A_2"), new Constant(1L), new Constant(0L)),
                                        new IfThenElse(positive("B"), new Constant(1L), new Constant(0L)))),
                        new Constant(10L))));

        translateAndCompareFeatureModel(model, expected);
    }

    @Test
    void orGroupIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.toOrGroup();
        root.makeMandatory();

        IMutableFeatureTree a = below(model, root, "A");
        a.toOrGroup();
        IMutableFeatureTree aa = below(model, a, "AA");
        aa.toOrGroup();
        IMutableFeatureTree ab = below(model, a, "AB");
        ab.toOrGroup();

        IMutableFeatureTree b = below(model, root, "B");
        b.toOrGroup();
        IMutableFeatureTree ba = below(model, b, "BA");
        ba.toOrGroup();

        IMutableFeatureTree c = below(model, root, "C");
        c.toOrGroup();
        IMutableFeatureTree ca = below(model, c, "CA");
        ca.toOrGroup();
        IMutableFeatureTree cb = below(model, c, "CB");
        cb.toOrGroup();
        IMutableFeatureTree cba = below(model, cb, "CBA");
        cba.toOrGroup();
        IMutableFeatureTree cbb = below(model, cb, "CBB");
        cbb.toOrGroup();
        IMutableFeatureTree cbc = below(model, cb, "CBC");
        cbc.toOrGroup();
        IMutableFeatureTree cc = below(model, c, "CC");
        cc.toOrGroup();

        IFormula expected = new Reference(new And(
                positive("root"),
                implies(positive("A"), positive("root")),
                implies(positive("B"), positive("root")),
                implies(positive("C"), positive("root")),
                implies(positive("root"), or(positive("A"), positive("B"), positive("C"))),
                implies(positive("AA"), positive("A")),
                implies(positive("AB"), positive("A")),
                implies(positive("A"), or(positive("AA"), positive("AB"))),
                implies(positive("BA"), positive("B")),
                implies(positive("B"), or(positive("BA"))),
                implies(positive("CA"), positive("C")),
                implies(positive("CB"), positive("C")),
                implies(positive("CC"), positive("C")),
                implies(positive("C"), or(positive("CA"), positive("CB"), positive("CC"))),
                implies(positive("CBA"), positive("CB")),
                implies(positive("CBB"), positive("CB")),
                implies(positive("CBC"), positive("CB")),
                implies(positive("CB"), or(positive("CBA"), positive("CBB"), positive("CBC")))));

        translateAndCompareFeatureModel(model, expected);
    }

    @Test
    void alternativeGroupIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.toAlternativeGroup();
        root.makeMandatory();

        IMutableFeatureTree a = below(model, root, "A");
        a.toAlternativeGroup();
        IMutableFeatureTree aa = below(model, a, "AA");
        aa.toAlternativeGroup();
        IMutableFeatureTree ab = below(model, a, "AB");
        ab.toAlternativeGroup();

        IMutableFeatureTree b = below(model, root, "B");
        b.toAlternativeGroup();
        IMutableFeatureTree ba = below(model, b, "BA");
        ba.toAlternativeGroup();

        IMutableFeatureTree c = below(model, root, "C");
        c.toAlternativeGroup();
        IMutableFeatureTree ca = below(model, c, "CA");
        ca.toAlternativeGroup();
        IMutableFeatureTree cb = below(model, c, "CB");
        cb.toAlternativeGroup();
        IMutableFeatureTree cba = below(model, cb, "CBA");
        cba.toAlternativeGroup();
        IMutableFeatureTree cbb = below(model, cb, "CBB");
        cbb.toAlternativeGroup();
        IMutableFeatureTree cbc = below(model, cb, "CBC");
        cbc.toAlternativeGroup();
        IMutableFeatureTree cc = below(model, c, "CC");
        cc.toAlternativeGroup();

        IFormula expected = new Reference(new And(
                positive("root"),
                implies(positive("A"), positive("root")),
                implies(positive("B"), positive("root")),
                implies(positive("C"), positive("root")),
                implies(positive("root"), new Choose(1, positive("A"), positive("B"), positive("C"))),
                implies(positive("AA"), positive("A")),
                implies(positive("AB"), positive("A")),
                implies(positive("A"), new Choose(1, positive("AA"), positive("AB"))),
                implies(positive("BA"), positive("B")),
                implies(positive("B"), new Choose(1, positive("BA"))),
                implies(positive("CA"), positive("C")),
                implies(positive("CB"), positive("C")),
                implies(positive("CC"), positive("C")),
                implies(positive("C"), new Choose(1, positive("CA"), positive("CB"), positive("CC"))),
                implies(positive("CBA"), positive("CB")),
                implies(positive("CBB"), positive("CB")),
                implies(positive("CBC"), positive("CB")),
                implies(positive("CB"), new Choose(1, positive("CBA"), positive("CBB"), positive("CBC")))));

        translateAndCompareFeatureModel(model, expected);
    }

    @Test
    void cardinalityGroupIsCorrectlyTranslated() {
        IMutableFeatureModel model = new FeatureModel(Identifiers.newCounterIdentifier()).mutate();

        IMutableFeatureTree root =
                model.addFeatureTreeRoot(model.addFeature("root")).mutate();
        root.toCardinalityGroup(0, 2);
        root.makeMandatory();

        IMutableFeatureTree a = below(model, root, "A");
        a.toCardinalityGroup(1, 3);
        IMutableFeatureTree aa = below(model, a, "AA");
        aa.toCardinalityGroup(0, 2);
        IMutableFeatureTree ab = below(model, a, "AB");
        ab.toCardinalityGroup(0, 2);

        IMutableFeatureTree b = below(model, root, "B");
        b.toCardinalityGroup(1, 1);
        IMutableFeatureTree ba = below(model, b, "BA");
        ba.toCardinalityGroup(0, 2);

        IMutableFeatureTree c = below(model, root, "C");
        c.toCardinalityGroup(0, 0);
        IMutableFeatureTree ca = below(model, c, "CA");
        ca.toCardinalityGroup(0, 2);
        IMutableFeatureTree cb = below(model, c, "CB");
        cb.toCardinalityGroup(3, Range.OPEN);
        IMutableFeatureTree cba = below(model, cb, "CBA");
        cba.toCardinalityGroup(0, 2);
        IMutableFeatureTree cbb = below(model, cb, "CBB");
        cbb.toCardinalityGroup(0, 2);
        IMutableFeatureTree cbc = below(model, cb, "CBC");
        cbc.toCardinalityGroup(0, 2);
        IMutableFeatureTree cc = below(model, c, "CC");
        cc.toCardinalityGroup(0, 2);

        IFormula expected = new Reference(new And(
                positive("root"),
                implies(positive("A"), positive("root")),
                implies(positive("B"), positive("root")),
                implies(positive("C"), positive("root")),
                implies(positive("root"), new AtMost(2, positive("A"), positive("B"), positive("C"))),
                implies(positive("AA"), positive("A")),
                implies(positive("AB"), positive("A")),
                implies(positive("A"), new Between(1, 3, positive("AA"), positive("AB"))),
                implies(positive("BA"), positive("B")),
                implies(positive("B"), new Choose(1, positive("BA"))),
                implies(positive("CA"), positive("C")),
                implies(positive("CB"), positive("C")),
                implies(positive("CC"), positive("C")),
                implies(positive("C"), new AtMost(0, positive("CA"), positive("CB"), positive("CC"))),
                implies(positive("CBA"), positive("CB")),
                implies(positive("CBB"), positive("CB")),
                implies(positive("CBC"), positive("CB")),
                implies(positive("CB"), new AtLeast(3, positive("CBA"), positive("CBB"), positive("CBC")))));

        translateAndCompareFeatureModel(model, expected);
    }
}
