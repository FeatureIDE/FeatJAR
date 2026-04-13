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

import java.util.Arrays;
import java.util.Objects;

/**
 * A tree of nodes labeled with some data.
 * Can be used, for example, to represent a tree of integers or strings.
 *
 * @param <T> the type of label
 * @author Sebastian Krieter
 */
public class LabeledTree<T> extends ATree<LabeledTree<T>> {
    protected T label;

    public LabeledTree() {
        super();
    }

    public LabeledTree(int childrenCount) {
        super(childrenCount);
    }

    public LabeledTree(T label) {
        super();
        this.label = label;
    }

    public LabeledTree(T label, int childrenCount) {
        super(childrenCount);
        this.label = label;
    }

    @SafeVarargs
    public static <T> LabeledTree<T> of(T label, LabeledTree<T>... children) {
        LabeledTree<T> labeledTree = new LabeledTree<>(label, children.length);
        labeledTree.setChildren(Arrays.asList(children));
        return labeledTree;
    }

    public T getLabel() {
        return label;
    }

    public void setLabel(T label) {
        this.label = label;
    }

    @Override
    public LabeledTree<T> cloneNode() {
        return new LabeledTree<>(label);
    }

    @Override
    public boolean equalsNode(LabeledTree<T> other) {
        return Objects.equals(getLabel(), other.getLabel());
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(getLabel());
    }

    @Override
    public String toString() {
        return String.format("LabeledTree[%s]", label);
    }
}
