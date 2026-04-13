/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.Attributes;
import de.featjar.base.data.Sets;
import de.featjar.base.data.identifier.IIdentifiable;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Defines useful {@link Attribute attributes} for {@link FeatureModel feature models},
 * {@link Feature features}, and {@link Constraint constraints}.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public class FeatureModelAttributes {

    public static final String ATTRIBUTE_NAMESPACE = FeatureModelAttributes.class.getCanonicalName() + ".attributes";
    public static final String FM_PROPERTY_NAMESPACE = FeatureModelAttributes.class.getCanonicalName() + ".properties";

    public static final Attribute<String> NAME = Attributes.get(FM_PROPERTY_NAMESPACE, "name", String.class)
            .setDefaultValueFunction(identifiable ->
                    "@" + ((IIdentifiable) identifiable).getIdentifier().toString())
            .setValidator(
                    (element, name) -> // TODO: can also be name of feature model or constraint, but this validates only
                            // feature name uniqueness
                            ((AFeatureModelElement) element)
                                    .getFeatureModel()
                                    .getFeature((String) name)
                                    .isEmpty());

    public static final Attribute<String> DESCRIPTION =
            Attributes.get(FM_PROPERTY_NAMESPACE, "description", String.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Attribute<LinkedHashSet<String>> TAGS = Attributes.getRaw(
                    FM_PROPERTY_NAMESPACE, "tags", LinkedHashSet.class)
            .setDefaultValueFunction(attributable -> Sets.<String>empty())
            .setCopyValueFunction(set -> new LinkedHashSet((Collection) set));

    public static final Attribute<Boolean> HIDDEN =
            Attributes.get(FM_PROPERTY_NAMESPACE, "hidden", Boolean.class).setDefaultValue(false);

    public static final Attribute<Boolean> ABSTRACT =
            Attributes.get(FM_PROPERTY_NAMESPACE, "abstract", Boolean.class).setDefaultValue(false);

    /**
     * Convenience method to get a feature model attribute with the appropriate name space.
     * Equivalent to: {@code Attributes.get(FeatureModelAttributes.ATTRIBUTE_NAMESPACE, name, type);}
     *
     * @param <T> the attribute class type
     * @param name the name of the attribute
     * @param type the value type of the attribute
     * @return the attribute instance
     */
    public static <T> Attribute<T> get(String name, Class<T> type) {
        return Attributes.get(ATTRIBUTE_NAMESPACE, name, type);
    }
}
