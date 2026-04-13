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
import de.featjar.base.io.input.AInput;
import java.nio.file.Path;

/**
 * A problem that occurs while parsing an {@link AInput}.
 * Stores a path and line number where the problem occurred.
 *
 * @author Sebastian Krieter
 */
public class ParseProblem extends Problem {
    // TODO: for hierarchical formats like UVL, there may be several files, so we should
    //  also store the path of the file with a parse problem.
    protected final Path path = null;
    protected final int lineNumber;

    /**
     * Create a new parse problem.
     *
     * @param exception the exception
     * @param lineNumber the line number
     */
    public ParseProblem(Exception exception, int lineNumber) {
        this(exception, Severity.ERROR, lineNumber);
    }

    /**
     * Create a new parse problem.
     *
     * @param exception the parse exception
     */
    public ParseProblem(ParseException exception) {
        this(exception, Severity.ERROR, exception.getLineNumber());
    }

    /**
     * Create a new parse problem.
     *
     * @param message    the message
     * @param severity   the severity
     * @param lineNumber the line number
     */
    public ParseProblem(String message, Severity severity, int lineNumber) {
        super(message, severity);
        this.lineNumber = lineNumber;
    }

    protected ParseProblem(Exception exception, Severity severity, int lineNumber) {
        super(exception, severity);
        this.lineNumber = lineNumber;
    }

    /**
     * {@return the line where the problem occurred}
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
