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

import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.formula.io.textual.JavaSymbols;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PreprocessorStructureTest {

    @Test
    public void testMatchingIfAndEndif() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList("//#if A", "code", "//#endif");

        List<Problem> problems = preprocessor.checkStructure(lines.stream());

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testEndifWithoutIf() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList("code", "//#endif");

        List<Problem> problems = preprocessor.checkStructure(lines.stream());
        ParseProblem problem = (ParseProblem) problems.get(0);

        assertEquals(1, problems.size());
        assertEquals(
                "#endif without #if. Suggestion: remove the #endif or add a matching #if before line 1.",
                problem.getMessage());
        assertEquals(Severity.ERROR, problem.getSeverity());
        assertEquals(2, problem.getLineNumber());
    }

    @Test
    public void testIfOnLastLineSuggestsRemoval() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList("code", "//#if A");

        List<Problem> problems = preprocessor.checkStructure(lines.stream());
        ParseProblem problem = (ParseProblem) problems.get(0);

        assertEquals(1, problems.size());
        assertEquals("#if has no matching #endif. Suggestion: remove the #if.", problem.getMessage());
        assertEquals(Severity.ERROR, problem.getSeverity());
        assertEquals(2, problem.getLineNumber());
    }

    @Test
    public void testNestedBlocks() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList("//#if A", "//#if B", "code", "//#endif", "//#endif");

        List<Problem> problems = preprocessor.checkStructure(lines.stream());

        assertTrue(problems.isEmpty());
    }

    @Test
    public void testTwoMissingEndifs() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList("//#if A", "//#if B");

        List<Problem> problems = preprocessor.checkStructure(lines.stream());
        ParseProblem firstProblem = (ParseProblem) problems.get(0);
        ParseProblem secondProblem = (ParseProblem) problems.get(1);

        assertEquals(2, problems.size());
        assertEquals(
                "#if has no matching #endif. Suggestion: add a matching #endif before line 2.",
                firstProblem.getMessage());
        assertEquals(1, firstProblem.getLineNumber());
        assertEquals("#if has no matching #endif. Suggestion: remove the #if.", secondProblem.getMessage());
        assertEquals(2, secondProblem.getLineNumber());
    }

    @Test
    public void testEndifWithoutIfAfterEndif() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList("//#if A", "code", "//#endif", "code", "//#endif");

        List<Problem> problems = preprocessor.checkStructure(lines.stream());
        ParseProblem problem = (ParseProblem) problems.get(0);

        assertEquals(1, problems.size());
        assertEquals(
                "#endif without #if. Suggestion: remove the #endif or add a matching #if on line 4.",
                problem.getMessage());
        assertEquals(5, problem.getLineNumber());
    }

    @Test
    public void testMissingEndifAfterCodeSuggestsEndOfFile() {
        Preprocessor preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList("//#if A", "code");

        List<Problem> problems = preprocessor.checkStructure(lines.stream());
        ParseProblem problem = (ParseProblem) problems.get(0);

        assertEquals(1, problems.size());
        assertEquals(
                "#if has no matching #endif. Suggestion: add a matching #endif at the end of the file.",
                problem.getMessage());
        assertEquals(1, problem.getLineNumber());
    }
}
