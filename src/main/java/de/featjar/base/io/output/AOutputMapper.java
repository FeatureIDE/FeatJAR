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

import de.featjar.base.data.Result;
import de.featjar.base.io.AIOMapper;
import de.featjar.base.io.IIOObject;
import de.featjar.base.io.IOMapperOptions;
import de.featjar.base.io.format.IFormat;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

/**
 * Maps paths to outputs.
 * Can represent a single output (e.g., one physical file) or a file hierarchy
 * (e.g., physical files referring to each other).
 *
 * @author Elias Kuiter
 */
public abstract class AOutputMapper extends AIOMapper<AOutput> {
    protected AOutputMapper(Path mainPath) {
        super(mainPath);
    }

    protected AOutputMapper(LinkedHashMap<Path, AOutput> ioMap, Path mainPath) {
        super(ioMap, mainPath);
    }

    protected abstract AOutput newOutput(Path path) throws IOException;

    /**
     * {@return a file output mapper that optionally writes to a ZIP or JAR file}
     *
     * @param mainPath the main path
     * @param charset the charset
     * @param options the {@link AIOMapper} options
     * @throws IOException if an I/O exception occurs
     */
    public static AOutputMapper of(Path mainPath, Charset charset, IOMapperOptions... options) throws IOException {
        return Arrays.asList(options).contains(IOMapperOptions.OUTPUT_FILE_JAR)
                ? new JARFileOutputMapper(
                        IIOObject.getPathWithNewExtension(mainPath, "jar"), mainPath.getFileName(), charset)
                : Arrays.asList(options).contains(IOMapperOptions.ZIP_COMPRESSION)
                        ? new ZIPFileOutputMapper(
                                IIOObject.getPathWithExtraExtension(mainPath, "zip"), mainPath.getFileName(), charset)
                        : new FileOutputMapper(mainPath, charset);
    }

    /**
     * A runnable that may throw an {@link IOException}.
     */
    public interface IORunnable {
        void run() throws IOException;
    }

    /**
     * Temporarily shifts the focus of this output mapper to another main path to execute some function.
     * Useful to parse a {@link IFormat} recursively.
     *
     * @param newMainPath the new main path
     * @param ioRunnable the runnable
     * @throws IOException if an I/O exception occurs
     */
    @SuppressWarnings("resource")
    public void withMainPath(Path newMainPath, IORunnable ioRunnable) throws IOException {
        // TODO: test whether relative paths and subdirectories are handled correctly
        create(newMainPath);
        Path oldMainPath = mainPath;
        mainPath = newMainPath;
        try {
            ioRunnable.run();
        } finally {
            mainPath = oldMainPath;
        }
    }

    /**
     * {@return a new output at a given path}
     *
     * @param path the path
     * @throws IOException if an I/O exception occurs
     */
    public AOutput create(Path path) throws IOException {
        Result<AOutput> outputResult = super.get(path);
        if (outputResult.isPresent()) return outputResult.get();
        AOutput output = newOutput(path);
        ioMap.put(path, output);
        return output;
    }
}
