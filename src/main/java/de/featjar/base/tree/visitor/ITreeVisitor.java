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
package de.featjar.base.tree.visitor;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.structure.ITree;
import java.util.List;
import java.util.function.Function;

/**
 * Visits each node of a tree in a depth-first search.
 * The actual traversal algorithm is {@link Trees#traverse(ITree, ITreeVisitor)}.
 *
 * @param <T> the type of tree
 * @param <U> the type of result
 * @author Sebastian Krieter
 */
public interface ITreeVisitor<T extends ITree<?>, U> {
    /**
     * All possible actions a traversal can take after visiting a tree node.
     */
    static enum TraversalAction {
        /**
         * Continue normally.
         * That is, traverse all children of the visited node.
         */
        CONTINUE,
        /**
         * Skip all children of the visited node.
         */
        SKIP_CHILDREN,
        /**
         * Skip all nodes left to be visited.
         * That is, stop the traversal, but still return a result, if already determined.
         */
        SKIP_ALL,
        /**
         * Signal that the traversal has failed.
         * That is, stop the traversal and do not return a result.
         */
        FAIL
    }

    /**
     * {@return the currently visited node}
     *
     * @param path the current traversal path, guaranteed to contain at least one node
     * @param <T> the type of node
     */
    static <T> T getCurrentNode(List<T> path) {
        return path.get(path.size() - 1);
    }

    /**
     * {@return the parent of the currently visited node}
     *
     * @param path the current traversal path, guaranteed to contain at least one node
     * @param <T> the type of node
     */
    static <T> Result<T> getParentNode(List<T> path) {
        return (path.size() > 1) ? Result.of(path.get(path.size() - 2)) : Result.empty();
    }

    /**
     * {@return a condition that is true if visiting the root implies it satisfying a given predicate}
     *
     * @param path the path to the visited node
     * @param predicate the predicate
     * @param message the message shown if the condition is {@code false}
     * @param <T> the type of node
     */
    static <T> Result<Void> rootValidator(List<T> path, Function<T, Boolean> predicate, String message) {
        return path.size() != 1 || predicate.apply(path.get(0))
                ? Result.ofVoid()
                : Result.ofVoid(new Problem(message, Problem.Severity.ERROR));
    }

    /**
     * {@return a problem with the the node about to be visited, if any}
     * If a problem is returned, the traversal algorithm will fail.
     *
     * @param path the path to the visited node
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default Result<Void> nodeValidator(List<T> path) {
        return Result.ofVoid();
    }

    /**
     * Visit a node for the first time.
     * Override this to implement preorder traversal.
     *
     * @param path the path to the visited node
     * @return the action the traversal algorithm must take next
     */
    default TraversalAction firstVisit(List<T> path) {
        return TraversalAction.CONTINUE;
    }

    /**
     * Visit a node in between the visits of its children.
     * Override this to implement inorder traversal.
     *
     * @param path the path to the visited node
     * @return the action the traversal algorithm must take next
     */
    default TraversalAction visit(List<T> path) {
        throw new UnsupportedOperationException();
    }

    default boolean isInorder() {
        return false;
    }

    /**
     * Visit a node for the last time.
     * Override this to implement postorder traversal.
     *
     * @param path the path to the visited node
     * @return the action the traversal algorithm must take next
     */
    default TraversalAction lastVisit(List<T> path) {
        return TraversalAction.CONTINUE;
    }

    /**
     * Resets any internal state of this tree visitor.
     * Should be overridden to allow for reusing this tree visitor instance.
     */
    default void reset() {}

    /**
     * {@return the result of the traversal, if any}
     */
    default Result<U> getResult() {
        return Result.empty();
    }
}
