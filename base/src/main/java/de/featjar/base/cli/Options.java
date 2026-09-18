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
import de.featjar.base.data.Result;
import de.featjar.base.env.ParameterList;
import de.featjar.base.io.format.AFormats;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.IFormatSupplier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An option for an {@link ICommand}. Parses a string value into an object.
 * Allows to set a default value.
 *
 * @param <T> the type of the option's value
 * @author Elias Kuiter
 */
public final class Options<T> {

    private Options() {}

    public static String joinToString(Object... elements) {
        return Arrays.stream(elements).map(String::valueOf).collect(Collectors.joining(","));
    }

    private static ParameterList<Option<?>> optionList = new ParameterList<>(IHasOptions.class);

    public static <R> Function<String, Result<R>> parser(Function<String, R> parser) {
        return arg -> {
            try {
                return Result.of(parser.apply(arg));
            } catch (Exception e) {
                return Result.empty(e);
            }
        };
    }

    /**
     * Default parser for boolean values.
     */
    public static final Function<String, Result<Boolean>> BooleanParser = parser(arg -> switch (arg.toLowerCase()) {
        case "true" -> Boolean.TRUE;
        case "false" -> Boolean.FALSE;
        default -> throw new IllegalArgumentException("Invalid argument: " + arg);
    });

    /**
     * Default parser for integer values.
     */
    public static final Function<String, Result<Integer>> IntegerParser = parser(Integer::parseInt);

    /**
     * Default parser for double values.
     */
    public static final Function<String, Result<Double>> DoubleParser = parser(Double::parseDouble);

    /**
     * Default parser for long values.
     */
    public static final Function<String, Result<Long>> LongParser = parser(Long::parseLong);

    /**
     * Default parser for string values. Returns the same string instance.
     */
    public static final Function<String, Result<String>> StringParser = arg -> Result.of(arg);

    /**
     * Default parser for any file paths.
     */
    public static final Function<String, Result<Path>> PathParser = parser(Path::of);

