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

import de.featjar.gui.model.FeatureTreeLayouter.NodeSubtreeResult;
import de.featjar.gui.model.FeatureTreeLayouter.TreeNode;
import de.featjar.gui.types.CardinalityType;
import de.featjar.gui.types.EdgeType;
import de.featjar.gui.types.FeatureImplementationTypes;
import de.featjar.gui.types.FeatureModelLables;
import de.featjar.gui.types.GroupNodeType;
import de.featjar.gui.utils.AttributeKeysUtils;
import de.featjar.gui.utils.CardinalityUtils;
import featJAR.Cardinality;
import featJAR.Constraint;
import featJAR.Feature;
import featJAR.FeatureModel;
import featJAR.GroupNode;
import featJAR.Identifiable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GEdgePlacementBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GLayoutOptions;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.graph.util.GConstants;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.emf.model.notation.Diagram;
import org.eclipse.glsp.server.emf.notation.EMFNotationGModelFactory;

/**
 * Builds the graphical model from the semantic feature model.
 *
 * Creates the structure for nodes, edges, labels and their CSS classes.
 * Positions and final sizes are left out, since the
 * client calculates the labels afterwards and {@link FeatureModelLayoutEngine} assigns
 * the positions once the calculations are available.
 */
public class FeatureModelGModelFactory extends EMFNotationGModelFactory {
    // Min. value for every node if no size is calculated by the client afterwards
    protected static final int DEFAULT_NODE_WIDTH = 100;
    protected static final int DEFAULT_NODE_HEIGHT = 30;
    // Fix values for group nodes (OR, XOR, AND) since they do not have a label
    private static final int groupNodeWidth = 40;
    private static final int groupNodeHeight = 20;

    private static final String LABEL_SUFFIX = "_label";
    private static final String CONSTRAINTS_TITLE = "constraints_title";

    List<GNode> gNodes = new ArrayList<>();
    List<GModelElement> gElements = new ArrayList<>();
    final GPoint currentPosition = GraphUtil.point(0, 0);

    private void clearAllGraphicalElements() {
        FeatureTreeLayouter.clear();
        gElements.clear();
        gNodes.clear();
    }

    @Override
    protected void fillRootElement(final EObject semanticModel, final Diagram notationModel, final GModelRoot newRoot) {
        FeatureModel emfFeatureModel = FeatureModel.class.cast(semanticModel);
        GGraph graph = GGraph.class.cast(newRoot);

        // If no root element is found, then render nothing
        if (emfFeatureModel.getRoots().size() == 0) {
            return;
        }
        clearAllGraphicalElements();

        Feature emfRoot = emfFeatureModel.getRoots().get(0);
        NodeSubtreeResult gRoot = constructFeatureSubtree(emfRoot, true);
        createConstraintBox(emfFeatureModel.getConstraints());

        graph.getChildren().addAll(gNodes);
        graph.getChildren().addAll(gElements);
    }

    /**
     * Constructs the {@link Feature} subtree at top children level which only contains {@link GNode}s.
     * Calls {@link FeatureModelGModelFactory#constructGroupNodeSubtree(GroupNode)}
     * to construct the respective subtree for its {@link GroupNode}s.
     *
     * @param feature the current {@link Feature}
     * @param isRoot the root feature get another CSS label
     * @return the {@link NodeSubtreeResult}
     */
    private NodeSubtreeResult constructFeatureSubtree(Feature feature, boolean isRoot) {
        String primaryCss = isRoot
                ? FeatureModelLables.ROOT_FEATURE
                : CardinalityType.of(feature.getCardinality()).value();
        GNode gNode = createNode(feature, primaryCss);

        FeatureImplementationTypes secondaryCss = AttributeKeysUtils.getFeatureType(feature);
        if (secondaryCss != FeatureImplementationTypes.NONE) {
            gNode.getCssClasses().add(secondaryCss.value());
        }

        TreeNode currentNode = new TreeNode(gNode.getId());

        for (GroupNode child : feature.getGroupNodeList()) {
            NodeSubtreeResult gChild = constructGroupNodeSubtree(child);
            currentNode.addChild(gChild.treeNode);
            createEdge(feature, child, gNode, gChild.gNode);
        }
        gNodes.add(gNode);
        return new NodeSubtreeResult(gNode, currentNode);
    }

