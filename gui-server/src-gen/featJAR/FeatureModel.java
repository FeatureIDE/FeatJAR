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
/**
 */
package featJAR;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Feature Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link featJAR.FeatureModel#getRoots <em>Roots</em>}</li>
 *   <li>{@link featJAR.FeatureModel#getConstraints <em>Constraints</em>}</li>
 * </ul>
 *
 * @see featJAR.FeatJARPackage#getFeatureModel()
 * @model
 * @generated
 */
public interface FeatureModel extends Identifiable {
    /**
     * Returns the value of the '<em><b>Roots</b></em>' containment reference list.
     * The list contents are of type {@link featJAR.Feature}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the value of the '<em>Roots</em>' containment reference list.
     * @see featJAR.FeatJARPackage#getFeatureModel_Roots()
     * @model containment="true"
     * @generated
     */
    EList<Feature> getRoots();

    /**
     * Returns the value of the '<em><b>Constraints</b></em>' containment reference list.
     * The list contents are of type {@link featJAR.Constraint}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the value of the '<em>Constraints</em>' containment reference list.
     * @see featJAR.FeatJARPackage#getFeatureModel_Constraints()
     * @model containment="true"
     * @generated
     */
    EList<Constraint> getConstraints();
} // FeatureModel
