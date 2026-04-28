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

import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.base.env.IBrowsable;
import de.featjar.base.io.graphviz.GraphVizTreeFormat;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.base.tree.visitor.TreePrinter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A tree of nodes that can be traversed.
 * Nodes are defined recursively.
 * For an example usage, see {@link LabeledTree}.
 * It is not supported to store children of a type different from the implementing type.
 * For this use case, consider a multi-level tree, where a {@link ALeafNode} references another {@link ITree}.
 * The parentage of nodes is not specified, so a node may occur in several nodes.
 * Thus, it is possible to store any directed acyclic graph.
 * For a directed acyclic graph with at most one parent per node, use {@link ARootedTree}.
 * Note that most consumers of {@link ITree} assume it to be acyclic.
 *
 * @param <T> the type of children, the implementing type must be castable to T
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
@SuppressWarnings("unchecked")
public interface ITree<T extends ITree<T>> extends IBrowsable<GraphVizTreeFormat<T>> {
    /**
     * {@return the children of this node}
     * The returned list must not be modified.
     */
    List<? extends T> getChildren();

    /**
     * Sets the children of this node.
     *
     * @param children the new children
     */
    void setChildren(List<? extends T> children);

    /**
     * {@return whether this node has any children}
     */
    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    /**
     * {@return how many children this node has}
     */
    default int getChildrenCount() {
        return getChildren().size();
    }

    /**
     * {@return a range that specifies the minimum and maximum number of this node's children}
     * The range is guaranteed to be respected for all mutating operations based on {@link #setChildren(List)}.
     * To guarantee that the range is respected at all times, call {@link #setChildren(List)} in the constructor.
     */
    default Range getChildrenCountRange() {
        return Range.open();
    }

    /**
     * {@return a function that validates this node's children}
     */
    default Predicate<T> getChildValidator() {
        return Objects::nonNull;
    }

    default void assertChildrenCountInRange(int newChildrenCount, Range range) {
        if (!range.test(newChildrenCount))
            throw new IllegalArgumentException(
                    String.format("attempted to set %d children, but expected one in %s", newChildrenCount, range));
    }

    default void assertChildrenCountInRange(int newChildrenCount) {
        assertChildrenCountInRange(newChildrenCount, getChildrenCountRange());
    }

    default void assertChildValidator(List<? extends T> children) {
        if (!children.stream().allMatch(getChildValidator()))
            throw new IllegalArgumentException(String.format(
                    "child %s is invalid",
                    children.stream()
                            .filter(c -> !getChildValidator().test(c))
                            .findFirst()
                            .orElse(null)));
    }

    default void assertChildValidator(T child) {
        if (!getChildValidator().test(child)) throw new IllegalArgumentException("child did not pass validation");
    }

    /**
     * {@return the n-th child of this node, if any}
     *
     * @param idx the index
     */
    default Result<T> getChild(int idx) {
        if (idx < 0 || idx >= getChildrenCount()) return Result.empty();
        return Result.ofNullable(getChildren().get(idx));
    }

    /**
     * {@return the first child of this node, if any}
     */
    default Result<T> getFirstChild() {
        return getChild(0);
    }

    /**
     * {@return the last child of this node, if any}
     */
    default Result<T> getLastChild() {
        return getChild(getChildrenCount() - 1);
    }

    /**
     * {@return the index of the given node in the list of children, if any}
     *
     * @param node the node
     */
    default Result<Integer> getChildIndex(T node) {
        return Result.ofIndex(getChildren().indexOf(node));
    }

    /**
     * {@return whether the given node is a child of this node}
     *
     * @param child the node
     */
    default boolean hasChild(T child) {
        return getChildIndex(child).isPresent();
    }

    /**
     * Adds a new child at a given position.
     * If the position is out of bounds, add the new child as the last child.
     *
     * @param index the new position
     * @param newChild the new child
     */
    default void addChild(int index, T newChild) {
        assertChildrenCountInRange(getChildrenCount() + 1);
        assertChildValidator(newChild);
        List<T> newChildren = new ArrayList<>(getChildren());
        if (index > getChildrenCount()) {
            newChildren.add(newChild);
        } else {
            newChildren.add(index, newChild);
        }
        setChildren(newChildren);
    }

    /**
     * Adds a new child as the last child.
     *
     * @param newChild the new child
     */
    default void addChild(T newChild) {
        assertChildrenCountInRange(getChildrenCount() + 1);
        assertChildValidator(newChild);
        List<T> newChildren = new ArrayList<>(getChildren());
        newChildren.add(newChild);
        setChildren(newChildren);
    }

