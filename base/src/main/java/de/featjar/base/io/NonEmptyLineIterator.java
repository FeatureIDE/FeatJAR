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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Reads a text-based stream line-by-line, skipping empty lines.
 *
 * @author Sebastian Krieter
 */
public class NonEmptyLineIterator implements Supplier<String> {

    private final BufferedReader reader;
    private String line = null;
    private int lineCount = 0;

    public NonEmptyLineIterator(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Reads the underlying stream and returns the next line or {@code null} if the stream was completely read.
     * This operation advances the underlying input stream.
     * @return the next line.
     */
    @Override
    public String get() {
        try {
            do {
                line = reader.readLine();
                if (line == null) {
                    return null;
                }
                lineCount++;
            } while (line.trim().isEmpty());
            return line;
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * {@return the last read line. Does not advance the underlying stream}
     */
    public String currentLine() {
        return line;
    }

    public void setCurrentLine(String line) {
        this.line = line;
    }

    public int getLineCount() {
        return lineCount;
    }
}
