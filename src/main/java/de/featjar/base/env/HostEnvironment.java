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
package de.featjar.base.env;

import java.util.Locale;

/**
 * Utilities for host-specific operations and information.
 *
 * @author Elias Kuiter
 */
public class HostEnvironment {
    /**
     * Operating systems distinguished by FeatJAR.
     */
    public enum OperatingSystem {
        /**
         * Windows
         */
        WINDOWS,
        /**
         * MacOS
         */
        MAC_OS,
        /**
         * Linux
         */
        LINUX,
        /**
         * Something else
         */
        UNKNOWN
    }

    /**
     * The operating system running FeatJAR.
     */
    public static final OperatingSystem OPERATING_SYSTEM;

    static {
        final String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        OPERATING_SYSTEM = osName.matches(".*(win).*")
                ? OperatingSystem.WINDOWS
                : osName.matches(".*(mac).*")
                        ? OperatingSystem.MAC_OS
                        : osName.matches(".*(nix|nux|aix).*") //
                                ? OperatingSystem.LINUX //
                                : OperatingSystem.UNKNOWN;
    }

    /**
     * The current user's home directory.
     */
    public static final String HOME_DIRECTORY = System.getProperty("user.home");

    /**
     * {@return whether FeatJAR is currently running on Windows}
     */
    public static boolean isWindows() {
        return OPERATING_SYSTEM == OperatingSystem.WINDOWS;
    }

    /**
     * {@return whether FeatJAR is currently running on macOS}
     */
    public static boolean isMacOS() {
        return OPERATING_SYSTEM == OperatingSystem.MAC_OS;
    }

    /**
     * {@return whether FeatJAR is currently running on Linux}
     */
    public static boolean isLinux() {
        return OPERATING_SYSTEM == OperatingSystem.LINUX;
    }
}
