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

import de.featjar.Common;
import de.featjar.FormatTest;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.FileInputMapper;
import de.featjar.feature.model.io.uvl.UVLFormulaFormat;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.predicate.Literal;
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
        FormatTest.testParse(getFormula("ABC-nAnBnC"), "uvl/ABC-nAnBnC", 1, new UVLFormulaFormat());
        FormatTest.testParse(getFormula("nA"), "uvl/nA", 3, new UVLFormulaFormat());
        FormatTest.testParse(getFormula("nAB"), "uvl/nAB", 1, new UVLFormulaFormat());
        // TODO: testSerializeAndParse
    }

    @Test
    void testUVLFormulaFormatSerialize() throws IOException {
        IFormula formula = new Or(
                new And(new Literal("Test1"), new Literal("Test2")),
                new BiImplies(new Literal("Test3"), new Literal("Test4")),
                new Implies(new Literal("Test5"), new Literal("Test6")),
                new Not(new Literal("Test7")));

        IFormat<IFormula> format = new UVLFormulaFormat();

        Result<String> result = format.serialize(formula);

        if (result.isEmpty()) {
            Assertions.fail();
        }
        String expected = new String(
                Files.readAllBytes(Path.of("src", "test", "resources", "uvl", "formulaSerializeResult.uvl")));
        Assertions.assertEquals(expected, result.get());
    }

    @Test
    void testUVLFormulaFormatParse() throws IOException {
        IFormat<IFormula> format = new UVLFormulaFormat();
        Result<IFormula> result = format.parse(new FileInputMapper(
                Path.of("src", "test", "resources", "uvl", "formulaSerializeResult.uvl"), Charset.defaultCharset()));

        if (result.isEmpty()) {
            Assertions.fail();
        }

        IFormula expected = new Reference(new Or(
                new And(new Literal("Test1"), new Literal("Test2")),
                new Or(
                        new BiImplies(new Literal("Test3"), new Literal("Test4")),
                        new Or(
                                new Implies(new Literal("Test5"), new Literal("Test6")),
                                new Not(new Literal("Test7"))))));

        Assertions.assertEquals(expected, result.get());
    }
}
