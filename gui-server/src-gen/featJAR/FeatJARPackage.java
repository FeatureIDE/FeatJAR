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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see featJAR.FeatJARFactory
 * @model kind="package"
 * @generated
 */
public interface FeatJARPackage extends EPackage {
    /**
     * The package name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNAME = "featJAR";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "https://github.com/FeatureIDE/FeatJAR";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "featJAR";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    FeatJARPackage eINSTANCE = featJAR.impl.FeatJARPackageImpl.init();

    /**
     * The meta object id for the '{@link featJAR.impl.IdentifiableImpl <em>Identifiable</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see featJAR.impl.IdentifiableImpl
     * @see featJAR.impl.FeatJARPackageImpl#getIdentifiable()
     * @generated
     */
    int IDENTIFIABLE = 1;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int IDENTIFIABLE__ID = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int IDENTIFIABLE__NAME = 1;

    /**
     * The feature id for the '<em><b>Attributes</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int IDENTIFIABLE__ATTRIBUTES = 2;

    /**
     * The number of structural features of the '<em>Identifiable</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int IDENTIFIABLE_FEATURE_COUNT = 3;

    /**
     * The number of operations of the '<em>Identifiable</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int IDENTIFIABLE_OPERATION_COUNT = 0;

    /**
     * The meta object id for the '{@link featJAR.impl.FeatureModelImpl <em>Feature Model</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see featJAR.impl.FeatureModelImpl
     * @see featJAR.impl.FeatJARPackageImpl#getFeatureModel()
     * @generated
     */
    int FEATURE_MODEL = 0;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_MODEL__ID = IDENTIFIABLE__ID;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_MODEL__NAME = IDENTIFIABLE__NAME;

    /**
     * The feature id for the '<em><b>Attributes</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_MODEL__ATTRIBUTES = IDENTIFIABLE__ATTRIBUTES;

    /**
     * The feature id for the '<em><b>Roots</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_MODEL__ROOTS = IDENTIFIABLE_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Constraints</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_MODEL__CONSTRAINTS = IDENTIFIABLE_FEATURE_COUNT + 1;

    /**
     * The number of structural features of the '<em>Feature Model</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_MODEL_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 2;

    /**
     * The number of operations of the '<em>Feature Model</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_MODEL_OPERATION_COUNT = IDENTIFIABLE_OPERATION_COUNT + 0;

    /**
     * The meta object id for the '{@link featJAR.impl.FeatureImpl <em>Feature</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see featJAR.impl.FeatureImpl
     * @see featJAR.impl.FeatJARPackageImpl#getFeature()
     * @generated
     */
    int FEATURE = 2;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE__ID = IDENTIFIABLE__ID;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE__NAME = IDENTIFIABLE__NAME;

    /**
     * The feature id for the '<em><b>Attributes</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE__ATTRIBUTES = IDENTIFIABLE__ATTRIBUTES;

    /**
     * The feature id for the '<em><b>Parent</b></em>' container reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE__PARENT = IDENTIFIABLE_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Cardinality</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE__CARDINALITY = IDENTIFIABLE_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Group Node List</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE__GROUP_NODE_LIST = IDENTIFIABLE_FEATURE_COUNT + 2;

    /**
     * The number of structural features of the '<em>Feature</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 3;

    /**
     * The number of operations of the '<em>Feature</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int FEATURE_OPERATION_COUNT = IDENTIFIABLE_OPERATION_COUNT + 0;

    /**
     * The meta object id for the '{@link featJAR.impl.ConstraintImpl <em>Constraint</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see featJAR.impl.ConstraintImpl
     * @see featJAR.impl.FeatJARPackageImpl#getConstraint()
     * @generated
     */
    int CONSTRAINT = 3;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONSTRAINT__ID = IDENTIFIABLE__ID;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONSTRAINT__NAME = IDENTIFIABLE__NAME;

    /**
     * The feature id for the '<em><b>Attributes</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONSTRAINT__ATTRIBUTES = IDENTIFIABLE__ATTRIBUTES;

    /**
     * The number of structural features of the '<em>Constraint</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONSTRAINT_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 0;

    /**
     * The number of operations of the '<em>Constraint</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONSTRAINT_OPERATION_COUNT = IDENTIFIABLE_OPERATION_COUNT + 0;

    /**
     * The meta object id for the '{@link featJAR.impl.GroupNodeImpl <em>Group Node</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see featJAR.impl.GroupNodeImpl
     * @see featJAR.impl.FeatJARPackageImpl#getGroupNode()
     * @generated
     */
    int GROUP_NODE = 4;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_NODE__ID = IDENTIFIABLE__ID;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_NODE__NAME = IDENTIFIABLE__NAME;

