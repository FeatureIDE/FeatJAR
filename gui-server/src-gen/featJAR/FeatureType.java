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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration
 * '<em><b>Feature Type</b></em>', and utility methods for working with them.
 * <!-- end-user-doc -->
 *
 * @see featJAR.FeatJARPackage#getNodeType()
 * @model
 * @generated
 */
public enum FeatureType implements Enumerator {
    /**
     * The '<em><b>HIDDEN</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #HIDDEN_VALUE
     * @generated
     * @ordered
     */
    HIDDEN(0, "HIDDEN", "HIDDEN"),

    /**
     * The '<em><b>ABSTRACT</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #ABSTRACT_VALUE
     * @generated
     * @ordered
     */
    ABSTRACT(1, "ABSTRACT", "ABSTRACT"),

    /**
     * The '<em><b>CONCRETE</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #CONCRETE_VALUE
     * @generated
     * @ordered
     */
    CONCRETE(2, "CONCRETE", "CONCRETE");

    /**
     * The '<em><b>HIDDEN</b></em>' literal value. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #HIDDEN
     * @model
     * @generated
     * @ordered
     */
    public static final int HIDDEN_VALUE = 0;

    /**
     * The '<em><b>ABSTRACT</b></em>' literal value. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #ABSTRACT
     * @model
     * @generated
     * @ordered
     */
    public static final int ABSTRACT_VALUE = 1;

    /**
     * The '<em><b>CONCRETE</b></em>' literal value. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #CONCRETE
     * @model
     * @generated
     * @ordered
     */
    public static final int CONCRETE_VALUE = 2;

    /**
     * An array of all the '<em><b>Feature Type</b></em>' enumerators. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private static final FeatureType[] VALUES_ARRAY = new FeatureType[] {
        HIDDEN, ABSTRACT, CONCRETE,
    };

    /**
     * A public read-only list of all the '<em><b>Feature Type</b></em>'
     * enumerators. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static final List<FeatureType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the '<em><b>Feature Type</b></em>' literal with the specified literal
     * value. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param literal the literal.
     * @return the matching enumerator or <code>null</code>.
     * @generated
     */
    public static FeatureType get(String literal) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            FeatureType result = VALUES_ARRAY[i];
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Feature Type</b></em>' literal with the specified name.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param name the name.
     * @return the matching enumerator or <code>null</code>.
     * @generated
     */
    public static FeatureType getByName(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            FeatureType result = VALUES_ARRAY[i];
            if (result.getName().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Feature Type</b></em>' literal with the specified integer
     * value. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the integer value.
     * @return the matching enumerator or <code>null</code>.
     * @generated
     */
    public static FeatureType get(int value) {
        switch (value) {
            case HIDDEN_VALUE:
                return HIDDEN;
            case ABSTRACT_VALUE:
                return ABSTRACT;
            case CONCRETE_VALUE:
                return CONCRETE;
        }
        return null;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private final int value;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private final String name;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private final String literal;

    /**
     * Only this class can construct instances. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    private FeatureType(int value, String name, String literal) {
        this.value = value;
        this.name = name;
        this.literal = literal;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getLiteral() {
        return literal;
    }

    /**
     * Returns the literal value of the enumerator, which is its string
     * representation. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String toString() {
        return literal;
    }
} // FeatureType
