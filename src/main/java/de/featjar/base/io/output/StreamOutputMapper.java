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

import de.featjar.base.data.Maps;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Maps virtual paths to stream outputs.
 *
 * @author Elias Kuiter
 */
public class StreamOutputMapper extends AOutputMapper {
    protected StreamOutputMapper(LinkedHashMap<Path, AOutput> ioMap, Path mainPath) {
        super(ioMap, mainPath);
    }

    /**
     * Creates a stream output mapper for a single stream.
     *
     * @param outputStream the output stream
     * @param charset      the charset
     */
    public StreamOutputMapper(OutputStream outputStream, Charset charset) {
        super(Maps.of(DEFAULT_MAIN_PATH, new StreamOutput(outputStream, charset)), DEFAULT_MAIN_PATH);
    }

    @Override
    protected AOutput newOutput(Path path) {
        throw new UnsupportedOperationException("cannot guess kind of requested output stream");
    }
}
