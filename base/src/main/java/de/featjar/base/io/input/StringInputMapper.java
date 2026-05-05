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

import de.featjar.base.data.Maps;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Maps virtual paths to string inputs.
 *
 * @author Elias Kuiter
 */
public class StringInputMapper extends AInputMapper {
    /**
     * Creates a string input mapper for a collection of strings.
     *
     * @param pathStringMap the map of paths to inputs
     * @param rootPath      the root path
     * @param mainPath      the main path
     * @param charset       the charset
     * @param fileExtension the file extension
     */
    public StringInputMapper(
            LinkedHashMap<Path, java.lang.String> pathStringMap,
            Path rootPath,
            Path mainPath,
            Charset charset,
            java.lang.String fileExtension) {
        super(relativizeRootPath(rootPath, mainPath));
        checkParameters(pathStringMap.keySet(), rootPath, mainPath);
        for (Entry<Path, String> entry : pathStringMap.entrySet()) {
            ioMap.put(
                    relativizeRootPath(rootPath, entry.getKey()),
                    new StringInput(entry.getValue(), charset, fileExtension));
        }
    }

    /**
     * Creates a string input mapper for a single string.
     *
     * @param string        the string
     * @param charset       the charset
     * @param fileExtension the file extension
     */
    public StringInputMapper(java.lang.String string, Charset charset, java.lang.String fileExtension) {
        this(Maps.of(DEFAULT_MAIN_PATH, string), null, DEFAULT_MAIN_PATH, charset, fileExtension);
    }
}
