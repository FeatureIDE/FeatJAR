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
package featJAR.impl;

import featJAR.Attributes;
import featJAR.Cardinality;
import featJAR.Constraint;
import featJAR.FeatJARFactory;
import featJAR.FeatJARPackage;
import featJAR.Feature;
import featJAR.FeatureModel;
import featJAR.GroupNode;
import featJAR.Identifiable;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class FeatJARPackageImpl extends EPackageImpl implements FeatJARPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass featureModelEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass identifiableEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass featureEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass constraintEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass groupNodeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass attributesEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass cardinalityEClass = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
     * package URI value.
     * <p>Note: the correct way to create the package is via the static
     * factory method {@link #init init()}, which also performs
     * initialization of the package, or returns the registered package,
     * if one already exists.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see featJAR.FeatJARPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private FeatJARPackageImpl() {
        super(eNS_URI, FeatJARFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
     *
     * <p>This method is used to initialize {@link FeatJARPackage#eINSTANCE} when that field is accessed.
     * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static FeatJARPackage init() {
        if (isInited) return (FeatJARPackage) EPackage.Registry.INSTANCE.getEPackage(FeatJARPackage.eNS_URI);

        // Obtain or create and register package
        Object registeredFeatJARPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
        FeatJARPackageImpl theFeatJARPackage = registeredFeatJARPackage instanceof FeatJARPackageImpl
                ? (FeatJARPackageImpl) registeredFeatJARPackage
                : new FeatJARPackageImpl();

        isInited = true;

        // Create package meta-data objects
        theFeatJARPackage.createPackageContents();

        // Initialize created meta-data
        theFeatJARPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theFeatJARPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(FeatJARPackage.eNS_URI, theFeatJARPackage);
        return theFeatJARPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getFeatureModel() {
        return featureModelEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getFeatureModel_Roots() {
        return (EReference) featureModelEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getFeatureModel_Constraints() {
        return (EReference) featureModelEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getIdentifiable() {
        return identifiableEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getIdentifiable_Id() {
        return (EAttribute) identifiableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getIdentifiable_Name() {
        return (EAttribute) identifiableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getIdentifiable_Attributes() {
        return (EReference) identifiableEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getFeature() {
        return featureEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getFeature_Parent() {
        return (EReference) featureEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getFeature_Cardinality() {
        return (EReference) featureEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getFeature_GroupNodeList() {
        return (EReference) featureEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getConstraint() {
        return constraintEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getGroupNode() {
        return groupNodeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getGroupNode_Parent() {
        return (EReference) groupNodeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getGroupNode_Cardinality() {
        return (EReference) groupNodeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EReference getGroupNode_FeatureList() {
        return (EReference) groupNodeEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getAttributes() {
        return attributesEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getAttributes_Key() {
        return (EAttribute) attributesEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getAttributes_Value() {
        return (EAttribute) attributesEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EClass getCardinality() {
        return cardinalityEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getCardinality_LowerBound() {
        return (EAttribute) cardinalityEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EAttribute getCardinality_UpperBound() {
        return (EAttribute) cardinalityEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public FeatJARFactory getFeatJARFactory() {
        return (FeatJARFactory) getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        featureModelEClass = createEClass(FEATURE_MODEL);
        createEReference(featureModelEClass, FEATURE_MODEL__ROOTS);
        createEReference(featureModelEClass, FEATURE_MODEL__CONSTRAINTS);

        identifiableEClass = createEClass(IDENTIFIABLE);
        createEAttribute(identifiableEClass, IDENTIFIABLE__ID);
        createEAttribute(identifiableEClass, IDENTIFIABLE__NAME);
        createEReference(identifiableEClass, IDENTIFIABLE__ATTRIBUTES);

        featureEClass = createEClass(FEATURE);
        createEReference(featureEClass, FEATURE__PARENT);
        createEReference(featureEClass, FEATURE__CARDINALITY);
        createEReference(featureEClass, FEATURE__GROUP_NODE_LIST);

        constraintEClass = createEClass(CONSTRAINT);

        groupNodeEClass = createEClass(GROUP_NODE);
        createEReference(groupNodeEClass, GROUP_NODE__PARENT);
        createEReference(groupNodeEClass, GROUP_NODE__CARDINALITY);
        createEReference(groupNodeEClass, GROUP_NODE__FEATURE_LIST);

        attributesEClass = createEClass(ATTRIBUTES);
        createEAttribute(attributesEClass, ATTRIBUTES__KEY);
        createEAttribute(attributesEClass, ATTRIBUTES__VALUE);

        cardinalityEClass = createEClass(CARDINALITY);
        createEAttribute(cardinalityEClass, CARDINALITY__LOWER_BOUND);
        createEAttribute(cardinalityEClass, CARDINALITY__UPPER_BOUND);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes
        featureModelEClass.getESuperTypes().add(this.getIdentifiable());
        featureEClass.getESuperTypes().add(this.getIdentifiable());
        constraintEClass.getESuperTypes().add(this.getIdentifiable());
        groupNodeEClass.getESuperTypes().add(this.getIdentifiable());

        // Initialize classes, features, and operations; add parameters
        initEClass(
                featureModelEClass,
                FeatureModel.class,
                "FeatureModel",
                !IS_ABSTRACT,
                !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEReference(
                getFeatureModel_Roots(),
                this.getFeature(),
                null,
                "roots",
                null,
                0,
                -1,
                FeatureModel.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEReference(
                getFeatureModel_Constraints(),
                this.getConstraint(),
                null,
                "constraints",
                null,
                0,
                -1,
                FeatureModel.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);

        initEClass(
                identifiableEClass,
                Identifiable.class,
                "Identifiable",
                IS_ABSTRACT,
                !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(
                getIdentifiable_Id(),
                ecorePackage.getEString(),
                "id",
                null,
                0,
                1,
                Identifiable.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                !IS_UNSETTABLE,
                IS_ID,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(
                getIdentifiable_Name(),
                ecorePackage.getEString(),
                "name",
                null,
                0,
                1,
                Identifiable.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                !IS_UNSETTABLE,
                !IS_ID,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEReference(
                getIdentifiable_Attributes(),
                this.getAttributes(),
                null,
                "attributes",
                null,
                0,
                -1,
                Identifiable.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);

        initEClass(featureEClass, Feature.class, "Feature", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(
                getFeature_Parent(),
                this.getGroupNode(),
                this.getGroupNode_FeatureList(),
                "parent",
                null,
                0,
                1,
                Feature.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                !IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEReference(
                getFeature_Cardinality(),
                this.getCardinality(),
                null,
                "cardinality",
                null,
                1,
                1,
                Feature.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEReference(
                getFeature_GroupNodeList(),
                this.getGroupNode(),
                this.getGroupNode_Parent(),
                "groupNodeList",
                null,
                0,
                -1,
                Feature.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);

        initEClass(
                constraintEClass,
                Constraint.class,
                "Constraint",
                !IS_ABSTRACT,
                !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);

        initEClass(
                groupNodeEClass,
                GroupNode.class,
                "GroupNode",
                !IS_ABSTRACT,
                !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEReference(
                getGroupNode_Parent(),
                this.getFeature(),
                this.getFeature_GroupNodeList(),
                "parent",
                null,
                0,
                1,
                GroupNode.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                !IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEReference(
                getGroupNode_Cardinality(),
                this.getCardinality(),
                null,
                "cardinality",
                null,
                1,
                1,
                GroupNode.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEReference(
                getGroupNode_FeatureList(),
                this.getFeature(),
                this.getFeature_Parent(),
                "featureList",
                null,
                0,
                -1,
                GroupNode.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                IS_COMPOSITE,
                !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);

        initEClass(
                attributesEClass,
                Attributes.class,
                "Attributes",
                !IS_ABSTRACT,
                !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(
                getAttributes_Key(),
                ecorePackage.getEString(),
                "key",
                null,
                0,
                1,
                Attributes.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                !IS_UNSETTABLE,
                !IS_ID,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(
                getAttributes_Value(),
                ecorePackage.getEString(),
                "value",
                null,
                0,
                1,
                Attributes.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                !IS_UNSETTABLE,
                !IS_ID,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);

        initEClass(
                cardinalityEClass,
                Cardinality.class,
                "Cardinality",
                !IS_ABSTRACT,
                !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(
                getCardinality_LowerBound(),
                ecorePackage.getEInt(),
                "lowerBound",
                "0",
                1,
                1,
                Cardinality.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                !IS_UNSETTABLE,
                !IS_ID,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(
                getCardinality_UpperBound(),
                ecorePackage.getEInt(),
                "upperBound",
                "-1",
                1,
                1,
                Cardinality.class,
                !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE,
                !IS_UNSETTABLE,
                !IS_ID,
                IS_UNIQUE,
                !IS_DERIVED,
                IS_ORDERED);

        // Create resource
        createResource(eNS_URI);
    }
} // FeatJARPackageImpl
