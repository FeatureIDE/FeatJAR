/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j;

import static de.featjar.analysis.sat4j.PreprocessorAnalyzer.Inclusion.ALWAYS;
import static de.featjar.analysis.sat4j.PreprocessorAnalyzer.Inclusion.NEVER;
import static de.featjar.analysis.sat4j.PreprocessorAnalyzer.Inclusion.SOMETIMES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.featjar.AnalysisTest;
import de.featjar.analysis.sat4j.PreprocessorAnalyzer.Inclusion;
import de.featjar.analysis.sat4j.cli.PreprocessorAnalyzerCommand;
import de.featjar.base.cli.OptionParser;
import de.featjar.base.io.IO;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.io.textual.CPPAssignmentFormat;
import de.featjar.formula.io.textual.JavaSymbols;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Uses the GPL feature model: Directed/Undirected and Weighted/Unweighted are alternatives, Base is mandatory.
 */
public class DeadCodeTest extends AnalysisTest {

    @Test
    public void contradictionIsDead() {
        assertEquals(
                List.of("Dead code at lines 2-2: Directed && !Directed"),
                dead("//#if Directed && !Directed", "a();", "//#endif"));
    }

    @Test
    public void alternativesTogetherAreDead() {
        assertEquals(
                List.of("Dead code at lines 2-3: Directed && Undirected"),
                dead("//#if Directed && Undirected", "a();", "b();", "//#endif"));
    }

    @Test
    public void elseOfMandatoryFeatureIsDead() {
        assertEquals(
                List.of("Dead code at lines 4-4: !Base"), dead("//#if Base", "a();", "//#else", "b();", "//#endif"));
    }

    @Test
    public void elseAfterAllAlternativesIsDead() {
        assertEquals(
                List.of("Dead code at lines 6-6: !Weighted && !Unweighted"),
                dead("//#if Weighted", "a();", "//#elif Unweighted", "b();", "//#else", "c();", "//#endif"));
    }

    @Test
    public void nestedBlockIsDeadBecauseOfOuterCondition() {
        assertEquals(
                List.of("Dead code at lines 4-4: Directed && Undirected"),
                dead("//#if Directed", "a();", "//#if Undirected", "b();", "//#endif", "c();", "//#endif"));
    }

    @Test
    public void satisfiableBlocksAreNotDead() {
        assertEquals(
                List.of(),
                dead(
                        "x();",
                        "//#if BFS || DFS",
                        "a();",
                        "//#elif !Base",
                        "//#endif",
                        "//#if Directed",
                        "b();",
                        "//#endif"));
    }

    // ------------------------------------------------------------------
    // Additional tests using features from the GPL model
    // ------------------------------------------------------------------

    @Test
    public void weightedAndUnweightedTogetherAreDead() {
        assertEquals(
                List.of("Dead code at lines 2-2: Weighted && Unweighted"),
                dead("//#if Weighted && Unweighted", "a();", "//#endif"));
    }

    @Test
    public void bfsAndDfsTogetherAreDead() {
        assertEquals(List.of("Dead code at lines 2-2: BFS && DFS"), dead("//#if BFS && DFS", "a();", "//#endif"));
    }

    @Test
    public void mstPrimAndMstKruskalTogetherAreDead() {
        assertEquals(
                List.of("Dead code at lines 2-2: MSTPrim && MSTKruskal"),
                dead("//#if MSTPrim && MSTKruskal", "a();", "//#endif"));
    }

    @Test
    public void stronglyConnectedWithUndirectedIsDead() {
        // StrongC -> Directed, and Directed/Undirected are alternatives.
        assertEquals(
                List.of("Dead code at lines 2-2: StronglyConnected && Undirected"),
                dead("//#if StronglyConnected && Undirected", "a();", "//#endif"));
    }

    @Test
    public void multipleDeadBlocksAreAllReported() {
        assertEquals(
                List.of("Dead code at lines 2-2: Weighted && Unweighted", "Dead code at lines 5-5: BFS && DFS"),
                dead("//#if Weighted && Unweighted", "a();", "//#endif", "//#if BFS && DFS", "b();", "//#endif"));
    }

    @Test
    public void inclusionsForPartialConfiguration() {
        assertEquals(
                List.of(NEVER, ALWAYS, NEVER, NEVER, NEVER, NEVER, SOMETIMES, NEVER),
                inclusions(
                        new Assignment("A", true),
                        "//#if A || B",
                        "a();",
                        "//#else",
                        "b();",
                        "//#endif",
                        "//#if A && B",
                        "c();",
                        "//#endif"));
    }

    @Test
    public void inclusionsUseTheFeatureModel() {
        // Directed excludes its alternative Undirected, and Base is mandatory.
        assertEquals(
                List.of(NEVER, NEVER, NEVER, NEVER, ALWAYS, NEVER),
                inclusions(
                        new Assignment("Directed", true),
                        "//#if Undirected",
                        "a();",
                        "//#endif",
                        "//#if Base",
                        "b();",
                        "//#endif"));
    }

