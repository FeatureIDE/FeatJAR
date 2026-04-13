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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utilities for handling Java archives.
 *
 * @author Elias Kuiter
 */
public class JARs {
    /**
     * Extracts a resource from the current Java archive into an output directory.
     *
     * @param resourceName the extracted resource's name, relative to the {@code src/main/resources} directory
     * @param outputPath the output directory
     * @throws IOException if an I/O exception occurs
     */
    public static void extractResource(String resourceName, Path outputPath) throws IOException {
        try (InputStream in = getResource(resourceName).openStream()) {
            Files.copy(in, outputPath);
        }
    }

    /**
     * {@return the latest modification date of a resource in the JAR}
     * @param resourceName the name of the resource
     * @throws IOException if an I/O exception occurs.
     */
    public static long getLastModificationDate(String resourceName) throws IOException {
        return getResource(resourceName).openConnection().getLastModified();
    }

    private static URL getResource(String resourceName) throws IOException {
        URL url = ClassLoader.getSystemClassLoader().getResource(resourceName);
        if (url == null) throw new IOException("no resource found at " + resourceName);
        return url;
    }
}
