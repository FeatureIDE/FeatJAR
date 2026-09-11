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

import featJAR.Cardinality;
import featJAR.FeatJARFactory;

/**
 * Everything around cardinality check creation for nodes and features.
 */
public final class CardinalityUtils {

    public static final int OPEN = -1;

    public static Cardinality createCardinality(int lowerBound, int upperBound) {
        Cardinality cardinality = FeatJARFactory.eINSTANCE.createCardinality();
        cardinality.setLowerBound(lowerBound);
        cardinality.setUpperBound(upperBound);
        return cardinality;
    }

    public static Cardinality createAndCardinality() {
        return CardinalityUtils.createCardinality(0, OPEN);
    }

    public static Cardinality createOrCardinality() {
        return CardinalityUtils.createCardinality(1, OPEN);
    }

    public static Cardinality createXorCardinality() {
        return CardinalityUtils.createCardinality(1, 1);
    }

    public static boolean isAnd(Cardinality c) {
        return c.getLowerBound() == 0 && c.getUpperBound() == OPEN;
    }

    public static boolean isOr(Cardinality c) {
        return c.getLowerBound() == 1 && c.getUpperBound() == OPEN;
    }

    public static boolean isXor(Cardinality c) {
        return c.getLowerBound() == 1 && c.getUpperBound() == 1;
    }

    public static boolean isCardinality(Cardinality c) {
        return !isAnd(c) && !isOr(c) && !isXor(c);
    }

    public static boolean isOptional(Cardinality cardinality) {
        return cardinality.getLowerBound() == 0;
    }

    public static boolean isMandatory(Cardinality cardinality) {
        return cardinality.getLowerBound() >= 1;
    }

    public static boolean isMultiple(Cardinality cardinality) {
        return cardinality.getUpperBound() > 1;
    }
}
