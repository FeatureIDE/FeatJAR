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
package de.featjar.gui.utils;

import de.featjar.gui.types.AttributeKeys;
import de.featjar.gui.types.FeatureImplementationTypes;
import featJAR.Attributes;
import featJAR.FeatJARFactory;
import featJAR.Feature;
import featJAR.Identifiable;
import java.util.Optional;

/**
 * Contains setter and getter methods for {@link Attributes} named in {@link AttributeKeys}
 */
public final class AttributeKeysUtils {

    private static void setAttribute(Identifiable element, String key, String value) {
        Optional<Attributes> existing = element.getAttributes().stream()
                .filter(a -> key.equals(a.getKey()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setValue(value);
        } else {
            Attributes attr = FeatJARFactory.eINSTANCE.createAttributes();
            attr.setKey(key);
            attr.setValue(value);
            element.getAttributes().add(attr);
        }
    }

    private static Optional<Attributes> getAttribute(Identifiable element, String key) {
        Optional<Attributes> attribute = element.getAttributes().stream()
                .filter(a -> key.equals(a.getKey()))
                .findFirst();
        return attribute;
    }

    public static void setFeatureImplementationType(Feature feature, FeatureImplementationTypes featureType) {
        setAttribute(feature, AttributeKeys.IMPLLEMENTATION, featureType.value());
    }

    public static FeatureImplementationTypes getFeatureType(Feature feature) {
        Optional<Attributes> a = getAttribute(feature, AttributeKeys.IMPLLEMENTATION);
        return a.isPresent()
                ? FeatureImplementationTypes.fromValue(a.get().getValue()).orElseThrow()
                : FeatureImplementationTypes.NONE;
    }

    public static void setHidden(Feature feature) {
        setAttribute(feature, AttributeKeys.HIDDEN, "");
    }

    public static boolean isHidden(Feature feature) {
        return !getAttribute(feature, AttributeKeys.HIDDEN).isEmpty();
    }
}
