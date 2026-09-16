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
package de.featjar.gui.model;

import featJAR.FeatJARPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.glsp.server.emf.notation.EMFNotationSourceModelStorage;

/**
 * Loads and stores the feature model as an EMF resource.
 *
 * Registers the FeatJAR package so the resource set can resolve the model's
 * namespace when a .featuremodel file is read.
 */
public class FeatureModelSourceModelStorage extends EMFNotationSourceModelStorage {
    @Override
    protected ResourceSet setupResourceSet(final ResourceSet resourceSet) {
        resourceSet.getPackageRegistry().put(FeatJARPackage.eINSTANCE.getNsURI(), FeatJARPackage.eINSTANCE);
        return super.setupResourceSet(resourceSet);
    }
}