    /**
     * The feature id for the '<em><b>Attributes</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_NODE__ATTRIBUTES = IDENTIFIABLE__ATTRIBUTES;

    /**
     * The feature id for the '<em><b>Parent</b></em>' container reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_NODE__PARENT = IDENTIFIABLE_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Cardinality</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_NODE__CARDINALITY = IDENTIFIABLE_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Feature List</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_NODE__FEATURE_LIST = IDENTIFIABLE_FEATURE_COUNT + 2;

    /**
     * The number of structural features of the '<em>Group Node</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_NODE_FEATURE_COUNT = IDENTIFIABLE_FEATURE_COUNT + 3;

    /**
     * The number of operations of the '<em>Group Node</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_NODE_OPERATION_COUNT = IDENTIFIABLE_OPERATION_COUNT + 0;

    /**
     * The meta object id for the '{@link featJAR.impl.AttributesImpl <em>Attributes</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see featJAR.impl.AttributesImpl
     * @see featJAR.impl.FeatJARPackageImpl#getAttributes()
     * @generated
     */
    int ATTRIBUTES = 5;

    /**
     * The feature id for the '<em><b>Key</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ATTRIBUTES__KEY = 0;

    /**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ATTRIBUTES__VALUE = 1;

    /**
     * The number of structural features of the '<em>Attributes</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ATTRIBUTES_FEATURE_COUNT = 2;

    /**
     * The number of operations of the '<em>Attributes</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ATTRIBUTES_OPERATION_COUNT = 0;

    /**
     * The meta object id for the '{@link featJAR.impl.CardinalityImpl <em>Cardinality</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see featJAR.impl.CardinalityImpl
     * @see featJAR.impl.FeatJARPackageImpl#getCardinality()
     * @generated
     */
    int CARDINALITY = 6;

    /**
     * The feature id for the '<em><b>Lower Bound</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CARDINALITY__LOWER_BOUND = 0;

    /**
     * The feature id for the '<em><b>Upper Bound</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CARDINALITY__UPPER_BOUND = 1;

    /**
     * The number of structural features of the '<em>Cardinality</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CARDINALITY_FEATURE_COUNT = 2;

    /**
     * The number of operations of the '<em>Cardinality</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CARDINALITY_OPERATION_COUNT = 0;

    /**
     * Returns the meta object for class '{@link featJAR.FeatureModel <em>Feature Model</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Feature Model</em>'.
     * @see featJAR.FeatureModel
     * @generated
     */
    EClass getFeatureModel();

    /**
     * Returns the meta object for the containment reference list '{@link featJAR.FeatureModel#getRoots <em>Roots</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Roots</em>'.
     * @see featJAR.FeatureModel#getRoots()
     * @see #getFeatureModel()
     * @generated
     */
    EReference getFeatureModel_Roots();

    /**
     * Returns the meta object for the containment reference list '{@link featJAR.FeatureModel#getConstraints <em>Constraints</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Constraints</em>'.
     * @see featJAR.FeatureModel#getConstraints()
     * @see #getFeatureModel()
     * @generated
     */
    EReference getFeatureModel_Constraints();

    /**
     * Returns the meta object for class '{@link featJAR.Identifiable <em>Identifiable</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Identifiable</em>'.
     * @see featJAR.Identifiable
     * @generated
     */
    EClass getIdentifiable();

    /**
     * Returns the meta object for the attribute '{@link featJAR.Identifiable#getId <em>Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Id</em>'.
     * @see featJAR.Identifiable#getId()
     * @see #getIdentifiable()
     * @generated
     */
    EAttribute getIdentifiable_Id();

    /**
     * Returns the meta object for the attribute '{@link featJAR.Identifiable#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see featJAR.Identifiable#getName()
     * @see #getIdentifiable()
     * @generated
     */
    EAttribute getIdentifiable_Name();

    /**
     * Returns the meta object for the containment reference list '{@link featJAR.Identifiable#getAttributes <em>Attributes</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Attributes</em>'.
     * @see featJAR.Identifiable#getAttributes()
     * @see #getIdentifiable()
     * @generated
     */
    EReference getIdentifiable_Attributes();

    /**
     * Returns the meta object for class '{@link featJAR.Feature <em>Feature</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Feature</em>'.
     * @see featJAR.Feature
     * @generated
     */
    EClass getFeature();

