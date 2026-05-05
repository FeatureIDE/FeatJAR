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
import java.util.Optional;

/**
 * An object that can be annotated with {@link Attribute} values to store
 * additional metadata.
 *
 * @author Elias Kuiter
 */
public interface IAttributable {
    Optional<Map<IAttribute<?>, Object>> getAttributes();

    default <T> Result<T> getAttributeValue(Attribute<T> attribute) {
        return attribute.apply(this);
    }

    default boolean hasAttributeValue(Attribute<?> attribute) {
        return getAttributeValue(attribute).isPresent();
    }

    default LinkedHashMap<IAttribute<?>, Object> cloneAttributes() {
        Optional<Map<IAttribute<?>, Object>> attributes = getAttributes();
        if (attributes.isEmpty()) {
            return null;
        }
        LinkedHashMap<IAttribute<?>, Object> clone =
                new LinkedHashMap<>((int) (attributes.get().size() * 1.5));
        attributes.get().entrySet().stream()
                .forEach(e -> clone.put(e.getKey(), e.getKey().copyValue(this).get()));
        return clone;
    }

    default <S> void checkType(Attribute<S> attribute, S value) {
        if (!attribute.getClassType().isInstance(value)) {
            throw new IllegalArgumentException(String.format(
                    "cannot set attribute of type %s to value of type %s", attribute.getClassType(), value.getClass()));
        }
    }

    default <S> void validate(Attribute<S> attribute, S value) {
        if (!attribute.getValidator().test(this, value)) {
            throw new IllegalArgumentException(
                    String.format("failed to validate attribute %s for value %s", attribute, value));
        }
    }

    default IMutatableAttributable mutate() {
        return (IMutatableAttributable) this;
    }

    static interface IMutatableAttributable extends IAttributable {
        <S> void setAttributeValue(Attribute<S> attribute, S value);

        default boolean toggleAttributeValue(Attribute<Boolean> attribute) {
            boolean toggledValue = !getAttributeValue(attribute)
                    .orGet(() -> attribute.getDefaultValue(this))
                    .orElse(Boolean.FALSE);
            setAttributeValue(attribute, toggledValue);
            return toggledValue;
        }

        <S> S removeAttributeValue(Attribute<S> attribute);
    }
}
