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
package de.featjar.base;

import de.featjar.base.cli.Commands;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.Cache;
import de.featjar.base.computation.FallbackCache;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.extension.AExtensionPoint;
import de.featjar.base.extension.ExtensionManager;
import de.featjar.base.extension.IExtension;
import de.featjar.base.io.IO;
import de.featjar.base.log.BufferedLog;
import de.featjar.base.log.CallerFormatter;
import de.featjar.base.log.ColorFormatter;
import de.featjar.base.log.ConfigurableLog;
import de.featjar.base.log.EmptyProgressBar;
import de.featjar.base.log.IProgressBar;
import de.featjar.base.log.Log;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.base.log.ProgressThread;
import de.featjar.base.log.TimeStampFormatter;
import de.featjar.base.log.VerbosityFormatter;
import de.featjar.base.shell.Shell;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Configures, initializes, and runs FeatJAR. To use FeatJAR, create a
 * {@link FeatJAR} object and use it. After usage, call {@link #close()} or use
 * a try...with block. If only a quick computation is needed, call
 * {@link #run(Consumer)} or {@link #apply(Function)}. For convenience, this
 * class inherits all methods in {@link IO} and provides access to the
 * {@link Log} with {@link #log()} and {@link Cache} with {@link #cache()}. Both
 * {@link #log()} and {@link #cache()} return fallback instances when used
 * outside a FeatJAR instantiation. For simplicity, only one FeatJAR instance
 * can exist at a time (although this limitation may be lifted in the future).
 * Thus, do not create FeatJAR objects at the same time in different threads.
 * Also, do not nest {@link #run(Consumer)} or {@link #apply(Function)} calls.
 * However, different FeatJAR instances can be created over time in the same
 * thread (e.g., to change the configuration).
 *
 * @author Elias Kuiter
 */
public final class FeatJAR extends IO implements AutoCloseable {
    /**
     * Name of the root package.
     */
    public static final String ROOT_PACKAGE_NAME = "de.featjar";
    /**
     * Name of the library.
     */
    public static final String LIBRARY_NAME = "feat.jar";

    /**
     * Error code for internal problems during computation.
     */
    public static final int ERROR_COMPUTING_RESULT = 1;
    /**
     * Error code for timeout during computation.
     */
    public static final int ERROR_TIMEOUT = 1;
    /**
     * Error code for problems during output of the computed result.
     */
    public static final int ERROR_WRITING_RESULT = 1;

    /**
     * Configures FeatJAR.
     */
    public static class Configuration {

        /**
         * This configuration's log sub-configuration.
         */
        public final ConfigurableLog.Configuration logConfig = new ConfigurableLog.Configuration();

        /**
         * This configuration's cache sub-configuration.
         */
        public final Cache.Configuration cacheConfig = new Cache.Configuration();

        /**
         * {@code true} if a separate thread is used for progress monitoring.
         */
        public boolean useProgressThread = false;

        /**
         * Configures this configuration's log sub-configuration.
         *
         * @param configurationConsumer the log configuration consumer
         * @return this configuration
         */
        public Configuration log(Consumer<ConfigurableLog.Configuration> configurationConsumer) {
            configurationConsumer.accept(logConfig);
            return this;
        }

        /**
         * Configures this configuration's cache sub-configuration.
         *
         * @param configurationConsumer the cache configuration consumer
         * @return this configuration
         */
        public Configuration cache(Consumer<Cache.Configuration> configurationConsumer) {
            configurationConsumer.accept(cacheConfig);
            return this;
        }

        /**
         * Initializes FeatJAR with this configuration.
         */
        public void initialize() {
            FeatJAR.initialize(this);
        }
    }

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

    /**
     * Option for printing version information.
     */
    public static final Option<Boolean> STACKTRACE_OPTION =
            Option.newFlag("print-stacktrace").setDescription("Print a stacktrace for all logged exceptions");

    /**
     * Option for writing less output to the console.
     */
    public static final Option<Boolean> QUIET_OPTION = Option.newFlag("quiet")
            .setDescription("Suppress all unnecessary output. (Overwrites --log-info and --log-error options)");

    /**
     * Option for writing progress regularly to the console.
     */
    public static final Option<Boolean> PROGRESS_OPTION =
            Option.newFlag("progress").setDescription("Shows progress regularly.");

    /**
     * Option to specify a path to a log file for non-error messages.
     */
    public static final Option<Path> INFO_FILE_OPTION =
            Option.newOption("info-file", Option.PathParser).setDescription("Path to info log file");

    /**
     * Option to specify a path to a log file for error messages.
     */
    public static final Option<Path> ERROR_FILE_OPTION =
            Option.newOption("error-file", Option.PathParser).setDescription("Path to error log file");

    /**
     * Option to configure which logging types count as non-error messages.
     */
    public static final Option<List<Log.Verbosity>> LOG_INFO_OPTION = Option.newEnumListOption(
                    "log-info", Log.Verbosity.class)
            .setDescription("Message types printed to the info stream")
            .setDefaultValue(List.of(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.PROGRESS));
    /**
     * Option to configure which logging types count as error messages.
     */
    public static final Option<List<Log.Verbosity>> LOG_ERROR_OPTION = Option.newEnumListOption(
                    "log-error", Log.Verbosity.class)
            .setDescription("Message types printed to the error stream.")
            .setDefaultValue(List.of(Log.Verbosity.WARNING, Log.Verbosity.ERROR));

    /**
     * Option to configure which logging types are written to the non-error log file (if one exists).
     */
    public static final Option<List<Log.Verbosity>> LOG_INFO_FILE_OPTION = Option.newEnumListOption(
                    "log-info-file", Log.Verbosity.class)
            .setDescription("Message types printed to the info file.")
            .setDefaultValue(List.of(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.DEBUG));

    /**
     * Option to configure which logging types are written to the error log file (if one exists).
     */
    public static final Option<List<Log.Verbosity>> LOG_ERROR_FILE_OPTION = Option.newEnumListOption(
                    "log-error-file", Log.Verbosity.class)
            .setDescription("Message types printed to the error file.")
            .setDefaultValue(List.of(Log.Verbosity.ERROR, Log.Verbosity.WARNING));

    /**
     * The current instance of FeatJAR. Only one instance can exist at a time.
     */
    private static FeatJAR instance;

    private static BufferedLog fallbackLog = new BufferedLog();
    private static FallbackCache fallbackCache = new FallbackCache();

    /**
     * Main entry point of FeatJAR.
     *
     * @param arguments command-line arguments
     */
    public static void main(String[] arguments) {
        if (arguments.length == 0) {
            System.exit(Shell.getInstance().run());
        } else {
            System.exit(run(arguments));
        }
    }

    /**
     * {@return a new empty FeatJAR configuration}
     */
    public static Configuration configure() {
        return new Configuration();
    }

    /**
     * {@return a new FeatJAR configuration with default values}
     */
    public static Configuration defaultConfiguration() {
        final Configuration configuration = new Configuration();
        configuration
                .logConfig
                .logToSystemOut(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.PROGRESS)
                .logToSystemErr(Log.Verbosity.ERROR)
                .addFormatter(new TimeStampFormatter())
                .addFormatter(new VerbosityFormatter());
        configuration.cacheConfig.setCachePolicy(Cache.CachePolicy.CACHE_NONE);
        return configuration;
    }

    /**
     * {@return a new FeatJAR configuration with values intended for logging problems in undefined state}
     */
    public static Configuration panicConfiguration() {
        final Configuration configuration = new Configuration();
        configuration
                .logConfig
                .logToSystemOut(Log.Verbosity.MESSAGE)
                .logToSystemErr(Log.Verbosity.ERROR, Log.Verbosity.WARNING);
        configuration.cacheConfig.setCachePolicy(Cache.CachePolicy.CACHE_NONE);
        return configuration;
    }

    /**
     * {@return a new FeatJAR configuration with values intended for test settings}
     */
    public static Configuration testConfiguration() {
        final Configuration configuration = new Configuration();
        configuration
                .logConfig
                .setPrintStacktrace(true)
                .logToSystemOut(Log.Verbosity.INFO, Log.Verbosity.DEBUG)
                .logToSystemErr(Log.Verbosity.ERROR, Log.Verbosity.WARNING)
                .addFormatter(new TimeStampFormatter())
                .addFormatter(new VerbosityFormatter())
                .addFormatter(new CallerFormatter());
        configuration.cacheConfig.setCachePolicy(Cache.CachePolicy.CACHE_NONE);
        return configuration;
    }

    /**
     * {@return a new FeatJAR configuration with values intended for shell settings}
     */
    public static Configuration shellConfiguration() {
        final Configuration configuration = new Configuration();
        configuration
                .logConfig
                .logToSystemOut(Log.Verbosity.MESSAGE, Log.Verbosity.INFO, Log.Verbosity.PROGRESS)
                .logToSystemErr(Log.Verbosity.ERROR, Log.Verbosity.WARNING)
                .addFormatter(new ColorFormatter());
        configuration.cacheConfig.setCachePolicy(Cache.CachePolicy.CACHE_NONE);
        return configuration;
    }

    /**
     * {@return the current FeatJAR instance}
     *
     * @throws IllegalStateException if FeatJAR is not initialized.
     */
    public static FeatJAR getInstance() {
        if (instance == null) throw new IllegalStateException("FeatJAR not initialized yet");
        return instance;
    }

    /**
     * {@return true if FeatJAR is initialized, false otherwise}
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Initializes FeatJAR with a default configuration.
     *
     * @return the new FeatJAR instance
     */
    public static FeatJAR initialize() {
        return initialize(defaultConfiguration());
    }

    /**
     * Initializes FeatJAR.
     *
     * @param configuration the FeatJAR configuration
     * @return the new FeatJAR instance
     */
    public static synchronized FeatJAR initialize(Configuration configuration) {
        if (instance != null) {
            throw new RuntimeException("FeatJAR already initialized");
        }
        log().debug("initializing FeatJAR");
        instance = new FeatJAR();
        instance.extensionManager = new ExtensionManager();
        instance.extensionManager.install();
        if (configuration != null) {
            instance.setConfiguration(configuration);
        }
        return instance;
    }

    /**
     * De-initializes FeatJAR.
     */
    public static synchronized void deinitialize() {
        if (instance != null) {
            FeatJAR.log().debug("de-initializing FeatJAR");

            if (instance.extensionManager != null) {
                instance.extensionManager.close();
            }

            if (instance.cache != null) {
                instance.cache.close();
            }
            instance.cache = null;

            if (instance.progressThread != null) {
                instance.progressThread.close();
            }
            instance.progressThread = null;

            instance.log = null;
            instance = null;
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param configuration the FeatJAR configuration
     * @param fn            the function
     */
    public static void run(Configuration configuration, Consumer<FeatJAR> fn) {
        try (FeatJAR featJAR = FeatJAR.initialize(configuration)) {
            fn.accept(featJAR);
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param fn the function
     */
    public static void run(Consumer<FeatJAR> fn) {
        try (FeatJAR featJAR = FeatJAR.initialize()) {
            fn.accept(featJAR);
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param configuration the FeatJAR configuration
     * @param fn            the function
     * @return the supplied object
     */
    public static <T> T apply(Configuration configuration, Function<FeatJAR, T> fn) {
        try (FeatJAR featJAR = FeatJAR.initialize(configuration)) {
            return fn.apply(featJAR);
        }
    }

    /**
     * Runs some function in a temporary FeatJAR instance.
     *
     * @param fn the function
     * @return the supplied object
     */
    public static <T> T apply(Function<FeatJAR, T> fn) {
        try (FeatJAR featJAR = FeatJAR.initialize()) {
            return fn.apply(featJAR);
        }
    }

    /**
     * Interpret arguments and run the specified command.
     *
     * @param arguments command-line arguments
     *
     * @return the exit code. 0 for a successful run, a number greater than 0 otherwise
     */
    public static int run(String... arguments) {
        try (FeatJAR featJAR = FeatJAR.initialize(null)) {
            return featJAR.runAfterInitialization(true, arguments);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return panic();
        }
    }

    /**
     * Interpret arguments and run with the test configuration.
     *
     * @param arguments command-line arguments
     *
     * @return the exit code. 0 for a successful run, a number greater than 0 otherwise
     *
     * @see #testConfiguration()
     */
    public static int runTest(String... arguments) {
        try (FeatJAR featJAR = FeatJAR.initialize(testConfiguration())) {
            return featJAR.runAfterInitialization(false, arguments);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return panic();
        }
    }

    /**
     * Interpret arguments and run the specified command. Skips the initialization. Assumes that a FeatJAR
     *
     * @param arguments command-line arguments
     *
     * @return the exit code. 0 for a successful run, a number greater than 0 otherwise
     */
    public static int runInternally(String... arguments) {
        return getInstance().runAfterInitialization(false, arguments);
    }

    private int runAfterInitialization(boolean configure, String... arguments) {
        OptionList optionInput = new OptionList(Option.getAllOptions(FeatJAR.class), arguments);

        List<Problem> problems = optionInput.parseArguments();

        if (configure) {
            setConfiguration(optionInput.getConfiguration());
        }

        if (Problem.containsError(problems)) {
            FeatJAR.log().problems(problems);
            FeatJAR.log()
                    .plainMessage(OptionList.printHelp(optionInput.getCommand().orElse(null)));
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }

        if (optionInput.isHelp()) {
            FeatJAR.log().plainMessage("This is FeatJAR!");
            FeatJAR.log()
                    .plainMessage(OptionList.printHelp(optionInput.getCommand().orElse(null)));
        } else if (optionInput.isVersion()) {
            FeatJAR.log().plainMessage(FeatJAR.LIBRARY_NAME + ", development version");
        } else {
            Result<ICommand> optionalCommand = optionInput.getCommand();
            FeatJAR.log().problems(problems);
            if (optionalCommand.isEmpty()) {
                FeatJAR.log().error("No command provided");
                FeatJAR.log().plainMessage(OptionList.printAvailableCommands());
                return FeatJAR.ERROR_COMPUTING_RESULT;
            } else {
                ICommand command = optionalCommand.get();
                FeatJAR.log().debug("Running command %s", command.getIdentifier());
                return command.run(optionInput);
            }
        }
        return 0;
    }

    private static int panic() {
        Log log = FeatJAR.log();
        if (log instanceof BufferedLog) {
            ConfigurableLog newLog = new ConfigurableLog();
            newLog.setConfiguration(panicConfiguration().logConfig);
            BufferedLog bufferedLog = (BufferedLog) log;
            bufferedLog.setPrintStacktrace(true);
            bufferedLog.flush(m -> {
                Supplier<String> originalMessage = m.getMessage();
                Supplier<String> message;
                Verbosity verbosity = m.getVerbosity();
                switch (verbosity) {
                    case DEBUG:
                        message = () -> "DEBUG: " + originalMessage.get();
                        break;
                    case ERROR:
                        message = () -> "ERROR: " + originalMessage.get();
                        break;
                    case INFO:
                        message = () -> "INFO: " + originalMessage.get();
                        break;
                    case MESSAGE:
                        message = m.getMessage();
                        break;
                    case PROGRESS:
                        message = () -> "PROGRESS: " + originalMessage.get();
                        break;
                    case WARNING:
                        message = () -> "WARNING: " + originalMessage.get();
                        break;
                    default:
                        throw new IllegalStateException(String.valueOf(verbosity));
                }
                newLog.print(message, verbosity, m.isFormat());
            });
        }
        return FeatJAR.ERROR_COMPUTING_RESULT;
    }

    /**
     * {@return the extension point for a given class installed in the current
     * FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public static <T extends AExtensionPoint<?>> T extensionPoint(Class<T> klass) {
        Result<T> extensionPoint = getInstance().getExtensionPoint(klass);
        if (extensionPoint.isEmpty())
            throw new RuntimeException("extension point " + klass + " not currently installed in FeatJAR");
        return extensionPoint.get();
    }

    /**
     * {@return the extension point for a given class installed in the current
     * FeatJAR instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public static <T extends IExtension> T extension(Class<T> klass) {
        Result<T> extension = getInstance().getExtension(klass);
        if (extension.isEmpty())
            throw new RuntimeException("extension " + klass + " not currently installed in FeatJAR");
        return extension.get();
    }

    /**
     * {@return the current FeatJAR instance's log, or a fallback log if
     * uninitialized}
     */
    public static Log log() {
        FeatJAR instance = FeatJAR.instance;
        return instance == null || instance.log == null ? fallbackLog : instance.log;
    }

    /**
     * {@return the current FeatJAR instance's cache, or a fallback cache if
     * uninitialized}
     */
    public static Cache cache() {
        FeatJAR instance = FeatJAR.instance;
        return instance == null || instance.cache == null ? fallbackCache : instance.cache;
    }

    /**
     * {@return the current FeatJAR instance's progress bar, or an empty progress bar if
     * uninitialized or --progress option was not provided}
     */
    public static IProgressBar progress() {
        FeatJAR instance = FeatJAR.instance;
        return instance == null || instance.progressThread == null ? new EmptyProgressBar() : instance.progressThread;
    }

    /**
     * This FeatJAR instance's extension manager. Holds references to all loaded
     * extension points and extensions.
     */
    private ExtensionManager extensionManager;

    private ConfigurableLog log;
    private Cache cache;
    private ProgressThread progressThread;

    private void setConfiguration(Configuration configuration) {
        ConfigurableLog newLog = getExtension(ConfigurableLog.class).orElseGet(ConfigurableLog::new);
        newLog.setConfiguration(configuration.logConfig);
        log = newLog;
        fallbackLog.setPrintStacktrace(configuration.logConfig.isPrintStacktrace());
        fallbackLog.flush(m -> log.print(m.getMessage(), m.getVerbosity(), m.isFormat()));

        cache = getExtension(Cache.class).orElseGet(Cache::new);
        cache.setConfiguration(configuration.cacheConfig);

        progressThread = configuration.useProgressThread ? new ProgressThread(1000) : null;
    }

    /**
     * De-initializes FeatJAR.
     */
    @Override
    public void close() {
        deinitialize();
    }

    /**
     * {@return this FeatJAR instance's extension manager}
     */
    public ExtensionManager getExtensionManager() {
        return extensionManager;
    }

    /**
     * {@return the extension point for a given class installed in this FeatJAR
     * instance's extension manager}
     *
     * @param <T>   the type of the extension point's class
     * @param klass the extension point's class
     */
    public <T extends AExtensionPoint<?>> Result<T> getExtensionPoint(Class<T> klass) {
        return extensionManager != null
                ? extensionManager.getExtensionPoint(klass)
                : Result.empty(new IllegalStateException("FeatJAR not initialized yet"));
    }

    /**
     * {@return the extension for a given class installed in this FeatJAR instance's
     * extension manager}
     *
     * @param <T>   the type of the extension's class
     * @param klass the extension's class
     */
    public <T extends IExtension> Result<T> getExtension(Class<T> klass) {
        return extensionManager != null ? extensionManager.getExtension(klass) : Result.empty();
    }
}