    /**
     * Returns the meta object for the container reference '{@link featJAR.Feature#getParent <em>Parent</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the container reference '<em>Parent</em>'.
     * @see featJAR.Feature#getParent()
     * @see #getFeature()
     * @generated
     */
    EReference getFeature_Parent();

    /**
     * Returns the meta object for the containment reference '{@link featJAR.Feature#getCardinality <em>Cardinality</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Cardinality</em>'.
     * @see featJAR.Feature#getCardinality()
     * @see #getFeature()
     * @generated
     */
    EReference getFeature_Cardinality();

    /**
     * Returns the meta object for the containment reference list '{@link featJAR.Feature#getGroupNodeList <em>Group Node List</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Group Node List</em>'.
     * @see featJAR.Feature#getGroupNodeList()
     * @see #getFeature()
     * @generated
     */
    EReference getFeature_GroupNodeList();

    /**
     * Returns the meta object for class '{@link featJAR.Constraint <em>Constraint</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Constraint</em>'.
     * @see featJAR.Constraint
     * @generated
     */
    EClass getConstraint();

    /**
     * Returns the meta object for class '{@link featJAR.GroupNode <em>Group Node</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Group Node</em>'.
     * @see featJAR.GroupNode
     * @generated
     */
    EClass getGroupNode();

    /**
     * Returns the meta object for the container reference '{@link featJAR.GroupNode#getParent <em>Parent</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the container reference '<em>Parent</em>'.
     * @see featJAR.GroupNode#getParent()
     * @see #getGroupNode()
     * @generated
     */
    EReference getGroupNode_Parent();

    /**
     * Returns the meta object for the containment reference '{@link featJAR.GroupNode#getCardinality <em>Cardinality</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Cardinality</em>'.
     * @see featJAR.GroupNode#getCardinality()
     * @see #getGroupNode()
     * @generated
     */
    EReference getGroupNode_Cardinality();

    /**
     * Returns the meta object for the containment reference list '{@link featJAR.GroupNode#getFeatureList <em>Feature List</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Feature List</em>'.
     * @see featJAR.GroupNode#getFeatureList()
     * @see #getGroupNode()
     * @generated
     */
    EReference getGroupNode_FeatureList();

    /**
     * Returns the meta object for class '{@link featJAR.Attributes <em>Attributes</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Attributes</em>'.
     * @see featJAR.Attributes
     * @generated
     */
    EClass getAttributes();

    /**
     * Returns the meta object for the attribute '{@link featJAR.Attributes#getKey <em>Key</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Key</em>'.
     * @see featJAR.Attributes#getKey()
     * @see #getAttributes()
     * @generated
     */
    EAttribute getAttributes_Key();

    /**
     * Returns the meta object for the attribute '{@link featJAR.Attributes#getValue <em>Value</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Value</em>'.
     * @see featJAR.Attributes#getValue()
     * @see #getAttributes()
     * @generated
     */
    EAttribute getAttributes_Value();

    /**
     * Returns the meta object for class '{@link featJAR.Cardinality <em>Cardinality</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Cardinality</em>'.
     * @see featJAR.Cardinality
     * @generated
     */
    EClass getCardinality();

    /**
     * Returns the meta object for the attribute '{@link featJAR.Cardinality#getLowerBound <em>Lower Bound</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Lower Bound</em>'.
     * @see featJAR.Cardinality#getLowerBound()
     * @see #getCardinality()
     * @generated
     */
    EAttribute getCardinality_LowerBound();

    /**
     * Returns the meta object for the attribute '{@link featJAR.Cardinality#getUpperBound <em>Upper Bound</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Upper Bound</em>'.
     * @see featJAR.Cardinality#getUpperBound()
     * @see #getCardinality()
     * @generated
     */
    EAttribute getCardinality_UpperBound();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    FeatJARFactory getFeatJARFactory();

    /**
     * <!-- begin-user-doc -->
     * Defines literals for the meta objects that represent
     * <ul>
     *   <li>each class,</li>
     *   <li>each feature of each class,</li>
     *   <li>each operation of each class,</li>
     *   <li>each enum,</li>
     *   <li>and each data type</li>
     * </ul>
     * <!-- end-user-doc -->
     * @generated
     */
    interface Literals {
        /**
         * The meta object literal for the '{@link featJAR.impl.FeatureModelImpl <em>Feature Model</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see featJAR.impl.FeatureModelImpl
         * @see featJAR.impl.FeatJARPackageImpl#getFeatureModel()
         * @generated
         */
        EClass FEATURE_MODEL = eINSTANCE.getFeatureModel();

