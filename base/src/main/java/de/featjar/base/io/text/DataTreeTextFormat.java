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
package de.featjar.base.io.text;

import de.featjar.base.data.Result;
import de.featjar.base.io.IDataTreeFormat;
import de.featjar.base.tree.DataTree;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.structure.ITree;
import de.featjar.base.tree.visitor.TreePrinter;

/**
 * An IFormat class that take an AnalysisTree as input and can serialize it into CSV String.
 * With the CSV having only four columns (AnalysisType, Name, Value, Class)
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 * @author Sebastian Krieter
 */
public class DataTreeTextFormat extends ATextFormat<DataTree<?>> implements IDataTreeFormat {

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> serialize(DataTree<?> dataTree) {
        return Trees.traverse(
                dataTree, new TreePrinter().setIndentation("| ").setToStringFunction(this::treeNodeToString));
    }

    private String treeNodeToString(ITree<?> node) {
        DataTree<?> dataNode = ((DataTree<?>) node);
        Object value = dataNode.getValue().orElse(null);
        return dataNode.getAttribute().getSimpleName() + (value == null ? "" : ": " + String.valueOf(value));
    }
}
