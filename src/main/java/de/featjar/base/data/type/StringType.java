/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.data.type;

/**
 * Type for the Java String data type.
 *
 * @author Sebastian Krieter
 */
public class StringType implements Type<String> {

    public static final StringType INSTANCE = new StringType();

    private StringType() {}

    public String toString() {
        return "StringType";
    }

    @Override
    public String parse(String text) {
        return text;
    }

    @Override
    public Class<String> getClassType() {
        return String.class;
    }
}
