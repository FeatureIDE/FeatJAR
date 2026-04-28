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
package de.featjar.formula;

import de.featjar.base.FeatJAR;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FormulaCommandsTest {

    @Test
    void convertToDimcasCommandFails() throws IOException {
        Path tempFile = Files.createTempFile("featJarTest", ".txt");
        int exitCode = FeatJAR.runTest(
                "convert-formula",
                "--input",
                "src/testFixtures/resources/GPL/model.xml",
                "--output-format",
                "DIMACS",
                "--overwrite",
                "--output",
                tempFile.toString());
        Assertions.assertEquals(1, exitCode);
    }

    @Test
    void convertToCNFDimcasCommandSucceeds() throws IOException {
        Path tempFile = Files.createTempFile("featJarTest", ".txt");
        int exitCode = FeatJAR.runTest(
                "convert-formula",
                "--input",
                "src/testFixtures/resources/GPL/model.xml",
                "--output-format",
                "CNF-DIMACS",
                "--overwrite",
                "--output",
                tempFile.toString());
        Assertions.assertEquals(0, exitCode);
        Assertions.assertEquals(
                Files.readString(Path.of("./src/test/resources/testConvertFormatCommand.dimacs")),
                Files.readString(tempFile));
    }

    @Test
    void printFormulaWorksCorrectly() throws IOException {
        Path tempFile = Files.createTempFile("featJarTest", ".txt");
        int exitCode = FeatJAR.runTest(
                "print",
                "--input",
                "../formula/src/testFixtures/resources/GPL/model.xml",
                "--tab",
                "TAB",
                "--notation",
                "PREFIX",
                "--format",
                "de.featjar.formula.io.textual.JavaSymbols",
                "--newline",
                "NEWLINE",
                "--enforce-parentheses",
                "--enquote-whitespace",
                "--output",
                tempFile.toString());
        Assertions.assertEquals(0, exitCode);
        Assertions.assertEquals(
                Files.readString(Path.of("./src/test/resources/testPrintCommand")), Files.readString(tempFile));
    }
}
