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

import de.featjar.base.data.Result;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Input or output mapped by a {@link AIOMapper}.
 * This could be a physical file, string, or stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IIOObject extends AutoCloseable {

    @Override
    void close() throws IOException;

    /**
     * {@return a path's file name without its extension}
     *
     * @param path the path
     */
    static String getFileNameWithoutExtension(Path path) {
        return getFileNameWithoutExtension(getFileName(path));
    }

    private static String getFileName(Path path) {
        final Path fileName = path.getFileName();
        if (fileName == null) {
            throw new InvalidPathException(path.toString(), "Empty path");
        }
        return fileName.toString();
    }

    /**
     * {@return a full file name's file name without its extension}
     *
     * @param fileName the file name
     */
    static String getFileNameWithoutExtension(String fileName) {
        final int extensionIndex = fileName.lastIndexOf('.');
        return (extensionIndex > 0) ? fileName.substring(0, extensionIndex) : fileName;
    }

    /**
     * {@return a path's file extension, if any}
     * A dot at the first position of the file name is ignored.
     * E.g., ".file" has no extension, but ".file.txt" would return "txt".
     *
     * @param path the path
     */
    static Result<String> getFileExtension(Path path) {
        return Result.ofNullable(path).mapResult(_path -> getFileExtension(getFileName(_path)));
    }

    /**
     * {@return a full file name's file extension, if any}
     * A dot at the first position of the file name is ignored.
     * E.g., ".file" has no extension, but ".file.txt" would return "txt".
     *
     * @param fileName the file name
     */
    static Result<String> getFileExtension(String fileName) {
        if (fileName == null) return Result.empty();
        final int extensionIndex = fileName.lastIndexOf('.');
        return Result.ofNullable(extensionIndex > 0 ? fileName.substring(extensionIndex + 1) : null);
    }

    /**
     * {@return a file name with a replaced file extension}
     *
     * @param fileName the file name
     * @param fileExtension the new file extension
     */
    static String getFileNameWithNewExtension(String fileName, String fileExtension) {
        final String fileNameWithoutExtension = IIOObject.getFileNameWithoutExtension(fileName);
        if (fileExtension == null) return fileNameWithoutExtension;
        return String.format("%s.%s", fileNameWithoutExtension, fileExtension);
    }

    /**
     * {@return a path with a replaced file extension}
     *
     * @param path the path
     * @param fileExtension the new file extension
     */
    static Path getPathWithNewExtension(Path path, String fileExtension) {
        return path.resolveSibling(getFileNameWithNewExtension(getFileName(path), fileExtension));
    }

    /**
     * {@return a path with a replaced file extension}
     *
     * @param fileName the file name
     * @param fileExtension the new file extension
     */
    static Path getPathWithNewExtension(String fileName, String fileExtension) {
        return Paths.get(getFileNameWithNewExtension(fileName, fileExtension));
    }

    /**
     * {@return a file name with an added file extension}
     *
     * @param fileName the file name
     * @param fileExtension the new file extension
     */
    static String getFileNameWithExtraExtension(String fileName, String fileExtension) {
        if (fileExtension == null) return fileName;
        return String.format("%s.%s", fileName, fileExtension);
    }

    /**
     * {@return a path with an added file extension}
     *
     * @param path the path
     * @param fileExtension the new file extension
     */
    static Path getPathWithExtraExtension(Path path, String fileExtension) {
        return path.resolveSibling(getFileNameWithExtraExtension(getFileName(path), fileExtension));
    }

    /**
     * {@return a path with an added file extension}
     *
     * @param fileName the file name
     * @param fileExtension the new file extension
     */
    static Path getPathWithExtraExtension(String fileName, String fileExtension) {
        return Paths.get(getFileNameWithExtraExtension(fileName, fileExtension));
    }
}
