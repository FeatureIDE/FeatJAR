/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.assignment;

public class ValuedBooleanAssignment extends BooleanAssignment {
    private static final long serialVersionUID = -6484298506539342496L;

    private long value;

    public ValuedBooleanAssignment(BooleanAssignment booleanAssignment) {
        super(booleanAssignment);
        this.value = 0;
    }

    public ValuedBooleanAssignment(BooleanAssignment booleanAssignment, long value) {
        super(booleanAssignment);
        this.value = value;
    }

    public ValuedBooleanAssignment(int... elements) {
        super(elements);
        this.value = 0;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void addToValue(long delta) {
        value = Math.addExact(value, delta);
    }

    public long getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