    /**
     * Default parser for existing file paths.
     */
    public static final Function<String, Result<Path>> ExistingPathParser = parser(arg -> {
        Path path = Path.of(arg);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Invalid argument: " + arg + ". Path does not exist.");
        }
        return path;
    });

    /**
     * Default parser for timeout values.
     */
    public static final Function<String, Result<Duration>> TimeoutParser = parser(arg -> {
        long seconds = Long.parseLong(arg);
        if (seconds < 0) {
            throw new IllegalArgumentException("Timeout value must not be negative.");
        }
        return Duration.ofSeconds(seconds);
    });

    static {
        FeatJAROptions.init();
        LogOptions.init();
    }

    /**
     * {@return all options registered for a given class}
     *
     * @param introducingClass the class for which the options are registered
     */
    public static List<Option<?>> getAllOptions(Class<?> introducingClass) {
        return new ArrayList<>(optionList.getParameterList(introducingClass));
    }

    /**
     * Registers a new {@link Options} for the calling class.
     *
     * @param <U> the type of the option
     * @param name the name of the option
     * @param parser the parser for parsing a string to the type of the option
     * @param defaultValue the default value
     *
     * @return the newly created option
     */
    public static <U> SingleOption<U> newOption(String name, Function<String, Result<U>> parser, String defaultValue) {
        return addOption(new SingleOption<>(name, parser, defaultValue));
    }

    /**
     * Registers a new {@link Options} for the calling class.
     *
     * @param <U> the type of the option
     * @param name the name of the option
     * @param parser the parser for parsing a string to the type of the option
     *
     * @return the newly created option
     */
    public static <U> SingleOption<U> newOption(String name, Function<String, Result<U>> parser) {
        return addOption(new SingleOption<>(name, parser));
    }

    /**
     * Registers a new {@link MultiOption} for the calling class.
     *
     * @param <U> the type of the option
     * @param name the name of the option
     * @param parser the parser for parsing a string to the type of the option
     *
     * @return the newly created list option
     */
    public static <U> MultiOption<U> newListOption(String name, Function<String, Result<U>> parser) {
        return addOption(new MultiOption<>(name, parser));
    }

    /**
     * Registers a new {@link EnumListOption} for the calling class.
     *
     * @param <E> the type of the option
     * @param name the name of the option
     * @param enumClass the enum class
     *
     * @return the newly created enum list option
     */
    public static <E extends Enum<E>> MultiOption<E> newEnumListOption(String name, Class<E> enumClass) {
        return addOption(new MultiOption<>(
                name,
                s -> Result.of(Enum.valueOf(enumClass, s.toUpperCase(Locale.ENGLISH))),
                Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toList(),
                ""));
    }

    /**
     * Registers a new {@link RangeOption} for the calling class.
     *
     * @param name the name of the option
     *
     * @return the newly created range option
     */
    public static RangeOption newRangeOption(String name) {
        return addOption(new RangeOption(name));
    }

    /**
     * Registers a new {@link Flag} option for the calling class.
     *
     * @param name the name of the option
     *
     * @return the newly created flag option
     */
    public static Flag newFlag(String name) {
        return addOption(new Flag(name));
    }

    /**
     * Registers a new {@link EnumOption} for the calling class.
     *
     * @param <E> the type of the option
     * @param name the name of the option
     * @param enumClass the enum class
     *
     * @return the newly created enum option
     */
    public static <E extends Enum<E>> SingleOption<E> newEnumOption(String name, Class<E> enumClass) {
        return addOption(new SingleOption<>(
                name,
                parser(s -> Enum.valueOf(enumClass, s.toUpperCase(Locale.ENGLISH))),
                Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toList()));
    }

    /**
     * Registers a new {@link StringEnumOption} for the calling class.
     *
     * @param name the name of the option
     * @param possibleValues the possible values for this option
     *
     * @return the newly created string enum option
     */
    public static SingleOption<String> newStringEnumOption(String name, String... possibleValues) {
        return addOption(new SingleOption<>(name, StringParser, Arrays.asList(possibleValues)));
    }

    /**
     * Registers a new {@link StringEnumOption} for the calling class.
     *
     * @param name the name of the option
     * @param possibleValues the possible values for this option
     *
     * @return the newly created string enum option
     */
    public static SingleOption<String> newStringEnumOption(String name, List<String> possibleValues) {
        return addOption(new SingleOption<>(name, StringParser, possibleValues));
    }

    /**
     * Registers a new {@link ChoiceOption} for the calling class.
     *
     * @param name the name of the option
     * @param possibleValues the possible values for this option
     *
     * @return the newly created option
     */
    public static <U> SingleOption<U> newChoiceOption(
            String name, Function<String, Result<U>> parser, String... possibleValues) {
        return addOption(new SingleOption<>(name, parser, Arrays.asList(possibleValues)));
    }

    /**
     * Registers a new {@link ChoiceOption} for the calling class.
     *
     * @param name the name of the option
     * @param possibleValues the possible values for this option
     *
     * @return the newly created option
     */
    public static <U> SingleOption<U> newChoiceOption(
            String name, Function<String, Result<U>> parser, List<String> possibleValues) {
        return addOption(new SingleOption<>(name, parser, possibleValues));
    }

    public static <T> SingleOption<IFormatSupplier<T>> newInputFormatOption(Class<? extends AFormats<T>> formatsClass) {
        List<IFormat<T>> formatList = FeatJAR.extensionPoint(formatsClass).getFormatList();
        ArrayList<String> possibleValues = new ArrayList<>(formatList.size() + 1);
        possibleValues.add("auto");
        formatList.stream().filter(IFormat::supportsParse).map(IFormat::getName).forEach(possibleValues::add);
        return addOption(new SingleOption<>(
                        "input-format",
                        name -> "auto".equals(name)
                                ? Result.of(FeatJAR.extensionPoint(formatsClass))
                                : Result.ofOptional(FeatJAR.extensionPoint(formatsClass)
                                                .getFormatByName(name))
                                        .map(IFormatSupplier::of),
                        possibleValues,
                        "auto")
                .setDescription("Format of the input. If not specified, tries to auto detect."));
    }

    public static <T> SingleOption<IFormat<T>> newOutputFormatOption(
            Class<? extends AFormats<T>> formatsClass, String defaultFormat) {
        return addOption(new SingleOption<>(
                        "output-format",
                        name -> Result.ofOptional(
                                FeatJAR.extensionPoint(formatsClass).getFormatByName(name)),
                        FeatJAR.extensionPoint(formatsClass).getFormatList().stream()
                                .filter(IFormat::supportsWrite)
                                .map(IFormat::getName)
                                .collect(Collectors.toList()),
                        defaultFormat)
                .setDescription("Format of the output"));
    }

    private static <T extends Option<?>> T addOption(T option) {
        optionList.addParameter(option);
        return option;
    }
}
