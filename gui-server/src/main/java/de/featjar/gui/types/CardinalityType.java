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
import de.featjar.feature.model.FeatureTree.Group;
import featJAR.Cardinality;

public enum CardinalityType {
    OPTIONAL_FEATURE("feature-optional"),
    MANDATORY_FEATURE("feature-mandatory"),
    MULTIPLE_FEATURE("feature-multiple");

    private final String value;

    CardinalityType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CardinalityType of(int lower, int upper) {
        if (lower == 0 && upper == 1) return OPTIONAL_FEATURE;
        if (lower == 1 && upper == 1) return MANDATORY_FEATURE;
        return MULTIPLE_FEATURE;
    }

    public static CardinalityType of(Cardinality c) {
        int lower = c.getLowerBound();
        int upper = c.getUpperBound();
        return of(lower, upper);
    }

    public static CardinalityType of(Group group) {
        int lower = group.getLowerBound();
        int upper = group.getUpperBound();
        return of(lower, upper);
    }

    public static Result<CardinalityType> fromValue(String val) {
        for (CardinalityType type : values()) {
            if (type.value().equals(val)) return Result.of(type);
        }
        return Result.empty(new IllegalArgumentException("Unknown FeatureCardinalityType: " + val));
    }
}
