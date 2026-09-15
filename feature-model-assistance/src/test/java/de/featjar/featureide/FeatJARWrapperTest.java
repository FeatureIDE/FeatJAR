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

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

public class FeatJARWrapperTest {
        private IFeature root, alternative, and, or, remove, removeremoveA, removeremoveB, removeremoveAremoveA, removeremoveBremoveA,
            altA, altB, altC, andMandatoryA, andOptionalB, andMandatoryAOptionalA, orA, orB, orC;

        private IConstraint removeConstraint1;
        private IConstraint removeConstraint2;

        private FeatureModelBuilder featureModelBuilder;
        private FeatJARWrapper featJARWrapper;

        private FeatureModelAnalyzer analyzer;

        private void removeTemporaryConstraintsAndSubtree() {
                featureModelBuilder.removeConstraint(removeConstraint2);
                featureModelBuilder.removeConstraint(removeConstraint1);

                assertTrue(featureModelBuilder.removeFeature(removeremoveA));
                assertTrue(featureModelBuilder.removeFeatureTree(remove));
        }
        @SuppressWarnings("unused")
        @BeforeEach
        public void initializeFeatureModelToTest(){
                featJARWrapper = new FeatJARWrapper();
                featureModelBuilder = featJARWrapper.featureModelBuilder();

                root = featureModelBuilder.addRoot("root");
                alternative = featureModelBuilder.addFeatureBelow("alternative", root);
                and = featureModelBuilder.addFeatureBelow("and", root);
                or = featureModelBuilder.addFeatureBelow("or", root);

                remove = featureModelBuilder.addFeatureBelow("remove", root);
                removeremoveA = featureModelBuilder.addFeatureBelow("removeremoveA", remove);
                removeremoveB = featureModelBuilder.addFeatureBelow("removeremoveB", remove);
                removeremoveAremoveA = featureModelBuilder.addFeatureBelow("removeremoveAremoveA", removeremoveA);
                removeremoveBremoveA = featureModelBuilder.addFeatureBelow("removeremoveBremoveA", removeremoveB);

                altA = featureModelBuilder.addFeatureBelow("altA", alternative);
                altB = featureModelBuilder.addFeatureBelow("altB", alternative);
                altC = featureModelBuilder.addFeatureBelow("altC", alternative);

                andMandatoryA = featureModelBuilder.addFeatureBelow("andMandatoryA", and);
                andOptionalB = featureModelBuilder.addFeatureBelow("andOptionalB", and);
                andMandatoryAOptionalA = featureModelBuilder.addFeatureBelow("andMandatoryAOptionalA", andMandatoryA);

                orA = featureModelBuilder.addFeatureBelow("orA", or);
                orB = featureModelBuilder.addFeatureBelow("orB", or);
                orC = featureModelBuilder.addFeatureBelow("orC", or);

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

                removeConstraint1 =
                        featureModelBuilder.addConstraint(new Not(featureModelBuilder.createLiteral(root)));
                removeConstraint2 =
                        featureModelBuilder.addConstraint(featureModelBuilder.createLiteral(removeremoveBremoveA));

                analyzer = featJARWrapper.featureModelAnalyzer(featureModelBuilder.getFeatureModel());

        }

        @SuppressWarnings("unused")
        @Test
        public void doesNotRemoveFeatureTreeWhenReferencedByConstraint(){
                assertFalse(featureModelBuilder.removeFeatureTree(remove));
        }

        @SuppressWarnings("unused")
        @Test
        public void doesRemoveFeatureAfterRemovingConstraints(){
                featureModelBuilder.removeConstraint(removeConstraint2);
                featureModelBuilder.removeConstraint(removeConstraint1);

                assertTrue(featureModelBuilder.removeFeature(removeremoveA)); //
                assertTrue(featureModelBuilder.removeFeatureTree(remove));
        }

        @SuppressWarnings("unused")
        @Test
        public void serializeFeatureModel(){
                removeTemporaryConstraintsAndSubtree();

                assertEquals(
                        Common.load("test_model.uvl", new StringTextFormat()),
                        new UVLFeatureModelFormat()
                                .serialize(featureModelBuilder.getFeatureModel())
                                .get());
        }

        @SuppressWarnings("unused")
        @Test
        public void doFeaturesExist(){
                removeTemporaryConstraintsAndSubtree();

                assertEquals(
                Arrays.asList(altA, altC, orB, orC), analyzer.toFeature(Arrays.asList("altA", "altC", "orB", "orC")));
                assertEquals(root, analyzer.toFeature("root"));
        }

        @SuppressWarnings("unused")
        @Test
        public void analyzerTest(){
                removeTemporaryConstraintsAndSubtree();

                analyzer.toFormula().orElseThrow();
                analyzer.toCNF().orElseThrow();
                analyzer.toClauseList().orElseThrow();
                analyzer.toSat4JSolver().orElseThrow();
        }

        @SuppressWarnings("unused")
        @Test  
        public void analyzerStatisticsTest(){
                removeTemporaryConstraintsAndSubtree();

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
        }

        @SuppressWarnings("unused")
        @Test
        public void restOfWrapperTest() {
                removeTemporaryConstraintsAndSubtree();

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