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

import de.featjar.base.log.Log.Verbosity;

/**
 * Prepends different colors to logs and appends a reset of the colors as suffix.
 *
 * @author Niclas Kleinert
 */
public class ColorFormatter implements IFormatter {

    private static final String TERMINAL_COLOR_LIGHT_BLUE = "\033[38;2;173;236;255m";
    private static final String TERMINAL_COLOR_YELLOW = "\033[38;2;255;255;0m";
    private static final String TERMINAL_COLOR_RED = "\033[38;2;255;0;0m";
    private static final String TERMINAL_COLOR_RESET = "\033[0m";

    @Override
    public String getPrefix(String message, Verbosity verbosity) {
        switch (verbosity) {
            case INFO:
                return TERMINAL_COLOR_LIGHT_BLUE;
            case WARNING:
                return TERMINAL_COLOR_YELLOW;
            case ERROR:
                return TERMINAL_COLOR_RED;
            case MESSAGE:
            case PROGRESS:
            case DEBUG:
                break;
            default:
                throw new IllegalStateException(String.valueOf(verbosity));
        }
        return "";
    }

    @Override
    public String getSuffix(String message, Verbosity verbosity) {
        return TERMINAL_COLOR_RESET;
    }
}
