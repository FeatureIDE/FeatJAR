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
import de.featjar.base.log.Log.Verbosity;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * Runs FeatJAR commands in one long-lived, machine-oriented shell session.
 *
 * <p>This class was initially generated with AI assistance to address an integration issue between the VS Code
 * extension and the original interactive shell. The original shell interpreted spaces as argument separators,
 * which made file paths containing spaces impossible to pass reliably. This protocol preserves each argument as a
 * separate tab-delimited field, so paths and other argument values may safely contain spaces.
 *
 * <p>The shell reads one tab-separated request per line from standard input. Command arguments must not contain
 * tabs or line breaks. Command output is Base64 URL encoded so it can contain line breaks safely. The shell
 * processes requests serially and emits exactly one response per request on standard output.
 *
 * <p>Request format: {@code RUN<TAB>argument...}. The special request {@code SHUTDOWN} closes the session.
 * Responses are {@code READY} and {@code RESULT<TAB>base64-output}.
 *
 * @author FeatJAR-Development-Team
 */
public final class ExtensionShell {
    private ExtensionShell() {}
    private static final String SHUTDOWN_REQUEST = "SHUTDOWN";
    private static final String RUN_REQUEST = "RUN";
    /**
     * Starts the extension shell as a standalone Java process.
     *
     * @param arguments ignored
     * @throws IOException if standard input cannot be read
     */
    public static void main(String[] arguments) throws IOException {
        System.exit(run());
    }

    /**
     * Initializes FeatJAR once and processes requests until shutdown or end of input.
     *
     * @return {@link FeatJAR#EXIT_SUCCESS} after a normal shutdown, otherwise an error code
     */
    public static int run() throws IOException {
        final PrintStream protocolOutput = System.out;
        final ByteArrayOutputStream commandBytes = new ByteArrayOutputStream();
        final PrintStream commandOutput = new PrintStream(commandBytes, true, StandardCharsets.UTF_8);
        final FeatJAR.Configuration configuration = FeatJAR.shellConfiguration();
        configuration.logConfig.resetLogStreams().logToStream(commandOutput, "extension-shell", Verbosity.MESSAGE);

        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                FeatJAR ignored = FeatJAR.initialize(configuration)) {
            protocolOutput.println("READY");
            protocolOutput.flush();

            String line;
            while ((line = input.readLine()) != null) {
                if (SHUTDOWN_REQUEST.equals(line)) {
                    return FeatJAR.EXIT_SUCCESS;
                }

                runRequest(line, commandBytes, protocolOutput);
            }

            return FeatJAR.EXIT_SUCCESS;
        } finally {
            commandOutput.close();
        }
    }

    private static void runRequest(String line, ByteArrayOutputStream commandBytes, PrintStream protocolOutput) {
        commandBytes.reset();
        try {
            final Request request = parseRequest(line);
            FeatJAR.runInternally(request.arguments());
            final String output = commandBytes.toString(StandardCharsets.UTF_8);
            protocolOutput.printf("RESULT\t%s%n", encode(output));
        } catch (Exception exception) {
            protocolOutput.printf(
                    "RESULT\t%s%n",
                    encode("ERROR: " + exception.getMessage()));
        } finally {
            protocolOutput.flush();
        }
    }

    private static Request parseRequest(String line) {
        final String[] fields = line.split("\\t", -1);
        if (fields.length < 1 || !RUN_REQUEST.equals(fields[0])) {
            throw new IllegalArgumentException("Expected a RUN request");
        }

        final String[] arguments = Arrays.copyOfRange(fields, 1, fields.length);
        return new Request(arguments);
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private record Request(String[] arguments) {}
}
