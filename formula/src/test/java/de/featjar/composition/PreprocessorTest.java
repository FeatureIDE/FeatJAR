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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.Common;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.tree.Trees;
import de.featjar.formula.assignment.Assignment;
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
    public void cppStyle() {
        List<String> lines = List.of("#if A", "a", "#elif B", "b", "#else", "c", "#endif");
        Preprocessor preprocessor = new Preprocessor(Preprocessor.Style.CPP);
        assertEquals(
                List.of("false", "A", "false", "!A && B", "false", "!A && !B", "false"),
                presenceConditions(preprocessor, lines));
        assertEquals(List.of("b"), preprocess(preprocessor, lines, new Assignment("A", false, "B", true)));
        assertEquals(List.of("A", "B"), preprocessor.extractVariableNames(lines.stream()));
    }

    @Test
    public void antennaStyle() {
        List<String> lines = List.of("//#if A && B", "a", "//#else", "b", "//#endif");
        Preprocessor preprocessor = new Preprocessor(Preprocessor.Style.ANTENNA);
        assertEquals(
                List.of("false", "A && B", "false", "!(A && B)", "false"), presenceConditions(preprocessor, lines));
        assertEquals(List.of("a"), preprocess(preprocessor, lines, new Assignment("A", true, "B", true)));
    }

    @Test
    public void mungeStyle() {
        List<String> lines = List.of("/*if[A]*/", "a", "/*else[A]*/", "b", "/*end[A]*/", "c");
        Preprocessor preprocessor = new Preprocessor(Preprocessor.Style.MUNGE);
        assertEquals(List.of("false", "A", "false", "!A", "false", "true"), presenceConditions(preprocessor, lines));
        assertEquals(List.of("b", "c"), preprocess(preprocessor, lines, new Assignment("A", false)));
        assertEquals(List.of("A"), preprocessor.extractVariableNames(lines.stream()));
        assertEquals(
                List.of("/*if[A]*/", "/*else[A]*/", "/*end[A]*/"), preprocessor.extractAnnotations(lines.stream()));
        assertEquals(List.of(), preprocessor.checkStructure(lines.stream()));
    }

    @Test
    public void mungeStyleIgnoresOtherStyles() {
        List<String> lines = List.of("#if A", "/* comment */", "//#endif");
        assertEquals(List.of(), new Preprocessor(Preprocessor.Style.MUNGE).extractAnnotations(lines.stream()));
    }

    @Test
    public void customStyle() {
        Preprocessor.Style style = new Preprocessor.Style(
                "<!--", "-->", "IF", "ELSEIF", "ELSE", "END", " ", "", false, JavaSymbols.INSTANCE);
        List<String> lines = List.of("<!-- IF A -->", "a", "<!-- ELSEIF B -->", "b", "<!-- END -->");
        Preprocessor preprocessor = new Preprocessor(style);
        assertEquals(List.of("false", "A", "false", "!A && B", "false"), presenceConditions(preprocessor, lines));
        assertEquals(List.of("b"), preprocess(preprocessor, lines, new Assignment("A", false, "B", true)));
        assertEquals(List.of(), preprocessor.checkSyntax(lines.stream()));
        assertEquals(List.of(), preprocessor.validate(lines.stream()));
    }

    @Test
    public void styleTextMatchesWhitespaceLiterally() {
        Preprocessor.Style style = new Preprocessor.Style(
                "< start >",
                "< end >",
                "IF FEATURE",
                "ELIF FEATURE",
                "ELSE BRANCH",
                "END BLOCK",
                " WHEN [",
                "] DONE ",
                false,
                JavaSymbols.INSTANCE);
        Preprocessor preprocessor = new Preprocessor(style);
        List<String> annotations = List.of(
                "< start >IF FEATURE WHEN [A] DONE < end >",
                "< start >ELIF FEATURE WHEN [B] DONE < end >",
                "< start >ELSE BRANCH< end >",
                "< start >END BLOCK< end >");
        assertEquals(annotations, preprocessor.extractAnnotations(annotations.stream()));
        assertEquals(List.of("A", "B"), preprocessor.extractVariableNames(annotations.stream()));
        assertEquals(List.of(), preprocessor.checkSyntax(annotations.stream()));
        assertEquals(List.of(), preprocessor.validate(annotations.stream()));

        for (String text : List.of(
                "< start >", "< end >", "IF FEATURE", "ELIF FEATURE", "ELSE BRANCH", "END BLOCK", "WHEN [", "] DONE")) {
            for (String whitespace : List.of("  ", "\t")) {
                List<String> changedAnnotations = annotations.stream()
                        .filter(line -> line.contains(text))
                        .map(line -> line.replace(text, text.replace(" ", whitespace)))
                        .collect(Collectors.toList());
                assertEquals(
                        List.of(),
                        preprocessor.extractAnnotations(changedAnnotations.stream()),
                        changedAnnotations.toString());
            }
        }
    }

    @Test
    public void conditionSeparatorMatchesWhitespaceLiterally() {
        Preprocessor preprocessor = new Preprocessor(Preprocessor.Style.CPP);
        List<ParseProblem> problems = preprocessor.checkSyntax(Stream.of("#if\tA", "#elif\tB"));
        assertEquals(2, problems.size());
        for (int i = 0; i < problems.size(); i++) {
            assertEquals(Severity.ERROR, problems.get(i).getSeverity());
            assertEquals(i + 1, problems.get(i).getLineNumber());
        }
        assertEquals(List.of(), preprocessor.extractAnnotations(Stream.of("#if\tA", "#elif\tB")));
    }

    @Test
    public void wrongStyleRecognizesNoAnnotations() {
        List<Preprocessor.Style> styles =
                List.of(Preprocessor.Style.CPP, Preprocessor.Style.ANTENNA, Preprocessor.Style.MUNGE);
        List<List<String>> files = List.of(
                List.of("#if A", "a", "#else", "b", "#endif"),
                List.of("//#if A", "a", "//#else", "b", "//#endif"),
                List.of("/*if[A]*/", "a", "/*else[A]*/", "b", "/*end[A]*/"));
        for (int i = 0; i < styles.size(); i++) {
            Preprocessor preprocessor = new Preprocessor(styles.get(i));
            for (int j = 0; j < files.size(); j++) {
                if (i == j) continue;
                List<String> lines = files.get(j);
                assertEquals(List.of(), preprocessor.extractAnnotations(lines.stream()));
                assertEquals(List.of(), preprocessor.extractVariableNames(lines.stream()));
                assertEquals(List.of(), preprocessor.checkSyntax(lines.stream()));
                assertEquals(List.of(), preprocessor.validate(lines.stream()));
                assertEquals(List.of(), preprocessor.checkStructure(lines.stream()));
                assertEquals(List.of("true", "true", "true", "true", "true"), presenceConditions(preprocessor, lines));
                assertEquals(lines, preprocess(preprocessor, lines, new Assignment("A", false)));
            }
        }
    }

    @Test
    public void wrongStyleWithSamePrefixReportsSyntaxErrors() {
        List<String> lines = List.of("/*if[A]*/", "a", "/*else[A]*/", "b", "/*end[A]*/");
        Preprocessor preprocessor = new Preprocessor(Preprocessor.Style.CPP.withPrefix("/*"));
        List<ParseProblem> problems = preprocessor.checkSyntax(lines.stream());
        assertEquals(3, problems.size());
        for (int i = 0; i < problems.size(); i++) {
            assertEquals(Severity.ERROR, problems.get(i).getSeverity());
            assertEquals(2 * i + 1, problems.get(i).getLineNumber());
            assertTrue(problems.get(i).getMessage().startsWith("Invalid annotation syntax:"));
        }
        assertEquals(List.of(), preprocessor.extractAnnotations(lines.stream()));
    }

    @Test
    public void wrongStyleReportsUnbalancedAnnotations() {
        List<String> lines = List.of("#if A", "a", "//#endif");
        Preprocessor preprocessor = new Preprocessor(Preprocessor.Style.ANTENNA);
        List<Problem> problems = preprocessor.checkStructure(lines.stream());
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).getMessage().startsWith("#endif without #if"));
        assertEquals(Severity.ERROR, problems.get(0).getSeverity());
        assertEquals(3, ((ParseProblem) problems.get(0)).getLineNumber());
        assertThrows(IllegalArgumentException.class, () -> preprocessor.computePresenceConditions(lines.stream()));
    }

    @Test
    public void wrongStyleReportsMissingEndif() {
        List<String> lines = List.of("#if A", "a", "//#endif");
        Preprocessor preprocessor = new Preprocessor(Preprocessor.Style.CPP);
        List<Problem> problems = preprocessor.checkStructure(lines.stream());
        assertEquals(1, problems.size());
        assertTrue(problems.get(0).getMessage().startsWith("#if has no matching #endif"));
        assertEquals(Severity.ERROR, problems.get(0).getSeverity());
        assertEquals(1, ((ParseProblem) problems.get(0)).getLineNumber());
    }

    @Test
    public void prefixAndSuffixWithSymbolCharacters() {
        Preprocessor.Style style =
                new Preprocessor.Style("(*", "*)", "if", "elif", "else", "endif", " ", "", false, JavaSymbols.INSTANCE);
        List<String> lines = List.of("(* if !(A && B) *)", "a", "(* elif A || B *)", "b", "(* endif *)");
        Preprocessor preprocessor = new Preprocessor(style);
        assertEquals(List.of("A", "B"), preprocessor.extractVariableNames(lines.stream()));
        assertEquals(List.of("a"), preprocess(preprocessor, lines, new Assignment("A", false, "B", false)));
        assertEquals(List.of("b"), preprocess(preprocessor, lines, new Assignment("A", true, "B", true)));

        List<String> negationPrefix = List.of("!if !A", "a", "!endif");
        assertEquals(
                List.of("a"),
                preprocess(new Preprocessor("!", JavaSymbols.INSTANCE), negationPrefix, new Assignment("A", false)));
    }

    @Test
    public void formulaOperatorsAsPrefixAndSuffix() {
        for (String delimiter : List.of("!", "&&", "||", "==", "(", ")")) {
            Preprocessor.Style style = new Preprocessor.Style(
                    delimiter, delimiter, "if", "elif", "else", "endif", " ", "", false, JavaSymbols.INSTANCE);
            Preprocessor preprocessor = new Preprocessor(style);
            List<String> lines = List.of(
                    delimiter + "if !(A && B) || C" + delimiter,
                    "a",
                    delimiter + "else" + delimiter,
                    "b",
                    delimiter + "endif" + delimiter,
                    "c");
            assertEquals(List.of("A", "B", "C"), preprocessor.extractVariableNames(lines.stream()), delimiter);
            assertEquals(List.of(), preprocessor.checkSyntax(lines.stream()), delimiter);
            assertEquals(List.of(), preprocessor.validate(lines.stream()), delimiter);
            assertEquals(List.of(), preprocessor.checkStructure(lines.stream()), delimiter);
            assertEquals(
                    List.of("a", "c"),
                    preprocess(preprocessor, lines, new Assignment("A", false, "B", true, "C", false)),
                    delimiter);
            assertEquals(
                    List.of("b", "c"),
                    preprocess(preprocessor, lines, new Assignment("A", true, "B", true, "C", false)),
                    delimiter);
        }
    }

    @Test
    public void emptyPrefixIsRejected() {
        IllegalArgumentException constructorException =
                assertThrows(IllegalArgumentException.class, () -> new Preprocessor("", JavaSymbols.INSTANCE));
        assertEquals("annotation prefix must not be empty", constructorException.getMessage());
        IllegalArgumentException styleException = assertThrows(
                IllegalArgumentException.class,
                () -> new Preprocessor.Style(
                        "", "", "if", "elif", "else", "endif", " ", "", false, JavaSymbols.INSTANCE));
        assertEquals("annotation prefix must not be empty", styleException.getMessage());
        for (Preprocessor.Style style :
                List.of(Preprocessor.Style.CPP, Preprocessor.Style.ANTENNA, Preprocessor.Style.MUNGE)) {
            IllegalArgumentException prefixException =
                    assertThrows(IllegalArgumentException.class, () -> style.withPrefix(""));
            assertEquals("annotation prefix must not be empty", prefixException.getMessage());
        }
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
        return presenceConditions(new Preprocessor("//#", JavaSymbols.INSTANCE), lines);
    }

    private static List<String> presenceConditions(Preprocessor preprocessor, List<String> lines) {
        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setSymbols(JavaSymbols.INSTANCE);
        return preprocessor.computePresenceConditions(lines.stream()).stream()
                .map(pc -> Trees.traverse(pc, serializer).orElseThrow())
                .collect(Collectors.toList());
    }

    private static List<String> preprocess(Preprocessor preprocessor, List<String> lines, Assignment assignment) {
        return preprocessor.preprocess(lines.stream(), assignment).collect(Collectors.toList());
    }
}
