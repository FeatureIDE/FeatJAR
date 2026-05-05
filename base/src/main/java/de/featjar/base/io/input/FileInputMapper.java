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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

/**
 * Maps physical paths to physical file inputs.
 *
 * @author Elias Kuiter
 */
public class FileInputMapper extends AInputMapper {
    /**
     * Creates a file input mapper for a collection of files.
     *
     * @param paths    the list of file paths
     * @param rootPath the root path
     * @param mainPath the main path
     * @param charset  the charset
     * @throws IOException if an I/O error has occurred
     */
    public FileInputMapper(List<Path> paths, Path rootPath, Path mainPath, Charset charset) throws IOException {
        super(relativizeRootPath(rootPath, mainPath));
        checkParameters(paths, rootPath, mainPath);
        for (Path currentPath : paths) {
            ioMap.put(relativizeRootPath(rootPath, currentPath), new FileInput(currentPath, charset));
        }
    }

    /**
     * Creates a file input mapper for a single file or file hierarchy.
     *
     * @param mainPath the main path
     * @param charset  the charset
     * @throws IOException if an I/O error has occurred
     */
    public FileInputMapper(Path mainPath, Charset charset) throws IOException {
        super(mainPath);
        ioMap.put(mainPath, new FileInput(mainPath, charset));
    }
}
