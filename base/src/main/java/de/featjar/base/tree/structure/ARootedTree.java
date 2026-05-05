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
import java.util.List;

/**
 * A tree of nodes, each of which has an optional parent.
 * Use this only if nodes need to know about their parents.
 * If possible, use {@link ATree} instead, which allows reusing subtrees.
 *
 * @param <T> the type of children, the implementing type must be castable to T
 * @author Elias Kuiter
 */
public abstract class ARootedTree<T extends IRootedTree<T>> extends ATree<T> implements IRootedTree<T> {
    /**
     * the parent node of this node
     */
    protected T parent = null;

    public ARootedTree() {
        super();
    }

    public ARootedTree(int childrenCount) {
        super(childrenCount);
    }

    @Override
    public Result<T> getParent() {
        return Result.ofNullable(parent);
    }

    @Override
    public void setParent(T newParent) {
        if (newParent == parent) {
            return;
        }
        parent = newParent;
    }

    /**
     * {@inheritDoc}
     * The old children are changed to have no parent node.
     * The new children are changed to have this node as parent node.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setChildren(List<? extends T> children) {
        for (final T child : getChildren()) {
            child.setParent(null);
        }
        super.setChildren(children);
        for (final T child : children) {
            child.setParent((T) this);
        }
    }

    /**
     * {@inheritDoc}
     * The new child is changed to have this node as parent node.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addChild(T newChild) {
        super.addChild(newChild);
        newChild.setParent((T) this);
    }

    /**
     * {@inheritDoc}
     * The new child is changed to have this node as parent node.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addChild(int index, T newChild) {
        super.addChild(index, newChild);
        newChild.setParent((T) this);
    }

    /**
     * {@inheritDoc}
     * The old child is changed to have no node.
     */
    @Override
    public void removeChild(T child) {
        super.removeChild(child);
        child.setParent(null);
    }

    /**
     * {@inheritDoc}
     * The old child is changed to have no node.
     */
    @Override
    public T removeChild(int index) {
        T child = super.removeChild(index);
        child.setParent(null);
        return child;
    }

    /**
     * {@inheritDoc}
     * The old child is changed to have no node.
     * The new child is changed to have this node as parent node.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean replaceChild(T oldChild, T newChild) {
        boolean modified = super.replaceChild(oldChild, newChild);
        if (modified) {
            oldChild.setParent(null);
            newChild.setParent((T) this);
        }
        return modified;
    }
}
