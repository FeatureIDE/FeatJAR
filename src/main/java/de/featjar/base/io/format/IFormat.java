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
import de.featjar.base.extension.IExtension;
import de.featjar.base.io.input.AInput;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.input.InputHeader;
import de.featjar.base.io.output.AOutput;
import de.featjar.base.io.output.AOutputMapper;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Parses and serializes objects.
 * For parsing, one or multiple {@link AInput inputs} are read from an {@link AInputMapper}.
 * For serializing, one or multiple {@link AOutput outputs} are written to an {@link AOutputMapper} or a {@link String}.
 *
 * @param <T> the type of the read/written object
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IFormat<T> extends IExtension {

    /**
     * Parses the content of an {@link AInputMapper} into a new object.
     *
     * @param inputMapper the input mapper
     * @return the parsed result
     */
    default Result<T> parse(AInputMapper inputMapper) {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the content of an {@link AInputMapper} into a supplied object.
     *
     * @param inputMapper the input mapper
     * @param supplier    the supplier
     * @return the parsed result
     */
    default Result<T> parse(AInputMapper inputMapper, Supplier<T> supplier) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return the given object serialized into a string}
     *
     * @param object the object
     */
    default Result<String> serialize(T object) {
        return Result.empty();
    }

    /**
     * Writes the given object to an {@link AOutputMapper}.
     *
     * @param object the object
     * @param outputMapper  the output mapper
     *
     * @throws IOException if an error occurs during writing
     */
    default void write(T object, AOutputMapper outputMapper) throws IOException {
        outputMapper
                .get()
                .writeText(serialize(object)
                        .orElseThrow(p ->
                                new IOException(Problem.getFirstException(p).orElse(null))));
    }

    /**
     * {@return the file extension for this format, if any}
     * There should be no leading ".".
     * The file extension is used to detect whether this format supports parsing a given file in {@link AFormats}.
     * If omitted, this format supports files without extensions.
     */
    default String getFileExtension() {
        return null;
    }

    /**
     * {@return a meaningful name for this format}
     */
    String getName();

    /**
     * {@return an instance of this format}
     * Call this method before {@link #parse(AInputMapper)}, {@link #parse(AInputMapper, Supplier)},
     * {@link #serialize(Object)}, or {@link #write(Object, AOutputMapper)} to avoid unintended concurrent access.
     * Implementing classes may return {@code this} if {@link #parse(AInputMapper)} and
     * {@link #serialize(Object)} are implemented without state (i.e., non-static fields).*/
    default IFormat<T> getInstance() {
        return this;
    }

    /**
     * {@return whether this format supports parsing}
     * If {@code true} this format implements {@link #parse(AInputMapper)}.
     */
    default boolean supportsParse() {
        return false;
    }

    /**
     * {@return whether this format supports writing}
     * If {@code true} this format implements {@link #write(Object, AOutputMapper)}.
     */
    default boolean supportsWrite() {
        return false;
    }

    /**
     * {@return whether this format supports parsing input with the given input header}
     *
     * @param inputHeader the input header
     */
    default boolean supportsContent(InputHeader inputHeader) {
        return supportsParse();
    }

    /**
     * {@return whether this format uses a textual rather than a binary representation}
     */
    default boolean isTextual() {
        return true;
    }
}
