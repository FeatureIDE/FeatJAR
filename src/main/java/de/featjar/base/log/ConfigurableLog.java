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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Maps;
import de.featjar.base.data.Sets;
import de.featjar.base.extension.IInitializer;
import de.featjar.base.io.MultiStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Logs messages to standard output and files. Formats log messages with
 * {@link IFormatter formatters}. TODO: instead of logging values directly, only
 * pass suppliers that are called if some log target is configured. this saves
 * time for creating log strings
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class ConfigurableLog implements Log, IInitializer {

    private static final PrintStream originalSystemOut = System.out;
    private static final PrintStream originalSystemErr = System.err;
    private static final StreamCollection originalOutStreamCollection = new StreamCollection(originalSystemOut);
    private static final StreamCollection originalErrStreamCollection = new StreamCollection(originalSystemErr);

    private static class StreamCollection {
        private final LinkedHashSet<PrintStream> streams = Sets.empty();

        public StreamCollection(PrintStream... streams) {
            this.streams.addAll(Arrays.asList(streams));
        }

        public void addStream(PrintStream stream) {
            streams.add(stream);
        }

        public LinkedHashSet<PrintStream> getStreams() {
            return streams;
        }
    }

    /**
     * Configures a log.
     * <ul>
     * <li>Allows to map {@link Log.Verbosity verbosities} to streams and log files with
     * {@link Configuration#logToStream(PrintStream, String, Log.Verbosity...) logToStream} and
     * {@link Configuration#logToFile(Path, Log.Verbosity...) logToFile}. Unmapped verbosities are not logged.</li>
     * <li>Allows to set {@link IFormatter formatters} with {@link Configuration#addFormatter(IFormatter) addFormatter}.</li>
     * <li>Allows to enable the stack trace for error logging with {@link Configuration#setPrintStacktrace(boolean) setPrintStacktrace}.</li>
     * </ul>
     */
    public static class Configuration {
        // TODO: to make this more general, we could use an OutputMapper here to
        // log to anything supported by an OutputMapper (even a ZIP file).
        private final LinkedHashMap<Verbosity, StreamCollection> logStreams = Maps.empty();
        private final LinkedList<IFormatter> formatters = new LinkedList<>();
        private boolean printStacktrace = false;

        private final LinkedHashMap<String, Integer> progressCharactersPerTarget = Maps.empty();
        private final LinkedHashMap<PrintStream, String> streamTargets = Maps.empty();

        /**
         * Constructs a new log configuration.
         */
        public Configuration() {
            resetLogStreams();
        }

        /**
         * {@return whether to print the stack trace}
         */
        public boolean isPrintStacktrace() {
            return printStacktrace;
        }
        /**
         * Sets whether to print the stack trace.
         * @param printStacktrace {@code true} to enable, {@code false} to disable
         * @return this configuration
         */
        public Configuration setPrintStacktrace(boolean printStacktrace) {
            this.printStacktrace = printStacktrace;
            return this;
        }

        /**
         * Resets all mapping for all streams.
         * @return this configuration
         */
        public Configuration resetLogStreams() {
            logStreams.clear();
            return this;
        }

        private void addStream(PrintStream stream, String target, Verbosity verbostiy) {
            StreamCollection multiStream = logStreams.get(verbostiy);
            if (multiStream == null) {
                multiStream = new StreamCollection();
                logStreams.put(verbostiy, multiStream);
            }
            multiStream.addStream(stream);
            streamTargets.put(stream, target);
            progressCharactersPerTarget.put(target, 0);
        }

        /**
         * Configures a stream to be a logging target.
         *
         * @param stream the stream
         * @param target the target of the stream (e.g., a specific file)
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToStream(PrintStream stream, String target, Verbosity... verbosities) {
            Objects.requireNonNull(stream);
            for (Verbosity verbosity : verbosities) {
                addStream(stream, target, verbosity);
            }
            return this;
        }

        /**
         * Configures a file to be a logging target.
         *
         * @param path        the path to the file
         * @param verbosities the logged verbosities
         * @return this configuration
         * @throws FileNotFoundException if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason.
         */
        public Configuration logToFile(Path path, Verbosity... verbosities) throws FileNotFoundException {
            Objects.requireNonNull(path);
            File file = path.toAbsolutePath().normalize().toFile();
            logToStream(
                    new PrintStream(new FileOutputStream(file), false, StandardCharsets.UTF_8),
                    file.toString(),
                    verbosities);
            return this;
        }

        /**
         * Configures the standard output stream to be a logging target.
         *
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToSystemOut(Verbosity... verbosities) {
            logToStream(originalSystemOut, "system", verbosities);
            return this;
        }

        /**
         * Configures the standard error stream to be a logging target.
         *
         * @param verbosities the logged verbosities
         * @return this configuration
         */
        public Configuration logToSystemErr(Verbosity... verbosities) {
            logToStream(originalSystemErr, "system", verbosities);
            return this;
        }

        /**
         * Configures a formatter for all logging targets.
         *
         * @param formatter the formatter
         * @return this configuration
         */
        public Configuration addFormatter(IFormatter formatter) {
            formatters.add(formatter);
            return this;
        }

        /**
         * Maps verbosities to appropriate standard streams if they are more or equally important than the given verbosity.
         * <br>
         * Order from most to least important: MESSAGE > ERROR > WARNING > INFO > DEBUG > PROGRESS
         * <br>
         * @param verbosity the least important verbosity that should be mapped
         * @return this configuration
         */
        public Configuration logAtMost(Verbosity verbosity) {
            switch (verbosity) {
                case MESSAGE:
                    logToSystemOut(Verbosity.MESSAGE);
                    break;
                case ERROR:
                    logToSystemErr(Verbosity.ERROR);
                    logToSystemOut(Verbosity.MESSAGE);
                    break;
                case WARNING:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE);
                    break;
                case INFO:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO);
                    break;
                case DEBUG:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.DEBUG);
                    break;
                case PROGRESS:
                    logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
                    logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.PROGRESS);
                    break;
                default:
                    break;
            }
            return this;
        }

        /**
         * Maps all verbosities to appropriate standard streams.
         * <br>
         * System.out: MESSAGE, INFO, DEBUG, PROGRESS
         * <br>
         * System.err: ERROR, WARNING
         * @return this configuration
         */
        public Configuration logAll() {
            logToSystemErr(Verbosity.ERROR, Verbosity.WARNING);
            logToSystemOut(Verbosity.MESSAGE, Verbosity.INFO, Verbosity.DEBUG, Verbosity.PROGRESS);
            return this;
        }
    }

    private Configuration configuration;
    /**
     * Creates a log based on the default configuration.
     */
    public ConfigurableLog() {}

    /**
     * Creates a log. Overrides the standard output/error streams. That is, calls to
     * {@link System#out} are equivalent to calling {@link #info(String, Object...)}.
     * Analogously, calls to {@link System#err} are equivalent to calling
     * {@link Log#error(String, Object...)}.
     *
     * @param configuration the configuration
     */
    public ConfigurableLog(Configuration configuration) {
        setConfiguration(configuration);
    }

    /**
     * {@inheritDoc} Resets the standard output/error streams.
     */
    @Override
    public void close() {
        if (configuration != null) {
            FeatJAR.log().debug("de-initializing log");
            System.setOut(originalSystemOut);
            System.setErr(originalSystemErr);
        }
    }

    /**
     * {@return this log's configuration}
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets this log's configuration.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(Configuration configuration) {
        FeatJAR.log().debug("setting new log configuration");
        this.configuration = configuration;
        if (configuration != null) {
            if (configuration.logStreams.get(Verbosity.MESSAGE) != null) {
                System.setOut(new PrintStream(new MultiStream(
                        configuration.logStreams.get(Verbosity.MESSAGE).getStreams())));
            }
            if (configuration.logStreams.get(Verbosity.ERROR) != null) {
                System.setErr(new PrintStream(new MultiStream(
                        configuration.logStreams.get(Verbosity.ERROR).getStreams())));
            }
        }
    }

    public void println(Supplier<String> message, Verbosity verbosity, boolean format) {
        if (configuration == null) {
            println(originalErrStreamCollection, message.get());
        } else {
            StreamCollection streamCollection = configuration.logStreams.get(verbosity);
            if (streamCollection != null) {
                if (format) {
                    println(streamCollection, formatMessage(message.get(), verbosity));
                } else {
                    println(streamCollection, message.get());
                }
            }
        }
    }

    public void print(Supplier<String> message, Verbosity verbosity, boolean format) {
        if (configuration == null) {
            print(originalErrStreamCollection, message.get());
        } else {
            StreamCollection streamCollection = configuration.logStreams.get(verbosity);
            if (streamCollection != null) {
                if (format) {
                    print(streamCollection, formatMessage(message.get(), verbosity));
                } else {
                    print(streamCollection, message.get());
                }
            }
        }
    }

    public void printProgress(Supplier<String> message) {
        if (configuration == null) {
            printProgress(originalOutStreamCollection, message.get());
        } else {
            StreamCollection streamCollection = configuration.logStreams.get(Verbosity.PROGRESS);
            if (streamCollection != null) {
                printProgress(streamCollection, formatMessage(message.get(), Verbosity.PROGRESS));
            }
        }
    }

    private void println(StreamCollection streamCollection, String message) {
        char[] charArray = message.toCharArray();
        synchronized (this) {
            for (PrintStream stream : streamCollection.getStreams()) {
                Integer progressSize =
                        configuration.progressCharactersPerTarget.get(configuration.streamTargets.get(stream));
                stream.println(fillBuffer(charArray, progressSize != null ? progressSize : 0));
                configuration.progressCharactersPerTarget.put(configuration.streamTargets.get(stream), 0);
            }
        }
    }

    private void print(StreamCollection streamCollection, String message) {
        char[] charArray = message.toCharArray();
        synchronized (this) {
            for (PrintStream stream : streamCollection.getStreams()) {
                Integer progressSize =
                        configuration.progressCharactersPerTarget.get(configuration.streamTargets.get(stream));
                stream.print(fillBuffer(charArray, progressSize != null ? progressSize : 0));
                configuration.progressCharactersPerTarget.put(configuration.streamTargets.get(stream), 0);
            }
        }
    }

    private void printProgress(StreamCollection streamCollection, String message) {
        char[] charArray = message.toCharArray();
        synchronized (this) {
            for (PrintStream stream : streamCollection.getStreams()) {
                Integer progressSize =
                        configuration.progressCharactersPerTarget.get(configuration.streamTargets.get(stream));
                stream.print(fillBuffer(charArray, progressSize != null ? progressSize : 0));
                configuration.progressCharactersPerTarget.put(
                        configuration.streamTargets.get(stream), charArray.length);
            }
        }
    }

    private char[] fillBuffer(char[] charArray, int progressSize) {
        if (progressSize == 0) {
            return charArray;
        } else {
            char[] buffer = new char[Math.max(progressSize, charArray.length) + 1];
            buffer[0] = '\r';
            System.arraycopy(charArray, 0, buffer, 1, charArray.length);
            Arrays.fill(buffer, charArray.length + 1, buffer.length, ' ');
            return buffer;
        }
    }

    public void println(Throwable error, Verbosity verbosity) {
        if (configuration == null) {
            println(originalErrStreamCollection, Log.getErrorMessage(error, true));
        } else {
            StreamCollection streamCollection = configuration.logStreams.get(verbosity);
            if (streamCollection != null) {
                println(
                        streamCollection,
                        formatMessage(Log.getErrorMessage(error, configuration.printStacktrace), verbosity));
            }
        }
    }

    private String formatMessage(String message, Verbosity verbosity) {
        if (configuration.formatters.isEmpty()) {
            return message != null ? message : "null";
        } else {
            final StringBuilder sb = new StringBuilder();
            final ListIterator<IFormatter> it = configuration.formatters.listIterator();
            while (it.hasNext()) {
                sb.append(it.next().getPrefix(message, verbosity));
            }
            sb.append(message != null ? message : "null");
            while (it.hasPrevious()) {
                sb.append(it.previous().getSuffix(message, verbosity));
            }
            return sb.toString();
        }
    }
}
