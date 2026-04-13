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
package de.featjar.base.io.format;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.extension.AExtensionPoint;
import de.featjar.base.io.IIOObject;
import de.featjar.base.io.input.InputHeader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages formats.
 * Should be extended to manage formats for a specific kind of object.
 *
 * @param <T> the type of the read/written object
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AFormats<T> extends AExtensionPoint<IFormat<T>> implements IFormatSupplier<T> {

    /**
     * {@return all formats that support a given file extension}
     *
     * @param fileExtension the file extension
     */
    public List<IFormat<T>> getFormatList(final String fileExtension) {
        return getExtensions().stream()
                .filter(IFormat::supportsParse)
                .filter(format -> Objects.equals(fileExtension, format.getFileExtension()))
                .collect(Collectors.toList());
    }

    /**
     * {@return all formats that support a given file path}
     *
     * @param path the path
     */
    public List<IFormat<T>> getFormatList(Path path) {
        return getFormatList(IIOObject.getFileExtension(path).orElse(null));
    }

    /**
     * {@return an array of the names of all installed formats for BooleanAssignmentGroup}.
     */
    public String[] getNames() {
        return getExtensions().stream().map(IFormat::getName).toArray(String[]::new);
    }

    /**
     * {@return the format that matches the given name}.
     * @param name the name to match
     */
    public Optional<IFormat<T>> getFormatByName(String name) {
        return getExtensions().stream()
                .filter(f -> Objects.equals(name, f.getName()))
                .findFirst();
    }

    @Override
    public Result<IFormat<T>> getFormat(InputHeader inputHeader) {
        return getExtensions().stream()
                .filter(format ->
                        Objects.equals(inputHeader.getFileExtension().orElse(null), format.getFileExtension()))
                .filter(format -> format.supportsContent(inputHeader))
                .findFirst()
                .map(Result::of)
                .orElseGet(() -> Result.empty(new Problem(
                        String.format("No suitable format found. Possible formats: %s", Arrays.toString(getNames())),
                        Problem.Severity.ERROR)));
    }

    public abstract Class<T> getType();
}
