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
package de.featjar.base.io.binary;

import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInput;
import de.featjar.base.io.output.AOutput;
import java.io.IOException;

/**
 * Helpers for parsing and writing an object from and into a binary file.
 *
 * @param <T> the type of the written object
 * @author Sebastian Krieter
 */
public abstract class ABinaryFormat<T> implements IFormat<T> {

    @Override
    public boolean isTextual() {
        return false;
    }

    protected void writeByteArray(AOutput out, byte[] bytes) throws IOException {
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    protected void writeString(AOutput out, String text) throws IOException {
        writeByteArray(out, text.getBytes(out.getCharset()));
    }

    protected byte[] readByteArray(AInput in) throws IOException {
        return in.readBytes(in.readInt());
    }

    protected String readString(AInput in) throws IOException {
        return new String(readByteArray(in), in.getCharset());
    }
}
