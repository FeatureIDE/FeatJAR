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
package de.featjar.base.env;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Executes an external executable in a process.
 *
 * @author Elias Kuiter
 */
public class Process implements Supplier<Result<List<String>>> {
    private final Path executablePath;
    private final Map<String, String> environmentVariables;
    private final List<String> arguments;
    private final Duration timeout;
    private boolean errorOccurred;
    /**
     * Constructs a new process object.
     * @param executablePath the path to the executable
     * @param arguments the command line arguments for the executable
     */
    public Process(Path executablePath, String... arguments) {
        this(executablePath, List.of(arguments));
    }
    /**
     * Constructs a new process object.
     * @param executablePath the path to the executable
     * @param arguments the command line arguments for the executable
     */
    public Process(Path executablePath, List<String> arguments) {
        this(executablePath, arguments, null, null);
    }
    /**
     * Constructs a new process object.
     * @param executablePath the path to the executable
     * @param arguments the command line arguments for the executable
     * @param timeout the timeout after which the process is stopped
     */
    public Process(Path executablePath, List<String> arguments, Duration timeout) {
        this(executablePath, arguments, null, timeout);
    }
    /**
     * Constructs a new process object.
     * @param executablePath the path to the executable
     * @param arguments the command line arguments for the executable
     * @param environmentVariables the environment variables for the executable
     */
    public Process(Path executablePath, List<String> arguments, Map<String, String> environmentVariables) {
        this(executablePath, arguments, environmentVariables, null);
    }
    /**
     * Constructs a new process object.
     * @param executablePath the path to the executable
     * @param environmentVariables the environment variables for the executable
     * @param timeout the timeout after which the process is stopped
     */
    public Process(Path executablePath, Map<String, String> environmentVariables, Duration timeout) {
        this(executablePath, null, environmentVariables, timeout);
    }

    /**
     * Constructs a new process object.
     * @param executablePath the path to the executable
     * @param arguments the command line arguments for the executable
     * @param environmentVariables the environment variables for the executable
     * @param timeout the timeout after which the process is stopped
     */
    public Process(
            Path executablePath, List<String> arguments, Map<String, String> environmentVariables, Duration timeout) {
        this.executablePath = Objects.requireNonNull(executablePath);
        this.arguments = arguments == null ? List.of() : arguments;
        this.environmentVariables = environmentVariables == null ? Map.of() : environmentVariables;
        this.timeout = timeout;
    }

    /**
     * Starts the process and lets it run in a separate thread.
     *
     * @return an instance of {@link java.lang.Process}
     *
     * @throws IOException if an I/O error occurs
     */
    public java.lang.Process start() throws IOException {
        List<String> command = new ArrayList<>();
        command.add(executablePath.toString());
        command.addAll(arguments);

        FeatJAR.log().debug(String.join(" ", command));
        FeatJAR.log().debug(environmentVariables);

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().putAll(environmentVariables);

        return processBuilder.start();
    }

    @Override
    public Result<List<String>> get() {
        List<String> output = new ArrayList<>();
        List<String> error = new ArrayList<>();
        Result<Void> result = run(output::add, error::add);

        if (result.isPresent()) {
            output.addAll(error);
            return result.map(r -> output);
        } else {
            if (error.isEmpty()) {
                return result.map(r -> null);
            } else {
                return Result.empty(new Problem(error.stream().collect(Collectors.joining("\n")), Severity.ERROR));
            }
        }
    }

    /**
     * Starts the process in a separate thread and waits for it to finish or until the timeout.
     * @param input the input to provide to the started process
     * @param outConsumer the consumer of the process' output stream
     * @param errConsumer the consumer of the process' error stream
     * @return a result containing potential problems during execution
     *
     * @see #run()
     * @see #run(Consumer, Consumer)
     */
    public Result<Void> run(String input, Consumer<String> outConsumer, Consumer<String> errConsumer) {
        java.lang.Process process = null;
        try {
            Instant start = Instant.now();
            process = start();
            if (input != null) {
                process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
                process.getOutputStream().close();
            }
            consumeInputStream(process.getInputStream(), outConsumer, false);
            consumeInputStream(process.getErrorStream(), errConsumer, true);
            boolean terminatedInTime = true;
            if (timeout != null && !timeout.isZero())
                terminatedInTime = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            else process.waitFor();
            long elapsedTime = Duration.between(start, Instant.now()).toMillis();
            final int exitValue = process.exitValue();
            Result<Void> result;
            if (!errorOccurred) {
                result = Result.ofVoid();
            } else {
                result = Result.empty(
                        new Problem(executablePath + " exited with value " + exitValue, Problem.Severity.ERROR));
            }
            return result.addProblemInformation(
                    new Problem("exit code = " + exitValue, Problem.Severity.INFO),
                    new Problem("in time = " + terminatedInTime, Problem.Severity.INFO),
                    new Problem("elapsed time in ms = " + elapsedTime, Problem.Severity.INFO));
        } catch (IOException | InterruptedException e) {
            return Result.empty(e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
                process = null;
            }
        }
    }

    /**
     * Starts the process in a separate thread and waits for it to finish or until the timeout.
     * @param outConsumer the consumer of the process' output stream
     * @param errConsumer the consumer of the process' error stream
     * @return a result containing potential problems during execution
     *
     * @see #run()
     * @see #run(String, Consumer, Consumer)
     */
    public Result<Void> run(Consumer<String> outConsumer, Consumer<String> errConsumer) {
        return run(null, outConsumer, errConsumer);
    }
    /**
     * Starts the process in a separate thread and waits for it to finish or until the timeout.
     * @return a result containing potential problems during execution
     *
     * @see #run(Consumer, Consumer)
     * @see #run(String, Consumer, Consumer)
     */
    public Result<Void> run() {
        return run(null, null, null);
    }

    private void consumeInputStream(InputStream inputStream, Consumer<String> consumer, boolean isError) {
        if (consumer != null) {
            new Thread(() -> {
                        try (BufferedReader reader =
                                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                                consumer.accept(line);
                                if (isError) errorOccurred = true;
                            }
                        } catch (final IOException e) {
                            FeatJAR.log().error(e);
                        }
                    })
                    .start();
        }
    }
}
