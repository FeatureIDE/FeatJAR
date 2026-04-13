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
package de.featjar.base.tree.structure;

import java.util.Collections;
import java.util.List;

/**
 * A leaf node of a tree.
 * A leaf does not have any children.
 * Nonetheless, it captures a children type, such that it can be added as a child to a non-leaf node.
 *
 * @param <T> the type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 */
public abstract class ALeafNode<T extends ITree<T>> implements ITree<T> {

    /**
     * {@return an empty list of children}
     */
    @Override
    public List<? extends T> getChildren() {
        return Collections.emptyList();
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     *
     * @param children ignored
     * @throws UnsupportedOperationException when called
     */
    @Override
    public void setChildren(List<? extends T> children) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return whether this node is equal to another}
     *
     * @param other the other node
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        return this == other || (other != null && getClass() == other.getClass() && equalsTree((T) other));
    }

    @Override
    public int hashCode() {
        return hashCodeNode();
    }
}
