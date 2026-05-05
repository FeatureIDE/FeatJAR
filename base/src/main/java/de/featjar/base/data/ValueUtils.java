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

import java.lang.reflect.Array;
import java.util.Objects;

public final class ValueUtils {
    private ValueUtils() {}

    public static int hashValue(Object v) {
        if (v == null) return 0;
        if (v.getClass().isArray()) {
            int len = Array.getLength(v);
            int hash = 1;
            for (int i = 0; i < len; i++) {
                Object elem = Array.get(v, i);
                hash = 31 * hash + Objects.hashCode(elem);
            }
            return hash;
        }
        return v.hashCode();
    }

    public static String toStringValue(Object v) {
        if (v != null && v.getClass().isArray()) {
            int maxIndex = Array.getLength(v) - 1;
            if (maxIndex == -1) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; ; i++) {
                sb.append(String.valueOf(Array.get(v, i)));
                if (i == maxIndex) {
                    return sb.append(']').toString();
                }
                sb.append(", ");
            }
        }
        return String.valueOf(v);
    }
}
