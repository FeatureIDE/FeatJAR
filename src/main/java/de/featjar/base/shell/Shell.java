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
package de.featjar.base.shell;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.shell.command.IShellCommand;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

/**
 * The basic shell that provides direct access to all commands of FeatJAR.
 *
 * @author Niclas Kleinert
 */
public class Shell {
    private static final String START_OF_TERMINAL_LINE = "$ ";

    private static Shell instance;

    public static Shell getInstance() {
        return (instance == null) ? (instance = new Shell()) : instance;
    }

    private static class ShellHistoryIterator {
        private final ShellHistory history;
        private int index;

        public ShellHistoryIterator(ShellHistory history) {
            this.history = history;
            reset();
        }

        public void reset() {
            index = history.lastIndex;
        }

        public String prevEntry() {
            if (index == -1) {
                return "";
            }
            if (--index < 0) {
                index = history.size - 1;
            }
            return history.lines[index];
        }

        public String curEntry() {
            if (index == -1) {
                return "";
            }
            return history.lines[index];
        }

        public String nextEntry() {
            if (index == -1) {
                return "";
            }
            index = (index + 1) % history.size;
            return history.lines[index];
        }
    }

    private static class ShellHistory {
        private final int maxSize = 100;

        private String[] lines = new String[maxSize];
        private int lastIndex = -1;
        private int size = 0;

        public void add(String line) {
            lastIndex = (lastIndex + 1) % maxSize;
            lines[lastIndex] = line;
            size = Math.min(size + 1, maxSize);
        }
    }

    private final ShellSession session;
    private final ShellHistory history;
    private ShellHistoryIterator historyIterator;
    private final Scanner shellScanner;
    private final BufferedReader reader;
    private StringBuilder input;
    private int cursorX;

    private Shell() {
        session = new ShellSession();
        history = new ShellHistory();

        if (isWindows()) {
            this.shellScanner = new Scanner(System.in);
            this.reader = null;
        } else {
            this.shellScanner = null;
            this.reader = new BufferedReader(new InputStreamReader(System.in));
        }
    }

    public int run() {
        FeatJAR.initialize(FeatJAR.shellConfiguration());
        printArt();
        FeatJAR.log().message("This is FeatJAR. Type in 'help' to see available commands.");
        while (true) {
            try {
                Optional<String> readCommand = readCommand();
                if (readCommand.isEmpty()) {
                    continue;
                }
                String commandLine = readCommand.get();
                LinkedList<String> cmdArg =
                        Arrays.stream(commandLine.split("\\s+")).collect(Collectors.toCollection(LinkedList::new));
                try {
                    Result<IShellCommand> parsedCommand = parseCommand(cmdArg.removeFirst());
                    if (parsedCommand.isEmpty()) {
                        continue;
                    }
                    history.add(commandLine);
                    parsedCommand.get().execute(session, cmdArg);
                } catch (CancellationException e) {
                    FeatJAR.log().message(e.getMessage());
                }
            } catch (CancellationException e) {
                FeatJAR.log().message(e.getMessage());
                exitInputMode();
                break;
            } catch (Throwable e) {
                FeatJAR.log().message(e.getMessage());
                exitInputMode();
                return 1;
            }
        }
        return 0;
    }

