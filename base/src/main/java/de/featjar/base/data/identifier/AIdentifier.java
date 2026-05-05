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
package de.featjar.base.data.identifier;

import java.util.Objects;

/**
 * Primary implementation of {@link IIdentifier}.
 *
 * @author Elias Kuiter
 */
public abstract class AIdentifier implements IIdentifier {
    protected final IIdentifierFactory factory;

    protected AIdentifier(IIdentifierFactory factory) {
        Objects.requireNonNull(factory);
        this.factory = factory;
    }

    public IIdentifierFactory getFactory() {
        return factory;
    }

    public IIdentifier getNewIdentifier() {
        return factory.get();
    }

    @Override
    public String toString() {
        return String.valueOf(System.identityHashCode(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AIdentifier that = (AIdentifier) o;
        return toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }
}
