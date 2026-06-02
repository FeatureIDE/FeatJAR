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

import de.featjar.base.FeatJAR;
import java.nio.file.Path;
import java.util.List;

public class FeatJAROptions implements IHasOptions {

    public static void init() {}

    /**
     * Option for setting the configuration file.
     */
    public static final Option<List<String>> CONFIGURATION_OPTION =
            Option.newListOption("config", Option.StringParser).setDescription("The names of configuration files");

    /**
     * Option for setting a directory containing configuration files.
     */
    public static final Option<Path> CONFIGURATION_DIR_OPTION =
            Option.newOption("config_dir", Option.PathParser).setDescription("The path to the configuration files");

    /**
     * Option for printing usage information.
     */
    public static final Option<ICommand> COMMAND_OPTION = Option.newOption(
                    "command", s -> FeatJAR.extensionPoint(Commands.class)
                            .getMatchingExtension(s)
                            .orElseThrow())
            .setDescription("Classpath from command to execute");

    /**
     * Option for printing usage information.
     */
    public static final Option<Boolean> HELP_OPTION = Option.newFlag("help").setDescription("Print usage information");

    /**
     * Option for printing version information.
     */
    public static final Option<Boolean> VERSION_OPTION =
            Option.newFlag("version").setDescription("Print version information");
}
