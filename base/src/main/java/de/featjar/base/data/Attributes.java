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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Util class for {@link Attribute attributes}.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public class Attributes {

    private static final LinkedHashMap<Attribute<?>, Attribute<?>> attributeSet = new LinkedHashMap<>();

    public static Set<Attribute<?>> getAllAttributes() {
        return Collections.unmodifiableSet(attributeSet.keySet());
    }

    public static void clearAllAttributes() {
        attributeSet.clear();
    }

    public static Set<Attribute<?>> getAllAttributes(String namespace) {
        return attributeSet.entrySet().stream()
                .filter(e -> Objects.equals(namespace, e.getKey().getNamespace()))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    public static <T> Attribute<T> get(Name name, Class<T> type) {
        return get(name.getNamespace(), name.getName(), type);
    }

    public static <T> Attribute<T> get(String name, Class<T> type) {
        return get(Name.DEFAULT_NAMESPACE, name, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> get(String namespace, String name, Class<T> type) {
        return getRaw(namespace, name, type);
    }

    @SuppressWarnings("rawtypes")
    public static Attribute getRaw(String namespace, String name, Class<?> type) {
        Attribute attribute = new Attribute<>(new Name(namespace, name), type);
        Attribute cachedAttribute = attributeSet.get(attribute);
        if (cachedAttribute == null) {
            attributeSet.put(attribute, attribute);
            return attribute;
        } else {
            if (type != cachedAttribute.getClassType()) {
                throw new IllegalArgumentException(String.format(
                        "Cannot create attribute for type %s. Attribute already defined for type %s.",
                        type.toString(), cachedAttribute.getClassType()));
            }
            return cachedAttribute;
        }
    }
}
