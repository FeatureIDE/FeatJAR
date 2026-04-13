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

import java.util.Objects;

public class Name implements Comparable<Name> {
    /**
     * The default name space for attributes.
     * Set to: {@code de.featjar.base.data}
     */
    public static final String DEFAULT_NAMESPACE = Name.class.getPackageName();

    private final String namespace;
    private final String name;

    /**
     * Constructs a new name with the {@link #DEFAULT_NAMESPACE default name space}.
     * @param name the name
     */
    public Name(String name) {
        this(DEFAULT_NAMESPACE, name);
    }

    /**
     * Constructs a new name.
     * @param namespace the name space
     * @param name the name
     */
    public Name(String namespace, String name) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return String.format("%s:%s", namespace, name);
    }

    @Override
    public String toString() {
        return String.format("Name{namespace='%s', name='%s'}", namespace, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name attribute = (Name) o;
        return namespace.equals(attribute.namespace) && name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    @Override
    public int compareTo(Name o) {
        final int namespaceDiff = namespace.compareTo(o.namespace);
        return namespaceDiff != 0 ? namespaceDiff : name.compareTo(o.name);
    }
}
