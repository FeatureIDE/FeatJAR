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

/**
 * An exception that occurs while parsing.
 *
 * @author Sebastian Krieter
 */
public class RuntimeParseException extends RuntimeException implements IParseError {

    private static final long serialVersionUID = -6948189323221248464L;

    protected final int lineNumber;
    protected final int position;

    /**
     * Creates a parse exception.
     *
     * @param message the message
     */
    public RuntimeParseException(String message) {
        this(message, -1);
    }

    /**
     * Creates a parse exception.
     *
     * @param message the message
     * @param lineNumber the line number
     */
    public RuntimeParseException(String message, int lineNumber) {
        this(message, lineNumber, -1);
    }

    /**
     * Creates a parse exception.
     *
     * @param message the message
     * @param lineNumber the line number
     * @param position the position within the line
     */
    public RuntimeParseException(String message, int lineNumber, int position) {
        super(message);
        this.lineNumber = lineNumber;
        this.position = position;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int getPosition() {
        return position;
    }
}
