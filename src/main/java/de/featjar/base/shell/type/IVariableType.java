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
package de.featjar.base.shell.type;

import de.featjar.base.data.Result;
import de.featjar.base.extension.IExtension;
import java.nio.file.Path;

public interface IVariableType<T> extends IExtension {

    /**
     * {@return the name of the type}
     */
    String getName();

    /**
     * {@return the class type}
     */
    Class<T> getClassType();

    /**
     * Loads a value of this type from the given path.
     *
     * @param path the path to a file
     *
     * @return a result containing the loaded value
     */
    Result<T> load(Path path);
}
