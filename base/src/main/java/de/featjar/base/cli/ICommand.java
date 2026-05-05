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

import de.featjar.base.extension.IExtension;
import de.featjar.base.shell.ShellSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A command run within a {@link Commands}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface ICommand extends IExtension {

    /**
     * {@return this command's description, if any}
     */
    default Optional<String> getDescription() {
        return Optional.empty();
    }

    /**
     * {@return this command's options}
     */
    default List<Option<?>> getOptions() {
        return new ArrayList<>();
    }

    /**
     * {@return this command's short name, if any} The short name can be used to call this command from the CLI.
     */
    default Optional<String> getShortName() {
        return Optional.empty();
    }

    /**
     * Runs this command with some given options.
     *
     * @param optionParser the option parser
     *
     * @return exit code
     */
    int run(OptionList optionParser);

    /**
     * Parses arguments into an option list.
     *
     * @param session the shell session
     * @param cmdParams the given arguments for the command
     * @return an option list containing parsed arguments
     */
    default OptionList getShellOptions(ShellSession session, List<String> cmdParams) {
        OptionList optionList = new OptionList();
        optionList.parseArguments();
        return optionList;
    }
}