    /**
     * Removes a child.
     *
     * @param child the child to be removed
     * @throws NoSuchElementException if the given old node is not a child
     */
    default void removeChild(T child) {
        assertChildrenCountInRange(getChildrenCount() - 1);
        List<T> newChildren = new ArrayList<>(getChildren());
        if (!newChildren.remove(child)) {
            throw new NoSuchElementException();
        }
        setChildren(newChildren);
    }

    /**
     * Removes the child at a given position.
     *
     * @param index the position to be removed
     * @return the removed child
     * @throws IndexOutOfBoundsException if the given index is out of bounds
     */
    default T removeChild(int index) {
        assertChildrenCountInRange(getChildrenCount() - 1);
        List<T> newChildren = new ArrayList<>(getChildren());
        T t = newChildren.remove(index);
        setChildren(newChildren);
        return t;
    }

    /**
     * Removes all children.
     */
    default void clearChildren() {
        if (getChildrenCount() > 0) {
            assertChildrenCountInRange(0);
            setChildren(new ArrayList<>());
        }
    }

    /**
     * Replaces each child of this node with a new child.
     * If the {@code mapper} returns null or the old child, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child with its index onto a new child
     * @return whether any child was modified
     */
    default boolean replaceChildren(BiFunction<Integer, T, ? extends T> mapper) {
        Objects.requireNonNull(mapper);
        boolean modified = false;
        final List<? extends T> oldChildren = getChildren();
        if (!oldChildren.isEmpty()) {
            final List<T> newChildren = new ArrayList<>(oldChildren.size());
            for (int i = 0; i < oldChildren.size(); i++) {
                T child = oldChildren.get(i);
                final T replacement = mapper.apply(i, child);
                if (replacement != null && replacement != child) {
                    newChildren.add(replacement);
                    modified = true;
                } else {
                    newChildren.add(child);
                }
            }
            if (modified) {
                setChildren(newChildren);
            }
        }
        return modified;
    }

    /**
     * Replaces each child of this node with a new child.
     * If the {@code mapper} returns null or the old child, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child onto a new child
     * @return whether any child was modified
     */
    default boolean replaceChildren(Function<T, ? extends T> mapper) {
        return replaceChildren((index, child) -> mapper.apply(child));
    }

    /**
     * Replaces a child with a new child.
     * Does nothing if the old child was not found.
     *
     * @param oldChild the old child
     * @param newChild the new child
     * @return whether any child was modified
     */
    default boolean replaceChild(T oldChild, T newChild) {
        return replaceChildren(child -> child == oldChild ? newChild : null);
    }

    /**
     * Replaces a child at an index with a new child.
     * Does nothing if the index is out of bounds.
     *
     * @param idx      the index
     * @param newChild the new child
     * @return whether any child was modified
     */
    default boolean replaceChild(int idx, T newChild) {
        return replaceChildren((index, child) -> index == idx ? newChild : null);
    }

    /**
     * Replaces each child of this node with a list of new children.
     * If the {@code mapper} returns null, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child with its index onto a list of new children
     * @return whether any child was modified
     */
    default boolean flatReplaceChildren(BiFunction<Integer, T, List<? extends T>> mapper) {
        Objects.requireNonNull(mapper);
        boolean modified = false;
        final List<? extends T> oldChildren = getChildren();
        if (!oldChildren.isEmpty()) {
            final ArrayList<T> newChildren = new ArrayList<>(oldChildren.size());
            for (int i = 0; i < oldChildren.size(); i++) {
                T child = oldChildren.get(i);
                final List<? extends T> replacement = mapper.apply(i, child);
                if (replacement != null) {
                    newChildren.addAll(replacement);
                    modified = true;
                } else {
                    newChildren.add(child);
                }
            }
            if (modified) {
                setChildren(newChildren);
            }
        }
        return modified;
    }

    /**
     * Replaces each child of this node with a new child.
     * If the {@code mapper} returns null or the old child, the respective child is kept unchanged.
     * If no child is changed, the list of children is kept unchanged.
     *
     * @param mapper maps an old child onto a new child
     * @return whether any child was modified
     */
    default boolean flatReplaceChildren(Function<T, List<? extends T>> mapper) {
        return flatReplaceChildren((index, child) -> mapper.apply(child));
    }

