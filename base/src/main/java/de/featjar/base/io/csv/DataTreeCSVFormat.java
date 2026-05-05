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
package de.featjar.base.io.csv;

import de.featjar.base.data.Result;
import de.featjar.base.io.IDataTreeFormat;
import de.featjar.base.tree.DataTree;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.base.tree.visitor.ITreeVisitor.TraversalAction;
import de.featjar.base.tree.visitor.PreOrderVisitor;

/**
 * An IFormat class that take an AnalysisTree as input and can serialize it into CSV String.
 * With the CSV having only four columns (AnalysisType, Name, Value, Class)
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 * @author Sebastian Krieter
 */
public class DataTreeCSVFormat extends ACSVFormat<DataTree<?>> implements IDataTreeFormat {

    private static final String NAMESPACE_SEPARATOR = "/";

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> serialize(DataTree<?> analysisTree) {
        final StringBuilder csv = new StringBuilder();
        csv.append("Prefix").append(VALUE_SEPARATOR);
        csv.append("Name").append(VALUE_SEPARATOR);
        csv.append("Type").append(VALUE_SEPARATOR);
        csv.append("Value").append(LINE_SEPARATOR);

        Trees.traverse(analysisTree, new PreOrderVisitor<>(path -> {
            final DataTree<?> node = ITreeVisitor.getCurrentNode(path);
            int parentIndex = path.size() - 1;
            if (parentIndex > 0) {
                csv.append(path.get(0).getAttribute().getSimpleName());
                for (int i = 1; i < parentIndex; i++) {
                    csv.append(NAMESPACE_SEPARATOR)
                            .append(path.get(i).getAttribute().getSimpleName());
                }
            }
            csv.append(VALUE_SEPARATOR);
            csv.append(node.getAttribute().getSimpleName()).append(VALUE_SEPARATOR);
            csv.append(node.getAttribute().getClassType().getSimpleName()).append(VALUE_SEPARATOR);
            csv.append(node.getValue().orElse(null)).append(LINE_SEPARATOR);
            return TraversalAction.CONTINUE;
        }));

        return Result.of(csv.toString());
    }
}
