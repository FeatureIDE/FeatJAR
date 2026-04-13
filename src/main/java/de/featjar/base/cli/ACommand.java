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

import java.nio.file.Path;
import java.util.List;

/**
 * The abstract class for any command.
 *
 * @author Sebastian Krieter
 */
public abstract class ACommand implements ICommand {

    /**
     * Input option for loading files.
     */
    public static final Option<Path> INPUT_OPTION = Option.newOption("input", Option.PathParser)
            .setDescription("Path to input file(s)")
            .setValidator(Option.PathValidator);

    /**
     * Output option for saving files.
     */
    public static final Option<Path> OUTPUT_OPTION =
            Option.newOption("output", Option.PathParser).setDescription("Path to output file(s)");

    /**
     * {@return all options registered for the calling class}
     */
    public final List<Option<?>> getOptions() {
        return Option.getAllOptions(getClass());
    }
}
