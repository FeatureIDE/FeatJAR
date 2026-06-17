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
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An option for an {@link ICommand}.
 * Parses a string argument into an object value.
 * Allows to set a description, default argument, and allowed arguments.
 *
 * @param <T> the type of the option's value
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Option<T> extends IParameter {

    /**
     * {@return this option's name}
     */
    String getName();

    /**
     * {@return this option's argument name on the command-line interface}
     */
    String getArgumentName();

    /**
     * {@return an indicator on how to specify the argument(s) for this option}
     */
    String getArgumentPlaceHolder();

    /**
     * {@return this option's description}
     */
    Optional<String> getDescription();

    /**
     * Sets this option's description.
     *
     * @param description the description
     * @return this option
     */
    default Option<T> setDescription(String description) {
        return setDescription(() -> description);
    }

    /**
     * Sets this option's description supplier. Should be used when the description
     * is complicated or only known after initialization.
     *
     * @param descriptionSupplier the description supplier
     * @return this option
     */
    Option<T> setDescription(Supplier<String> descriptionSupplier);

    /**
     * {@return this option's default argument}
     */
    Optional<String> getDefaultArgument();

    /**
     * {@return this option's default value}
     * The value parsed from this option's {@link #getDefaultArgument() default argument}.
     */
    Result<T> getDefaultValue();

    /**
     * Sets this option's default argument.
     *
     * @param defaultArgument the default value
     * @return this option
     */
    Option<T> setDefaultArgument(String defaultArgument);

    /**
     * {@return the collection of arguments accepted by this option}
     * Returns an empty set if the option accepts any argument.
     * @see #validateArgument(String)
     */
    Optional<Collection<String>> getPossibleArguments();

    /**
     * Set the collection of allowed arguments for this option.
     * If none or an empty set is given, the option allows any argument.
     *
     * @param possibleArguments the possible arguments
     * @return this option
     * @see #validateArgument(String)
     */
    Option<T> setPossibleArguments(Collection<String> possibleArguments);

    /**
     * Checks whether this option accepts the given argument.
     * @param argument the argument to check
     * @return {@code true} if the option accepts the given argument, {@code false} otherwise
     */
    boolean validateArgument(String argument);

    /**
     * Applies the option parser.
     *
     * @param s the string to be parsed
     * @return a {@link Result} containing the parsed value.
     */
    Result<T> parse(String s);
}
