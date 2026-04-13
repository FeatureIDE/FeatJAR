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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.IIOObject;
import de.featjar.base.io.NonEmptyLineIterator;
import de.featjar.base.io.format.IFormat;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Readable input source of a {@link IFormat}.
 * Can be a physical file, string, or arbitrary input stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AInput implements IIOObject {
    protected final InputStream inputStream;
    protected final Charset charset;
    protected final java.lang.String fileExtension;

    protected AInput(InputStream inputStream, Charset charset, java.lang.String fileExtension) {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(charset);
        this.inputStream = new BufferedInputStream(inputStream);
        this.charset = charset;
        this.fileExtension = fileExtension;
    }

    /**
     * {@return this input's charset}
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * {@return the full string read from this input, if any}
     */
    public Result<String> read() {
        try {
            return Result.of(new String(inputStream.readAllBytes(), charset));
        } catch (final IOException e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    public byte[] readBytes(int size) throws IOException {
        final byte[] bytes = new byte[size];
        final int byteCount = inputStream.readNBytes(bytes, 0, bytes.length);
        if (byteCount != bytes.length) {
            throw new IOException("Stream ended before expected end!");
        }
        return bytes;
    }

    public int readInt() throws IOException {
        final byte[] integerBytes = new byte[Integer.BYTES];
        final int byteCount = inputStream.readNBytes(integerBytes, 0, integerBytes.length);
        if (byteCount != integerBytes.length) {
            throw new IOException("Stream ended before expected end!");
        }
        return ((integerBytes[0] & 0xff) << 24)
                | ((integerBytes[1] & 0xff) << 16)
                | ((integerBytes[2] & 0xff) << 8)
                | ((integerBytes[3] & 0xff));
    }

    public byte readByte() throws IOException {
        final int readByte = inputStream.read();
        if (readByte < 0) {
            throw new IOException("Stream ended before expected end!");
        }
        return (byte) readByte;
    }

    public boolean readBool() throws IOException {
        final int boolByte = inputStream.read();
        if (boolByte < 0) {
            throw new IOException("Stream ended before expected end!");
        }
        return boolByte == 1;
    }

    /**
     * {@return a reader for this input}
     */
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    /**
     * {@return a stream of all lines read from this input}
     */
    public Stream<String> getLineStream() {
        return getReader().lines();
    }

    /**
     * {@return an iterator of lines read from this input, skipping empty lines}
     */
    public NonEmptyLineIterator getNonEmptyLineIterator() {
        return new NonEmptyLineIterator(getReader());
    }

    /**
     * {@return complete text read from this input}
     */
    public String text() {
        return getLineStream().collect(Collectors.joining("\n"));
    }

    /**
     * {@return a list of all lines read from this input}
     */
    public List<String> readLines() {
        return getLineStream().collect(Collectors.toList());
    }

    /**
     * {@return this input's stream}
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * {@return this input's header, if any}
     */
    public Result<InputHeader> getInputHeader() {
        final byte[] bytes = new byte[InputHeader.MAX_HEADER_SIZE];
        try {
            try {
                inputStream.mark(InputHeader.MAX_HEADER_SIZE);
                final int byteCount = inputStream.read(bytes, 0, InputHeader.MAX_HEADER_SIZE);
                if (byteCount == -1) return Result.empty(new IOException("Could not read input stream."));
                return Result.of(new InputHeader(
                        fileExtension, //
                        byteCount == InputHeader.MAX_HEADER_SIZE ? bytes : Arrays.copyOf(bytes, byteCount), //
                        charset));
            } finally {
                inputStream.reset();
            }
        } catch (final IOException e) {
            return Result.empty(e);
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
