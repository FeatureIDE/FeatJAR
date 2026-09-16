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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.glsp.graph.GNode;

/**
 * Computes the tree layout for the feature model.
 *
 * Called after {@link FeatureModelLayoutEngine}.
 * Leaves are placed one after another using their measured
 * width, and inner nodes are centered above their children, such that features with
 * long labels get their needed space.
 */
public class FeatureTreeLayouter {

    /**
     * Holds GNode and the corresponding layout tree node.
     */
    protected static class NodeSubtreeResult {
        public final GNode gNode;
        public final FeatureTreeLayouter.TreeNode treeNode;

        public NodeSubtreeResult(final GNode gNode, final FeatureTreeLayouter.TreeNode treeNode) {
            this.gNode = gNode;
            this.treeNode = treeNode;
        }
    }

    public static List<TreeNode> allTreeNodes = new ArrayList<>();

    /**
     * Size of the corresponding GNode. Filled by {@link FeatureTreeLayouter#applyMeasuredSizes(List, double, double)} before
     * the positions are computed, so the layout can respect labels of any
     * length instead of assuming a fixed node width.
     */
    public static class TreeNode {
        public String id;
        public double x;
        public double y;

        public double width;
        public double height;

        public List<TreeNode> children = new ArrayList<>();

        public TreeNode(final String id) {
            this.id = id;
            allTreeNodes.add(this);
        }

        public void addChild(final TreeNode child) {
            children.add(child);
        }

        @Override
        public String toString() {
            return String.format("%s(%.1f, %.1f, w=%.1f)", id, x, y, width);
        }
    }

    /**
     * Contains coordinates.
     */
    public static class Point {
        public final double x;
        public final double y;

        public Point(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point(" + x + ", " + y + ")";
        }
    }

    public static class LayoutContext {
        public double nextX = 0;
    }

    /*
     * Copies the sizes the client measured into the tree nodes, so the layout can
     * space nodes according to their real width instead of a fixed default.
     * Nodes without a measured size fall back to the given defaults.
     */
    public static void applyMeasuredSizes(
            final List<GNode> gNodes, final double defaultWidth, final double defaultHeight) {
        for (TreeNode treeNode : allTreeNodes) {
            GNode gNode = mapTreeNodeToGNode(treeNode, gNodes);
            boolean hasSize = gNode != null && gNode.getSize() != null;

            treeNode.width =
                    hasSize && gNode.getSize().getWidth() > 0 ? gNode.getSize().getWidth() : defaultWidth;
            treeNode.height =
                    hasSize && gNode.getSize().getHeight() > 0 ? gNode.getSize().getHeight() : defaultHeight;
        }
    }

    /*
     * Computes the position of every node. Leaves are placed one after another
     * using their own width plus a gap, so wide labels do not overlap their
     * neighbors. Inner nodes are centered above their children.
     */
    public static void computePositions(
            final TreeNode node,
            final double startY,
            final double horizontalGap,
            final double verticalGap,
            final LayoutContext ctx) {
        if (node == null) {
            return;
        }

        node.y = startY;

        if (node.children.isEmpty()) {
            node.x = ctx.nextX;
            ctx.nextX += node.width + horizontalGap;
        } else {
            double childY = startY + node.height + verticalGap;
            for (TreeNode child : node.children) {
                computePositions(child, childY, horizontalGap, verticalGap, ctx);
            }

            TreeNode first = node.children.get(0);
            TreeNode last = node.children.get(node.children.size() - 1);
            double childrenCenter = (first.x + (last.x + last.width)) / 2.0;
            node.x = childrenCenter - (node.width / 2.0);
        }
    }

    public static GNode mapTreeNodeToGNode(final TreeNode treeNode, final List<GNode> gNodeList) {
        for (GNode g : gNodeList) {
            if (g.getId().equals(treeNode.id)) {
                return g;
            }
        }
        return null;
    }

    public static TreeNode mapGNodeToTreeNode(final GNode gnode) {
        for (TreeNode t : allTreeNodes) {
            if (t.id.equals(gnode.getId())) {
                return t;
            }
        }
        return null;
    }

    public static void clear() {
        allTreeNodes.clear();
    }

    /**
     * Returns the last in display order. Repeatedly takes the last child of
     * each node until a leaf is reached. Returns null if root is null.
     */
    public static TreeNode findRightmostLeaf(final TreeNode root) {
        if (root == null) {
            return null;
        }
        TreeNode current = root;
        while (!current.children.isEmpty()) {
            current = current.children.get(current.children.size() - 1);
        }
        return current;
    }

    /**
     * Computes an anchor point centered horizontally under the rightmost leaf,
     * using the measured size of that leaf.
     *
     * @param root    the layout tree root (from mapGNodeToTreeNode(gRootNode))
     * @param marginY additional vertical gap below the leaf
     * @return Point where x = center under the leaf, y = bottom + margin
     */
    public static Point computeAnchorBelowRightmostLeaf(final TreeNode root, final double marginY) {
        TreeNode leaf = findRightmostLeaf(root);
        if (leaf == null) {
            return new Point(0, 0);
        }
        double centerX = leaf.x + (leaf.width / 2.0);
        double bottomY = leaf.y + leaf.height + marginY;
        return new Point(centerX, bottomY);
    }

    /**
     * Computes the top left position to place a rectangular legend of given width
     * so that it is horizontally centered under the rightmost leaf.
     *
     * @param root        the layout tree root
     * @param legendWidth the legend's width
     * @param marginY     vertical margin below the leaf
     * @return Point = top left for the legend rectangle
     */
    public static Point computeLegendTopLeftUnderRightmostLeaf(
            final TreeNode root, final double legendWidth, final double marginY) {
        Point anchor = computeAnchorBelowRightmostLeaf(root, marginY);
        double topLeftX = anchor.x - (legendWidth / 2.0);
        double topLeftY = anchor.y;
        return new Point(topLeftX, topLeftY);
    }

    /* Finds the deepest (max y) leaf under root. If multiple share the same y, it
     * picks the rightmost (max x).
     */
    public static TreeNode findDeepestRightmostLeaf(final TreeNode root) {
        if (root == null) {
            return null;
        }

        TreeNode best = null;
        double bestY = Double.NEGATIVE_INFINITY;
        double bestX = Double.NEGATIVE_INFINITY;

        final ArrayList<TreeNode> stack = new ArrayList<>();
        stack.add(root);
        while (!stack.isEmpty()) {
            TreeNode n = stack.remove(stack.size() - 1);
            if (n.children.isEmpty()) {
                if (n.y > bestY || (n.y == bestY && n.x > bestX)) {
                    best = n;
                    bestY = n.y;
                    bestX = n.x;
                }
            } else {
                for (TreeNode element : n.children) {
                    stack.add(element);
                }
            }
        }
        return best;
    }

    public static double computeYBelowDeepestRightmostLeaf(final TreeNode root, final double marginY) {
        TreeNode leaf = findDeepestRightmostLeaf(root);
        if (leaf == null) {
            return marginY;
        }
        return leaf.y + leaf.height + marginY;
    }
}
