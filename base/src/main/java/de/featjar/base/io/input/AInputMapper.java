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

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.AIOMapper;
import de.featjar.base.io.IOMapperOptions;
import de.featjar.base.io.format.IFormat;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Maps paths to inputs.
 * Can represent a single input (e.g., one physical file) or a file hierarchy
 * (e.g., physical files referring to each other).
 *
 * @author Elias Kuiter
 */
public abstract class AInputMapper extends AIOMapper<AInput> {
    protected AInputMapper(Path mainPath) {
        super(mainPath);
    }

    protected AInputMapper(LinkedHashMap<Path, AInput> ioMap, Path mainPath) {
        super(ioMap, mainPath);
    }

    /**
     * Temporarily shifts the focus of this input mapper to another main path to execute some function.
     * Useful to parse a {@link IFormat} recursively.
     *
     * @param newMainPath the new main path
     * @param supplier the supplier
     * @return the result of the supplier
     * @param <T> the type of the supplier's result
     */
    public <T> Result<T> withMainPath(Path newMainPath, Supplier<Result<T>> supplier) {
        // TODO: test whether relative paths and subdirectories are handled correctly
        if (ioMap.get(newMainPath) == null)
            return Result.empty(new Problem("could not find main path " + mainPath, Problem.Severity.WARNING));
        Path oldMainPath = mainPath;
        mainPath = newMainPath;
        Result<T> result = supplier.get();
        mainPath = oldMainPath;
        return result;
    }

    public static AInputMapper of(Path mainPath, Charset charset, IOMapperOptions... options) throws IOException {
        return (Arrays.asList(options).contains(IOMapperOptions.ZIP_COMPRESSION))
                ? new ZIPFileInputMapper(mainPath, charset)
                : new FileInputMapper(
                        Arrays.asList(options).contains(IOMapperOptions.INPUT_FILE_HIERARCHY)
                                ? getFilePathsInDirectory(mainPath.getParent())
                                : List.of(mainPath),
                        mainPath.getParent(),
                        mainPath,
                        charset);
    }
}
