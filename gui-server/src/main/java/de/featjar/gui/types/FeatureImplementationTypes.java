/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-gui-server.
 *
 * gui-server is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * gui-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gui-server. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE> for further information.
 */
package de.featjar.gui.types;

import de.featjar.base.data.Result;

public enum FeatureImplementationTypes {
    NONE("none"),
    ABSTRACT("abstract"),
    CONCRETE("concrete");

    private final String value;

    private FeatureImplementationTypes(String value) {
        this.value = value;
    }

    public static Result<FeatureImplementationTypes> fromValue(String val) {
        for (FeatureImplementationTypes fit : values()) {
            if (fit.value().equals(val)) return Result.of(fit);
        }
        return Result.empty(new IllegalArgumentException("Unknown FeatureImplementationTypes: " + val));
    }

    public String value() {
        return value;
    }
}
