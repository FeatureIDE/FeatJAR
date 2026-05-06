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
import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An option for an {@link ICommand}. Parses a string value into an object.
 * Allows to set a default value.
 *
 * @param <T> the type of the option's value
 * @author Elias Kuiter
 */
public class Option<T> {

    /**
     * Default parser for boolean values.
     */
    public static final Function<String, Boolean> BooleanParser = s -> {
        switch (s) {
            case "true":
                return Boolean.TRUE;
            case "false":
                return Boolean.FALSE;
            default:
                throw new RuntimeException(String.format("%s is not a boolean", s));
        }
    };

    /**
     * Default parser for integer values.
     */
    public static final Function<String, Integer> IntegerParser = Integer::parseInt;

    /**
     * Default parser for double values.
     */
    public static final Function<String, Double> DoubleParser = Double::parseDouble;

    /**
     * Default parser for long values.
     */
    public static final Function<String, Long> LongParser = Long::parseLong;

    /**
     * Default parser for string values. Returns the same string instance.
     */
    public static final Function<String, String> StringParser = s -> s;

    /**
     * Default parser for file paths.
     */
    public static final Function<String, Path> PathParser = Path::of;

    /**
     * Default path validator. Checks if the specified file or directory exist.
     */
    public static final Predicate<Path> PathValidator = Files::exists;

    private static List<Pair<Class<?>, Option<?>>> list = new ArrayList<>();

    /**
     * Registers a new {@link Option} for the calling class.
     *
     * @param <U> the type of the option
     * @param name the name of the option
     * @param parser the parser for parsing a string to the type of the option
     * @param defaultValue the default value
     *
     * @return the newly created option
     */
    public static <U> Option<U> newOption(String name, Function<String, U> parser, U defaultValue) {
        Option<U> option = new Option<>(name, parser, defaultValue);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    /**
     * Registers a new {@link Option} for the calling class.
     *
     * @param <U> the type of the option
     * @param name the name of the option
     * @param parser the parser for parsing a string to the type of the option
     *
     * @return the newly created option
     */
    public static <U> Option<U> newOption(String name, Function<String, U> parser) {
        Option<U> option = new Option<>(name, parser);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    /**
     * Registers a new {@link ListOption} for the calling class.
     *
     * @param <U> the type of the option
     * @param name the name of the option
     * @param parser the parser for parsing a string to the type of the option
     *
     * @return the newly created list option
     */
    public static <U> ListOption<U> newListOption(String name, Function<String, U> parser) {
        ListOption<U> option = new ListOption<>(name, parser);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
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
    public static <E extends Enum<E>> EnumListOption<E> newEnumListOption(String name, Class<E> enumClass) {
        EnumListOption<E> option = new EnumListOption<>(name, enumClass);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    /**
     * Registers a new {@link RangeOption} for the calling class.
     *
     * @param name the name of the option
     *
     * @return the newly created range option
     */
    public static RangeOption newRangeOption(String name) {
        RangeOption option = new RangeOption(name);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    /**
     * Registers a new {@link Flag} option for the calling class.
     *
     * @param name the name of the option
     *
     * @return the newly created flag option
     */
    public static Flag newFlag(String name) {
        Flag option = new Flag(name);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
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
    public static <E extends Enum<E>> EnumOption<E> newEnumOption(String name, Class<E> enumClass) {
        EnumOption<E> option = new EnumOption<>(name, enumClass);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    /**
     * Registers a new {@link StringEnumOption} for the calling class.
     *
     * @param name the name of the option
     * @param possibleValues the possible values for this option
     *
     * @return the newly created string enum option
     */
    public static StringEnumOption newStringEnumOption(String name, String... possibleValues) {
        StringEnumOption option = new StringEnumOption(name, Arrays.asList(possibleValues));
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    /**
     * Registers a new {@link StringEnumOption} for the calling class.
     *
     * @param name the name of the option
     * @param possibleValues the possible values for this option
     *
     * @return the newly created string enum option
     */
    public static StringEnumOption newStringEnumOption(String name, List<String> possibleValues) {
        StringEnumOption option = new StringEnumOption(name, possibleValues);
        list.add(new Pair<>(getCallingClass(), option));
        return option;
    }

    /**
     * {@return all options registered for a given class}
     *
     * @param clazz the class for which the options are registered
     */
    public static List<Option<?>> getAllOptions(Class<?> clazz) {
        return list.stream()
                .filter(e -> e.getKey().isAssignableFrom(clazz))
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    /**
     * Deregisters all {@link Option options} for all classes.
     */
    public static void deleteAllOptions() {
        list.clear();
        list = null;
    }

    private static Class<?> getCallingClass() {
        try {
            return Class.forName(Thread.currentThread().getStackTrace()[3].getClassName());
        } catch (ClassNotFoundException e) {
            FeatJAR.log().error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * The name of the option.
     */
    protected final String name;

    /**
     * A parser the parses a string into the type of the option.
     */
    protected final Function<String, T> parser;

    /**
     * A description supplier for the option. Can be used when the description
     * is complicated or only known after initialization.
     */
    protected Supplier<String> descriptionSupplier = () -> null;

    /**
     * The default value of the option.
     */
    protected T defaultValue;

    /**
     * A validator that check whether a given value is valid.
     */
    protected Predicate<T> validator = t -> true;

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     */
    protected Option(String name, Function<String, T> parser) {
        this.name = name;
        this.parser = parser;
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param defaultValue the default value in case no other is provided or can be parsed
     */
    protected Option(String name, Function<String, T> parser, T defaultValue) {
        this.name = name;
        this.parser = parser;
        this.defaultValue = defaultValue;
    }

    /**
     * {@return this option's name}
     */
    public String getName() {
        return name;
    }

    /**
     * {@return this option's argument name on the command-line interface}
     */
    public String getArgumentName() {
        return "--" + name;
    }

    /**
     * {@return this option's parser}
     */
    public Function<String, T> getParser() {
        return parser;
    }

    /**
     * {@return this option's description}
     */
    public Result<String> getDescription() {
        return Result.ofNullable(descriptionSupplier.get());
    }

    /**
     * Sets this option's description.
     *
     * @param description the description
     * @return this option
     */
    public Option<T> setDescription(String description) {
        return setDescription(() -> description);
    }

    /**
     * Sets this option's description supplier. Should be used when the description
     * is complicated or only known after initialization.
     *
     * @param descriptionSupplier the description supplier
     * @return this option
     */
    public Option<T> setDescription(Supplier<String> descriptionSupplier) {
        this.descriptionSupplier = descriptionSupplier;
        return this;
    }

    /**
     * {@return this option's default value}
     */
    public Result<T> getDefaultValue() {
        return Result.ofNullable(defaultValue);
    }

    /**
     * Sets this option's default value.
     *
     * @param defaultValue the default value
     * @return this option
     */
    public Option<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * {@return this option's validator}
     */
    public Predicate<T> getValidator() {
        return validator;
    }

    /**
     * Sets this option's validator.
     *
     * @param validator the validator
     * @return this option
     */
    public Option<T> setValidator(Predicate<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "%s <value>%s%s",
                getArgumentName(),
                getDescription().map(d -> ": " + d).orElse(""),
                getDefaultValue().map(s -> " (default: " + s + ")").orElse(""));
    }

    /**
     * Applies the option parser.
     *
     * @param s the string to be parsed
     * @return a {@link Result} containing the parsed value.
     */
    Result<T> parse(String s) {
        try {
            return Result.of(parser.apply(s));
        } catch (Exception e) {
            return Result.empty(e);
        }
    }
}