    private Result<IShellCommand> parseCommand(String commandString) throws AbortException {
        ShellCommands shellCommandsExentionsPoint = ShellCommands.getInstance();
        List<IShellCommand> commands = shellCommandsExentionsPoint.getExtensions().stream()
                .filter(command -> command.getShortName()
                        .map(name -> name.toLowerCase().startsWith(commandString))
                        .orElse(Boolean.FALSE))
                .collect(Collectors.toList());

        if (commands.size() > 1) {
            Map<Integer, IShellCommand> ambiguousCommands = new HashMap<>();
            int i = 1;

            FeatJAR.log()
                    .info(
                            ("Command name '%s' is ambiguous! choose one of the following %d commands (leave blank to abort): \n"),
                            commandString,
                            commands.size());

            for (IShellCommand c : commands) {
                Optional<String> commandDescription = IShellCommand.getCommandDescriptionString(c);
                if (commandDescription.isPresent()) {
                    FeatJAR.log().message(i + "." + commandDescription.get());
                    ambiguousCommands.put(i, c);
                    i++;
                }
            }

            String choice = readText().orElse("");

            if (choice.isBlank()) {
                return Result.empty();
            }
            int parsedChoice;
            try {
                parsedChoice = Integer.parseInt(choice);
            } catch (NumberFormatException e) {
                return Result.empty(addProblem(Severity.ERROR, String.format("'%s' is no vaild number", choice), e));
            }

            for (Map.Entry<Integer, IShellCommand> entry : ambiguousCommands.entrySet()) {
                if (Objects.equals(entry.getKey(), parsedChoice)) {
                    return Result.of(entry.getValue());
                }
            }
            return Result.empty(addProblem(
                    Severity.ERROR,
                    "Command name '%s' is ambiguous! It matches the following commands: \n%s and wrong number !",
                    commandString,
                    commands.stream().map(IShellCommand::getIdentifier).collect(Collectors.joining("\n"))));
        }

        IShellCommand command = null;
        if (commands.isEmpty()) {
            Result<IShellCommand> matchingExtension = shellCommandsExentionsPoint.getMatchingExtension(commandString);
            if (matchingExtension.isEmpty()) {
                FeatJAR.log().message("No such command '" + commandString + "'. \n <help> shows all viable commands");
                return Result.empty(addProblem(Severity.ERROR, "No command matched the name '%s'!", commandString));
            }
            command = matchingExtension.get();
        } else {
            if (commands.get(0).getShortName().get().toLowerCase().matches(commandString)) {
                command = commands.get(0);
                return Result.of(command);
            }
            FeatJAR.log()
                    .message(
                            "Do you mean: %s? (y)es/(n)o/(a)bort",
                            commands.get(0).getShortName().get());
            String choice = readResponse().orElse("");
            if (choice.isEmpty()) {
                command = commands.get(0);
            } else {
                return Result.empty();
            }
        }
        return Result.of(command);
    }

    private Problem addProblem(Severity severity, String message, Object... arguments) {
        return new Problem(String.format(message, arguments), severity);
    }

    private void handleTabulatorAutoComplete(String start) {
        if (input.length() == 0) {
            return;
        }
        List<String> commands = ShellCommands.getInstance().getExtensions().stream()
                .filter(command -> command.getShortName()
                        .map(name -> name.toLowerCase().startsWith(String.valueOf(input)))
                        .orElse(Boolean.FALSE))
                .map(cmd -> cmd.getShortName().get().toLowerCase())
                .collect(Collectors.toList());

        if (commands.isEmpty()) {
            return;
        }

        String prefix = commands.get(0);

        for (int i = 1; i < commands.size(); i++) {
            prefix = calculateSimilarPrefix(prefix, commands.get(i));
        }
        input.setLength(0);
        input = input.append(prefix);
        cursorX = input.length();

        displayCharacters(start, String.valueOf(input));
    }

