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
package de.featjar;

import de.featjar.base.FeatJAR;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SAT4JCommandsTest {

    @Test
    void testProjectionCommand() throws IOException {
        Path tempFile = Files.createTempFile("featJarTest", ".txt");
        int exitCode = FeatJAR.runTest(
                "projection-sat4j",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--slice",
                "DirectedWithEdges,DirectedWithNeighbors",
                "--output",
                tempFile.toString(),
                "--overwrite");
        Assertions.assertEquals(0, exitCode);
        String output = Files.readString(tempFile);
        Assertions.assertFalse(output.contains("DirectedWithEdges"));
        Assertions.assertFalse(output.contains("DirectedWithNeighbors"));
        Assertions.assertTrue(output.contains("DirectedOnlyVertices"));
    }

    @Test
    void testProjectionCommand2() throws IOException {
        Path tempFile = Files.createTempFile("featJarTest", ".txt");
        int exitCode = FeatJAR.runTest(
                "projection-sat4j",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--project",
                "DirectedOnlyVertices,UndirectedWithEdges,UndirectedWithNeighbors,UndirectedOnlyVertices",
                "--output",
                tempFile.toString(),
                "--overwrite",
                "--print-stacktrace");
        Assertions.assertEquals(0, exitCode);
        String output = Files.readString(tempFile);
        Assertions.assertFalse(output.contains("DirectedWithEdges"));
        Assertions.assertFalse(output.contains("DirectedWithNeighbors"));
        Assertions.assertTrue(output.contains("DirectedOnlyVertices"));
        Assertions.assertTrue(output.contains("UndirectedWithEdges"));
        Assertions.assertTrue(output.contains("UndirectedWithNeighbors"));
        Assertions.assertTrue(output.contains("UndirectedOnlyVertices"));
        Assertions.assertTrue(output.split("\n").length > 0);
        Assertions.assertEquals(6, output.split("\n")[0].split(";").length);
    }

    @Test
    void testProjectionCommand3() throws IOException {
        Path tempFile = Files.createTempFile("featJarTest", ".txt");
        int exitCode = FeatJAR.runTest(
                "projection-sat4j",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--project",
                "DirectedWithEdges,DirectedWithNeighbors,DirectedOnlyVertices,UndirectedWithEdges,UndirectedWithNeighbors,UndirectedOnlyVertices",
                "--slice",
                "DirectedWithEdges,DirectedWithNeighbors",
                "--output",
                tempFile.toString(),
                "--overwrite",
                "--print-stacktrace");
        Assertions.assertEquals(0, exitCode);
        String output = Files.readString(tempFile);
        Assertions.assertFalse(output.contains("DirectedWithEdges"));
        Assertions.assertFalse(output.contains("DirectedWithNeighbors"));
        Assertions.assertTrue(output.contains("DirectedOnlyVertices"));
        Assertions.assertTrue(output.contains("UndirectedWithEdges"));
        Assertions.assertTrue(output.contains("UndirectedWithNeighbors"));
        Assertions.assertTrue(output.contains("UndirectedOnlyVertices"));
        Assertions.assertTrue(output.contains("DirectedOnlyVertices"));
        Assertions.assertTrue(output.split("\n").length > 0);
        Assertions.assertEquals(6, output.split("\n")[0].split(";").length);
    }

    @Test
    void testCoreCommand() throws IOException {
        int exitCode = FeatJAR.runTest("core-sat4j", "--input", "../formula/src/testFixtures/resources/GPL/model.xml");
        Assertions.assertEquals(0, exitCode);

        exitCode = FeatJAR.runTest(
                "core-sat4j",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--seed",
                "0",
                "--solver_timeout",
                "10",
                "--non-parallel",
                "true",
                "--timeout",
                "10");
        Assertions.assertEquals(0, exitCode);
    }

    @Test
    void testAtomicSetsCommand() throws IOException {
        int exitCode =
                FeatJAR.runTest("atomic-sets-sat4j", "--input", "../formula/src/testFixtures/resources/GPL/model.xml");
        Assertions.assertEquals(0, exitCode);

        exitCode = FeatJAR.runTest(
                "atomic-sets-sat4j",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--seed",
                "0",
                "--solver_timeout",
                "10",
                "--non-parallel",
                "true",
                "--timeout",
                "10");
        Assertions.assertEquals(0, exitCode);
    }

    @Test
    void testSolutionCountCommand() throws IOException {
        int exitCode = FeatJAR.runTest("count-sat4j", "--input", "../formula/src/testFixtures/resources/GPL/model.xml");
        Assertions.assertEquals(0, exitCode);

        exitCode = FeatJAR.runTest(
                "count-sat4j",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--seed",
                "0",
                "--solver_timeout",
                "10",
                "--non-parallel",
                "true",
                "--timeout",
                "10");
        Assertions.assertEquals(0, exitCode);
    }

    @Test
    void testSolutionsCommand() throws IOException {
        int exitCode =
                FeatJAR.runTest("solutions-sat4j", "--input", "../formula/src/testFixtures/resources/GPL/model.xml");
        Assertions.assertEquals(0, exitCode);

        exitCode = FeatJAR.runTest(
                "solutions-sat4j",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--seed",
                "0",
                "--solver_timeout",
                "10",
                "--n",
                "10",
                "--strategy",
                "negative",
                "--no-duplicates",
                "true",
                "--non-parallel",
                "true",
                "--timeout",
                "10");
        Assertions.assertEquals(0, exitCode);
    }

    @Test
    void testTWiseCommand() throws IOException {
        int exitCode = FeatJAR.runTest("yasa", "--input", "../formula/src/testFixtures/resources/GPL/model.xml");
        Assertions.assertEquals(0, exitCode);

        exitCode = FeatJAR.runTest(
                "yasa",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--seed",
                "0",
                "--solver_timeout",
                "1000",
                "--n",
                "2",
                "--t",
                "4",
                "--i",
                "2",
                "--non-parallel",
                "true",
                "--timeout",
                "100");
        Assertions.assertEquals(0, exitCode);

        exitCode = FeatJAR.runTest(
                "yasa",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--seed",
                "0",
                "--solver_timeout",
                "1000",
                "--n",
                "10",
                "--t",
                "5",
                "--i",
                "100",
                "--timeout",
                "1");
        Assertions.assertEquals(1, exitCode);
    }
}