    @Test
    public void inclusionsForNestedAndElifAnnotations() {
        assertEquals(
                List.of(NEVER, NEVER, NEVER, NEVER, NEVER, NEVER, SOMETIMES, NEVER, NEVER, ALWAYS),
                inclusions(
                        new Assignment("A", false),
                        "//#if A",
                        "//#if B",
                        "a();",
                        "//#endif",
                        "//#elif B",
                        "//#if C",
                        "b();",
                        "//#endif",
                        "//#endif",
                        "c();"));
    }

    @Test
    public void contradictionsAndTautologiesAreDecidedWithoutAssignments() {
        assertEquals(
                List.of(NEVER, NEVER, NEVER, ALWAYS, NEVER, NEVER, NEVER),
                inclusions(
                        new Assignment(),
                        "//#if A && !A",
                        "never",
                        "//#elif A || !A",
                        "always",
                        "//#else",
                        "never",
                        "//#endif"));
    }

    @Test
    public void contradictionsAcrossNestedConditionsAreNeverIncluded() {
        assertEquals(
                List.of(NEVER, NEVER, NEVER, NEVER, SOMETIMES, NEVER, NEVER),
                inclusions(
                        new Assignment(),
                        "//#if A",
                        "//#if !A",
                        "never",
                        "//#else",
                        "sometimes",
                        "//#endif",
                        "//#endif"));
    }

    @Test
    public void reasoningUsesBothPositiveAndNegativeAssignments() {
        String[] lines = {"//#if (A || B) && (!A || B)", "code", "//#endif"};
        assertEquals(List.of(NEVER, ALWAYS, NEVER), inclusions(new Assignment("B", true), lines));
        assertEquals(List.of(NEVER, NEVER, NEVER), inclusions(new Assignment("B", false), lines));
        assertEquals(List.of(NEVER, SOMETIMES, NEVER), inclusions(new Assignment(), lines));
    }

    @Test
    public void unassignedVariablesAndUnusedAssignmentsRemainIndependent() {
        assertEquals(
                List.of(NEVER, SOMETIMES, NEVER, NEVER, NEVER, ALWAYS, ALWAYS),
                inclusions(
                        new Assignment("Unused", true, "A", null, "B", false),
                        "//#if A",
                        "sometimes",
                        "//#elif B",
                        "never",
                        "//#endif",
                        "",
                        "plain"));
    }

    @Test
    public void linesWithoutAnnotationsAreAlwaysIncluded() {
        assertEquals(List.of(ALWAYS, ALWAYS), inclusions(new Assignment(), "", "code"));
        assertEquals(List.of(), inclusions(new Assignment()));
    }

    @Test
    public void nonBooleanAssignmentsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> inclusions(new Assignment("A", 1), "//#if A", "code", "//#endif"));
    }

    @Test
    public void undefInConfigurationIsFalse() {
        CPPAssignmentFormat format = new CPPAssignmentFormat();
        Assignment assignment = IO.load("#define A\n#undef B\n", format).orElseThrow();
        assertEquals(List.of(NEVER, NEVER, NEVER), inclusions(assignment, "//#if A && B", "code", "//#endif"));
        assertEquals("#define A\n#undef B", format.serialize(assignment).orElseThrow());
    }

    @Test
    public void commandPrintsInclusionsWithSelectedStyle(@TempDir Path directory) throws IOException {
        Path input = Files.write(
                directory.resolve("input.java"),
                List.of(
                        "plain",
                        "/*if[A && B]*/",
                        "sometimes",
                        "/*end[A && B]*/",
                        "/*if[!A]*/",
                        "never",
                        "/*end[!A]*/"));
        Path configuration = Files.writeString(directory.resolve("config.h"), "#define A\n");
        Path featureModel = Files.writeString(directory.resolve("model.dimacs"), "p cnf 1 1\n1 0\n");
        Path output = directory.resolve("output.txt");
        PreprocessorAnalyzerCommand command = new PreprocessorAnalyzerCommand();
        OptionParser options = new OptionParser(
                command.getOptions(),
                "--mode",
                "PRINT_INCLUSIONS",
                "--annotation-style",
                "MUNGE",
                "--input",
                input.toString(),
                "--configuration",
                configuration.toString(),
                "--feature-model",
                featureModel.toString(),
                "--output",
                output.toString());
        assertEquals(List.of(), options.parseArguments());
        assertEquals(0, command.run(options));
        assertEquals(
                List.of(
                        "ALWAYS    plain",
                        "NEVER     /*if[A && B]*/",
                        "SOMETIMES sometimes",
                        "NEVER     /*end[A && B]*/",
                        "NEVER     /*if[!A]*/",
                        "NEVER     never",
                        "NEVER     /*end[!A]*/"),
                Files.readAllLines(output));
    }

    private static List<Inclusion> inclusions(Assignment assignment, String... lines) {
        return new PreprocessorAnalyzer("//#", JavaSymbols.INSTANCE)
                .computeInclusions(Stream.of(lines), loadFormula("GPL/model.xml"), assignment);
    }

    private static List<String> dead(String... lines) {
        return new PreprocessorAnalyzer("//#", JavaSymbols.INSTANCE)
                .findDeadCode(Stream.of(lines), loadFormula("GPL/model.xml"));
    }
}
