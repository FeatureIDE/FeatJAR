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
package de.featjar.base.extension;

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
public abstract class AExtensionPoint<T extends IExtension> implements IExtensionPoint<T> {
    private final ExtensionList<T> extensions = new ExtensionList<>();

    @Override
    public boolean installExtension(T extension) {
        return extensions.addExtension(extension);
    }

    @Override
    public boolean uninstallExtension(T extension) {
        return extensions.removeExtension(extension);
    }

    @Override
    public void uninstallExtensions() {
        extensions.removeAll();
    }

    @Override
    public List<T> getExtensions() {
        return extensions.getExtensions();
    }

    @Override
    public Result<T> getExtension(String identifier) {
        return extensions.getExtension(identifier);
    }

    @Override
    public Result<T> getMatchingExtension(String partOfIdentifier) {
        return extensions.getMatchingExtension(partOfIdentifier);
    }

    @Override
    public List<T> getMatchingExtensions(String regex) {
        return extensions.getMatchingExtensions(regex);
    }
}
