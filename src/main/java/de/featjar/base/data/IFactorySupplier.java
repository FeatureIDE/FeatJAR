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
package de.featjar.base.data;

import de.featjar.base.io.format.IFormat;
import java.nio.file.Path;

/**
 * Creates a factory for a given file format and path.
 *
 * @param <T> the type of the created instance
 *
 * @author Sebastian Krieter
 */
public interface IFactorySupplier<T> {

    /**
     * {@return a constant factory supplier that always returns a given factory}
     *
     * @param factory the factory
     * @param <T> the type of the created instance
     */
    static <T> IFactorySupplier<T> of(IFactory<T> factory) {
        return (path, format) -> Result.of(factory);
    }

    /**
     * {@return a factory that fits the given file format and path, if any}
     *
     * @param path   the file path
     * @param format the file format
     */
    Result<IFactory<T>> getFactory(Path path, IFormat<T> format);
}
