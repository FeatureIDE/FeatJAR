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
package de.featjar.feature.model;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NonBooleanLiteral;
import de.featjar.formula.structure.term.value.Variable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 */
public class TranslateFormulaTest {

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void deinit() {
        FeatJAR.deinitialize();
    }

    @Test
    public void testInteger() {
        IFeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        addValues(featureModel, Integer.class);

        IFormula result = Computations.of(featureModel).map(ComputeFormula::new).compute();
        IFormula formula = buildFormula(Integer.class);
        Assertions.assertEquals(Expressions.print(formula), Expressions.print(result));
    }

    @Test
    public void testBoolean() {
        IFeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        addValues(featureModel, Boolean.class);

        IFormula result = Computations.of(featureModel).map(ComputeFormula::new).compute();
        IFormula formula = buildBooleanForumla();
        Assertions.assertEquals(Expressions.print(formula), Expressions.print(result));
    }

    @Test
    public void testFloat() {
        IFeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        addValues(featureModel, Float.class);

        IFormula result = Computations.of(featureModel).map(ComputeFormula::new).compute();
        IFormula formula = buildFormula(Float.class);
        Assertions.assertEquals(Expressions.print(formula), Expressions.print(result));
    }

    private void addValues(IFeatureModel featureModel, Class<?> type) {
        IFeature root = featureModel.mutate().addFeature("root");
        IFeatureTree rootTree = featureModel.mutate().addFeatureTreeRoot(root);
        rootTree.getFeatureTreeRoot().mutate().toAndGroup();
        for (short i = 0; i < 5; i++) {
            IFeature feature = featureModel.mutate().addFeature(i + "feature");
            feature.mutate().setName("feature" + i);
            feature.mutate().setType(type);
            rootTree.mutate().addFeatureBelow(feature);

            FeatJAR.log().info("Added Feature " + feature.getName().get() + " with type " + feature.getType());
        }
    }

    private IFormula buildFormula(Class<?> type) {
        return new Reference(new And(
                Expressions.implies(new NonBooleanLiteral(new Variable("feature0", type)), new Literal("root")),
                Expressions.implies(new NonBooleanLiteral(new Variable("feature1", type)), new Literal("root")),
                Expressions.implies(new NonBooleanLiteral(new Variable("feature2", type)), new Literal("root")),
                Expressions.implies(new NonBooleanLiteral(new Variable("feature3", type)), new Literal("root")),
                Expressions.implies(new NonBooleanLiteral(new Variable("feature4", type)), new Literal("root"))));
    }

    private IFormula buildBooleanForumla() {
        return new Reference(new And(
                Expressions.implies(Expressions.literal("feature0"), Expressions.literal("root")),
                Expressions.implies(Expressions.literal("feature1"), Expressions.literal("root")),
                Expressions.implies(Expressions.literal("feature2"), Expressions.literal("root")),
                Expressions.implies(Expressions.literal("feature3"), Expressions.literal("root")),
                Expressions.implies(Expressions.literal("feature4"), Expressions.literal("root"))));
    }
}
