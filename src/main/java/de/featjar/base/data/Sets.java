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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utilities for handling linked hash sets.
 * Prefer to use {@link LinkedHashSet} over {@link java.util.HashSet}, as it guarantees determinism.
 *
 * @author Elias Kuiter
 */
public class Sets {
    /**
     * {@return a new empty LinkedHashSet}
     * @param <T> the type of elements within the set
     */
    public static <T> LinkedHashSet<T> empty() {
        return new LinkedHashSet<>();
    }

    /**
     * {@return a new LinkedHashSet containing the provided objects}
     * @param <T> the type of elements within the set
     * @param objects the objects to be in the set
     */
    @SafeVarargs
    public static <T> LinkedHashSet<T> of(T... objects) {
        return new LinkedHashSet<>(Set.of(objects));
    }

    /**
     * {@return a new LinkedHashSet containing all elements from the given sets}
     * @param <T> the type of elements within the set
     * @param sets the sets from which to add elements
     */
    @SafeVarargs
    public static <T> LinkedHashSet<T> union(Collection<T>... sets) {
        LinkedHashSet<T> newSet = new LinkedHashSet<>();
        for (Collection<T> set : sets) {
            newSet.addAll(set);
        }
        return newSet;
    }

    /**
     * {@return a Collector providing a LInkedHashSet}
     * @param <T> the type of elements within the set
     */
    public static <T> Collector<T, ?, LinkedHashSet<T>> toSet() {
        return Collectors.toCollection(LinkedHashSet::new);
    }
}
