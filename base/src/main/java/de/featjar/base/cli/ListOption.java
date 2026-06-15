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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An option for an {@link ICommand}. Parses a string value into an object.
 * Allows to set a default value.
 *
 * @param <T> the type of the option's value
 * @author Elias Kuiter
 */
public class ListOption<T> extends AOption<List<T>> {

    /**
     * A parser that parses a string into the type of the option.
     */
    protected final Function<String, T> parser;

    /**
     * A validator that check whether a given value is valid.
     */
    protected Predicate<T> validator;

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     */
    protected ListOption(String name, Function<String, T> parser) {
        this(name, parser, null, null, null);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param validator the validator for the option's value
     */
    protected ListOption(String name, Function<String, T> parser, Predicate<T> validator) {
        this(name, parser, validator, null, null);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param possibleValues the possibleValues for the option
     */
    protected ListOption(String name, Function<String, T> parser, Collection<String> possibleValues) {
        this(name, parser, null, possibleValues, null);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param defaultValue the default value in case no other is provided or can be parsed
     */
    protected ListOption(String name, Function<String, T> parser, String defaultValue) {
        this(name, parser, null, null, defaultValue);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param possibleValues the possibleValues for the option
     * @param defaultValue the default value in case no other is provided or can be parsed
     */
    protected ListOption(
            String name, Function<String, T> parser, Collection<String> possibleValues, String defaultValue) {
        this(name, parser, null, possibleValues, defaultValue);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param validator the validator for the option's value
     * @param defaultValue the default value in case no other is provided or can be parsed
     */
    protected ListOption(String name, Function<String, T> parser, Predicate<T> validator, String defaultValue) {
        this(name, parser, validator, null, defaultValue);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param validator the validator for the option's value
     * @param possibleValues the possibleValues for the option
     * @param defaultValue the default value in case no other is provided or can be parsed
     */
    protected ListOption(
            String name,
            Function<String, T> parser,
            Predicate<T> validator,
            Collection<String> possibleValues,
            String defaultValue) {
        super(name, defaultValue);
        this.parser = Objects.requireNonNull(parser);
        this.validator = validator == null ? t -> true : validator;
        setPossibleValues(possibleValues);
    }

    /**
     * {@return this option's parser}
     */
    public Function<String, List<T>> getParser() {
        return arg -> Arrays.stream(arg.split("[,\n]")).map(parser).toList();
    }

    @Override
    public boolean validateArgument(String argument) {
        String[] splitArguments = argument.split("[,\n]");
        return getPossibleValues()
                .map(possibleValues -> Arrays.stream(splitArguments)
                        .map(s -> s.toUpperCase(Locale.ENGLISH))
                        .allMatch(possibleValues::contains))
                .orElse(Boolean.TRUE);
    }

    @Override
    public boolean validateValue(List<T> values) {
        return values.stream().allMatch(validator::test);
    }

    public ListOption<T> setValidator(Predicate<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public ListOption<T> setDefaultValue(String defaultValue) {
        return (ListOption<T>) super.setDefaultValue(defaultValue);
    }

    @Override
    public ListOption<T> setPossibleValues(Collection<String> possibleValues) {
        return (ListOption<T>) super.setPossibleValues(possibleValues);
    }

    @Override
    public ListOption<T> setDescription(Supplier<String> descriptionSupplier) {
        return (ListOption<T>) super.setDescription(descriptionSupplier);
    }

    @Override
    public ListOption<T> setDescription(String description) {
        return (ListOption<T>) super.setDescription(description);
    }

    @Override
    protected String getArgumentPlaceHolder() {
        return "<value1,value2,...>";
    }
}
