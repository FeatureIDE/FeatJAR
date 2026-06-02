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
package de.featjar.base.cli;

import de.featjar.base.log.Log;
import java.nio.file.Path;
import java.util.List;

public class LogOptions implements IHasOptions {

    public static void init() {}

    /**
     * Option for printing version information.
     */
    public static final Option<Boolean> STACKTRACE_OPTION =
            Option.newFlag("print-stacktrace").setDescription("Print a stacktrace for all logged exceptions");

    /**
     * Option for writing less output to the console.
     */
    public static final Option<Boolean> QUIET_OPTION = Option.newFlag("quiet")
            .setDescription("Suppress all unnecessary output. (Overwrites --log-info and --log-error options)");

    /**
     * Option for writing progress regularly to the console.
     */
    public static final Option<Boolean> PROGRESS_OPTION =
            Option.newFlag("progress").setDescription("Shows progress regularly.");

    /**
     * Option to specify a path to a log file for non-error messages.
     */
    public static final Option<Path> INFO_FILE_OPTION =
            Option.newOption("info-file", Option.PathParser).setDescription("Path to info log file");

    /**
     * Option to specify a path to a log file for error messages.
     */
    public static final Option<Path> ERROR_FILE_OPTION =
            Option.newOption("error-file", Option.PathParser).setDescription("Path to error log file");

    /**
     * Option to configure which logging types count as non-error messages.
     */
    public static final Option<List<Log.Verbosity>> LOG_INFO_OPTION = Option.newEnumListOption(
                    "log-info", Log.Verbosity.class)
            .setDescription("Message types printed to the info stream")
            .setDefaultValue(List.of(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.PROGRESS));
    /**
     * Option to configure which logging types count as error messages.
     */
    public static final Option<List<Log.Verbosity>> LOG_ERROR_OPTION = Option.newEnumListOption(
                    "log-error", Log.Verbosity.class)
            .setDescription("Message types printed to the error stream.")
            .setDefaultValue(List.of(Log.Verbosity.WARNING, Log.Verbosity.ERROR));

    /**
     * Option to configure which logging types are written to the non-error log file (if one exists).
     */
    public static final Option<List<Log.Verbosity>> LOG_INFO_FILE_OPTION = Option.newEnumListOption(
                    "log-info-file", Log.Verbosity.class)
            .setDescription("Message types printed to the info file.")
            .setDefaultValue(List.of(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.DEBUG));

    /**
     * Option to configure which logging types are written to the error log file (if one exists).
     */
    public static final Option<List<Log.Verbosity>> LOG_ERROR_FILE_OPTION = Option.newEnumListOption(
                    "log-error-file", Log.Verbosity.class)
            .setDescription("Message types printed to the error file.")
            .setDefaultValue(List.of(Log.Verbosity.ERROR, Log.Verbosity.WARNING));
}
