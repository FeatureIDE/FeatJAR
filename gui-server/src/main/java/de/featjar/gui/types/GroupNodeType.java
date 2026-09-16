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
import de.featjar.gui.utils.CardinalityUtils;
import featJAR.Cardinality;

public enum GroupNodeType {
    OR_NODE("node-or"),
    XOR_NODE("node-xor"),
    AND_NODE("node-and"),
    CARDINALITY_NODE("node-cardinality");

    private final String value;

    private GroupNodeType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public int lowerBound() {
        return switch (this) {
            case OR_NODE -> 1;
            case XOR_NODE -> 1;
            case AND_NODE -> 0;
            case CARDINALITY_NODE -> 2;
        };
    }

    public int upperBound() {
        return switch (this) {
            case OR_NODE -> CardinalityUtils.OPEN;
            case XOR_NODE -> 1;
            case AND_NODE -> CardinalityUtils.OPEN;
            case CARDINALITY_NODE -> 5;
        };
    }

    public static GroupNodeType of(int lower, int upper) {
        if (lower == 1 && upper == -1) return OR_NODE;
        if (lower == 1 && upper == 1) return XOR_NODE;
        if (lower == 0 && upper == -1) return AND_NODE;
        return CARDINALITY_NODE;
    }

    public static GroupNodeType of(Cardinality c) {
        int lower = c.getLowerBound();
        int upper = c.getUpperBound();
        return of(lower, upper);
    }

    public static GroupNodeType of(Group group) {
        int lower = group.getLowerBound();
        int upper = group.getUpperBound();
        return of(lower, upper);
    }

    public static Result<GroupNodeType> fromValue(String val) {
        for (GroupNodeType type : values()) {
            if (type.value().equals(val)) return Result.of(type);
        }
        return Result.empty(new IllegalArgumentException("Unknown NodeType: " + val));
    }
}
