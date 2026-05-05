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

/**
 * An extension implements some functionality that can be loaded by an {@link AExtensionPoint}.
 * Extensions can be registered in {@code resources/extensions.xml}.
 * Initialization is done by the {@link ExtensionManager} with a public no-arg constructor, which must be available.
 * De-initialization is done with {@link #close()}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface IExtension {

    /**
     * {@return a unique identifier for this extension}
     */
    default String getIdentifier() {
        return getClass().getCanonicalName();
    }

    /**
     * De-initializes this extension, called by {@link ExtensionManager}.
     * Similar to {@link AutoCloseable#close()}, but called explicitly instead of implicitly in a try...with block.
     */
    default void close() {}
}
