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

/**
 * Counts the maximum depth of a tree.
 * Can be passed a class up to which should be counted (e.g., to exclude details in a tree).
 *
 * @author Sebastian Krieter
 */
public class TreeDepthCounter implements ITreeVisitor<ITree<?>, Integer> {
    private Class<? extends ITree<?>> terminalClass = null;
    private int maxDepth = 0;

    public Class<? extends ITree<?>> getTerminalClass() {
        return terminalClass;
    }

    public void setTerminalClass(Class<? extends ITree<?>> terminalClass) {
        this.terminalClass = terminalClass;
    }

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final int depth = path.size();
        if (maxDepth < depth) {
            maxDepth = depth;
        }
        final ITree<?> node = ITreeVisitor.getCurrentNode(path);
        if ((terminalClass != null) && terminalClass.isInstance(node)) {
            return TraversalAction.SKIP_CHILDREN;
        } else {
            return TraversalAction.CONTINUE;
        }
    }

    @Override
    public void reset() {
        maxDepth = 0;
    }

    @Override
    public Result<Integer> getResult() {
        return Result.of(maxDepth);
    }
}
