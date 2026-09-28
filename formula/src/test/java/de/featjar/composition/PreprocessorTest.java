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

import de.featjar.Common;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.tree.Trees;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.JavaSymbols;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/***
 * added the unit test
 * Tests {@link Preprocessor#computePresenceConditions(java.util.stream.Stream)}.
 */
public class PreprocessorTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void nestedAnnotations() {
        List<String> lines = List.of(
                "//#if A",
                "  System.out.println(\"\");",
                "//#if B",
                "  System.out.println(\"\");",
                "  System.out.println(\"\");",
                "//#else",
                "  System.out.println(\"\");",
                "//#endif",
                "  System.out.println(\"\");",
                "//#endif",
                "  System.out.println(\"\");");
        assertEquals(
                List.of("false", "A", "false", "A && B", "A && B", "false", "A && !B", "false", "A", "false", "true"),
                presenceConditions(lines));
    }

    @Test
    public void elifAnnotations() {
        List<String> lines = List.of(
                "//#if A",
                "  System.out.println(\"\");",
                "//#elif B",
                "  System.out.println(\"\");",
                "//#else",
                "  System.out.println(\"\");",
                "//#endif",
                "  System.out.println(\"\");");
        assertEquals(
                List.of(
                        "false", // //#if A
                        "A", // inside #if A
                        "false", // //#elif B
                        "!A && B", // inside #elif B
                        "false", // //#else
                        "!A && !B", // inside #else
                        "false", // //#endif
                        "true"), // outside all blocks
                presenceConditions(lines));
    }

    @Test
    public void noAnnotations() {
        assertEquals(List.of("true", "true"), presenceConditions(List.of("int a;", "int b;")));
    }

    @Test
    public void featureNotInModelIsReported() {
        List<ParseProblem> problems = new Preprocessor("//#", JavaSymbols.INSTANCE)
                .findUnknownFeatures(
                        Stream.of(
                                "//#if A",
                                "  System.out.println(\"\");",
                                "//#else",
                                "  System.out.println(\"\");",
                                "//#endif"),
                        loadFormula("GPL/model.xml"));

        assertEquals(1, problems.size());
        assertEquals("unknown feature \"A\"", problems.get(0).getMessage());
        assertEquals(Severity.ERROR, problems.get(0).getSeverity());
        assertEquals(1, problems.get(0).getLineNumber());
    }

    @Test
    public void knownFeaturesAreNotReported() {
        assertTrue(unknownFeatures(
                        "//#if Directed && !Weighted",
                        "a();",
                        "//#elif BFS || DFS",
                        "b();",
                        "//#else",
                        "c();",
                        "//#endif")
                .isEmpty());
    }

    @Test
    public void noAnnotationsHaveNoUnknownFeatures() {
        assertTrue(unknownFeatures("int x = 1;", "System.out.println(x);").isEmpty());
    }

    @Test
    public void unknownFeatureInElifIsReported() {
        assertEquals(
                List.of("line 3: unknown feature \"B\""),
                unknownFeatures("//#if Directed", "a();", "//#elif Undirected && B", "b();", "//#endif"));
    }

    @Test
    public void everyUnknownFeatureOfAnAnnotationIsReported() {
        assertEquals(
                List.of("line 1: unknown feature \"A\"", "line 1: unknown feature \"B\""),
                unknownFeatures("//#if A && Base || B", "a();", "//#endif"));
    }

    @Test
    public void unknownFeatureInNestedAnnotationIsReported() {
        assertEquals(
                List.of("line 3: unknown feature \"C\""),
                unknownFeatures("//#if Base", "a();", "//#if C", "b();", "//#endif", "//#endif"));
    }

    @Test
    public void sameUnknownFeatureIsReportedOnEveryLine() {
        assertEquals(
                List.of("line 1: unknown feature \"A\"", "line 4: unknown feature \"A\""),
                unknownFeatures("//#if A", "a();", "//#endif", "//#if !A", "b();", "//#endif"));
    }

    private static List<String> unknownFeatures(String... lines) {
        return new Preprocessor("//#", JavaSymbols.INSTANCE)
                .findUnknownFeatures(Stream.of(lines), loadFormula("GPL/model.xml")).stream()
                        .map(p -> String.format("line %d: %s", p.getLineNumber(), p.getMessage()))
                        .collect(Collectors.toList());
    }

    private static List<String> presenceConditions(List<String> lines) {
        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setSymbols(JavaSymbols.INSTANCE);
        return new Preprocessor("//#", JavaSymbols.INSTANCE)
                .computePresenceConditions(lines.stream()).stream()
                        .map(pc -> Trees.traverse(pc, serializer).orElseThrow())
                        .collect(Collectors.toList());
    }
}
