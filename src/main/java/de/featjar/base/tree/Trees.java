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
package de.featjar.base.tree;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.tree.structure.ITree;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.base.tree.visitor.ITreeVisitor.TraversalAction;
import de.featjar.base.tree.visitor.TreePrinter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Traverses and manipulates trees. Most methods available in {@link Trees} are
 * also available on {@link ITree} instances.
 *
 * @author Sebastian Krieter
 */
public class Trees {

    /**
     * Thrown when a visitor requests the {@link de.featjar.base.tree.visitor.ITreeVisitor.TraversalAction#FAIL}
     * action.
     */
    public static class VisitorFailException extends Exception {
        private static final long serialVersionUID = -3736018981484477491L;

        private final List<Problem> problems;

        public VisitorFailException(Problem... problems) {
            this(List.of(problems));
        }

        public VisitorFailException(List<Problem> problems) {
            this.problems = List.copyOf(problems);
        }

        public List<Problem> getProblems() {
            return problems;
        }
    }

    /**
     * Traverses a tree using depth-first search, allowing for pre-, in-, and post-order
     * traversal.
     *
     * @param node    the starting node of the tree
     * @param visitor the visitor
     * @return the optional result from the visitor
     * @param <R> the type of result
     * @param <T> the type of tree
     */
    public static <R, T extends ITree<?>> Result<R> traverse(T node, ITreeVisitor<T, R> visitor) {
        visitor.reset();
        try {
            if (visitor.isInorder()) {
                depthFirstSearchInorder(node, visitor);
            } else {
                depthFirstSearch(node, visitor);
            }
            return visitor.getResult();
        } catch (final VisitorFailException e) {
            return Result.empty(e.problems);
        }
    }

    /**
     * Creates a preorder stream of the descendents of a tree. Is more efficient
     * than {@link #traverse(ITree, ITreeVisitor)}, but lacks support for
     * {@link TraversalAction}.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    public static <T extends ITree<T>> Stream<T> preOrderStream(T node) {
        return StreamSupport.stream(new PreOrderSpliterator<>(node), false);
    }

    /**
     * Creates a postorder stream of the descendents of a tree. Is more efficient
     * than {@link #traverse(ITree, ITreeVisitor)}, but lacks support for
     * {@link TraversalAction}.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    public static <T extends ITree<T>> Stream<T> postOrderStream(T node) {
        return StreamSupport.stream(new PostOrderSpliterator<>(node), false);
    }

    /**
     * Creates an inner-order stream of the descendents of a tree. Is more efficient
     * than {@link #traverse(ITree, ITreeVisitor)}, but lacks support for
     * {@link TraversalAction}.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    public static <T extends ITree<T>> Stream<T> innerOrderStream(T node) {
        return StreamSupport.stream(new InnerOrderSpliterator<>(node), false);
    }

    /**
     * Creates a level-order stream of the descendents of a tree.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    public static <T extends ITree<T>> Stream<T> levelOrderStream(T node) {
        return StreamSupport.stream(new LevelOrderSpliterator<>(node), false);
    }

    /**
     * Creates a parallel stream of the descendants of a tree. Does not make any
     * guarantees regarding the order of the descendants.
     *
     * @param node the starting node of the tree
     * @return the stream
     * @param <T> the type of tree
     */
    // FIXME: does not work currently!
    public static <T extends ITree<T>> Stream<T> parallelStream(T node) {
        return StreamSupport.stream(new ParallelSpliterator<>(node), true);
    }

    /**
     * Tests whether two nodes (and their children) are equal.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @return whether the first node is deeply equal to the second node
     * @param <T> the type of tree
     */
    public static <T extends ITree<T>> boolean equals(T node1, T node2) {
        if (node1 == node2) {
            return true;
        }
        if ((node1 == null) || (node2 == null)) {
            return false;
        }
        final LinkedList<T> stack1 = new LinkedList<>();
        final LinkedList<T> stack2 = new LinkedList<>();
        stack1.push(node1);
        stack2.push(node2);
        while (!stack1.isEmpty()) {
            final T currentNode1 = stack1.pop();
            final T currentNode2 = stack2.pop();

            if (currentNode1 != currentNode2) {
                if ((currentNode1 == null) || (currentNode2 == null)) {
                    return false;
                } else {
                    if (currentNode1.getChildrenCount() != currentNode2.getChildrenCount()
                            || !currentNode1.equalsNode(currentNode2)) {
                        return false;
                    }
                    stack1.addAll(0, currentNode1.getChildren());
                    stack2.addAll(0, currentNode2.getChildren());
                }
            }
        }
        return true;
    }