    /**
     * Constructs the {@link Feature} subtree at top children level which only contains {@link GNode}s.
     * Calls {@link FeatureModelGModelFactory#constructFeatureSubtree(Feature, boolean))}
     * to construct the respective subtree for its {@link Feature}s.
     *
     * @param groupNode the current {@link GroupNode}
     * @return the {@link}
     */
    private NodeSubtreeResult constructGroupNodeSubtree(GroupNode groupNode) {
        GNode gNode;
        Cardinality groupNodeCardinality = groupNode.getCardinality();
        gNode = createNode(groupNode, GroupNodeType.of(groupNodeCardinality).value());
        TreeNode currentNode = new TreeNode(gNode.getId());

        for (Feature child : groupNode.getFeatureList()) {
            // TODO Hidden does currently not work
            if (!AttributeKeysUtils.isHidden(child)) {
                // gNode.getCssClasses().add(CssClass.HIDDEN.getCssClass());
                NodeSubtreeResult gChild = constructFeatureSubtree(child, false);
                currentNode.addChild(gChild.treeNode);
                createEdge(groupNode, child, gNode, gChild.gNode);
            }
        }
        gNodes.add(gNode);
        return new NodeSubtreeResult(gNode, currentNode);
    }

    /**
     * Creates the graphical representations for {@link Feature} and {@link GroupNode}.
     *
     * @param identifiable the model element
     * @param cssType the corresponding CSS class
     * @return the created {@link GNode}
     */
    private GNode createNode(final Identifiable identifiable, final String cssType) {

        GNodeBuilder nodeBuilder = new GNodeBuilder(DefaultTypes.NODE)
                .id(idGenerator.getOrCreateId(identifiable))
                .addCssClass(cssType)
                .position(currentPosition);

        boolean isDiamond = cssType.equals(GroupNodeType.AND_NODE.value())
                || cssType.equals(GroupNodeType.CARDINALITY_NODE.value());

        if (identifiable instanceof Feature feature) {
            nodeBuilder
                    .layout(GConstants.Layout.VBOX)
                    .layoutOptions(new GLayoutOptions()
                            .vAlign(GConstants.VAlign.CENTER)
                            .hAlign(GConstants.HAlign.CENTER)
                            .minWidth(DEFAULT_NODE_WIDTH)
                            .minHeight(DEFAULT_NODE_HEIGHT))
                    .add(new GLabelBuilder(FeatureModelLables.EDITABLE_LABEL)
                            .text(feature.getName())
                            .id(feature.getId() + LABEL_SUFFIX)
                            .build());
            nodeBuilder.addArgument("lowerBound", feature.getCardinality().getLowerBound());
            nodeBuilder.addArgument("upperBound", feature.getCardinality().getUpperBound());

        } else if (identifiable instanceof GroupNode groupNode) {

           int w = cssType.equals(GroupNodeType.AND_NODE.value()) ? groupNodeWidth : (isDiamond ? groupNodeWidth + 20 : groupNodeWidth);
           int h = cssType.equals(GroupNodeType.AND_NODE.value()) ? groupNodeHeight : (isDiamond ? groupNodeHeight + 30 : groupNodeHeight);

            nodeBuilder.addArgument("lowerBound", groupNode.getCardinality().getLowerBound());
            nodeBuilder.addArgument("upperBound", groupNode.getCardinality().getUpperBound());

            if (cssType.equals(GroupNodeType.CARDINALITY_NODE.value())) {
                Cardinality cardinality = groupNode.getCardinality();
                nodeBuilder
                        .layout(GConstants.Layout.VBOX)
                        .layoutOptions(new GLayoutOptions()
                                .vAlign(GConstants.VAlign.CENTER)
                                .hAlign(GConstants.HAlign.CENTER)
                                .minWidth(DEFAULT_NODE_WIDTH)
                                .minHeight(DEFAULT_NODE_HEIGHT)
                                .paddingLeft(25)
                                .paddingRight(25))
                        .add(new GLabelBuilder(FeatureModelLables.NODE_CARDINALITY_LABEL)
                                .id(identifiable.getId() + "_bounds")
                                .text(formatBounds(cardinality.getLowerBound(), cardinality.getUpperBound()))
                                .build());
            }

            nodeBuilder.size(GraphUtil.dimension(w, h));

        } else {
            // TODO add error handling
        }

        applyShapeData(identifiable, nodeBuilder);
        GNode gNode = nodeBuilder.build();

        return gNode;
    }

