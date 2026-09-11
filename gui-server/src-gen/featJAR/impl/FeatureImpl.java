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

import featJAR.Cardinality;
import featJAR.FeatJARPackage;
import featJAR.Feature;
import featJAR.GroupNode;
import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Feature</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link featJAR.impl.FeatureImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link featJAR.impl.FeatureImpl#getCardinality <em>Cardinality</em>}</li>
 *   <li>{@link featJAR.impl.FeatureImpl#getGroupNodeList <em>Group Node List</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FeatureImpl extends IdentifiableImpl implements Feature {
    /**
     * The cached value of the '{@link #getCardinality() <em>Cardinality</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getCardinality()
     * @generated
     * @ordered
     */
    protected Cardinality cardinality;

    /**
     * The cached value of the '{@link #getGroupNodeList() <em>Group Node List</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGroupNodeList()
     * @generated
     * @ordered
     */
    protected EList<GroupNode> groupNodeList;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected FeatureImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return FeatJARPackage.Literals.FEATURE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public GroupNode getParent() {
        if (eContainerFeatureID() != FeatJARPackage.FEATURE__PARENT) return null;
        return (GroupNode) eInternalContainer();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetParent(GroupNode newParent, NotificationChain msgs) {
        msgs = eBasicSetContainer((InternalEObject) newParent, FeatJARPackage.FEATURE__PARENT, msgs);
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void setParent(GroupNode newParent) {
        if (newParent != eInternalContainer()
                || (eContainerFeatureID() != FeatJARPackage.FEATURE__PARENT && newParent != null)) {
            if (EcoreUtil.isAncestor(this, newParent))
                throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
            NotificationChain msgs = null;
            if (eInternalContainer() != null) msgs = eBasicRemoveFromContainer(msgs);
            if (newParent != null)
                msgs = ((InternalEObject) newParent)
                        .eInverseAdd(this, FeatJARPackage.GROUP_NODE__FEATURE_LIST, GroupNode.class, msgs);
            msgs = basicSetParent(newParent, msgs);
            if (msgs != null) msgs.dispatch();
        } else if (eNotificationRequired())
            eNotify(new ENotificationImpl(
                    this, Notification.SET, FeatJARPackage.FEATURE__PARENT, newParent, newParent));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Cardinality getCardinality() {
        return cardinality;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetCardinality(Cardinality newCardinality, NotificationChain msgs) {
        Cardinality oldCardinality = cardinality;
        cardinality = newCardinality;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(
                    this, Notification.SET, FeatJARPackage.FEATURE__CARDINALITY, oldCardinality, newCardinality);
            if (msgs == null) msgs = notification;
            else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void setCardinality(Cardinality newCardinality) {
        if (newCardinality != cardinality) {
            NotificationChain msgs = null;
            if (cardinality != null)
                msgs = ((InternalEObject) cardinality)
                        .eInverseRemove(this, EOPPOSITE_FEATURE_BASE - FeatJARPackage.FEATURE__CARDINALITY, null, msgs);
            if (newCardinality != null)
                msgs = ((InternalEObject) newCardinality)
                        .eInverseAdd(this, EOPPOSITE_FEATURE_BASE - FeatJARPackage.FEATURE__CARDINALITY, null, msgs);
            msgs = basicSetCardinality(newCardinality, msgs);
            if (msgs != null) msgs.dispatch();
        } else if (eNotificationRequired())
            eNotify(new ENotificationImpl(
                    this, Notification.SET, FeatJARPackage.FEATURE__CARDINALITY, newCardinality, newCardinality));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EList<GroupNode> getGroupNodeList() {
        if (groupNodeList == null) {
            groupNodeList = new EObjectContainmentWithInverseEList<GroupNode>(
                    GroupNode.class, this, FeatJARPackage.FEATURE__GROUP_NODE_LIST, FeatJARPackage.GROUP_NODE__PARENT);
        }
        return groupNodeList;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case FeatJARPackage.FEATURE__PARENT:
                if (eInternalContainer() != null) msgs = eBasicRemoveFromContainer(msgs);
                return basicSetParent((GroupNode) otherEnd, msgs);
            case FeatJARPackage.FEATURE__GROUP_NODE_LIST:
                return ((InternalEList<InternalEObject>) (InternalEList<?>) getGroupNodeList())
                        .basicAdd(otherEnd, msgs);
        }
        return super.eInverseAdd(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case FeatJARPackage.FEATURE__PARENT:
                return basicSetParent(null, msgs);
            case FeatJARPackage.FEATURE__CARDINALITY:
                return basicSetCardinality(null, msgs);
            case FeatJARPackage.FEATURE__GROUP_NODE_LIST:
                return ((InternalEList<?>) getGroupNodeList()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
        switch (eContainerFeatureID()) {
            case FeatJARPackage.FEATURE__PARENT:
                return eInternalContainer()
                        .eInverseRemove(this, FeatJARPackage.GROUP_NODE__FEATURE_LIST, GroupNode.class, msgs);
        }
        return super.eBasicRemoveFromContainerFeature(msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case FeatJARPackage.FEATURE__PARENT:
                return getParent();
            case FeatJARPackage.FEATURE__CARDINALITY:
                return getCardinality();
            case FeatJARPackage.FEATURE__GROUP_NODE_LIST:
                return getGroupNodeList();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case FeatJARPackage.FEATURE__PARENT:
                setParent((GroupNode) newValue);
                return;
            case FeatJARPackage.FEATURE__CARDINALITY:
                setCardinality((Cardinality) newValue);
                return;
            case FeatJARPackage.FEATURE__GROUP_NODE_LIST:
                getGroupNodeList().clear();
                getGroupNodeList().addAll((Collection<? extends GroupNode>) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case FeatJARPackage.FEATURE__PARENT:
                setParent((GroupNode) null);
                return;
            case FeatJARPackage.FEATURE__CARDINALITY:
                setCardinality((Cardinality) null);
                return;
            case FeatJARPackage.FEATURE__GROUP_NODE_LIST:
                getGroupNodeList().clear();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case FeatJARPackage.FEATURE__PARENT:
                return getParent() != null;
            case FeatJARPackage.FEATURE__CARDINALITY:
                return cardinality != null;
            case FeatJARPackage.FEATURE__GROUP_NODE_LIST:
                return groupNodeList != null && !groupNodeList.isEmpty();
        }
        return super.eIsSet(featureID);
    }
} // FeatureImpl
