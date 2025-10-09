/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.base.log;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Supplier;

/**
 * Logs messages to standard output and files. Formats log messages with {@link IFormatter formatters}.
 * TODO: instead of logging values directly, only pass suppliers that are called if some log target is configured. this saves time for creating log strings
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Log {
    /**
     * Logging verbosity. Each verbosity defines a type of message that can be logged. In addition, defines a log level that includes all log messages of the message type and all types above.
     */
    public enum Verbosity {
        /**
         * Regular message. For explicit console output.
         */
        MESSAGE,
        /**
         * Error message. Typically used to log critical exceptions and errors.
         */
        ERROR,
        /**
         * Warning message. Typically used to log non-critical warnings.
         */
        WARNING,
        /**
         * Info message. Typically used to log high-level information.
         */
        INFO,
        /**
         * Debug message. Typically used to log low-level information.
         */
        DEBUG,
        /**
         * Progress message. Typically used to signal progress in long-running jobs.
         */
        PROGRESS;
    }

    static String getErrorMessage(Throwable error, boolean printStacktrace) {
        if (printStacktrace) {
            StringWriter errorWriter = new StringWriter();
            error.printStackTrace(new PrintWriter(errorWriter));
            return errorWriter.toString();
        } else {
            StringBuilder errorStringBuilder = new StringBuilder();
            Throwable e = error;
            while (e != null) {
                StackTraceElement stackTrace = e.getStackTrace()[0];
                errorStringBuilder.append(String.format(
                        "%s (%s @ %s:%d)%n\t",
                        e.getMessage(), e.getClass().getName(), stackTrace.getClassName(), stackTrace.getLineNumber()));
                e = e.getCause();
            }
            int length = errorStringBuilder.length();
            if (length > 0) {
                errorStringBuilder.setLength(length - 2);
            }
            return errorStringBuilder.toString();
        }
    }

    /**
     * Logs a problem.
     *
     * @param problem the problem
     */
    default void problem(Problem problem) {
        switch (problem.getSeverity()) {
            case ERROR:
                Exception exception = problem.getException();
                if (exception == null) {
                    error(problem::getMessage);
                } else {
                    error(exception);
                }
                break;
            case WARNING:
                warning(problem::getMessage);
                break;
            case INFO:
                info(problem::getMessage);
                break;
            default:
                break;
        }
    }

    /**
     * Logs a problem.
     *
     * @param problem the problem
     * @param verbosity the verbosity at which the problem is shown
     */
    default void problem(Problem problem, Verbosity verbosity) {
        switch (problem.getSeverity()) {
            case ERROR:
                println(problem.getException(), verbosity);
                break;
            case WARNING:
            case INFO:
                println(problem::getMessage, verbosity, true);
                break;
            default:
                break;
        }
    }

    /**
     * Logs problems.
     *
     * @param problems the problems
     */
    default void problems(List<Problem> problems) {
        for (Problem problem : problems) {
            problem(problem);
        }
    }

    /**
     * Logs problems of the result.
     *
     * @param result the result
     */
    default void problems(Result<?> result) {
        for (Problem problem : result.getProblems()) {
            problem(problem);
        }
    }

    /**
     * Logs problems.
     *
     * @param problems the problems
     * @param verbosity the verbosity at which the problems are shown
     */
    default void problems(List<Problem> problems, Verbosity verbosity) {
        for (Problem problem : problems) {
            problem(problem, verbosity);
        }
    }

    /**
     * Logs problems of the result.
     *
     * @param result the result
     * @param verbosity the verbosity at which the problems are shown
     */
    default void problems(Result<?> result, Verbosity verbosity) {
        for (Problem problem : result.getProblems()) {
            problem(problem, verbosity);
        }
    }

    /**
     * Logs an error message.
     *
     * @param message the error message
     */
    default void error(Supplier<String> message) {
        println(message, Verbosity.ERROR, true);
    }

    /**
     * Logs a {@link String#format(String, Object...) formatted} error message.
     *
     * @param formatMessage the message with format specifiers
     * @param elements      the arguments for the format specifiers in the format
     *                      message
     */
    default void error(String formatMessage, Object... elements) {
        error(() -> String.format(formatMessage, elements));
    }

    /**
     * Logs an error message.
     *
     * @param error the error object
     */
    default void error(Throwable error) {
        println(error, Verbosity.ERROR);
    }

    /**
     * Logs a warning message.
     *
     * @param message the warning message
     */
    default void warning(Supplier<String> message) {
        println(message, Verbosity.WARNING, true);
    }

    /**
     * Logs a {@link String#format(String, Object...) formatted} warning message.
     *
     * @param formatMessage the message with format specifiers
     * @param elements      the arguments for the format specifiers in the format
     *                      message
     */
    default void warning(String formatMessage, Object... elements) {
        warning(() -> String.format(formatMessage, elements));
    }

    /**
     * Logs a warning message.
     *
     * @param warning the warning object
     */
    default void warning(Throwable warning) {
        println(warning, Verbosity.WARNING);
    }

    /**
     * Logs an info message.
     *
     * @param message the message
     */
    default void info(Supplier<String> message) {
        println(message, Verbosity.INFO, true);
    }

    /**
     * Logs a {@link String#format(String, Object...) formatted} info message.
     *
     * @param formatMessage the message with format specifiers
     * @param elements      the arguments for the format specifiers in the format
     *                      message
     */
    default void info(String formatMessage, Object... elements) {
        info(() -> String.format(formatMessage, elements));
    }

    /**
     * Logs an info message.
     *
     * @param messageObject the message object
     */
    default void info(Object messageObject) {
        info(() -> String.valueOf(messageObject));
    }

    /**
     * Logs a regular message.
     *
     * @param message the message
     */
    default void message(Supplier<String> message) {
        println(message, Verbosity.MESSAGE, true);
    }

    /**
     * Logs a regular message without any formatters.
     *
     * @param message the message
     */
    default void plainMessage(Supplier<String> message) {
        println(message, Verbosity.MESSAGE, false);
    }

    /**
     * Logs a {@link String#format(String, Object...) formatted} regular message.
     *
     * @param formatMessage the message with format specifiers
     * @param elements      the arguments for the format specifiers in the format
     *                      message
     */
    default void message(String formatMessage, Object... elements) {
        message(() -> String.format(formatMessage, elements));
    }

    /**
     * Logs a regular message.
     *
     * @param messageObject the message object
     */
    default void message(Object messageObject) {
        message(() -> String.valueOf(messageObject));
    }

    /**
     * Logs a regular message without any formatters.
     *
     * @param messageObject the message object
     */
    default void plainMessage(Object messageObject) {
        plainMessage(() -> String.valueOf(messageObject));
    }

    default void noLineBreakMessage(Supplier<String> message) {
        print(message, Verbosity.MESSAGE, false);
    }

    default void noLineBreakMessage(String formatMessage, Object... elements) {
        noLineBreakMessage(() -> String.format(formatMessage, elements));
    }

    default void noLineBreakMessage(Object messageObject) {
        noLineBreakMessage(() -> String.valueOf(messageObject));
    }

    /**
     * Logs a debug message.
     *
     * @param message the message
     */
    default void debug(Supplier<String> message) {
        println(message, Verbosity.DEBUG, true);
    }

    /**
     * Logs a {@link String#format(String, Object...) formatted} debug message.
     *
     * @param formatMessage the message with format specifiers
     * @param elements      the arguments for the format specifiers in the format
     *                      message
     */
    default void debug(String formatMessage, Object... elements) {
        debug(() -> String.format(formatMessage, elements));
    }

    /**
     * Logs a debug message.
     *
     * @param messageObject the message object
     */
    default void debug(Object messageObject) {
        debug(() -> String.valueOf(messageObject));
    }

    /**
     * Logs a progress message.
     *
     * @param message the message
     */
    default void progress(Supplier<String> message) {
        printProgress(message);
    }

    /**
     * Logs a {@link String#format(String, Object...) formatted} progress message.
     *
     * @param formatMessage the message with format specifiers
     * @param elements      the arguments for the format specifiers in the format
     *                      message
     */
    default void progress(String formatMessage, Object... elements) {
        progress(() -> String.format(formatMessage, elements));
    }

    /**
     * Logs a progress message.
     *
     * @param messageObject the message object
     */
    default void progress(Object messageObject) {
        progress(() -> String.valueOf(messageObject));
    }

    /**
     * Logs a message.
     *
     * @param message   the message
     * @param verbosity the kind of verbosity to log
     * @param format whether to apply the {@link ConfigurableLog.Configuration configured} {@link IFormatter formatters} to the message before logging
     */
    default void log(Supplier<String> message, Verbosity verbosity, boolean format) {
        println(message, verbosity, format);
    }

    default void printProgress(Supplier<String> message) {
        println(message, Verbosity.PROGRESS, false);
    }

    default void dispose() {
        println(() -> "", Verbosity.PROGRESS, false);
    }

    void print(Supplier<String> message, Verbosity verbosity, boolean format);

    void println(Supplier<String> message, Verbosity verbosity, boolean format);

    void println(Throwable error, Verbosity verbosity);
}
