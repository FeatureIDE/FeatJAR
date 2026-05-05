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
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps virtual paths to string outputs.
 *
 * @author Elias Kuiter
 */
public class StringOutputMapper extends AOutputMapper {
    protected final Charset charset;

    /**
     * Creates a file output mapper for a collection of strings.
     *
     * @param charset the charset
     */
    public StringOutputMapper(Charset charset) {
        super(Maps.of(DEFAULT_MAIN_PATH, new StringOutput(charset)), DEFAULT_MAIN_PATH);
        this.charset = charset;
    }

    @Override
    protected AOutput newOutput(Path path) {
        return new StringOutput(charset);
    }

    /**
     * {@return the collection of strings}
     */
    public LinkedHashMap<Path, java.lang.String> getOutputStrings() {
        return ioMap.entrySet().stream()
                .collect(Maps.toMap(
                        Map.Entry::getKey, e -> e.getValue().getOutputStream().toString()));
    }
}
