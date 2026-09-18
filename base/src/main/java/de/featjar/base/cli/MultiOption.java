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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for options that allow multiple values.
 *
 * @param <T> the type of the option's values
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class MultiOption<T> extends AOption<List<T>> {

    /**
     * A parser that parses a string into the type of the option.
     */
    protected final Function<String, Result<T>> parser;

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     */
    protected MultiOption(String name, Function<String, Result<T>> parser) {
        this(name, parser, null, null);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param possibleValues the possibleValues for the option
     */
    protected MultiOption(String name, Function<String, Result<T>> parser, Collection<String> possibleValues) {
        this(name, parser, possibleValues, null);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param defaultArgument the default value in case no other is provided or can be parsed
     */
    protected MultiOption(String name, Function<String, Result<T>> parser, String defaultArgument) {
        this(name, parser, null, defaultArgument);
    }

    /**
     * Creates an option.
     *
     * @param name   the name of the option
     * @param parser the parser for the option's value
     * @param possibleValues the possibleValues for the option
     * @param defaultArgument the default value in case no other is provided or can be parsed
     */
    protected MultiOption(
            String name, Function<String, Result<T>> parser, Collection<String> possibleValues, String defaultArgument) {
        super(name, defaultArgument);
        this.parser = Objects.requireNonNull(parser);
        setPossibleArguments(possibleValues);
    }

    @Override
    public Function<String, Result<List<T>>> getParser() {
        return Options.parser(arg -> Arrays.stream(arg.split("[,\n]"))
                .map(parser)
                .map(Result::orElseThrow)
                .toList());
    }

    @Override
    public boolean validateArgument(String argument) {
        return possibleValues == null
                || Arrays.stream(argument.toLowerCase(Locale.ENGLISH).split("[,\n]"))
                        .allMatch(possibleValues::containsKey);
    }

    @Override
    public MultiOption<T> setDefaultArgument(String defaultArgument) {
        return (MultiOption<T>) super.setDefaultArgument(defaultArgument);
    }

    @Override
    public MultiOption<T> setPossibleArguments(Collection<String> possibleValues) {
        return (MultiOption<T>) super.setPossibleArguments(possibleValues);
    }

    @Override
    public MultiOption<T> setDescription(Supplier<String> descriptionSupplier) {
        return (MultiOption<T>) super.setDescription(descriptionSupplier);
    }

    @Override
    public MultiOption<T> setDescription(String description) {
        return (MultiOption<T>) super.setDescription(description);
    }

    @Override
    public String getArgumentPlaceHolder() {
        return "<value1,value2,...>";
    }
}