    /**
     * Creates the graphical representation for an edge with a
     * suitable {@link EdgeType} between to nodes.
     *
     * @param parent the EMF source
     * @param child the EMF target
     * @param gParent the {@link GNode} of the parent
     * @param gChild the {@link GNode} of the source
     */
    private void createEdge(
            final Identifiable parent, final Identifiable child, final GNode gParent, final GNode gChild) {
        EdgeType edgeType;
        String cardinalityLabel = null;

        if (child instanceof Feature) {
            Feature feature = (Feature) child;
            Cardinality featureCardinality = feature.getCardinality();
            Cardinality featureGroupCardinality = feature.getParent().getCardinality();

            if (CardinalityUtils.isOr(featureGroupCardinality) || CardinalityUtils.isXor(featureGroupCardinality)) {
                edgeType = EdgeType.BASIC_EDGE;
            } else if (CardinalityUtils.isOptional(featureCardinality)) {
                edgeType = EdgeType.OPTIONAL_EDGE;
            } else if (CardinalityUtils.isMandatory(featureCardinality)) {
                edgeType = EdgeType.MANDATORY_EDGE;
            } else {
                // Multiple features with custom bounds
                edgeType = EdgeType.BASIC_EDGE;
                cardinalityLabel = formatBounds(featureCardinality.getLowerBound(), featureCardinality.getUpperBound());
            }
        } else {
            edgeType = EdgeType.BASIC_EDGE;
        }

        GEdgeBuilder edgeBuilder = new GEdgeBuilder(edgeType.value())
                .id(parent.getId() + "_to_" + child.getId())
                .source(gParent)
                .target(gChild);

        if (cardinalityLabel != null) {
            edgeBuilder.add(new GLabelBuilder(FeatureModelLables.EDGE_CARDINALITY_LABEL)
                    .id(parent.getId() + "_to_" + child.getId() + "_card")
                    .text(cardinalityLabel)
                    .edgePlacement(new GEdgePlacementBuilder()
                            .position(0.5) // middle of the edge
                            .side(GConstants.EdgeSide.TOP)
                            .offset(5)
                            .build())
                    .build());
        }

        gElements.add(edgeBuilder.build());
    }

    /*
     * Formats the label text with cardinalities for multiple features.
     */
    private String formatBounds(int lower, int upper) {
        return "[" + lower + ".." + (upper == -1 ? "*" : String.valueOf(upper)) + "]";
    }

    /**
     * Creates a box which contains all constraints.
     *
     * @param constraints all existing constraints as strings
     * @return the 'box' as gNode
     */
    private GNode createConstraintBox(final List<Constraint> constraints) {
        int legendWidth = 290;
        int rowHeight = 24;
        int topPadding = 8;

        GNodeBuilder legendBuilder = new GNodeBuilder("constraint-box")
                .id("cross-tree-contraints")
                .layoutOptions(new GLayoutOptions().minWidth(legendWidth).minHeight(120))
                .addCssClass(FeatureModelLables.CONSTRAINT_BOX);

        int y = topPadding;

        GNode title = createTitleRow("Constraints", legendWidth);
        title.setPosition(GraphUtil.point(10, y));
        legendBuilder.add(title);
        y += rowHeight;

        for (Constraint constraint : constraints) {
            GNode node = createConstraintNode(constraint, legendWidth);
            node.setPosition(GraphUtil.point(10, y));
            legendBuilder.add(node);
            y += rowHeight;
        }

        GNode legend = legendBuilder.build();

        gElements.add(legend);
        return legend;
    }

    /* Creates the title row of the constraint box */
    private GNode createTitleRow(final String text, final int legendWidth) {
        return new GNodeBuilder(DefaultTypes.NODE)
                .id(CONSTRAINTS_TITLE)
                .addCssClass(FeatureModelLables.CONSTRAINT_TITLE)
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .hAlign(GConstants.HAlign.CENTER)
                        .minWidth(legendWidth - 20)
                        .minHeight(20))
                .size(GraphUtil.dimension(legendWidth - 20, 20))
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .id(CONSTRAINTS_TITLE + LABEL_SUFFIX)
                        .text(text)
                        .build())
                .build();
    }

    /*
     *	Creates a selectable node for a constraint, containing its text as a label
     */
    public GNode createConstraintNode(final Constraint constraint, int legendWidth) {
        String gId = constraint.getId();

        GNode gNode = new GNodeBuilder(DefaultTypes.NODE)
                .id(gId)
                .addCssClass(FeatureModelLables.CONSTRAINT_NODE)
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .hAlign(GConstants.HAlign.CENTER)
                        .minHeight(16)
                        .minWidth(legendWidth - 20))
                .add(new GLabelBuilder(FeatureModelLables.EDITABLE_LABEL)
                        .id(gId + LABEL_SUFFIX)
                        .text(constraint.getName())
                        .addCssClass(FeatureModelLables.CONSTRAINT_LABEL)
                        .addArgument("wrap", true)
                        .build())
                .build();

        return gNode;
    }
}
