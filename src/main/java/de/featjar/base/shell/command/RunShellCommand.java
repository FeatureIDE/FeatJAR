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
package de.featjar.base.shell.command;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Commands;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.shell.AbortException;
import de.featjar.base.shell.Shell;
import de.featjar.base.shell.ShellSession;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Allows {@link ACommand} to be run via the shell with the possibility to
 * alter certain options before the command is executed.
 *
 * @author Niclas Kleinert
 * @author Sebastian Krieter
 */
public class RunShellCommand implements IShellCommand {

    @Override
    public void execute(ShellSession session, LinkedList<String> cmdParams) throws AbortException {
        if (cmdParams.isEmpty()) {
            FeatJAR.log().info(String.format("Usage: %s", getDescription().orElse("")));
            return;
        }

        if ("help".equals(cmdParams.get(0))) {
            List<ICommand> extensions = Commands.getInstance().getExtensions();
            extensions.stream()
                    .map(c -> c.getShortName().orElse(null))
                    .filter(Objects::nonNull)
                    .forEach(FeatJAR.log()::message);
            return;
        }

        try {
            Result<ICommand> commandResult = Commands.getCommandByName(cmdParams.get(0));
            if (commandResult.isEmpty()) {
                FeatJAR.log().problems(commandResult);
                return;
            }
            ICommand cliCommand = commandResult.get();

            int runResult = cliCommand.run(alterOptions(cliCommand));

            if (runResult == 0) {
                FeatJAR.log().message("Successfull");
            } else {
                FeatJAR.log().error("Errorcode '%d' occured in command '%s'", runResult, cliCommand.getIdentifier());
            }

        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            FeatJAR.log().error(iae.getMessage());
            FeatJAR.log().info(String.format("Usage %s", getDescription().get()));
        }
    }

    private OptionList alterOptions(ICommand cliCommand) throws AbortException {
        FeatJAR.log().message("Alter options? Select a number or leave blank to proceed.");
        OptionList shellOptions = new OptionList();
        shellOptions.addOptions(cliCommand.getOptions());

        printOptions(shellOptions);

        while (true) {
            Shell.message("Enter number:");
            String indexString = Shell.readText().orElse("").toLowerCase();
            if (indexString.isBlank()) {
                break;
            }

            Option<?> option;
            try {
                int index = Integer.parseInt(indexString);
                if (1 <= index && index <= shellOptions.getOptions().size()) {
                    option = shellOptions.getOptions().get(index - 1);
                } else {
                    FeatJAR.log().error("Number does not exist");
                    continue;
                }
            } catch (NumberFormatException e) {
                FeatJAR.log().error("Only decimal numbers are a valid choice");
                continue;
            }

            Shell.message("Enter new value:");
            String valueString = Shell.readText().orElse("");

            List<Problem> problems = shellOptions.parseOption(option, valueString);
            if (problems.isEmpty()) {
                printOptions(shellOptions);
            } else {
                FeatJAR.log().problems(problems);
            }
        }
        return shellOptions;
    }

    private void printOptions(OptionList shellOptions) {
        AtomicInteger i = new AtomicInteger(1);
        shellOptions.getOptions().forEach(o -> {
            FeatJAR.log()
                    .message(
                            "\t%d: %s = %s",
                            i.getAndIncrement(),
                            o.getName(),
                            shellOptions.getResult(o).map(String::valueOf).orElse("NULL"));
        });
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("run");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("run cli command with variables from seesion");
    }
}
