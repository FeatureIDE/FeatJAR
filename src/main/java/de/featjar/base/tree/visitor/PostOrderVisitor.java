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

import de.featjar.base.tree.Trees;
import de.featjar.base.tree.structure.ITree;
import java.util.List;
import java.util.function.Function;

/**
 * A convenience class that calls a function for each node in a tree in post-order.
 * The actual traversal algorithm is {@link Trees#traverse(ITree, ITreeVisitor)}.
 *
 * @param <T> the type of tree
 *
 * @author Sebastian Krieter
 */
public class PostOrderVisitor<T extends ITree<?>> implements ITreeVisitor<T, Void> {

    private Function<List<T>, TraversalAction> function;

    public Function<List<T>, TraversalAction> getFunction() {
        return function;
    }

    public void setFunction(Function<List<T>, TraversalAction> function) {
        this.function = function;
    }

    public PostOrderVisitor(Function<List<T>, TraversalAction> function) {
        this.function = function;
    }

    @Override
    public TraversalAction lastVisit(List<T> path) {
        return function.apply(path);
    }
}