    /**
     * Clones a node (and its children).
     *
     * @param root the node
     * @return a deep clone of the node
     * @param <T> the type of tree
     */
    @SuppressWarnings("unchecked")
    public static <T extends ITree<T>, R extends T> R clone(R root) {
        if (root == null) {
            return null;
        }

        final ArrayList<T> path = new ArrayList<>();
        final LinkedList<StackEntry<T>> stack = new LinkedList<>();
        stack.push(new StackEntry<>(root));

        while (!stack.isEmpty()) {
            final StackEntry<T> entry = stack.peek();
            final T node = entry.node;
            if (entry.remainingChildren == null) {
                path.add((T) node.cloneNode());
                entry.remainingChildren = new LinkedList<>(node.getChildren());
            }
            if (!entry.remainingChildren.isEmpty()) {
                stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
            } else {
                final int childrenCount = node.getChildrenCount();
                if (childrenCount > 0) {
                    final List<T> subList = path.subList(path.size() - childrenCount, path.size());
                    path.get(path.size() - (childrenCount + 1)).setChildren(subList);
                    subList.clear();
                }
                stack.pop();
            }
        }
        return (R) path.get(0);
    }

    /**
     * Sorts a node (and its children).
     *
     * @param root the node
     * @param <T>  the type of tree
     */
    public static <T extends ITree<T>> void sort(T root) {
        sort(root, Comparator.comparing(T::toString));
    }

    /**
     * Sorts a node (and its children).
     *
     * @param root       the node
     * @param comparator comparator used for sorting
     * @param <T>        the type of tree
     */
    public static <T extends ITree<T>> void sort(T root, Comparator<T> comparator) {
        final LinkedList<StackEntry<T>> stack = new LinkedList<>();
        stack.push(new StackEntry<>(root));

        while (!stack.isEmpty()) {
            final StackEntry<T> entry = stack.peek();
            final T node = entry.node;
            if (entry.remainingChildren == null) {
                entry.remainingChildren = new LinkedList<>(node.getChildren());
            }
            if (!entry.remainingChildren.isEmpty()) {
                stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
            } else {
                if (node.hasChildren()) {
                    final ArrayList<T> children = new ArrayList<>(node.getChildren());
                    children.sort(comparator);
                    node.setChildren(children);
                }
                stack.pop();
            }
        }
    }

    /**
     * {@return a human-readable string of the given tree}
     * Default print method for trees, see {@link TreePrinter} for more options.
     * @param root the tree to print
     */
    public static String print(ITree<?> root) {
        return Trees.traverse(root, new TreePrinter()).get();
    }

    private static class StackEntry<T> {
        private final T node;
        private List<T> remainingChildren;

        public StackEntry(T node) {
            this.node = node;
        }
    }

    private static class PreOrderSpliterator<T extends ITree<T>> implements Spliterator<T> {

        final LinkedList<T> stack = new LinkedList<>();

