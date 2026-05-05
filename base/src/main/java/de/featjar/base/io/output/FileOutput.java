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
package de.featjar.base.io.output;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A physical file output.
 *
 * @author Elias Kuiter
 */
public class FileOutput extends AOutput {

    /**
     * Creates a physical file output. This method creates non-existent files and replaces existing ones.
     *
     * @param path    the path
     * @param charset the charset
     * @throws IOException if an I/O error occurs
     *
     * @see FileOutput#FileOutput(Path, Charset, boolean, boolean)
     */
    public FileOutput(Path path, Charset charset) throws IOException {
        this(path, charset, true, true);
    }

    /**
     * Creates a physical file output.
     *
     * @param path    the path
     * @param charset the charset
     * @param overwrite whether an existing file should be replaced or appended to
     * @param createNewFiles whether a non-existing file should be created
     * @throws IOException if an I/O error occurs
     */
    public FileOutput(Path path, Charset charset, boolean overwrite, boolean createNewFiles) throws IOException {
        super(newOutputStream(path, overwrite, createNewFiles), charset);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static OutputStream newOutputStream(Path path, boolean overwrite, boolean createNewFiles)
            throws IOException {
        final Path parent = path.getParent();
        if (parent != null) {
            if (createNewFiles) {
                Files.createDirectories(parent);
            } else {
                throw new IOException(String.format("Path %s does not exist", parent.toString()));
            }
        }
        OpenOption appendOption = overwrite ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.APPEND;
        return new BufferedOutputStream(
                createNewFiles
                        ? Files.newOutputStream(path, StandardOpenOption.WRITE, appendOption, StandardOpenOption.CREATE)
                        : Files.newOutputStream(path, StandardOpenOption.WRITE, appendOption));
    }
}
