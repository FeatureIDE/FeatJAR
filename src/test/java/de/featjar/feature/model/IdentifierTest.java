/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.featjar.base.data.identifier.AIdentifier;
import de.featjar.base.data.identifier.IIdentifiable;
import de.featjar.base.data.identifier.IIdentifier;
import de.featjar.base.data.identifier.Identifiers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AIdentifier} and {@link IIdentifiable}.
 *
 * @author Elias Kuiter
 */
public class IdentifierTest {
    IFeatureModel featureModel;

    @BeforeEach
    public void createFeatureModel() {
        featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
    }

    @Test
    void identifierCounter() {
        IIdentifier identifier = Identifiers.newCounterIdentifier();
        assertEquals("1", identifier.toString());
        assertEquals("2", identifier.getNewIdentifier().toString());
        assertEquals("3", identifier.getNewIdentifier().toString());
        assertNotEquals(identifier.toString(), identifier.getNewIdentifier().toString());
        assertEquals(identifier, identifier.getFactory().parse(identifier.toString()));
    }

    @Test
    void identifierUUID() {
        IIdentifier identifier = Identifiers.newUUIDIdentifier();
        assertNotEquals(identifier.toString(), identifier.getNewIdentifier().toString());
        assertEquals(identifier, identifier.getFactory().parse(identifier.toString()));
    }

    @Test
    void identifiable() {
        IIdentifier identifier = Identifiers.newCounterIdentifier();
        featureModel = new FeatureModel(identifier);
        assertEquals("1", featureModel.getIdentifier().toString());
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        Assertions.assertEquals(
                "3", featureModel.getRootFeatures().get(0).getIdentifier().toString());
        Assertions.assertEquals("4", identifier.getFactory().get().toString());
        Assertions.assertEquals(
                "5", featureModel.getRootFeatures().get(0).getNewIdentifier().toString());
        featureModel = new FeatureModel(identifier.getNewIdentifier());
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        assertEquals("6", featureModel.getIdentifier().toString());
        Assertions.assertEquals(
                "8", featureModel.getRootFeatures().get(0).getIdentifier().toString());
        assertEquals("9", featureModel.getNewIdentifier().toString());
        assertEquals(
                "3",
                new FeatureModel(Identifiers.newCounterIdentifier())
                        .getNewIdentifier()
                        .toString());
    }
}
