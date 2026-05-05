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

public class FeatureModelFormatConversionTest {

    @Test
    void convertToXML() throws IOException {
        // TODO implement
    }

    private void writeAndCompare(String input, String output, String format) throws IOException {
        Path tempFile = Files.createTempFile("featJarTest", "");
        int exitCode = FeatJAR.runTest(
                "convert-model",
                "--input",
                "src/test/resources/de/featjar/feature/model/" + input,
                "--output-format",
                format,
                "--overwrite",
                "--output",
                tempFile.toString());
        Assertions.assertEquals(0, exitCode);
        byte[] expected = Files.readAllBytes(Path.of("src/test/resources/de/featjar/feature/model/" + output));
        byte[] actual = Files.readAllBytes(tempFile);
        Assertions.assertTrue(Objects.deepEquals(expected, actual));
    }
}
