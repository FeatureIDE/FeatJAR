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

import de.featjar.base.io.IIOObject;
import de.featjar.base.io.format.IFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Writable output target of a {@link IFormat}.
 * Can be a physical file or arbitrary output stream.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AOutput implements IIOObject {
    protected final OutputStream outputStream;
    protected final Charset charset;

    protected AOutput(OutputStream outputStream, Charset charset) {
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(charset);
        this.outputStream = outputStream;
        this.charset = charset;
    }

    /**
     * {@return this output's charset}
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * {@return this output's stream}
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void writeBytes(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    /**
     * Writes a text with the given encoding to this output.
     *
     * @param text the string
     * @throws IOException if an I/O error occurs
     */
    public void writeText(String text) throws IOException {
        outputStream.write(text.getBytes(charset));
    }

    public void writeInt(int value) throws IOException {
        final byte[] integerBytes = new byte[Integer.BYTES];
        integerBytes[0] = (byte) ((value >>> 24) & 0xff);
        integerBytes[1] = (byte) ((value >>> 16) & 0xff);
        integerBytes[2] = (byte) ((value >>> 8) & 0xff);
        integerBytes[3] = (byte) (value & 0xff);
        outputStream.write(integerBytes);
    }

    public void writeByte(byte value) throws IOException {
        outputStream.write(value);
    }

    public void writeBool(boolean value) throws IOException {
        outputStream.write((byte) (value ? 1 : 0));
    }
}
