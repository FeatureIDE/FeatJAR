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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Base class for an option.
 *
 * @param <T> the type of the option's value
 * @author Sebastian Krieter
 * @author Elias Kuiter
 *
 * @see SingleOption
 * @see MultiOption
 */
public abstract class AOption<T> implements Option<T> {

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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getArgumentName() {
        return "--" + name;
    }

    @Override
    public Result<T> parse(String argument) {
        try {
            T value = getParser().apply(argument);
            return getValidator().test(value)
                    ? Result.of(value)
                    : Result.empty(new IllegalArgumentException("Invalid argument " + argument));
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    /**
     * {@return this option's parser}
     */
    protected abstract Function<String, T> getParser();

    /**
     * {@return this option's validator}
     */
    protected abstract Predicate<T> getValidator();

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(descriptionSupplier.get());
    }

    @Override
    public Option<T> setDescription(String description) {
        return setDescription(() -> description);
    }

    @Override
    public Option<T> setDescription(Supplier<String> descriptionSupplier) {
        this.descriptionSupplier = descriptionSupplier;
        return this;
    }

    @Override
    public Optional<String> getDefaultArgument() {
        return Optional.ofNullable(defaultArgument);
    }

    @Override
    public Result<T> getDefaultValue() {
        try {
            return Result.ofOptional(getDefaultArgument().map(getParser()));
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    @Override
    public Option<T> setDefaultArgument(String defaultArgument) {
        this.defaultArgument = defaultArgument;
        return this;
    }

    @Override
    public Optional<Set<String>> getPossibleArguments() {
        return Optional.ofNullable(possibleValues).map(Collections::unmodifiableSet);
    }

    @Override
    public Option<T> setPossibleArguments(Collection<String> possibleValues) {
        this.possibleValues = possibleValues == null
                ? null
                : possibleValues.stream()
                        .map(s -> s.toUpperCase(Locale.ENGLISH))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        return this;
    }

    @Override
    public boolean validateArgument(String argument) {
        return getPossibleArguments()
                .map(v -> v.contains(argument.toUpperCase(Locale.ENGLISH)))
                .orElse(Boolean.TRUE);
    }

    @Override
    public String toString() {
        return String.format(
                "%s %s%s%s%s",
                getArgumentName(),
                getArgumentPlaceHolder(),
                getDescription().map(d -> ": " + d).orElse(""),
                getPossibleArguments()
                        .map(list -> " (possible values: " + list.stream().collect(Collectors.joining(",")) + ")")
                        .orElse(""),
                getDefaultArgument().map(v -> " (default: " + v + ")").orElse(""));
    }

    protected String getArgumentPlaceHolder() {
        return "<value>";
    }
}
