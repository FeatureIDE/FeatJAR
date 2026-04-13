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
package de.featjar.base.extension;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import java.util.List;

/**
 * An extension point installs {@link IExtension extensions} of a given type.
 * As a naming convention, an extension named "Thing" should be registered in an extension point named "Things".
 * Extension points can be registered in {@code resources/extensions.xml}.
 * Initialization is done by the {@link ExtensionManager} with a public no-arg constructor, which must be available.
 * De-initialization is done with {@link #close()}.
 *
 * @param <T> the type of the loaded extensions
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IExtensionPoint<T extends IExtension> {

    /**
     * {@return a unique identifier for this extension point}
     */
    default String getIdentifier() {
        return getClass().getCanonicalName();
    }

    /**
     * Installs a new extension at this extension point.
     *
     * @param extension the extension
     * @return whether this extension is new and was installed correctly
     */
    boolean installExtension(T extension);

    /**
     * Uninstalls an extension installed at this extension point.
     *
     * @param extension the extension
     * @return whether this extension was installed before
     */
    boolean uninstallExtension(T extension);

    /**
     * Uninstalls all extensions installed at this extension point.
     */
    void uninstallExtensions();

    /**
     * De-initializes this extension point, called by {@link ExtensionManager}.
     * Similar to {@link AutoCloseable#close()}, but called explicitly instead of implicitly in a try...with block.
     */
    default void close() {
        FeatJAR.log().debug("uninstalling extension point " + getClass().getName());
        uninstallExtensions();
    }

    /**
     * {@return all extensions installed at this extension point}
     * The list is in the same order as the extensions were installed with {@link #installExtension(IExtension)}.
     */
    List<T> getExtensions();

    /**
     * {@return the installed extension point for a given class, if any}
     *
     * @param klass the class
     */
    default Result<T> getExtension(Class<? extends IExtension> klass) {
        return getExtension(klass.getCanonicalName());
    }

    /**
     * {@return the installed extension with a given identifier, if any}
     *
     * @param identifier the identifier
     */
    Result<T> getExtension(String identifier);

    /**
     * {@return the installed extension matching the given part of its identifier, if any}
     * The matching is case-insensitive.
     * If no extensions match or the match is ambiguous, an empty result is returned.
     *
     * @param partOfIdentifier the part of the extension's identifier
     */
    Result<T> getMatchingExtension(String partOfIdentifier);

    /**
     * {@return all installed extensions matching the given regular expression}
     * The matching is case-insensitive.
     * If no extensions match, an empty list is returned.
     *
     * @param regex the regular expression
     */
    List<T> getMatchingExtensions(String regex);
}
