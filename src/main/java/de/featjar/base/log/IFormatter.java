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

import de.featjar.base.log.Log.Verbosity;

/**
 * Formats a log message.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IFormatter {
    /**
     * {@return a prefix to a log message}
     * @param message the message
     * @param verbosity the verbosity level of the message
     */
    default String getPrefix(String message, Verbosity verbosity) {
        return "";
    }

    /**
     * {@return a suffix to a log message}
     * @param message the message
     * @param verbosity the verbosity level of the message
     */
    default String getSuffix(String message, Verbosity verbosity) {
        return "";
    }
}