    private String calculateSimilarPrefix(String oldPrefix, String nextString) {
        int minPrefixLength = Math.min(oldPrefix.length(), nextString.length());
        int i = 0;
        while (i < minPrefixLength && oldPrefix.charAt(i) == nextString.charAt(i)) {
            i++;
        }
        return oldPrefix.substring(0, i);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Displays the typed characters in the console.
     *
     * @param typedText the typed characters
     */
    private void displayCharacters(String start, String typedText) {
        /*
         * '\r' moves the cursor to the beginning of the line
         * '\u001B[2K' or '\033[2K' erases the entire line
         * '\u001B' (unicode) or '\033' (octal) for ESC work fine here
         * '\u001B[#G' moves cursor to column #
         * see for more documentation: https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797
         */
        FeatJAR.log().noLineBreakMessage("\r\033[2K%s%s\033[%dG", start, typedText, cursorX + start.length() + 1);
    }

    /**
     * Static access for {@link #readShellCommand(String)}
     * Reads characters one by one without line buffering into a string until ENTER is pressed.
     * Handles special keys. ESC cancels every command. Ensures that arrow keys work as in a normal shell (including a terminal history).
     * Page keys are ignored. Interrupts do not need special treatment and, therefore, work as usual.
     *
     * @param prompt the message that is shown in the terminal
     * @return all normal keys combined into a string
     * @throws AbortException
     */
    public static Optional<String> readCommand() throws AbortException {
        FeatJAR.log().noLineBreakMessage(START_OF_TERMINAL_LINE);
        return Shell.getInstance().readShellCommand(START_OF_TERMINAL_LINE);
    }

    /**
     * Static access for {@link #readShellCommand(String)}
     * Reads characters one by one without line buffering into a string until ENTER is pressed.
     * Handles special keys. ESC cancels every command. Ensures that arrow keys work as in a normal shell (including a terminal history).
     * Page keys are ignored. Interrupts do not need special treatment and, therefore, work as usual.
     *
     * @param prompt the message that is shown in the terminal
     * @return all normal keys combined into a string
     * @throws AbortException
     */
    public static Optional<String> readText() throws AbortException {
        return Shell.getInstance().readShellCommand("");
    }

    public static void message(String message, Object... args) {
        FeatJAR.log().message(message, args);
    }

    public static Optional<String> readResponse() throws AbortException {
        return Shell.getInstance().read();
    }

    private Optional<String> read() throws AbortException {
        if (isWindows()) {
            String inputWindows = shellScanner.nextLine().trim();
            return inputWindows.isEmpty() ? Optional.empty() : Optional.of(inputWindows);
        }

        input = new StringBuilder();
        int key;

        try {
            enterInputMode();
            key = reader.read();
            if (isEscape(key)) {
                handleEscapeKey("", key);
            } else {
                handleNormalKey("", key, false);
            }
        } catch (IOException e) {
            FeatJAR.log().error(e);
        } finally {
            exitInputMode();
        }
        cursorX = 0;
        return input.length() == 0 ? Optional.empty() : Optional.of(String.valueOf(input));
    }

    private Optional<String> readShellCommand(String start) throws AbortException {
        if (isWindows()) {
            String inputWindows = shellScanner.nextLine().trim();
            return inputWindows.isEmpty() ? Optional.empty() : Optional.of(inputWindows);
        }

        input = new StringBuilder();
        int key;
        historyIterator = null;

        try {
            enterInputMode();
            while (true) {
                key = reader.read();

                if (isEnter(key)) {
                    FeatJAR.log().noLineBreakMessage("\r\n");
                    break;
                }

                if (isTabulator(key)) {
                    exitHitory(start);
                    handleTabulatorAutoComplete(start);
                } else if (isBackspace(key)) {
                    exitHitory(start);
                    handleBackspaceKey(start);
                } else if (isEscape(key)) {
                    handleEscapeKey(start, key);
                } else {
                    exitHitory(start);
                    handleNormalKey(start, key, true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exitInputMode();
        }
        cursorX = 0;

        return input.length() == 0 ? Optional.empty() : Optional.of(String.valueOf(input));
    }

    private void handleEscapeKey(String start, int key) throws IOException, AbortException {
        key = getNextKey(key);
        if (key == '[') {
            key = getNextKey(key);
            if (isPageKey(key)) {
                // ignore
            } else if (isDelete(key)) {
                exitHitory(start);
                handleDeleteKey(start, key);
            } else {
                handleArrowKeys(start, key);
            }
        } else if (input.length() != 0) {
            exitHitory(start);
            resetInputLine(start);
        } else {
            exitInputMode();
            throw new AbortException();
        }
    }

    private int getNextKey(int key) throws IOException {
        return reader.ready() ? reader.read() : key;
    }

    private boolean isPageKey(int key) throws IOException {
        return (key == 53 || key == 54) && getNextKey(key) == 126;
    }

    private boolean isTabulator(int key) {
        return key == 9;
    }

    private boolean isDelete(int key) {
        return key == 51;
    }

    private boolean isEscape(int key) {
        return key == 27;
    }

    private boolean isBackspace(int key) {
        return key == 127 || key == 8;
    }

    private boolean isEnter(int key) {
        return key == '\r' || key == '\n';
    }

    private void handleArrowKeys(String start, int key) {
        final char ARROW_UP = 'A', ARROW_DOWN = 'B', ARROW_RIGHT = 'C', ARROW_LEFT = 'D';
        switch (key) {
            case ARROW_UP:
                if (historyIterator == null) {
                    historyIterator = new ShellHistoryIterator(history);
                } else {
                    historyIterator.nextEntry();
                }
                showHistory(start);
                break;
            case ARROW_DOWN:
                if (historyIterator != null) {
                    historyIterator.prevEntry();
                    showHistory(start);
                }
                break;
            case ARROW_RIGHT:
                moveCursorRight();
                break;
            case ARROW_LEFT:
                moveCursorLeft();
                break;
        }
    }

    private void handleBackspaceKey(String start) {
        if (input.length() != 0) {
            if (cursorX >= 0) {
                if (cursorX <= input.length() && cursorX != 0) {
                    input.deleteCharAt(cursorX - 1);
                    displayCharacters(start, input.toString());
                    FeatJAR.log().noLineBreakMessage("\b");
                }
                if (cursorX != 0) {
                    cursorX--;
                }
            }
        }
    }

    private void handleDeleteKey(String start, int key) throws IOException {
        key = getNextKey(key);
        if (key == '~') {
            if (input.length() != 0 && cursorX != input.length()) {
                input.deleteCharAt(cursorX);
                displayCharacters(start, input.toString());
            }
        }
    }

    private void handleNormalKey(String start, int key, boolean display) {
        cursorX++;

        if (input.length() == 0) {
            input.append((char) key);
        } else {
            input.insert(cursorX - 1, (char) key);
        }
        if (display) {
            displayCharacters(start, input.toString());
        }
    }

    private void resetInputLine(String start) {
        input.setLength(0);
        cursorX = 0;
        displayCharacters(start, "");
    }

    /*
     * Moves out of the command history and resets the two lastArrowKey booleans.
     */

    private void exitHitory(String start) {
        if (historyIterator != null) {
            resetInputLine(start);
            historyIterator = null;
        }
    }

    private void showHistory(String start) {
        String historyEntry = historyIterator.curEntry();
        input.setLength(0);
        input.append(historyEntry);
        cursorX = input.length() - 1;
        displayCharacters(start, historyEntry);
    }

    private void moveCursorLeft() {
        if (cursorX > 0) {
            cursorX--;
            FeatJAR.log().noLineBreakMessage("\033[D");
        }
    }

    private void moveCursorRight() {
        if (cursorX < input.length()) {
            cursorX++;
            FeatJAR.log().noLineBreakMessage("\033[C");
        }
    }

    /**
     *Sets the terminal into a 'raw' like mode that has no line buffer such that the shell can read a single key press,
     *signals like CTRL+C do still work.
     */
    private void enterInputMode() {
        try {
            /*
             * sh executes the command in a new console.
             * -c tells the shell to read commands from the following string.
             * stty change and print terminal line settings
             * -icanon disables the classical line buffered input mode, instead every key press gets directly send to the terminal
             * -echo has to be disabled in combination with icanon to allow ANSI escape sequences to actually to what they are supposed to do
             * (e.g. "\033[D" to move the cursor one space to the left). Otherwise the control code gets directly printed to the console
             * without executing the the ANSI escape sequence.
             */
            Runtime.getRuntime()
                    .exec(new String[] {"sh", "-c", "stty -icanon -echo </dev/tty"})
                    .waitFor();
        } catch (InterruptedException | IOException e) {
            FeatJAR.log().error("Could not enter terminal input mode: " + e.getMessage());
        }
    }

    /**
     * Resets the the changes made in {@link Shell#enterInputMode()} and sets the terminal back into 'cooked' (normal) mode.
     */
    private void exitInputMode() {
        try {
            Runtime.getRuntime()
                    .exec(new String[] {"sh", "-c", "stty sane < /dev/tty"})
                    .waitFor();
        } catch (IOException | InterruptedException e) {
            FeatJAR.log().error("Could not leave terminal input mode: " + e.getMessage());
        }
    }

    private void printArt() {
        FeatJAR.log().message(" _____             _       _    _     ____   ____   _            _  _ ");
        FeatJAR.log().message("|  ___|___   __ _ | |_    | |  / \\   |  _ \\ / ___| | |__    ___ | || |");
        FeatJAR.log().message("| |_  / _ \\ / _` || __|_  | | / _ \\  | |_) |\\___ \\ | '_ \\  / _ \\| || |");
        FeatJAR.log().message("|  _||  __/| (_| || |_| |_| |/ ___ \\ |  _ <  ___) || | | ||  __/| || |");
        FeatJAR.log().message("|_|   \\___| \\__,_| \\__|\\___//_/   \\_\\|_| \\_\\|____/ |_| |_| \\___||_||_|");
        FeatJAR.log().message("\n");
    }
}
