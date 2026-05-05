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
package de.featjar.base.env;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A temporary file that can be automatically deleted after use.
 *
 * @author Sebastian Krieter
 */
public class TempFile implements AutoCloseable {

    private final Path path;

    /**
     * Create a new temporary file. The file is created in the default
     * temporary-file directory and deleted when closed.
     *
     * @param prefix the prefix of the temporary file's name
     * @param suffix the suffix of the temporary file's name
     *
     * @throws IOException if an I/O error occurs
     */
    public TempFile(String prefix, String suffix) throws IOException {
        path = Files.createTempFile(prefix, suffix);
    }

    /**
     * {@return the path of this file}
     */
    public Path getPath() {
        return path;
    }

    @Override
    public void close() throws Exception {
        Files.deleteIfExists(path);
    }
}
