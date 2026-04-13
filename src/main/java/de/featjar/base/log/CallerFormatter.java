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
package de.featjar.base.log;

import de.featjar.base.FeatJAR;
import de.featjar.base.env.StackTrace;
import de.featjar.base.log.Log.Verbosity;

/**
 * Prepends a log message with the location of the logging code.
 * To this end, looks for the most recent element on the stack that does not belong
 * to {@link Thread} or to the {@link de.featjar.base.log} package and prints it.
 *
 * @author Elias Kuiter
 */
public class CallerFormatter implements IFormatter {

    private boolean includeLineNumber;

    public CallerFormatter() {}

    public CallerFormatter(boolean includeLineNumber) {
        this.includeLineNumber = includeLineNumber;
    }

    @Override
    public String getPrefix(String message, Verbosity verbosity) {
        return String.format(
                "[%s] ",
                new StackTrace()
                        .removeTop()
                        .removeClassNamePrefix(getClass().getPackageName())
                        .getTop()
                        .map(this::getName)
                        .orElse(""));
    }

    private String getName(StackTraceElement stackTraceElement) {
        String shortName =
                shorten(String.format("%s.%s", stackTraceElement.getClassName(), stackTraceElement.getMethodName()));
        if (includeLineNumber) {
            shortName += ":" + stackTraceElement.getLineNumber();
        }
        return shortName;
    }

    private static String shorten(String s) {
        return s.replace(FeatJAR.ROOT_PACKAGE_NAME + ".", "")
                .replaceAll("\\.<.*", "")
                .replaceAll("\\.lambda\\$.*", "");
    }
}
