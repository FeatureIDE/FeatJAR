/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.composition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.formula.io.textual.JavaSymbols;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class PreprocessorStructureTest {

    @Test
    public void testMatchingIfAndEndif() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = new ArrayList<>();
        lines.add("//#if A");
        lines.add("code");
        lines.add("//#endif");

        List<String> problems = preprocessor.checkStructure(lines.stream());

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testEndifWithoutIf() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = new ArrayList<>();
        lines.add("code");
        lines.add("//#endif");

        List<String> problems = preprocessor.checkStructure(lines.stream());

        assertEquals(1, problems.size());
        assertEquals("Line 2: #endif without #if. Suggestion: remove the #endif or add a matching #if.", problems.get(0));
    }

    @Test
    public void testMissingEndif() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("code");
        lines.add("//#if A");

        List<String> problems = preprocessor.checkStructure(lines.stream());

        assertEquals(1, problems.size());
        assertEquals("Line 3: #if has no matching #endif. Suggestion: add a matching #endif.", problems.get(0));
    }

    @Test
    public void testNestedBlocks() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = new ArrayList<>();
        lines.add("//#if A");
        lines.add("//#if B");
        lines.add("code");
        lines.add("//#endif");
        lines.add("//#endif");

        List<String> problems = preprocessor.checkStructure(lines.stream());

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testTwoMissingEndifs() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = new ArrayList<>();
        lines.add("//#if A");
        lines.add("//#if B");

        List<String> problems = preprocessor.checkStructure(lines.stream());

        assertEquals(2, problems.size());
        assertEquals("Line 1: #if has no matching #endif. Suggestion: add a matching #endif.", problems.get(0));
        assertEquals("Line 2: #if has no matching #endif. Suggestion: add a matching #endif.", problems.get(1));
    }
}
