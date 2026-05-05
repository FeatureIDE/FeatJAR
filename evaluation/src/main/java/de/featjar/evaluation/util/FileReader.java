/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-evaluation.
 *
 * evaluation is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with evaluation. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-evaluation> for further information.
 */
package de.featjar.evaluation.util;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormatSupplier;
import de.featjar.base.log.Log.Verbosity;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Reads and parses files from a resource location.
 * @param <T> the type of object to be parsed.
 * @author Sebastian Krieter
 */
public class FileReader<T> {

    private String defaultFileName = "model";
    private String defaultFileExtension = "xml";
    private Path pathToFiles;
    private IFormatSupplier<T> formatSupplier;

    public FileReader(
            Path pathToFiles, IFormatSupplier<T> formatSupplier, String defaultFileName, String defaultFileExtension) {
        this.pathToFiles = pathToFiles;
        this.formatSupplier = formatSupplier;
        this.defaultFileName = defaultFileName;
        this.defaultFileExtension = defaultFileExtension;
    }

    public final Result<T> read(final String name) {
        Result<T> fm = null;

        fm = readFromFolder(pathToFiles, name);
        if (fm.isPresent()) {
            return fm;
        }

        fm = readFromFile(pathToFiles, name);
        if (fm.isPresent()) {
            return fm;
        }

        fm = readFromZip(pathToFiles, name);

        return fm;
    }

    public Path getPathToFiles() {
        return pathToFiles;
    }

    public void setPathToFiles(Path pathToFiles) {
        this.pathToFiles = pathToFiles;
    }

    public String getDefaultFileName() {
        return defaultFileName;
    }

    public void setDefaultFileName(String defaultFileName) {
        this.defaultFileName = defaultFileName;
    }

    public String getDefaultFileExtension() {
        return defaultFileExtension;
    }

    public void setDefaultFileExtension(String defaultFileExtension) {
        this.defaultFileExtension = defaultFileExtension;
    }

    public IFormatSupplier<T> getFormatSupplier() {
        return formatSupplier;
    }

    public void setFormatSupplier(IFormatSupplier<T> formatSupplier) {
        this.formatSupplier = formatSupplier;
    }

    public Result<T> loadFile(final Path path) {
        return IO.load(path, formatSupplier);
    }

    public Result<T> readFromFolder(final Path rootPath, final String name) {
        final Path folder = rootPath.resolve(name);
        FeatJAR.log().debug("Trying to load from folder " + folder);
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            final Path path = folder.resolve(defaultFileName + "." + defaultFileExtension);
            if (Files.exists(path)) {
                return loadFile(path);
            } else {
                return readFromFile(folder, defaultFileName);
            }
        } else {
            return Result.empty();
        }
    }

    public Result<T> readFromFile(final Path rootPath, final String name) {
        FeatJAR.log().debug("Trying to load from file " + name);
        Result<T> loadedFm = loadFile(rootPath.resolve(name));
        if (loadedFm.isPresent()) {
            return loadedFm;
        } else {
            FeatJAR.log().problems(loadedFm.getProblems(), Verbosity.DEBUG);
        }
        final Filter<Path> fileFilter = file -> Files.isReadable(file)
                && Files.isRegularFile(file)
                && file.getFileName().toString().matches("^" + Pattern.quote(name) + "\\.\\w+$");
        try (DirectoryStream<Path> files = Files.newDirectoryStream(rootPath, fileFilter)) {
            final Iterator<Path> iterator = files.iterator();
            while (iterator.hasNext()) {
                final Path next = iterator.next();
                FeatJAR.log().debug("Trying to load from file %s", next);
                loadedFm = loadFile(next);
                if (loadedFm.isPresent()) {
                    return loadedFm;
                } else {
                    FeatJAR.log().error("Error while loading file %s", next);
                    FeatJAR.log().problems(loadedFm.getProblems(), Verbosity.ERROR);
                }
            }
            return Result.empty();
        } catch (final IOException e) {
            FeatJAR.log().error(e);
        }
        return Result.empty();
    }

    protected Result<T> readFromZip(final Path rootPath, final String name) {
        final Filter<Path> fileFilter = file -> Files.isReadable(file)
                && Files.isRegularFile(file)
                && file.getFileName().toString().matches(".*[.]zip\\Z");
        try (DirectoryStream<Path> files = Files.newDirectoryStream(rootPath, fileFilter)) {
            for (final Path path : files) {
                FeatJAR.log().debug("Trying to load from zip file " + path);
                final URI uri = URI.create("jar:" + path.toUri().toString());
                try (final FileSystem zipFs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
                    for (final Path root : zipFs.getRootDirectories()) {
                        Result<T> fm = readFromFolder(root, name);
                        if (fm.isPresent()) {
                            return fm;
                        }
                        fm = readFromFile(root, name);
                        if (fm.isPresent()) {
                            return fm;
                        }
                    }
                } catch (final IOException e) {
                    FeatJAR.log().error(e);
                }
            }
        } catch (final IOException e) {
            FeatJAR.log().error(e);
        }
        return Result.empty();
    }
}
