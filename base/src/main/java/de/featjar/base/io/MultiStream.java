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

import de.featjar.base.data.Sets;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * A stream that dispatches to several streams when written to.
 * Can be used to pipe some output through to many streams.
 *
 * @author Sebastian Krieter
 */
public class MultiStream extends OutputStream {

    protected final LinkedHashSet<OutputStream> streams = Sets.empty();

    public MultiStream(OutputStream... streams) {
        this(Sets.of(streams));
    }

    public MultiStream(LinkedHashSet<? extends OutputStream> streams) {
        this.streams.addAll(streams);
    }

    public void addStream(OutputStream stream) {
        streams.add(stream);
    }

    public void clearStreams() {
        streams.clear();
    }

    public LinkedHashSet<OutputStream> getStreams() {
        return streams;
    }

    @Override
    public void flush() throws IOException {
        for (final OutputStream outputStream : streams) {
            outputStream.flush();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        for (final OutputStream outputStream : streams) {
            outputStream.write(buf, off, len);
        }
    }

    @Override
    public void write(int b) throws IOException {
        for (final OutputStream outputStream : streams) {
            outputStream.write(b);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void write(byte[] b) throws IOException {
        for (final OutputStream outputStream : streams) {
            outputStream.write(b);
        }
    }
}
