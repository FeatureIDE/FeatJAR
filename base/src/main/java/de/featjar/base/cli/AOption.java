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

import de.featjar.base.data.Result;
import de.featjar.base.env.IParameter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An option for an {@link ICommand}. Parses a string value into an object.
 * Allows to set a default value.
 *
 * @param <T> the type of the option's value
 * @author Elias Kuiter
 */
public abstract class AOption<T> implements IParameter {

    /**
     * The name of the option.
     */
    protected final String name;

    /**
     * A description supplier for the option. Can be used when the description
     * is complicated or only known after initialization.
     */
    protected Supplier<String> descriptionSupplier = () -> null;

    /**
     * The default value of the option.
     */
    protected String defaultArgument;

    protected LinkedHashSet<String> possibleValues;

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     */
    protected AOption(String name) {
        super();
        this.name = name;
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param defaultValue the default value in case no other is provided or can be parsed
     */
    protected AOption(String name, String defaultValue) {
        super();
        this.name = name;
        this.defaultArgument = defaultValue;
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
    protected abstract Function<String, T> getParser();

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
    public AOption<T> setDescription(String description) {
        return setDescription(() -> description);
    }

    /**
     * Sets this option's description supplier. Should be used when the description
     * is complicated or only known after initialization.
     *
     * @param descriptionSupplier the description supplier
     * @return this option
     */
    public AOption<T> setDescription(Supplier<String> descriptionSupplier) {
        this.descriptionSupplier = descriptionSupplier;
        return this;
    }

    /**
     * {@return this option's default value}
     */
    public Result<String> getDefaultArgument() {
        return Result.ofNullable(defaultArgument);
    }

    /**
     * {@return this option's default value}
     */
    public Result<T> getDefaultValue() {
        return getDefaultArgument().map(getParser());
    }

    /**
     * Sets this option's default value.
     *
     * @param defaultValue the default value
     * @return this option
     */
    public AOption<T> setDefaultValue(String defaultValue) {
        this.defaultArgument = defaultValue;
        return this;
    }

    public Optional<Set<String>> getPossibleValues() {
        return Optional.ofNullable(possibleValues).map(Collections::unmodifiableSet);
    }

    public AOption<T> setPossibleValues(Collection<String> possibleValues) {
        this.possibleValues = possibleValues == null
                ? null
                : possibleValues.stream()
                        .map(s -> s.toUpperCase(Locale.ENGLISH))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        return this;
    }

    public boolean validateArgument(String argument) {
        return getPossibleValues()
                .map(v -> v.contains(argument.toUpperCase(Locale.ENGLISH)))
                .orElse(Boolean.TRUE);
    }

    public boolean validateValue(T value) {
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "%s <%s>%s%s%s",
                getArgumentName(),
                getArgumentPlaceHolder(),
                getDescription().map(d -> ": " + d).orElse(""),
                getPossibleValues()
                        .map(list -> " (possible values: " + list.stream().collect(Collectors.joining(",")) + ")")
                        .orElse(""),
                getDefaultValue().map(v -> " (default: " + v + ")").orElse(""));
    }

    protected String getArgumentPlaceHolder() {
        return "value";
    }

    /**
     * Applies the option parser.
     *
     * @param s the string to be parsed
     * @return a {@link Result} containing the parsed value.
     */
    Result<T> parse(String s) {
        try {
            return Result.of(getParser().apply(s));
        } catch (Exception e) {
            return Result.empty(e);
        }
    }
}
