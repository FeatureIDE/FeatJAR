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
package de.featjar.feature.model.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Attributes;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.LatexFeatureModelFormat;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test the full output with a test feature model and attributes, constrains and more
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class LatexFeatureModelFormatTest {

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void deinit() {
        FeatJAR.deinitialize();
    }

    @Test
    public void featureModelIsCorrectlySerializedToLatexDocument() throws IOException {
        String expectedFileContent = loadExpectedFile();

        FeatureModel featureModel = createFeatureModel();
        Result<String> serialize = new LatexFeatureModelFormat().serialize(featureModel);
        assertTrue(serialize.isPresent(), Problem.printProblems(serialize.getProblems()));

        assertEquals(expectedFileContent, serialize.get());
    }

    private String loadExpectedFile() throws IOException {
        StringBuilder testFileLines = new StringBuilder();
        for (String line : IO.readLines(
                ClassLoader.getSystemResourceAsStream("de/featjar/feature/model/io/tikz/test-output.tex"))) {
            testFileLines.append(line).append(System.lineSeparator());
        }
        String expectedFileContent = testFileLines.toString();
        return expectedFileContent;
    }

    private FeatureModel createFeatureModel() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());

        IFeature featureRootS = featureModel.mutate().addFeature("Hello");
        IFeature feature = featureModel.mutate().addFeature("Feature");
        IFeature world1 = featureModel.mutate().addFeature("World1");
        world1.mutate().setAttributeValue(Attributes.get("size", Double.class), 6000.0);
        world1.mutate().setAttributeValue(Attributes.get("population", Integer.class), 1);
        IFeature world2 = featureModel.mutate().addFeature("World2");
        IFeature wonderful1 = featureModel.mutate().addFeature("Wonderful1");
        wonderful1.mutate().setAttributeValue(Attributes.get("who", String.class), "you");
        wonderful1.mutate().setAttributeValue(Attributes.get("when", String.class), "now");
        IFeature beautiful1 = featureModel.mutate().addFeature("Beautiful1");
        IFeature wonderful2 = featureModel.mutate().addFeature("Wonderful2");
        IFeature beautiful2 = featureModel.mutate().addFeature("Beautiful2");
        IFeature wonderful3 = featureModel.mutate().addFeature("Wonderful3");
        wonderful3.mutate().setAttributeValue(Attributes.get("who", String.class), "you");
        IFeature beautiful3 = featureModel.mutate().addFeature("Beautiful3");
        IFeature meaningful1 = featureModel.mutate().addFeature("Meaningful1");
        IFeature meaningful2 = featureModel.mutate().addFeature("Meaningful2");
        IFeature meaningful3 = featureModel.mutate().addFeature("Meaningful3");

        featureRootS.mutate().setAbstract();

        // first tree
        IFeatureTree rootTree = featureModel.mutate().addFeatureTreeRoot(featureRootS);
        rootTree.mutate().makeMandatory();
        rootTree.mutate().toAndGroup();

        IFeatureTree firstFeatureTree = rootTree.mutate().addFeatureBelow(feature);
        feature.mutate().setAbstract();
        int group1 = firstFeatureTree.mutate().addAlternativeGroup();
        int group2 = firstFeatureTree.mutate().addOrGroup();
        int group3 = firstFeatureTree.mutate().addCardinalityGroup(Range.of(7, 8));

        firstFeatureTree.mutate().addFeatureBelow(wonderful1, 0, group1);
        firstFeatureTree.mutate().setFeatureCardinality(Range.of(0, 2));
        firstFeatureTree.mutate().addFeatureBelow(beautiful1, 1, group1);

        firstFeatureTree.mutate().addFeatureBelow(wonderful2, 2, group2);
        IFeatureTree beautiful2FeatureTree = firstFeatureTree.mutate().addFeatureBelow(beautiful2, 3, group2);

        int group4 = beautiful2FeatureTree.mutate().addCardinalityGroup(Range.of(0, 2));
        beautiful2FeatureTree.mutate().addFeatureBelow(meaningful1, 0, group4);
        beautiful2FeatureTree.mutate().addFeatureBelow(meaningful2, 1, group4);
        beautiful2FeatureTree.mutate().addFeatureBelow(meaningful3, 2, group4);

        firstFeatureTree.mutate().addFeatureBelow(wonderful3, 4, group3);
        firstFeatureTree.mutate().addFeatureBelow(beautiful3, 5, group3);

        rootTree.mutate().addFeatureBelow(world1);

        IFeatureTree world2FeatureTree = rootTree.mutate().addFeatureBelow(world2);
        world2FeatureTree.mutate().setFeatureCardinality(Range.of(1, 1));

        // Constraints
        featureModel.addConstraint(new And(Expressions.literal("World1"), Expressions.literal("Wonderful1")));
        featureModel.addConstraint(new Implies(
                Expressions.literal("World2"),
                new BiImplies(Expressions.literal("Beautiful2"), Expressions.literal("Beautiful3"))));
        return featureModel;
    }
}