    /**
     * Clones this node (not its children).
     * For deep cloning, use {@link #cloneTree()}.
     *
     * @return a shallow clone of this node
     */
    ITree<T> cloneNode();

    /**
     * Clones this node (and its children).
     * Relies on {@link #cloneNode()}.
     *
     * @return a deep clone of this node
     */
    default T cloneTree() {
        return Trees.clone((T) this);
    }

    /**
     * Tests whether two nodes (not their children) are equal.
     * For deep cloning, use {@link #equalsTree(ITree)}.
     *
     * @param other the other node
     * @return whether this node is shallowly equal to the other node
     */
    boolean equalsNode(T other);

    /**
     * Tests whether two nodes (and their children) are equal.
     * Relies on {@link #equalsNode(ITree)}.
     *
     * @param other the other node
     * @return whether this node is deeply equal to the other node
     */
    default boolean equalsTree(T other) {
        return Trees.equals((T) this, other);
    }

    /**
     * {@return the hash code of this node (not its children)}
     * For deep hash code calculation, use {@link #hashCodeTree()}.
     */
    int hashCodeNode();

    /**
     * {@return the hash code of this node (and its children)}
     * Relies on {@link #hashCodeNode()}.
     */
    default int hashCodeTree() {
        int hashCode = hashCodeNode();
        for (T child : getChildren()) {
            hashCode += (hashCode * 37) + child.hashCodeTree();
        }
        return hashCode;
    }

    /**
     * Traverses the tree using depth-first search, allowing for pre- and postorder traversal.
     * Only accepts tree visitors that operate on T.
     * For more general visitors, use {@link Trees#traverse(ITree, ITreeVisitor)} instead.
     *
     * @param treeVisitor the tree visitor
     * @param <R>         the type of result
     * @return the result from the visitor
     */
    default <R> Result<R> traverse(ITreeVisitor<T, R> treeVisitor) {
        return Trees.traverse((T) this, treeVisitor);
    }

    /**
     * {@return the tree printed as a string}
     */
    default String print() {
        return Trees.traverse(this, new TreePrinter()).orElse("");
    }

    /**
     * {@return a parallel stream of the descendants of this node}
     */
    default Stream<? extends T> parallelStream() {
        return Trees.parallelStream((T) this);
    }

    /**
     * {@return a preorder stream of the descendants of this node}
     */
    default Stream<? extends T> preOrderStream() {
        return Trees.preOrderStream((T) this);
    }

    /**
     * {@return a postorder stream of the descendants of this node}
     */
    default Stream<? extends T> postOrderStream() {
        return Trees.postOrderStream((T) this);
    }

    /**
     * {@return an inner-order stream of the descendants of this node}
     */
    default Stream<? extends T> innerOrderStream() {
        return Trees.innerOrderStream((T) this);
    }

    /**
     * {@return a lever-order stream of the descendants of this node}
     */
    default Stream<? extends T> levelOrderStream() {
        return Trees.levelOrderStream((T) this);
    }

    /**
     * {@return the descendants of this node in no particular order}
     */
    default LinkedHashSet<? extends T> getDescendants() {
        return parallelStream().collect(Sets.toSet());
    }

    /**
     * {@return a preorder list of the descendants of this node}
     */
    default List<? extends T> getDescendantsAsPreOrder() {
        return preOrderStream().collect(Collectors.toList());
    }

    /**
     * {@return a postorder list of the descendants of this node}
     */
    default List<? extends T> getDescendantsAsPostOrder() {
        return postOrderStream().collect(Collectors.toList());
    }

    /**
     * {@return a level-order list of the descendants of this node}
     */
    default List<? extends T> getDescendantsAsLevelOrder() {
        return levelOrderStream().collect(Collectors.toList());
    }

    /**
     * Sorts this node (and its children).
     */
    default void sort() {
        Trees.sort((T) this);
    }

    /**
     * Sorts this node (and its children).
     *
     * @param comparator comparator used for sorting
     */
    default void sort(Comparator<T> comparator) {
        Trees.sort((T) this, comparator);
    }

    @Override
    default Result<URI> getBrowseURI(GraphVizTreeFormat<T> argument) {
        return argument.serialize((T) this).mapResult(ITree::buildURI);
    }

    private static Result<URI> buildURI(String dot) {
        try {
            return Result.of(new URI("https", "edotor.net", "", "engine=dot", dot));
        } catch (URISyntaxException e) {
            return Result.empty(e);
        }
    }
}
