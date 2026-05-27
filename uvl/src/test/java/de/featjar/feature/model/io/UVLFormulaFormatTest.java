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

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.Common;
import de.featjar.FormatTest;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.FileInputMapper;
import de.featjar.feature.model.io.uvl.UVLFormulaFormat;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.DefLiteral;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.function.integer.IntegerDivide;
import de.featjar.formula.structure.term.function.integer.IntegerMultiply;
import de.featjar.formula.structure.term.function.string.StringLength;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UVLFormulaFormatTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    void testFixtures() {
        FormatTest.testParse(getFormula("Root-ABC-nAnBnC"), "uvl/ABC-nAnBnC", 1, new UVLFormulaFormat());
    }

    public static Feature newFeature(FeatureModel uvlModel, String variableName) {
        Feature uvlFeature = new Feature(variableName);
        uvlModel.getFeatureMap().put(variableName, uvlFeature);
        return uvlFeature;
    }

    @Test
    void testFixtures2() {
        FormatTest.testSerializeAndParse(getFormula("nA"), new UVLFormulaFormat(), Expressions::print);
        FormatTest.testSerializeAndParse(getFormula("nAB"), new UVLFormulaFormat(), Expressions::print);
        // TODO currently wrong in UVL parser
//                FormatTest.testSerializeAndParse(getFormula("ABC-nAnBnC"), new UVLFormulaFormat(),
//         Expressions::print);
    }

    @Test
    void testUVLFormulaFormatSerialize() throws IOException {
        IFormula formula = new Or(
                new And(new Literal("Test1"), new Literal("Test2")),
                new BiImplies(new Literal("Test3"), new Literal("Test4")),
                new Implies(new Literal("Test5"), new Literal("Test6")),
                new Not(new Literal("Test7")));

        Result<String> result = new UVLFormulaFormat().serialize(formula);

        assertTrue(result.isPresent(), result::printProblems);

        String expected = new String(
                Files.readAllBytes(Path.of("src", "test", "resources", "uvl", "formulaSerializeResult.uvl")));
        Assertions.assertEquals(expected, result.get());
    }

    @Test
    void testUVLFormulaFormatParse() throws IOException {
        Result<IFormula> result = new UVLFormulaFormat()
                .parse(new FileInputMapper(
                        Path.of("src", "test", "resources", "uvl", "formulaSerializeResult.uvl"),
                        Charset.defaultCharset()));

        assertTrue(result.isPresent(), result::printProblems);

        IFormula expected = new Reference(new Or(
                new Or(
                        new Or(
                                new And(new Literal("Test1"), new Literal("Test2")),
                                new BiImplies(new Literal("Test3"), new Literal("Test4"))),
                        new Implies(new Literal("Test5"), new Literal("Test6"))),
                new Not(new Literal("Test7"))));

        compare(expected, result.get(), Expressions::print);
    }

    @Test
    public void testUVLFormulaFormatParseWithAttributes() throws IOException {
        IFormat<IFormula> format = new UVLFormulaFormat();
        Result<IFormula> computedFormula = format.parse(new FileInputMapper(
                Path.of("src", "test", "resources", "uvl", "featureModelSerializeResultWithAttributes.uvl"),
                Charset.defaultCharset()));

        if (computedFormula.isEmpty()) {
            Assertions.fail();
        }

        IFormula expectedFormula = new Reference(new And(
                new Literal("Salad"),
                new Implies(new Literal("Veggies"), new Literal("Salad")),
                new Implies(new Literal("Salad"), new Literal("Veggies")),
                new Implies(new DefLiteral(new Variable("Arugula", String.class)), new Literal("Salad")),
                new Implies(new Literal("Tomatoes"), new Literal("Veggies")),
                new Implies(new Literal("Cucumber"), new Literal("Veggies")),
                new Implies(new Literal("Fennel"), new Literal("Veggies")),
                new Implies(new Literal("Beets"), new Literal("Veggies")),
                new Implies(
                        new Literal("Veggies"),
                        new Or(
                                new Literal("Tomatoes"),
                                new Literal("Cucumber"),
                                new Literal("Fennel"),
                                new Literal("Beets"))),
                new Implies(new Literal("Fennel"), new And(new Literal("Beets"), new Not(new Literal("Cucumber")))),
                new GreaterEqual(new IntegerAdd(new Constant(90l), new Constant(100l)), new Constant(80d)),
                new LessThan(new IntegerMultiply(new Constant(80l), new Constant(100l)), new Constant(100000d)),
                new Equals(new Constant(100l), new Constant(100d)),
                new BiImplies(new Literal("Beets"), new Or(new Literal("Cucumber"), new Not(new Literal("Tomatoes")))),
                new LessEqual(
                        new IntegerAdd(new Constant(100l), new IntegerMultiply(new Constant(-1l), new Constant(80l))),
                        new Constant(30d)),
                new GreaterThan(new IntegerDivide(new Constant(100l), new Constant(25l)), new Constant(3d)),
                new Equals(new Constant("Cherry"), new Constant("Cherry")),
                new Implies(
                        new And(new DefLiteral(new Variable("Arugula", String.class))),
                        new Equals(new StringLength(new Variable("Arugula", String.class)), new Constant(7d)))));

        compare(expectedFormula, computedFormula.get(), Expressions::print);
    }
}
