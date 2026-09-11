/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-gui-server.
 *
 * gui-server is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * gui-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gui-server. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE> for further information.
 */
package de.featjar.gui.io;

import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.ExpressionSerializer.Notation;
import de.featjar.formula.io.textual.ShortSymbols;
import de.featjar.gui.types.AttributeKeys;
import de.featjar.gui.types.FeatureImplementationTypes;
import java.util.UUID;

/**
 * Writes a feature model in the FeatJAR EMF format.
 *
 */
// TODO use AXMLWriter
public class EMFFeatureModelWriter {

    private StringBuilder sb;

    public Result<String> serialize(IFeatureModel model) {
        sb = new StringBuilder();

        String name = model.getName().orElse("unnamed");
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<featJAR:FeatureModel xmi:version=\"2.0\" ")
                .append("xmlns:xmi=\"http://www.omg.org/XMI\" ")
                .append("xmlns:featJAR=\"https://github.com/FeatureIDE/FeatJAR\" ")
                .append(EMFFeatureModelFormat.ID)
                .append("=\"")
                .append(nextId())
                .append("\" ")
                .append(EMFFeatureModelFormat.NAME)
                .append("=\"")
                .append(name)
                .append("\">\n");

        for (IFeatureTree root : model.getRoots()) {
            writeFeature(root, EMFFeatureModelFormat.ROOTS, 1);
        }

        for (IConstraint constraint : model.getConstraints()) {
            writeConstraint(constraint, 1);
        }

        sb.append("</featJAR:FeatureModel>\n");
        return Result.of(sb.toString());
    }

    private void writeFeature(IFeatureTree tree, String tag, int depth) {
        String name = tree.getFeature().getName().orElse("");
        String implementation = tree.getFeature().isAbstract()
                ? FeatureImplementationTypes.ABSTRACT.value()
                : FeatureImplementationTypes.CONCRETE.value();

        boolean hasChildren = !tree.getChildren().isEmpty();

        sb.append(indent(depth))
                .append("<")
                .append(tag)
                .append(" ")
                .append(EMFFeatureModelFormat.ID)
                .append("=\"")
                .append(nextId())
                .append("\"")
                .append(" ")
                .append(EMFFeatureModelFormat.NAME)
                .append("=\"")
                .append(name)
                .append("\">\n");

        sb.append(indent(depth + 1))
                .append("<")
                .append(EMFFeatureModelFormat.ATTRIBUTES)
                .append(" ")
                .append(EMFFeatureModelFormat.KEY)
                .append("=\"")
                .append(AttributeKeys.IMPLLEMENTATION)
                .append("\"")
                .append(" ")
                .append(EMFFeatureModelFormat.VALUE)
                .append("=\"")
                .append(implementation)
                .append("\"/>\n");

        if (tree.getFeature().isHidden()) {
            sb.append(indent(depth + 1))
                    .append("<")
                    .append(EMFFeatureModelFormat.ATTRIBUTES)
                    .append(" ")
                    .append(EMFFeatureModelFormat.KEY)
                    .append("=\"")
                    .append(AttributeKeys.HIDDEN)
                    .append("\"")
                    .append(" ")
                    .append(EMFFeatureModelFormat.VALUE)
                    .append("=\"\"/>\n");
        }

        writeFeatureCardinality(tree, depth + 1);

        if (hasChildren) {
            writeGroupNode(tree, depth);
        }

        sb.append(indent(depth)).append("</").append(tag).append(">\n");
    }

    private void writeGroupNode(IFeatureTree tree, int depth) {
        Group group = tree.getChildren().get(0).getParentGroup().orElseThrow();

        sb.append(indent(depth + 1))
                .append("<")
                .append(EMFFeatureModelFormat.GROUP_NODE_LIST)
                .append(" ")
                .append(EMFFeatureModelFormat.ID)
                .append("=\"")
                .append(nextId())
                .append("\" ")
                .append(EMFFeatureModelFormat.NAME)
                .append("=\"\">\n");

        writeGroupCardinality(group, depth + 2);

        for (IFeatureTree child : tree.getChildren()) {
            writeFeature(child, EMFFeatureModelFormat.FEATURE_LIST, depth + 2);
        }

        sb.append(indent(depth + 1))
                .append("</")
                .append(EMFFeatureModelFormat.GROUP_NODE_LIST)
                .append(">\n");
    }

    private void writeGroupCardinality(Group group, int depth) {
        sb.append(indent(depth)).append("<").append(EMFFeatureModelFormat.CARDINALITY);

        sb.append(" ")
                .append(EMFFeatureModelFormat.LOWER_BOUND)
                .append("=\"")
                .append(group.getLowerBound())
                .append("\"");

        sb.append(" ")
                .append(EMFFeatureModelFormat.UPPER_BOUND)
                .append("=\"")
                .append(group.getUpperBound())
                .append("\"");

        sb.append("/>\n");
    }

    private void writeFeatureCardinality(IFeatureTree tree, int depth) {
        sb.append(indent(depth)).append("<").append(EMFFeatureModelFormat.CARDINALITY);

        sb.append(" ")
                .append(EMFFeatureModelFormat.LOWER_BOUND)
                .append("=\"")
                .append(tree.getFeatureCardinalityLowerBound())
                .append("\"");

        sb.append(" ")
                .append(EMFFeatureModelFormat.UPPER_BOUND)
                .append("=\"")
                .append(tree.getFeatureCardinalityUpperBound())
                .append("\"");

        sb.append("/>\n");
    }

    private void writeConstraint(IConstraint constraint, int depth) {
        // This behavior is desired, the name is the formula
        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setSymbols(ShortSymbols.INSTANCE);
        serializer.setNotation(Notation.INFIX);

        String name = Trees.traverse(constraint.getFormula(), serializer).orElse("");

        sb.append(indent(depth))
                .append("<")
                .append(EMFFeatureModelFormat.CONSTRAINTS)
                .append(" ")
                .append(EMFFeatureModelFormat.ID)
                .append("=\"")
                .append(nextId())
                .append("\" ")
                .append(EMFFeatureModelFormat.NAME)
                .append("=\"")
                .append(name)
                .append("\"/>\n");
    }

    private String indent(int depth) {
        return "  ".repeat(depth); // Two spaces
    }

    private String nextId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
