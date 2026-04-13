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

import de.featjar.base.extension.IExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A native binary bundled with FeatJAR.
 * Right now, whenever a native binary is needed, it is extracted into the user's home directory.
 * This is necessary so the binary can be executed.
 * Only the binaries required on the current operating system should be extracted.
 * For now, the binaries are just kept in the home directory indefinitely.
 *
 * @author Elias Kuiter
 */
public abstract class ABinary implements IExtension {
    /**
     * The directory used to store native binaries.
     */
    public static final Path BINARY_DIRECTORY = Paths.get(HostEnvironment.HOME_DIRECTORY, ".featjar-bin");

    /**
     * Initializes a native binary by extracting all its resources into the binary directory.
     *
     * @throws IOException if binary cannot be found or moved
     */
    public ABinary() throws IOException {
        extractResources(getResourceNames());
    }

    /**
     * {@return the names of all resources (i.e., executables and libraries) to be extracted for this binary}
     * All names are relative to the {@code src/main/resources/bin} directory.
     */
    protected abstract LinkedHashSet<String> getResourceNames();

    /**
     * {@return the name of this binary's executable}
     * Returns {@code null} if this binary has no executable (i.e., it only provides library files).
     */
    protected String getExecutableName() {
        return null;
    }

    /**
     * {@return the path to this binary's executable, if any}
     * Returns {@code null} if this binary has no executable (i.e., it only provides library files).
     */
    public final Path getExecutablePath() {
        return getExecutableName() != null ? BINARY_DIRECTORY.resolve(getExecutableName()) : null;
    }

    /**
     * Executes this binary's executable with the given arguments.
     * Creates a process and waits until it exits or a timeout occurs.
     *
     * @param arguments the arguments passed to this binary's executable
     * @param timeout the timeout
     * @return the output of the process as a line stream, if any
     */
    public Process getProcess(List<String> arguments, Duration timeout) {
        return new Process(getExecutablePath(), arguments, timeout);
    }

    /**
     * Executes this binary's executable with the given arguments.
     * Creates a process and waits until it exits.
     *
     * @param arguments the arguments passed to this binary's executable
     * @return the output of the process as a line stream, if any
     */
    public final Process getProcess(String... arguments) {
        return getProcess(List.of(arguments), null);
    }

    /**
     * Extracts this binary's resources into the binary directory.
     * Each resource is set to be executable.
     *
     * @param resourceNames the names of the available resources, where the binary can be found
     * @throws IOException if binary cannot be found or moved
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void extractResources(LinkedHashSet<String> resourceNames) throws IOException {
        Files.createDirectories(BINARY_DIRECTORY);
        for (String resourceName : resourceNames) {
            final Path outputPath = BINARY_DIRECTORY.resolve(resourceName);
            if (Files.notExists(outputPath)) {
                JARs.extractResource("bin/" + resourceName, outputPath);
                outputPath.toFile().setExecutable(true);
            } else if (isNewer(resourceName, outputPath)) {
                Files.delete(outputPath);
                JARs.extractResource("bin/" + resourceName, outputPath);
                outputPath.toFile().setExecutable(true);
            }
        }
    }

    private boolean isNewer(String resourceName, Path outputPath) throws IOException {
        final long localFile = Files.readAttributes(outputPath, BasicFileAttributes.class)
                .lastModifiedTime()
                .to(TimeUnit.MILLISECONDS);
        final long jarFile = JARs.getLastModificationDate("bin/" + resourceName);
        return jarFile > localFile;
    }
}
