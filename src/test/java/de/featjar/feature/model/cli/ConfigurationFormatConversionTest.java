/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.cli;

import de.featjar.base.FeatJAR;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Kilian Hüppe
 * @author Knut Köhnlein
 */
public class ConfigurationFormatConversionTest {

    @Test
    void convertToSimpleBinary() throws IOException {
        writeAndCompare("solutions_dimacs.dimacs", "solutions_simple_binary.bin", "SimpleBinary");
        writeAndCompare("solutions_grouped_binary.bin", "solutions_simple_binary.bin", "SimpleBinary");
        writeAndCompare("solutions_grouped_csv.csv", "solutions_simple_binary.bin", "SimpleBinary");
        writeAndCompare("solutions_groups_grouped_csv.csv", "solutions_simple_binary.bin", "SimpleBinary");
        writeAndCompare("solutions_simple_binary.bin", "solutions_simple_binary.bin", "SimpleBinary");
        writeAndCompare("solutions_simple_csv.csv", "solutions_simple_binary.bin", "SimpleBinary");
    }

    @Test
    void convertToGroupedBinary() throws IOException {
        writeAndCompare("solutions_dimacs.dimacs", "solutions_grouped_binary.bin", "GroupedBinary");
        writeAndCompare("solutions_grouped_binary.bin", "solutions_grouped_binary.bin", "GroupedBinary");
        writeAndCompare("solutions_grouped_csv.csv", "solutions_grouped_binary.bin", "GroupedBinary");
        writeAndCompare("solutions_simple_binary.bin", "solutions_grouped_binary.bin", "GroupedBinary");
        writeAndCompare("solutions_simple_csv.csv", "solutions_grouped_binary.bin", "GroupedBinary");
    }

    @Test
    void convertToSimpleCSV() throws IOException {
        writeAndCompare("solutions_dimacs.dimacs", "solutions_simple_csv.csv", "SimpleCSV");
        writeAndCompare("solutions_grouped_binary.bin", "solutions_simple_csv.csv", "SimpleCSV");
        writeAndCompare("solutions_grouped_csv.csv", "solutions_simple_csv.csv", "SimpleCSV");
        writeAndCompare("solutions_groups_grouped_csv.csv", "solutions_simple_csv.csv", "SimpleCSV");
        writeAndCompare("solutions_simple_binary.bin", "solutions_simple_csv.csv", "SimpleCSV");
        writeAndCompare("solutions_simple_csv.csv", "solutions_simple_csv.csv", "SimpleCSV");
    }

    @Test
    void convertToGroupedCSV() throws IOException {
        writeAndCompare("solutions_dimacs.dimacs", "solutions_grouped_csv.csv", "GroupedCSV");
        writeAndCompare("solutions_grouped_binary.bin", "solutions_grouped_csv.csv", "GroupedCSV");
        writeAndCompare("solutions_grouped_csv.csv", "solutions_grouped_csv.csv", "GroupedCSV");
        writeAndCompare("solutions_groups_grouped_csv.csv", "solutions_groups_grouped_csv.csv", "GroupedCSV");
        writeAndCompare("solutions_simple_binary.bin", "solutions_grouped_csv.csv", "GroupedCSV");
        writeAndCompare("solutions_simple_csv.csv", "solutions_grouped_csv.csv", "GroupedCSV");
    }

    @Test
    void convertToDIMACS() throws IOException {
        writeAndCompare("solutions_dimacs.dimacs", "solutions_dimacs.dimacs", "DIMACS");
        writeAndCompare("solutions_grouped_binary.bin", "solutions_dimacs.dimacs", "DIMACS");
        writeAndCompare("solutions_grouped_csv.csv", "solutions_dimacs.dimacs", "DIMACS");
        writeAndCompare("solutions_groups_grouped_csv.csv", "solutions_groups_dimacs.dimacs", "DIMACS");
        writeAndCompare("solutions_simple_binary.bin", "solutions_dimacs.dimacs", "DIMACS");
        writeAndCompare("solutions_simple_csv.csv", "solutions_dimacs.dimacs", "DIMACS");
    }

    private void writeAndCompare(String input, String output, String format) throws IOException {
        Path tempFile = Files.createTempFile("featJarTest", "");
        int exitCode = FeatJAR.runTest(
                "convert-configuration",
                "--input",
                "src/test/resources/de/featjar/feature/configuration/" + input,
                "--output-format",
                format,
                "--overwrite",
                "--output",
                tempFile.toString());
        Assertions.assertEquals(0, exitCode);
        byte[] expected = Files.readAllBytes(Path.of("src/test/resources/de/featjar/feature/configuration/" + output));
        byte[] actual = Files.readAllBytes(tempFile);
        Assertions.assertTrue(Objects.deepEquals(expected, actual));
    }
}
