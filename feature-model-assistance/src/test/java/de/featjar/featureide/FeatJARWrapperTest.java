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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.Common;
import de.featjar.base.data.Void;
import de.featjar.base.io.text.StringTextFormat;
import de.featjar.base.tree.DataTree;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.io.uvl.UVLFeatureModelFormat;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import java.math.BigInteger;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class FeatJARWrapperTest {

    @SuppressWarnings("unused")
    @Test
    public void featjarWrapperWorksCorrectly() {
        FeatJARWrapper featJARWrapper = new FeatJARWrapper();

        FeatureModelBuilder featureModelBuilder = featJARWrapper.featureModelBuilder();
        IFeature root = featureModelBuilder.addRoot("root");
        IFeature alternative = featureModelBuilder.addFeatureBelow("alternative", root);
        IFeature and = featureModelBuilder.addFeatureBelow("and", root);
        IFeature or = featureModelBuilder.addFeatureBelow("or", root);

        IFeature remove = featureModelBuilder.addFeatureBelow("remove", root);
        IFeature removeremoveA = featureModelBuilder.addFeatureBelow("removeremoveA", remove);
        IFeature removeremoveB = featureModelBuilder.addFeatureBelow("removeremoveB", remove);
        IFeature removeremoveAremoveA = featureModelBuilder.addFeatureBelow("removeremoveAremoveA", removeremoveA);
        IFeature removeremoveBremoveA = featureModelBuilder.addFeatureBelow("removeremoveBremoveA", removeremoveB);

        IFeature altA = featureModelBuilder.addFeatureBelow("altA", alternative);
        IFeature altB = featureModelBuilder.addFeatureBelow("altB", alternative);
        IFeature altC = featureModelBuilder.addFeatureBelow("altC", alternative);

        IFeature andMandatoryA = featureModelBuilder.addFeatureBelow("andMandatoryA", and);
        IFeature andOptionalB = featureModelBuilder.addFeatureBelow("andOptionalB", and);
        IFeature andMandatoryAOptionalA = featureModelBuilder.addFeatureBelow("andMandatoryAOptionalA", andMandatoryA);

        IFeature orA = featureModelBuilder.addFeatureBelow("orA", or);
        IFeature orB = featureModelBuilder.addFeatureBelow("orB", or);
        IFeature orC = featureModelBuilder.addFeatureBelow("orC", or);

        featureModelBuilder.setGroupFeaturesIsInToAlternative(altA);
        featureModelBuilder.setGroupFeaturesIsInToAnd(andMandatoryA);
        featureModelBuilder.setGroupFeaturesIsInToOr(orA);

        featureModelBuilder.setFeatureToMandatory(andMandatoryA);
        featureModelBuilder.setFeatureToOptional(andOptionalB);
        featureModelBuilder.setFeatureToOptional(andMandatoryAOptionalA);

        featureModelBuilder.addConstraint(new And(
                new Not(featureModelBuilder.createLiteral(altC)),
                new Not(featureModelBuilder.createLiteral(andMandatoryAOptionalA))));

        featureModelBuilder.addConstraint(
                new Implies(featureModelBuilder.createLiteral(orB), featureModelBuilder.createLiteral(orC)));

        IConstraint removeConstraint1 =
                featureModelBuilder.addConstraint(new Not(featureModelBuilder.createLiteral(root)));
        IConstraint removeConstraint2 =
                featureModelBuilder.addConstraint(featureModelBuilder.createLiteral(removeremoveBremoveA));

        assertFalse(featureModelBuilder.removeFeatureTree(remove));

        featureModelBuilder.removeConstraint(removeConstraint2);
        featureModelBuilder.removeConstraint(removeConstraint1);

        assertTrue(featureModelBuilder.removeFeature(removeremoveA));
        assertTrue(featureModelBuilder.removeFeatureTree(remove));

        assertEquals(
                Common.load("test_model.uvl", new StringTextFormat()),
                new UVLFeatureModelFormat()
                        .serialize(featureModelBuilder.getFeatureModel())
                        .get());

        FeatureModelAnalyzer analyzer = featJARWrapper.featureModelAnalyzer(featureModelBuilder.getFeatureModel());

        assertEquals(
                Arrays.asList(altA, altC, orB, orC), analyzer.toFeature(Arrays.asList("altA", "altC", "orB", "orC")));
        assertEquals(root, analyzer.toFeature("root"));

        analyzer.toFormula().orElseThrow();
        analyzer.toCNF().orElseThrow();
        analyzer.toClauseList().orElseThrow();
        analyzer.toSat4JSolver().orElseThrow();

        DataTree<Void> statistics = analyzer.statistics();
        assertEquals(
                4, statistics.getChild(0).get().getChild(0).get().getValue().get());
        assertEquals(
                2.4, statistics.getChild(0).get().getChild(1).get().getValue().get());
        assertEquals(
                8L, statistics.getChild(0).get().getChild(2).get().getValue().get());
        assertEquals(
                3L, statistics.getChild(0).get().getChild(3).get().getValue().get());
        assertEquals(
                5L, statistics.getChild(0).get().getChild(4).get().getValue().get());
        assertEquals(
                4L, statistics.getChild(1).get().getChild(0).get().getValue().get());
        assertEquals(
                4L, statistics.getChild(1).get().getChild(1).get().getValue().get());
        assertEquals(
                4L, statistics.getChild(1).get().getChild(2).get().getValue().get());

        assertTrue(analyzer.isSatisfiable().orElseThrow());
        assertFalse(analyzer.isVoid().orElseThrow());
        assertEquals(Arrays.asList("root"), analyzer.core().orElseThrow());
        assertEquals(
                Arrays.asList("altC", "andMandatoryAOptionalA"), analyzer.dead().orElseThrow());
        assertEquals(
                BigInteger.valueOf(54), analyzer.numberOfValidConfigurations().orElseThrow());
        analyzer.atomicSets().orElseThrow();

        analyzer.project(Arrays.asList("altA", "altC", "orB", "orC")).orElseThrow();
        analyzer.slice(Arrays.asList("altA", "altC", "orB", "orC")).orElseThrow();

        analyzer.allConfigurations().orElseThrow();
        analyzer.randomConfigurations(10, 1L).orElseThrow();
        analyzer.twiseConfigurations(2).orElseThrow();
    }
}
