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

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Iterator over all parents of a given node.
 * @param <T> the type of tree
 */
public class ParentIterator<T extends IRootedTree<T>> implements Spliterator<T> {

    private T currentNode;

    /**
     * Constructs a new iterator instance.
     *
     * @param currentNode the start node
     * @param startWithCurrentNode if {@code true} the iterator will start with the given node, if {@code false} it will start with the given node's parent.
     */
    public ParentIterator(T currentNode, boolean startWithCurrentNode) {
        this.currentNode = currentNode == null
                ? null
                : startWithCurrentNode ? currentNode : currentNode.getParent().orElse(null);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (currentNode == null) {
            return false;
        }
        action.accept(currentNode);
        currentNode = currentNode.getParent().orElse(null);
        return currentNode != null;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL;
    }
}
