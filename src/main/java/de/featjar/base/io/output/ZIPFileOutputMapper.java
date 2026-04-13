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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * Maps virtual paths to a ZIP file output.
 *
 * @author Elias Kuiter
 */
public class ZIPFileOutputMapper extends AOutputMapper {
    protected final ZipOutputStream zipOutputStream;
    protected final Charset charset;

    /**
     * Creates a ZIP file output mapper.
     *
     * @param zipPath  the ZIP file path
     * @param mainPath the main path
     * @param charset  the charset
     * @throws ZipException if a ZIP error has occurred
     * @throws IOException if an I/O error has occurred
     */
    public ZIPFileOutputMapper(Path zipPath, Path mainPath, Charset charset) throws IOException {
        super(mainPath);
        this.zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toString()));
        this.charset = charset;
        ioMap.put(mainPath, new ZIPEntryOutput(mainPath, zipOutputStream, charset));
    }

    @Override
    protected AOutput newOutput(Path path) throws IOException {
        return new ZIPEntryOutput(path, zipOutputStream, charset);
    }

    @Override
    public void close() throws IOException {
        super.close();
        zipOutputStream.close();
    }
}
