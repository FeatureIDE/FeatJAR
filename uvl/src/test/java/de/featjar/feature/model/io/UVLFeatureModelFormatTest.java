/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-uvl.
 *
 * uvl is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * uvl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uvl. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-uvl> for further information.
 */
package de.featjar.feature.model.io;

import de.featjar.FormatTest;
import de.featjar.analysis.sat4j.computation.ComputeSatisfiableSAT4J;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.FileInputMapper;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.uvl.UVLFeatureModelFormat;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UVLFeatureModelFormatTest {

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
        childTree1.mutate().toAlternativeGroup();

        IFeature childFeature2 = featureModel.mutate().addFeature("Test2");
        IFeatureTree childTree2 = rootTree.mutate().addFeatureBelow(childFeature2);
        childTree2.mutate().toOrGroup();

        IFeature childFeature3 = featureModel.mutate().addFeature("Test3");
        childTree1.mutate().addFeatureBelow(childFeature3);

        IFeature childFeature4 = featureModel.mutate().addFeature("Test4");
        childTree1.mutate().addFeatureBelow(childFeature4);

        IFeature childFeature5 = featureModel.mutate().addFeature("Test5");
        childTree2.mutate().addFeatureBelow(childFeature5);

        IFeature childFeature6 = featureModel.mutate().addFeature("Test6");
        childTree2.mutate().addFeatureBelow(childFeature6);

        IFeature childFeature7 = featureModel.mutate().addFeature("Test7");
        IFeatureTree childTree7 = rootTree.mutate().addFeatureBelow(childFeature7);
        childTree7.mutate().makeMandatory();

        IFormula formula1 = new Or(
                new And(new Literal("Test1"), new Literal("Test2")),
                new BiImplies(new Literal("Test3"), new Literal("Test4")),
                new Implies(new Literal("Test5"), new Literal("Test6")),
                new Not(new Literal("Test7")));

        // constraints
        featureModel.mutate().addConstraint(formula1);
        UVLFeatureModelFormatTest.featureModel = featureModel;
    }

    @Test
    void testFixtures() {
        FormatTest.testParseAndSerialize("uvl/ABC-nAnBnC", new UVLFeatureModelFormat());
        FormatTest.testParseAndSerialize("uvl/nA", new UVLFeatureModelFormat());
        FormatTest.testParseAndSerialize("uvl/nAB", new UVLFeatureModelFormat());
    }

    @Test
    void testUVLFeatureModelFormatSerialize() throws IOException {
        UVLFeatureModelFormat format = new UVLFeatureModelFormat();
        Result<String> featureModelString = format.serialize(featureModel);

        if (featureModelString.isEmpty()) {
            Assertions.fail();
        }

        String expected = new String(
                Files.readAllBytes(Path.of("src", "test", "resources", "uvl", "featureModelSerializeResult.uvl")));
        Assertions.assertEquals(expected, featureModelString.get());
    }

    @Test
    void testUVLFeatureModelFormatParse() throws IOException {
        IFormat<IFeatureModel> format = new UVLFeatureModelFormat();
        Result<IFeatureModel> result = format.parse(new FileInputMapper(
                Path.of("src", "test", "resources", "uvl", "featureModelSerializeResult.uvl"),
                Charset.defaultCharset()));

        if (result.isEmpty()) {
            Assertions.fail();
        }

        IFeatureModel parsedFeatureModel = result.get();

        // testing root
        IFeature rootFeature = parsedFeatureModel.getFeature("root").get();
        List<String> rootChildrenNames = rootFeature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, rootChildrenNames.size());
        Assertions.assertTrue(rootChildrenNames.contains("Test1"));
        Assertions.assertTrue(rootChildrenNames.contains("Test2"));
        Assertions.assertTrue(rootChildrenNames.contains("Test7"));

        // testing Test1 feature
        IFeature test1Feature = parsedFeatureModel.getFeature("Test1").get();
        Assertions.assertTrue(
                test1Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test1Feature.getFeatureTree().get().isOptional());
        List<String> test1ChildrenNames = test1Feature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, test1ChildrenNames.size());
        Assertions.assertTrue(test1ChildrenNames.contains("Test3"));
        Assertions.assertTrue(test1ChildrenNames.contains("Test4"));

        // testing Test2 feature
        IFeature test2Feature = parsedFeatureModel.getFeature("Test2").get();
        Assertions.assertTrue(
                test2Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test2Feature.getFeatureTree().get().isOptional());
        List<String> test2ChildrenNames = test2Feature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, test2ChildrenNames.size());
        Assertions.assertTrue(test2ChildrenNames.contains("Test5"));
        Assertions.assertTrue(test2ChildrenNames.contains("Test6"));

        // testing Test3 feature
        IFeature test3Feature = parsedFeatureModel.getFeature("Test3").get();
        Assertions.assertTrue(
                test3Feature.getFeatureTree().get().getParentGroup().get().isAlternative());
        Assertions.assertTrue(test3Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test4 feature
        IFeature test4Feature = parsedFeatureModel.getFeature("Test4").get();
        Assertions.assertTrue(
                test4Feature.getFeatureTree().get().getParentGroup().get().isAlternative());
        Assertions.assertTrue(test4Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test5 feature
        IFeature test5Feature = parsedFeatureModel.getFeature("Test5").get();
        Assertions.assertTrue(
                test5Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test5Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test6 feature
        IFeature test6Feature = parsedFeatureModel.getFeature("Test6").get();
        Assertions.assertTrue(
                test6Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test6Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test7 feature
        IFeature test7Feature = parsedFeatureModel.getFeature("Test7").get();
        Assertions.assertTrue(
                test7Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test7Feature.getFeatureTree().get().isMandatory());
        Assertions.assertTrue(test7Feature.getFeatureTree().get().getChildren().isEmpty());

        Assertions.assertEquals(1, parsedFeatureModel.getConstraints().size());
        IFormula constraint =
                parsedFeatureModel.getConstraints().iterator().next().getFormula();
        IFormula constraint2 = featureModel.getConstraints().iterator().next().getFormula();

        Boolean notEquivalent = Computations.of((IFormula) new Not(new BiImplies(constraint, constraint2)))
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSatisfiableSAT4J::new)
                .compute();

        Assertions.assertFalse(notEquivalent);
    }

    @Test
    void testUVLFeatureModelFormatParseWithGroupCardinality() throws IOException {
        IFormat<IFeatureModel> format = new UVLFeatureModelFormat();
        Result<IFeatureModel> result = format.parse(new FileInputMapper(
                Path.of("src", "test", "resources", "uvl", "featureModelSerializeResultWithGroupCardinalities.uvl"),
                Charset.defaultCharset()));

        if (result.isEmpty()) {
            Assertions.fail(result.printProblems());
        }

        IFeatureModel parsedFeatureModel = result.get();

        // testing root
        IFeature rootFeature = parsedFeatureModel.getFeature("root").get();
        List<String> rootChildrenNames = rootFeature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(4, rootChildrenNames.size());
        Assertions.assertTrue(rootChildrenNames.contains("Test1"));
        Assertions.assertTrue(rootChildrenNames.contains("Test2"));
        Assertions.assertTrue(rootChildrenNames.contains("Test7"));
        Assertions.assertTrue(rootChildrenNames.contains("Test11"));

        // testing Test1 feature
        IFeature test1Feature = parsedFeatureModel.getFeature("Test1").get();
        Assertions.assertTrue(
                test1Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test1Feature.getFeatureTree().get().isOptional());
        List<String> test1ChildrenNames = test1Feature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, test1ChildrenNames.size());
        Assertions.assertTrue(test1ChildrenNames.contains("Test3"));
        Assertions.assertTrue(test1ChildrenNames.contains("Test4"));

        // testing Test2 feature
        IFeature test2Feature = parsedFeatureModel.getFeature("Test2").get();
        Assertions.assertTrue(
                test2Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test2Feature.getFeatureTree().get().isOptional());
        List<String> test2ChildrenNames = test2Feature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, test2ChildrenNames.size());
        Assertions.assertTrue(test2ChildrenNames.contains("Test5"));
        Assertions.assertTrue(test2ChildrenNames.contains("Test6"));

        // testing Test3 feature
        IFeature test3Feature = parsedFeatureModel.getFeature("Test3").get();
        Assertions.assertTrue(
                test3Feature.getFeatureTree().get().getParentGroup().get().isAlternative());
        Assertions.assertTrue(test3Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test4 feature
        IFeature test4Feature = parsedFeatureModel.getFeature("Test4").get();
        Assertions.assertTrue(
                test4Feature.getFeatureTree().get().getParentGroup().get().isAlternative());
        Assertions.assertTrue(test4Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test5 feature
        IFeature test5Feature = parsedFeatureModel.getFeature("Test5").get();
        Assertions.assertTrue(
                test5Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test5Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test6 feature
        IFeature test6Feature = parsedFeatureModel.getFeature("Test6").get();
        Assertions.assertTrue(
                test6Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test6Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test7 feature
        IFeature test7Feature = parsedFeatureModel.getFeature("Test7").get();
        Assertions.assertTrue(
                test7Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test7Feature.getFeatureTree().get().isOptional());
        List<String> test7ChildrenNames = test7Feature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, test7ChildrenNames.size());
        Assertions.assertTrue(test7ChildrenNames.contains("Test8"));
        Assertions.assertTrue(test7ChildrenNames.contains("Test9"));
        Assertions.assertTrue(test7ChildrenNames.contains("Test10"));

        // testing Test8 feature
        IFeature test8Feature = parsedFeatureModel.getFeature("Test8").get();
        Group parentGroup8 =
                test8Feature.getFeatureTree().get().getParentGroup().get();
        Assertions.assertTrue(parentGroup8.isCardinalityGroup());
        Assertions.assertTrue(parentGroup8.getLowerBound() == 0);
        Assertions.assertTrue(parentGroup8.getUpperBound() == 2);
        Assertions.assertTrue(test8Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test9 feature
        IFeature test9Feature = parsedFeatureModel.getFeature("Test9").get();
        Group parentGroup9 =
                test9Feature.getFeatureTree().get().getParentGroup().get();
        Assertions.assertTrue(parentGroup9.isCardinalityGroup());
        Assertions.assertTrue(parentGroup9.getLowerBound() == 0);
        Assertions.assertTrue(parentGroup9.getUpperBound() == 2);
        Assertions.assertTrue(test9Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test10 feature
        IFeature test10Feature = parsedFeatureModel.getFeature("Test10").get();
        Group parentGroup10 =
                test10Feature.getFeatureTree().get().getParentGroup().get();
        Assertions.assertTrue(parentGroup10.isCardinalityGroup());
        Assertions.assertTrue(parentGroup10.getLowerBound() == 0);
        Assertions.assertTrue(parentGroup10.getUpperBound() == 2);
        Assertions.assertTrue(test10Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Test11 feature
        IFeature test11Feature = parsedFeatureModel.getFeature("Test11").get();
        Assertions.assertTrue(
                test11Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test11Feature.getFeatureTree().get().isMandatory());
    }

    @Test
    void testUVLFeatureModelFormatParseWithFeatureCardinality() throws IOException {
        IFormat<IFeatureModel> format = new UVLFeatureModelFormat();
        Result<IFeatureModel> result = format.parse(new FileInputMapper(
                Path.of("src", "test", "resources", "uvl", "featureModelSerializeResultWithFeatureCardinalities.uvl"),
                Charset.defaultCharset()));

        if (result.isEmpty()) {
            Assertions.fail(result.printProblems());
        }

        IFeatureModel parsedFeatureModel = result.get();

        // testing Sandwich
        IFeature rootFeature = parsedFeatureModel.getFeature("Sandwich").get();
        List<String> rootChildrenNames = rootFeature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(4, rootChildrenNames.size());
        Assertions.assertTrue(rootChildrenNames.contains("Bread"));
        Assertions.assertTrue(rootChildrenNames.contains("Sauce"));
        Assertions.assertTrue(rootChildrenNames.contains("Cheese"));
        Assertions.assertTrue(rootChildrenNames.contains("Pickle"));

        // testing Bread
        IFeature test1Feature = parsedFeatureModel.getFeature("Bread").get();
        Assertions.assertTrue(
                test1Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test1Feature.getFeatureTree().get().isMandatory());

        // testing Sauce
        IFeature test2Feature = parsedFeatureModel.getFeature("Sauce").get();
        Assertions.assertTrue(
                test2Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test2Feature.getFeatureTree().get().isOptional());
        List<String> test2ChildrenNames = test2Feature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, test2ChildrenNames.size());
        Assertions.assertTrue(test2ChildrenNames.contains("Ketchup"));
        Assertions.assertTrue(test2ChildrenNames.contains("Mustard"));

        // testing Cheese
        IFeature test3Feature = parsedFeatureModel.getFeature("Cheese").get();
        Assertions.assertTrue(
                test3Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(test3Feature.getFeatureTree().get().isOptional());
        List<String> test3ChildrenNames = test3Feature.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(3, test3ChildrenNames.size());
        Assertions.assertTrue(test3ChildrenNames.contains("Cheddar"));
        Assertions.assertTrue(test3ChildrenNames.contains("Gouda"));
        Assertions.assertTrue(test3ChildrenNames.contains("Goat"));

        // testing Pickle
        IFeature test4Feature = parsedFeatureModel.getFeature("Pickle").get();
        Assertions.assertTrue(
                test4Feature.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertEquals(1, test4Feature.getFeatureTree().get().getFeatureCardinalityLowerBound());
        Assertions.assertEquals(3, test4Feature.getFeatureTree().get().getFeatureCardinalityUpperBound());
        Assertions.assertTrue(test4Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Ketchup and Mustard
        IFeature test5Feature = parsedFeatureModel.getFeature("Ketchup").get();
        Assertions.assertTrue(
                test5Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test5Feature.getFeatureTree().get().getChildren().isEmpty());

        IFeature test6Feature = parsedFeatureModel.getFeature("Mustard").get();
        Assertions.assertTrue(
                test6Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test6Feature.getFeatureTree().get().getChildren().isEmpty());

        // testing Goat, Gouda and Cheddar
        IFeature test7Feature = parsedFeatureModel.getFeature("Goat").get();
        Assertions.assertTrue(
                test7Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test7Feature.getFeatureTree().get().getChildren().isEmpty());

        IFeature test8Feature = parsedFeatureModel.getFeature("Gouda").get();
        Assertions.assertTrue(
                test8Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test8Feature.getFeatureTree().get().getChildren().isEmpty());

        IFeature test9Feature = parsedFeatureModel.getFeature("Cheddar").get();
        Assertions.assertTrue(
                test9Feature.getFeatureTree().get().getParentGroup().get().isOr());
        Assertions.assertTrue(test9Feature.getFeatureTree().get().getChildren().isEmpty());
    }

    @Test
    void testUVLFeatureModelFormatParseWithAttributes() throws IOException {
        IFormat<IFeatureModel> format = new UVLFeatureModelFormat();
        Result<IFeatureModel> result = format.parse(new FileInputMapper(
                Path.of("src", "test", "resources", "uvl", "featureModelSerializeResultWithAttributes.uvl"),
                Charset.defaultCharset()));

        if (result.isEmpty()) {
            Assertions.fail();
        }

        IFeatureModel parsedFeatureModel = result.get();

        // Salad
        IFeature saladRoot = parsedFeatureModel.getFeature("Salad").get();
        List<String> saladRootChildren = saladRoot.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(2, saladRootChildren.size());
        Assertions.assertTrue(saladRootChildren.contains("Veggies"));
        Assertions.assertTrue(saladRootChildren.contains("Arugula"));

        // Veggies
        IFeature mandatoryVeggies = parsedFeatureModel.getFeature("Veggies").get();
        Assertions.assertTrue(
                mandatoryVeggies.getFeatureTree().get().getParentGroup().get().isAnd());
        Assertions.assertTrue(mandatoryVeggies.getFeatureTree().get().isMandatory());
        List<String> mandatoryVeggiesChildren = mandatoryVeggies.getFeatureTree().get().getChildren().stream()
                .map((it) -> it.getFeature().getName().get())
                .collect(Collectors.toList());
        Assertions.assertEquals(4, mandatoryVeggiesChildren.size());
        Assertions.assertTrue(mandatoryVeggiesChildren.contains("Tomatoes"));
        Assertions.assertTrue(mandatoryVeggiesChildren.contains("Cucumber"));
        Assertions.assertTrue(mandatoryVeggiesChildren.contains("Fennel"));
        Assertions.assertTrue(mandatoryVeggiesChildren.contains("Beets"));

        // Arugula
        IFeature optionalArugula = parsedFeatureModel.getFeature("Arugula").get();
        Assertions.assertTrue(optionalArugula.getType() == String.class);
        Assertions.assertTrue(optionalArugula.getFeatureTree().get().isOptional());
        Assertions.assertTrue(
                optionalArugula.getFeatureTree().get().getChildren().isEmpty());

        Map<IAttribute<?>, Object> arugulaAttributes =
                optionalArugula.getAttributes().get();
        List<Map.Entry<IAttribute<?>, Object>> arugulaFreshness = arugulaAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("Freshness")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 90)
                .collect(Collectors.toList());
        Assertions.assertTrue(arugulaFreshness.size() == 1);

        List<Map.Entry<IAttribute<?>, Object>> arugulaFoodMiles = arugulaAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("FoodMiles")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 20)
                .collect(Collectors.toList());
        Assertions.assertTrue(arugulaFoodMiles.size() == 1);

        // Tomatoes
        IFeature tomatoes = parsedFeatureModel.getFeature("Tomatoes").get();
        Assertions.assertTrue(tomatoes.getFeatureTree().get().getChildren().isEmpty());
        Assertions.assertTrue(
                tomatoes.getFeatureTree().get().getParentGroup().get().isOr());
        Map<IAttribute<?>, Object> tomatoesAttributes = tomatoes.getAttributes().get();
        List<Map.Entry<IAttribute<?>, Object>> tomatoesFreshness = tomatoesAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("Freshness")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 30)
                .collect(Collectors.toList());
        Assertions.assertTrue(tomatoesFreshness.size() == 1);

        List<Map.Entry<IAttribute<?>, Object>> tomatoesFoodMiles = tomatoesAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("FoodMiles")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 100)
                .collect(Collectors.toList());
        Assertions.assertTrue(tomatoesFoodMiles.size() == 1);

        List<Map.Entry<IAttribute<?>, Object>> tomatoType = tomatoesAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("tomatoType")
                        && a.getKey().getType().getClassType().equals(String.class)
                        && ((String) a.getValue()).toString().equals("Cherry"))
                .collect(Collectors.toList());
        Assertions.assertTrue(tomatoType.size() == 1);

        // Cucumber
        IFeature cucumber = parsedFeatureModel.getFeature("Cucumber").get();
        Assertions.assertTrue(cucumber.getFeatureTree().get().getChildren().isEmpty());
        Assertions.assertTrue(
                cucumber.getFeatureTree().get().getParentGroup().get().isOr());
        Map<IAttribute<?>, Object> cucumberAttributes = cucumber.getAttributes().get();
        List<Map.Entry<IAttribute<?>, Object>> cucumberFreshness = cucumberAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("Freshness")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 25)
                .collect(Collectors.toList());
        Assertions.assertTrue(cucumberFreshness.size() == 1);

        List<Map.Entry<IAttribute<?>, Object>> cucumberFoodMiles = cucumberAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("FoodMiles")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 80)
                .collect(Collectors.toList());
        Assertions.assertTrue(cucumberFoodMiles.size() == 1);

        // Fennel
        IFeature fennel = parsedFeatureModel.getFeature("Fennel").get();
        Assertions.assertTrue(fennel.getFeatureTree().get().getChildren().isEmpty());
        Assertions.assertTrue(
                fennel.getFeatureTree().get().getParentGroup().get().isOr());
        Map<IAttribute<?>, Object> fennelAttributes = fennel.getAttributes().get();
        Assertions.assertTrue(fennelAttributes.entrySet().stream()
                .anyMatch(a -> a.getKey().getSimpleName().equals("Freshness")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 100));

        List<Map.Entry<IAttribute<?>, Object>> fennelFoodMiles = fennelAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("FoodMiles")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 11)
                .collect(Collectors.toList());
        Assertions.assertTrue(fennelFoodMiles.size() == 1);

        // Beets
        IFeature beets = parsedFeatureModel.getFeature("Beets").get();
        Assertions.assertTrue(beets.getFeatureTree().get().getChildren().isEmpty());
        Assertions.assertTrue(
                beets.getFeatureTree().get().getParentGroup().get().isOr());
        Map<IAttribute<?>, Object> beetsAttributes = beets.getAttributes().get();
        List<Map.Entry<IAttribute<?>, Object>> beetsFreshness = fennelAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("Freshness")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 100)
                .collect(Collectors.toList());
        Assertions.assertTrue(beetsFreshness.size() == 1);

        List<Map.Entry<IAttribute<?>, Object>> beetsFoodMiles = fennelAttributes.entrySet().stream()
                .filter(a -> a.getKey().getSimpleName().equals("FoodMiles")
                        && a.getKey().getType().getClassType().equals(Long.class)
                        && ((Long) a.getValue()).longValue() == 11)
                .collect(Collectors.toList());
        Assertions.assertTrue(beetsFoodMiles.size() == 1);
    }
}
