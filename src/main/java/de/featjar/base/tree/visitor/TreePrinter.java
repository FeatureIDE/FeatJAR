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

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Prints a tree as a string.
 * Useful for debugging.
 * Implemented as a preorder traversal.
 *
 * @author Sebastian Krieter
 */
public class TreePrinter implements ITreeVisitor<ITree<?>, String> {
    private final StringBuilder treeStringBuilder = new StringBuilder();
    private String indentation = "  ";
    private Predicate<ITree<?>> filter = null;
    private Function<ITree<?>, String> toStringFunction = Object::toString;

    public String getIndentation() {
        return indentation;
    }

    public Predicate<ITree<?>> getFilter() {
        return filter;
    }

    public Function<ITree<?>, String> getToStringFunction() {
        return toStringFunction;
    }

    public TreePrinter setIndentation(String indentation) {
        this.indentation = indentation;
        return this;
    }

    public TreePrinter setFilter(Predicate<ITree<?>> filter) {
        this.filter = filter;
        return this;
    }

    public TreePrinter setToStringFunction(Function<ITree<?>, String> toStringFunction) {
        this.toStringFunction = toStringFunction;
        return this;
    }

    @Override
    public void reset() {
        treeStringBuilder.delete(0, treeStringBuilder.length());
    }

    @Override
    public Result<String> getResult() {
        return Result.of(treeStringBuilder.toString());
    }

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final ITree<?> currentNode = ITreeVisitor.getCurrentNode(path);
        if ((filter == null) || filter.test(currentNode)) {
            try {
                treeStringBuilder.append(String.valueOf(indentation).repeat(Math.max(0, path.size() - 1)));
                treeStringBuilder.append(toStringFunction.apply(currentNode));
                treeStringBuilder.append('\n');
            } catch (final Exception e) {
                return TraversalAction.SKIP_ALL;
            }
        }
        return TraversalAction.CONTINUE;
    }
}
