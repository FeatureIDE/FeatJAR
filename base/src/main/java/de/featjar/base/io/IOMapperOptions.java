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
package de.featjar.base.io;

import de.featjar.base.io.input.FileInput;
import de.featjar.base.io.output.AOutputMapper;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * Options for an {@link AIOMapper}.
 *
 * @author Elias Kuiter
 */
public enum IOMapperOptions {
    /**
     * Whether to map not only the given main file, but also all other files residing in the same directory.
     * Only supported for parsing {@link FileInput} objects.
     */
    INPUT_FILE_HIERARCHY,
    /**
     * Whether to use a single ZIP archive instead of (several) physical files.
     */
    ZIP_COMPRESSION,
    /**
     * Whether to create a single JAR archive instead of (several) physical files.
     * Only supported for writing with {@link AOutputMapper#of(Path, Charset, IOMapperOptions...)}.
     */
    OUTPUT_FILE_JAR
}
