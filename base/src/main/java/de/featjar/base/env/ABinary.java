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

import de.featjar.base.FeatJAR;
import de.featjar.base.env.HostEnvironment.OperatingSystem;
import de.featjar.base.extension.IExtension;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private static final Path FEATJAR_BINARY_DIRECTORY = Paths.get(HostEnvironment.HOME_DIRECTORY, ".featjar-bin");

    /**
     * Initializes a native binary by extracting all its resources into the binary directory.
     *
     * @throws IOException if binary cannot be found or moved
     */
    public ABinary() {
        extractResources();
    }

    /**
     * {@return the name of the project this binary is located in}
     */
    protected abstract String getCategory();

    /**
     * {@return the name of this binary}
     */
    protected abstract String getName();

    /**
     * {@return the name of directory where files for the given operating system are located}
     */
    protected String getOSResourceDirectory(OperatingSystem os) {
        return switch (os) {
            case WINDOWS -> "win";
            case MAC_OS, LINUX -> "unix";
            case UNKNOWN -> "unkown";
            default -> throw new IllegalStateException("Unexpected value: " + os);
        };
    }

    /**
     * {@return the name of this binary's executable}
     * Returns an empty Optional if this binary has no executable (i.e., it only provides library files).
     */
    protected Optional<String> getExecutableName() {
        return Optional.empty();
    }

    /**
     * {@return the path to this binary's executable, if any}
     * Returns an empty Optional if this binary has no executable (i.e., it only provides library files).
     */
    public final Optional<Path> getExecutablePath() {
        return getExecutableName().map(name -> getDirectory().resolve(name));
    }

    /**
     * {@return the path to this binary's directory}
     */
    public final Path getDirectory() {
        return FEATJAR_BINARY_DIRECTORY.resolve(getCategory()).resolve(getName());
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
        final Optional<Path> executablePath = getExecutablePath();
        if (executablePath.isEmpty()) {
            throw new UnsupportedOperationException("No executable available");
        } else {
            return new Process(executablePath.get(), arguments, timeout);
        }
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
    protected void extractResources() {
        final Path outputDir = getDirectory();

        final String resourceDirName = String.format(
                "de/featjar/binary/%s/%s/%s",
                getCategory(), getName(), getOSResourceDirectory(HostEnvironment.OPERATING_SYSTEM));
        final Path resourceDir;
        FileSystem jarFileSystem = null;
        try {
            final URI uri = ClassLoader.getSystemClassLoader()
                    .getResource(resourceDirName)
                    .toURI();
            if ("jar".equals(uri.getScheme())) {
                jarFileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap(), null);
                resourceDir = jarFileSystem.getPath(resourceDirName);
            } else {
                resourceDir = Path.of(uri);
            }
            if (!Files.exists(resourceDir)) {
                FeatJAR.log()
                        .warning(
                                "Binary %s:%s has no files for operating system %s.",
                                getCategory(), getName(), HostEnvironment.OPERATING_SYSTEM);
                return;
            }
            final Optional<Path> executablePath = getExecutablePath();
            Files.walk(resourceDir).skip(1).filter(Files::isRegularFile).forEach(resourceFile -> {
                final Path outputFile =
                        outputDir.resolve(resourceDir.relativize(resourceFile).toString());
                // Replace if resourceFile file is newer
                if (Comparator.comparing(this::lastModified).compare(resourceFile, outputFile) > 0) {
                    FeatJAR.log().debug("Copying %s to %s", resourceFile.toString(), outputFile.toString());
                    try {
                        Files.deleteIfExists(outputFile);
                        Files.createDirectories(outputDir);
                        Files.copy(resourceFile, outputFile);
                    } catch (IOException e) {
                        FeatJAR.log().error(e);
                    }
                    if (executablePath.isPresent()) {
                        if (Objects.equals(outputFile, executablePath.get())) {
                            outputFile.toFile().setExecutable(true);
                        }
                    }
                }
            });
        } catch (Exception e) {
            FeatJAR.log().error(e);
        } finally {
            if (jarFileSystem != null) {
                try {
                    jarFileSystem.close();
                } catch (IOException e) {
                    FeatJAR.log().error(e);
                }
            }
        }
    }

    private FileTime lastModified(Path p) {
        try {
            return Files.readAttributes(p, BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            return FileTime.from(Instant.MIN);
        }
    }
}
