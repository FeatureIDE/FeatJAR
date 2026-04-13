/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.io.input;

import de.featjar.base.io.IIOObject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipInputStream;

/**
 * A stream input.
 *
 * @author Elias Kuiter
 */
public class ZIPEntryInput extends AInput {

    /**
     * Creates a physical file input.
     *
     * @param path    the path
     * @param charset the charset
     * @throws IOException if an I/O error occurs
     */
    public ZIPEntryInput(Path path, Charset charset) throws IOException {
        super(openZipStream(path, charset), charset, removeZipExtension(path));
    }

    private static ZipInputStream openZipStream(Path path, Charset charset) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(path, StandardOpenOption.READ), charset);
        zipStream.getNextEntry();
        return zipStream;
    }

    private static String removeZipExtension(Path path) {
        String fileName = path.getFileName().toString();
        return IIOObject.getFileExtension(
                        fileName.endsWith(".zip") ? fileName.substring(0, fileName.length() - 4) : fileName)
                .orElse(null);
    }
}
