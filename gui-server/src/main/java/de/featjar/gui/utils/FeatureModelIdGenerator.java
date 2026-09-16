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

import java.util.UUID;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.glsp.server.emf.EMFIdGenerator;

/**
 * A generator class that creates a unique identifier for a given EObject.
 * Ideally, the generated IDs should be considered stable during resource close/load and across model modifications.
 * The ids are used when indexing the element and may be given to the GModel element.
 * {@link EMFIdGenerator}
 */
public class FeatureModelIdGenerator implements EMFIdGenerator {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOrCreateId(final EObject element) {
        /* The first guard returns a position-based fragments for elements that do not
         * have an ID in the EMF model such as the Cardinality and Attributes classes since
         * both contain just values are are not entities such as e.g. Feature.
         */
        if (element.eClass().getEIDAttribute() == null) {
            return EcoreUtil.getURI(element).fragment();
        }
        String id = EcoreUtil.getID(element);

        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().replace("-", "");
            EcoreUtil.setID(element, id);
        }
        return id;
    }
}
