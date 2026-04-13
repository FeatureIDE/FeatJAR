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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Maps virtual paths to stream inputs.
 *
 * @author Elias Kuiter
 */
public class StreamInputMapper extends AInputMapper {
    /**
     * Creates a stream input mapper for a collection of streams.
     *
     * @param pathInputStreamMap the map of paths to inputs
     * @param rootPath           the root path
     * @param mainPath           the main path
     * @param charset            the charset
     * @param fileExtension      the file extension
     */
    public StreamInputMapper(
            LinkedHashMap<Path, InputStream> pathInputStreamMap,
            Path rootPath,
            Path mainPath,
            Charset charset,
            java.lang.String fileExtension) {
        super(relativizeRootPath(rootPath, mainPath));
        checkParameters(pathInputStreamMap.keySet(), rootPath, mainPath);
        for (Entry<Path, InputStream> entry : pathInputStreamMap.entrySet()) {
            ioMap.put(
                    relativizeRootPath(rootPath, entry.getKey()),
                    new StreamInput(entry.getValue(), charset, fileExtension));
        }
    }

    /**
     * Creates a stream input mapper for a single stream.
     *
     * @param inputStream   the input stream
     * @param charset       the charset
     * @param fileExtension the file extension
     */
    public StreamInputMapper(InputStream inputStream, Charset charset, java.lang.String fileExtension) {
        this(Maps.of(DEFAULT_MAIN_PATH, inputStream), null, DEFAULT_MAIN_PATH, charset, fileExtension);
    }
}
