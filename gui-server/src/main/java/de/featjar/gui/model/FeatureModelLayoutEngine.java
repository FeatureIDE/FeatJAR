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
package de.featjar.gui.model;

import com.google.inject.Inject;
import de.featjar.gui.model.FeatureTreeLayouter.LayoutContext;
import de.featjar.gui.model.FeatureTreeLayouter.TreeNode;
import de.featjar.gui.types.FeatureModelLables;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.emf.notation.EMFNotationModelState;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.operations.LayoutOperation;

/**
 * Assigns the positions of the feature tree.
 *
 * Runs after the client has reported the computed bounds for each element.
 * The spacing can then picture the real width of each node depending on its label width.
 * Otherwise, the labels of long names would extend beyond the boundaries of their nodes.
 * This cannot be done in the GModelFactory because it runs before the client's calculations.
 */
public class FeatureModelLayoutEngine implements LayoutEngine {

    @Inject
    protected EMFNotationModelState modelState;

    private static final double HORIZONTAL_GAP = 50;
    private static final double VERTICAL_GAP = 70;
    private static final double CONSTRAINT_BOX_MARGIN_Y = 160;

    @Override
    public void layout(Optional<LayoutOperation> layoutOperation) {
        GModelRoot root = modelState.getRoot();
        if (root == null || FeatureTreeLayouter.allTreeNodes.isEmpty()) {
            return;
        }
        List<GNode> gNodes = collectGNodes(root);

        // Take over the sizes the client measured
        FeatureTreeLayouter.applyMeasuredSizes(
                gNodes, FeatureModelGModelFactory.DEFAULT_NODE_WIDTH, FeatureModelGModelFactory.DEFAULT_NODE_HEIGHT);

        // The first tree node created by the factory is the root of the tree
        TreeNode treeRoot = FeatureTreeLayouter.allTreeNodes.get(0);

        LayoutContext ctx = new LayoutContext();
        FeatureTreeLayouter.computePositions(treeRoot, 0, HORIZONTAL_GAP, VERTICAL_GAP, ctx);

        // Write the computed positions back to the graphical model
        for (TreeNode treeNode : FeatureTreeLayouter.allTreeNodes) {
            GNode gNode = FeatureTreeLayouter.mapTreeNodeToGNode(treeNode, gNodes);
            if (gNode != null) {
                gNode.setPosition(GraphUtil.point(treeNode.x, treeNode.y));
            }
        }

        Optional<GNode> foundBox = findConstraintBox(root);

        if (foundBox.isPresent()) {
            GNode box = foundBox.get();
            double boxY = FeatureTreeLayouter.computeYBelowDeepestRightmostLeaf(treeRoot, CONSTRAINT_BOX_MARGIN_Y);
            double boxWidth = box.getSize() != null ? box.getSize().getWidth() : 0;
            double treeCenterX = treeRoot.x + (treeRoot.width / 2.0);

            box.setPosition(GraphUtil.point(treeCenterX - (boxWidth / 2.0), boxY));
        }
    }

    /**
     * Finds the constraint box which is currently display
     * as a {@link GNode}.
     *
     * @param root the container with all graphical elements
     * @return the box or an empty {@link Optional}
     */
    private Optional<GNode> findConstraintBox(final GModelRoot root) {
        for (GModelElement child : root.getChildren()) {
            if (child instanceof GNode
                    && child.getCssClasses() != null
                    && child.getCssClasses().contains(FeatureModelLables.CONSTRAINT_BOX)) {
                return Optional.of((GNode) child);
            }
        }
        return Optional.empty();
    }

    /**
     * Collects the the {@link GNode} elements from all top level nodes
     * of the graph (the children of the root).
     * Top level nodes are: {@link GNode}, {@link GEdge}, and the constraint box.
     *
     * @param root the container with all graphical elements
     * @return list of {@link GNodes}
     */
    private List<GNode> collectGNodes(final GModelRoot root) {
        List<GNode> nodes = new ArrayList<>();
        for (GModelElement child : root.getChildren()) {
            if (child instanceof GNode) {
                nodes.add((GNode) child);
            }
        }
        return nodes;
    }
}