        /**
         * The meta object literal for the '<em><b>Roots</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference FEATURE_MODEL__ROOTS = eINSTANCE.getFeatureModel_Roots();

        /**
         * The meta object literal for the '<em><b>Constraints</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference FEATURE_MODEL__CONSTRAINTS = eINSTANCE.getFeatureModel_Constraints();

        /**
         * The meta object literal for the '{@link featJAR.impl.IdentifiableImpl <em>Identifiable</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see featJAR.impl.IdentifiableImpl
         * @see featJAR.impl.FeatJARPackageImpl#getIdentifiable()
         * @generated
         */
        EClass IDENTIFIABLE = eINSTANCE.getIdentifiable();

        /**
         * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute IDENTIFIABLE__ID = eINSTANCE.getIdentifiable_Id();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute IDENTIFIABLE__NAME = eINSTANCE.getIdentifiable_Name();

        /**
         * The meta object literal for the '<em><b>Attributes</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference IDENTIFIABLE__ATTRIBUTES = eINSTANCE.getIdentifiable_Attributes();

        /**
         * The meta object literal for the '{@link featJAR.impl.FeatureImpl <em>Feature</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see featJAR.impl.FeatureImpl
         * @see featJAR.impl.FeatJARPackageImpl#getFeature()
         * @generated
         */
        EClass FEATURE = eINSTANCE.getFeature();

        /**
         * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference FEATURE__PARENT = eINSTANCE.getFeature_Parent();

        /**
         * The meta object literal for the '<em><b>Cardinality</b></em>' containment reference feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference FEATURE__CARDINALITY = eINSTANCE.getFeature_Cardinality();

        /**
         * The meta object literal for the '<em><b>Group Node List</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference FEATURE__GROUP_NODE_LIST = eINSTANCE.getFeature_GroupNodeList();

        /**
         * The meta object literal for the '{@link featJAR.impl.ConstraintImpl <em>Constraint</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see featJAR.impl.ConstraintImpl
         * @see featJAR.impl.FeatJARPackageImpl#getConstraint()
         * @generated
         */
        EClass CONSTRAINT = eINSTANCE.getConstraint();

        /**
         * The meta object literal for the '{@link featJAR.impl.GroupNodeImpl <em>Group Node</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see featJAR.impl.GroupNodeImpl
         * @see featJAR.impl.FeatJARPackageImpl#getGroupNode()
         * @generated
         */
        EClass GROUP_NODE = eINSTANCE.getGroupNode();

        /**
         * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference GROUP_NODE__PARENT = eINSTANCE.getGroupNode_Parent();

        /**
         * The meta object literal for the '<em><b>Cardinality</b></em>' containment reference feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference GROUP_NODE__CARDINALITY = eINSTANCE.getGroupNode_Cardinality();

        /**
         * The meta object literal for the '<em><b>Feature List</b></em>' containment reference list feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EReference GROUP_NODE__FEATURE_LIST = eINSTANCE.getGroupNode_FeatureList();

        /**
         * The meta object literal for the '{@link featJAR.impl.AttributesImpl <em>Attributes</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see featJAR.impl.AttributesImpl
         * @see featJAR.impl.FeatJARPackageImpl#getAttributes()
         * @generated
         */
        EClass ATTRIBUTES = eINSTANCE.getAttributes();

        /**
         * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ATTRIBUTES__KEY = eINSTANCE.getAttributes_Key();

        /**
         * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute ATTRIBUTES__VALUE = eINSTANCE.getAttributes_Value();

        /**
         * The meta object literal for the '{@link featJAR.impl.CardinalityImpl <em>Cardinality</em>}' class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @see featJAR.impl.CardinalityImpl
         * @see featJAR.impl.FeatJARPackageImpl#getCardinality()
         * @generated
         */
        EClass CARDINALITY = eINSTANCE.getCardinality();

        /**
         * The meta object literal for the '<em><b>Lower Bound</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute CARDINALITY__LOWER_BOUND = eINSTANCE.getCardinality_LowerBound();

        /**
         * The meta object literal for the '<em><b>Upper Bound</b></em>' attribute feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        EAttribute CARDINALITY__UPPER_BOUND = eINSTANCE.getCardinality_UpperBound();
    }
} // FeatJARPackage
