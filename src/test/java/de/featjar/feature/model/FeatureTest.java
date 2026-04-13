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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.data.identifier.Identifiers;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FeatureTest {
    private static FeatureModel featureModel;

    @BeforeAll
    public static void setup() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());

        // features
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        rootTree.mutate().toAndGroup();

        IFeature childFeature1 = featureModel.mutate().addFeature("Test1");
        IFeatureTree childTree1 = rootTree.mutate().addFeatureBelow(childFeature1);

        IFeature childFeature2 = featureModel.mutate().addFeature("Test2");
        IFeatureTree childTree2 = rootTree.mutate().addFeatureBelow(childFeature2);

        IFeature childFeature3 = featureModel.mutate().addFeature("Test3");
        IFeatureTree childTree3 = childTree1.mutate().addFeatureBelow(childFeature3);
        childTree1.mutate().toAlternativeGroup();

        IFeature childFeature4 = featureModel.mutate().addFeature("Test4");
        childTree1.mutate().addFeatureBelow(childFeature4);

        IFeature childFeature5 = featureModel.mutate().addFeature("Test5");
        IFeatureTree childTree5 = childTree2.mutate().addFeatureBelow(childFeature5);
        childTree2.mutate().toOrGroup();

        IFeature childFeature6 = featureModel.mutate().addFeature("Test6");
        childTree2.mutate().addFeatureBelow(childFeature6);

        IFeature childFeature7 = featureModel.mutate().addFeature("Test7");
        IFeatureTree childTree7 = rootTree.mutate().addFeatureBelow(childFeature7);
        childTree7.mutate().makeMandatory();

        IFeature childFeature8 = featureModel.mutate().addFeature("Test8");
        childTree3.mutate().addFeatureBelow(childFeature8);

        IFormula formula1 = new Or(
                new And(new Literal("Test1"), new Literal("Test2")),
                new BiImplies(new Literal("Test3"), new Literal("Test4")),
                new Implies(new Literal("Test5"), new Literal("Test6")),
                new Not(new Literal("Test7")));

        // constraints
        featureModel.mutate().addConstraint(formula1);

        featureModel.getFeature("Test1").get().mutate().setHidden(true);

        FeatureTest.featureModel = featureModel;
    }

    @Test
    public void hasHiddenParentMethodCorrectlyTraversesTreeToCheckIfThereIsAParentThatIsHidden() {
        IFeature root = featureModel.getFeature("root").get();
        IFeature test1 = featureModel.getFeature("Test1").get();
        IFeature test2 = featureModel.getFeature("Test2").get();
        IFeature test3 = featureModel.getFeature("Test3").get();
        IFeature test5 = featureModel.getFeature("Test5").get();
        IFeature test8 = featureModel.getFeature("Test8").get();

        // Tests the visible root.
        assertFalse(root.hasHiddenParent());

        // Tests the hidden feature.
        assertFalse(test1.hasHiddenParent());

        // Tests a visible feature at the same depth of the hidden feature (test1).
        assertFalse(test2.hasHiddenParent());

        // Tests child of hidden feature (test1).
        assertTrue(test3.hasHiddenParent());

        // Tests child of visible feature (test2).
        assertFalse(test5.hasHiddenParent());

        // Tests grandchild for hidden feature (test1).
        assertTrue(test8.hasHiddenParent());
    }
}
