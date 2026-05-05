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

import de.featjar.base.data.Result;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface IRootedTree<T extends IRootedTree<T>> extends ITree<T> {
    /**
     * {@return the parent node of this node, if any}
     */
    // TODO should be an Optional
    Result<T> getParent();

    /**
     * {@return a stream of all (transitive) parents of this node up to the root of the tree, starting with the parent of this node}
     */
    @SuppressWarnings("unchecked")
    default Stream<T> parentStream() {
        return StreamSupport.stream(new ParentIterator<>((T) this, false), false);
    }

    /**
     * {@return a stream of all (transitive) parents of this node up to the root of the tree, starting with this node}
     */
    @SuppressWarnings("unchecked")
    default Stream<T> pathToRoot() {
        return StreamSupport.stream(new ParentIterator<>((T) this, true), false);
    }

    /**
     * Sets the parent node of this node.
     *
     * @param newParent the new parent node
     */
    void setParent(T newParent);

    /**
     * {@return whether this node has a parent node}
     */
    default boolean hasParent() {
        return getParent().isPresent();
    }

    /**
     * {@return whether the given node is an ancestor of this node}
     *
     * @param node the node
     */
    default boolean isAncestor(IRootedTree<T> node) {
        Result<T> currentParent = getParent();
        while (currentParent.isPresent()) {
            if (node == currentParent.get()) {
                return true;
            }
            currentParent = currentParent.get().getParent();
        }
        return false;
    }

    /**
     * {@return the root node of this tree}
     */
    @SuppressWarnings("unchecked")
    default T getRoot() {
        T currentTree = (T) this;
        while (currentTree.getParent().isPresent()) {
            currentTree = currentTree.getParent().get();
        }
        return currentTree;
    }

    /**
     * {@return the index of this node in its parent's list of children, if any}
     */
    @SuppressWarnings("unchecked")
    default Result<Integer> getIndex() {
        return getParent().mapResult(parent -> parent.getChildIndex((T) this));
    }
}
