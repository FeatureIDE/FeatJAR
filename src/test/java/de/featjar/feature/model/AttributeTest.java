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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.Attributes;
import de.featjar.base.data.IAttributable;
import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Name;
import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Attribute}, {@link FeatureModelAttributes}, and {@link IAttributable}.
 *
 * @author Elias Kuiter
 */
public class AttributeTest {
    FeatureModel featureModel;
    Attribute<String> attribute = Attributes.get(new Name("any", "test"), String.class);

    @BeforeEach
    public void createFeatureModel() {
        featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
    }

    @AfterAll
    public static void deinit() {
        Attributes.clearAllAttributes();
    }

    @Test
    public void attribute() {
        assertEquals("any", attribute.getNamespace());
        assertEquals("test", attribute.getSimpleName());
        assertEquals(String.class, attribute.getClassType());
    }

    @Test
    public void attributableGetSet() {
        LinkedHashMap<IAttribute<?>, Object> attributeToValueMap = new LinkedHashMap<>();
        Attribute<String> attributeWithDefaultValue =
                Attributes.get("test", String.class).setDefaultValue("default");
        Assertions.assertTrue(featureModel.getAttributeValue(attribute).isEmpty());
        Assertions.assertEquals(Result.of("default"), featureModel.getAttributeValue(attributeWithDefaultValue));
        assertEquals(attributeToValueMap, featureModel.getAttributes().get());
        featureModel.mutate().setAttributeValue(attribute, "value");
        attributeToValueMap.put(attribute, "value");
        IAttributable attributable = new IAttributable() {
            @Override
            public Optional<Map<IAttribute<?>, Object>> getAttributes() {
                return Optional.of(attributeToValueMap);
            }
        };
        Assertions.assertEquals(Result.of("value"), featureModel.getAttributeValue(attribute));
        assertEquals(Result.of("value"), attribute.apply(attributable));
        assertTrue(featureModel.getAttributes().isPresent());
        assertEquals(attributeToValueMap, featureModel.getAttributes().get());
        featureModel.mutate().removeAttributeValue(attribute);
        attributeToValueMap.clear();
        Assertions.assertEquals(Result.empty(), featureModel.getAttributeValue(attribute));
        assertEquals(attributeToValueMap, featureModel.getAttributes().get());
    }

    @Test
    public void attributableToggle() {
        Attribute<Boolean> booleanAttribute =
                Attributes.get("testBoolean", Boolean.class).setDefaultValue(false);
        Assertions.assertEquals(Result.of(false), featureModel.getAttributeValue(booleanAttribute));
        featureModel.mutate().toggleAttributeValue(booleanAttribute);
        Assertions.assertEquals(Result.of(true), featureModel.getAttributeValue(booleanAttribute));
    }

    @Test
    public void attributesName() {
        Assertions.assertEquals(featureModel.getName(), featureModel.getAttributeValue(FeatureModelAttributes.NAME));
        Assertions.assertEquals(
                "@" + featureModel.getIdentifier(), featureModel.getName().get());
        Assertions.assertEquals(Result.empty(), featureModel.getDescription());
    }

    @Test
    public void attributesDescription() {
        featureModel.mutate().setDescription("desc");
        Assertions.assertEquals(Result.of("desc"), featureModel.getDescription());
        featureModel.mutate().setDescription(null);
        Assertions.assertEquals(Result.empty(), featureModel.getDescription());
    }

    @Test
    public void attributesHidden() {
        Assertions.assertTrue(featureModel.getRootFeatures().isEmpty());
        IFeature addFeature = featureModel.addFeature("hiddenFeature");
        Assertions.assertFalse(addFeature.isHidden());
        addFeature.mutate().setHidden(true);
        Assertions.assertTrue(addFeature.isHidden());
        Assertions.assertFalse(addFeature.mutate().toggleHidden());
    }
}