        public PreOrderSpliterator(T node) {
            if (node != null) {
                stack.addFirst(node);
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (stack.isEmpty()) {
                return false;
            } else {
                final T node = stack.removeFirst();
                consumer.accept(node);
                stack.addAll(0, node.getChildren());
                return true;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }
    }

    private static class PostOrderSpliterator<T extends ITree<T>> implements Spliterator<T> {

        final LinkedList<StackEntry<T>> stack = new LinkedList<>();

        public PostOrderSpliterator(T node) {
            if (node != null) {
                stack.push(new StackEntry<>(node));
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (stack.isEmpty()) {
                return false;
            }
            while (!stack.isEmpty()) {
                final StackEntry<T> entry = stack.peek();
                if (entry.remainingChildren == null) {
                    entry.remainingChildren = new LinkedList<>(entry.node.getChildren());
                }
                if (!entry.remainingChildren.isEmpty()) {
                    stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
                } else {
                    consumer.accept(entry.node);
                    stack.pop();
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }
    }

    private static class InnerOrderSpliterator<T extends ITree<T>> implements Spliterator<T> {

        final LinkedList<StackEntry<T>> stack = new LinkedList<>();

        public InnerOrderSpliterator(T node) {
            if (node != null) {
                stack.push(new StackEntry<>(node));
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (stack.isEmpty()) {
                return false;
            }
            while (!stack.isEmpty()) {
                final StackEntry<T> entry = stack.peek();
                if (entry.remainingChildren == null) {
                    final List<? extends T> children = entry.node.getChildren();
                    if (children.isEmpty()) {
                        consumer.accept(entry.node);
                        stack.pop();
                        return true;
                    } else if (children.size() == 1) {
                        consumer.accept(entry.node);
                        stack.pop();
                        entry.remainingChildren = new LinkedList<>(children);
                        stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
                        return true;
                    } else {
                        entry.remainingChildren = new LinkedList<>(children);
                        stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
                    }
                } else {
                    if (entry.remainingChildren.isEmpty()) {
                        stack.pop();
                    } else {
                        consumer.accept(entry.node);
                        stack.push(new StackEntry<>(entry.remainingChildren.remove(0)));
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }
    }

    private static class LevelOrderSpliterator<T extends ITree<T>> implements Spliterator<T> {

        final LinkedList<T> queue = new LinkedList<>();

        public LevelOrderSpliterator(T node) {
            if (node != null) {
                queue.addFirst(node);
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (queue.isEmpty()) {
                return false;
            } else {
                final T node = queue.removeFirst();
                consumer.accept(node);
                queue.addAll(node.getChildren());
                return true;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }
    }

    private static class ParallelSpliterator<T extends ITree<T>> implements Spliterator<T> {

        final LinkedList<T> stack = new LinkedList<>();

        public ParallelSpliterator(T node) {
            if (node != null) {
                stack.push(node);
            }
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            if (stack.isEmpty()) {
                return false;
            } else {
                final T node = stack.pop();
                consumer.accept(node);
                stack.addAll(0, node.getChildren());
                return true;
            }
        }

        @Override
        public Spliterator<T> trySplit() {
            if (!stack.isEmpty()) {
                return new ParallelSpliterator<>(stack.pop());
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends ITree<?>> void depthFirstSearchInorder(T node, ITreeVisitor<T, ?> visitor)
            throws VisitorFailException {
        if (node == null) {
            return;
        }
        final ArrayList<T> path = new ArrayList<>();

        final ArrayDeque<StackEntry<T>> stack = new ArrayDeque<>();
        stack.addLast(new StackEntry<>(node));
        while (!stack.isEmpty()) {
            final StackEntry<T> entry = stack.getLast();
            if (entry.remainingChildren == null) {
                path.add(entry.node);
                Result<Void> problem = visitor.nodeValidator(path);
                if (problem.hasProblems()) throw new VisitorFailException(problem.getProblems());
                final TraversalAction traversalAction = visitor.firstVisit(path);
                switch (traversalAction) {
                    case CONTINUE:
                        entry.remainingChildren = new LinkedList<>((Collection<? extends T>) entry.node.getChildren());
                        break;
                    case SKIP_CHILDREN:
                        entry.remainingChildren = Collections.emptyList();
                        break;
                    case SKIP_ALL:
                        return;
                    case FAIL:
                        throw new VisitorFailException(new Problem("visitor failed", Problem.Severity.ERROR));
                    default:
                        throw new IllegalStateException(String.valueOf(traversalAction));
                }
                if (!entry.remainingChildren.isEmpty()) {
                    stack.addLast(new StackEntry<>(entry.remainingChildren.remove(0)));
                }
            } else if (!entry.remainingChildren.isEmpty()) {
                stack.addLast(new StackEntry<>(entry.remainingChildren.remove(0)));
                final TraversalAction traversalAction = visitor.visit(path);
                switch (traversalAction) {
                    case CONTINUE:
                        break;
                    case SKIP_CHILDREN:
                        stack.removeLast();
                        path.remove(path.size() - 1);
                        continue;
                    case SKIP_ALL:
                        return;
                    case FAIL:
                        throw new VisitorFailException(new Problem("visitor failed", Problem.Severity.ERROR));
                    default:
                        throw new IllegalStateException(String.valueOf(traversalAction));
                }
            } else {
                final TraversalAction traversalAction = visitor.lastVisit(path);
                switch (traversalAction) {
                    case CONTINUE:
                    case SKIP_CHILDREN:
                        break;
                    case SKIP_ALL:
                        return;
                    case FAIL:
                        throw new VisitorFailException(new Problem("visitor failed", Problem.Severity.ERROR));
                    default:
                        throw new IllegalStateException(String.valueOf(traversalAction));
                }
                stack.removeLast();
                path.remove(path.size() - 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends ITree<?>> void depthFirstSearch(T node, ITreeVisitor<T, ?> visitor)
            throws VisitorFailException {
        if (node != null) {
            final ArrayList<T> path = new ArrayList<>();

            final ArrayDeque<T> stack = new ArrayDeque<>();
            stack.addLast(node);
            while (!stack.isEmpty()) {
                final T curNode = stack.getLast();
                if (path.isEmpty() || (curNode != path.get(path.size() - 1))) {
                    path.add(curNode);
                    Result<Void> problem = visitor.nodeValidator(path);
                    if (problem.hasProblems()) throw new VisitorFailException(problem.getProblems());
                    final TraversalAction traversalAction = visitor.firstVisit(path);
                    switch (traversalAction) {
                        case CONTINUE:
                            final Collection<? extends T> children = (Collection<? extends T>) curNode.getChildren();
                            children.forEach(stack::addFirst);
                            children.forEach(c -> stack.addLast(stack.removeFirst()));
                            break;
                        case SKIP_CHILDREN:
                            break;
                        case SKIP_ALL:
                            return;
                        case FAIL:
                            throw new VisitorFailException(new Problem("visitor failed", Problem.Severity.ERROR));
                        default:
                            throw new IllegalStateException(String.valueOf(traversalAction));
                    }
                } else {
                    final TraversalAction traversalAction = visitor.lastVisit(path);
                    switch (traversalAction) {
                        case CONTINUE:
                        case SKIP_CHILDREN:
                            break;
                        case SKIP_ALL:
                            return;
                        case FAIL:
                            throw new VisitorFailException(new Problem("visitor failed", Problem.Severity.ERROR));
                        default:
                            throw new IllegalStateException(String.valueOf(traversalAction));
                    }
                    stack.removeLast();
                    path.remove(path.size() - 1);
                }
            }
        }
    }
}
