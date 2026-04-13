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
package de.featjar.base.data;

/**
 * A non-null unit value.
 * Can be used to distinguish an erroneous empty result (i.e., {@link Result#empty(Problem...)})
 * from an intended empty result (i.e., {@link Result#ofVoid(Problem...)}).
 * This is useful to return a {@link Result} from a method that should return {@code void}.
 * This is necessary because Java only has the null unit value {@link java.lang.Void}.
 *
 * @author Elias Kuiter
 */
public class Void {
    /**
     * Singleton instance of Void.
     */
    protected static final de.featjar.base.data.Void VOID = new de.featjar.base.data.Void();
}
