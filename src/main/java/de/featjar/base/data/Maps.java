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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for handling linked hash maps.
 * Prefer to use {@link LinkedHashMap} over {@link java.util.HashMap}, as it guarantees determinism.
 *
 * @author Elias Kuiter
 */
public class Maps {
    /**
     * Creates a new empty {@link LinkedHashMap}.
     * @param <T> the type of the keys
     * @param <U> the type of the values
     * @return a new map instance
     */
    public static <T, U> LinkedHashMap<T, U> empty() {
        return new LinkedHashMap<>();
    }
    /**
     * Creates a new {@link LinkedHashMap} with a single mapping.
     * @param <T> the type of the keys
     * @param <U> the type of the values
     * @param key the key of the mapping
     * @param value the value of the mapping
     * @return a new map instance
     */
    public static <T, U> LinkedHashMap<T, U> of(T key, U value) {
        return new LinkedHashMap<>(Map.of(key, value));
    }

    /**
     * Creates a new {@link LinkedHashMap} from a key and value mapper.
     * Can be used with {@link Stream#collect(Collector)}.
     * @param <K> the type of the keys
     * @param <U> the type of the values
     * @param <T> the type of element in the stream
     * @param keyMapper a function converting an element to a key
     * @param valueMapper a function converting an element to a value
     * @return a new map instance
     */
    public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toMap(
            Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (k, v) -> {
                    throw new IllegalStateException(java.lang.String.format("duplicate key %s", k));
                },
                LinkedHashMap::new);
    }
}
