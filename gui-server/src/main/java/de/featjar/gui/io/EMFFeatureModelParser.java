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

import static de.featjar.gui.utils.CardinalityUtils.OPEN;
import static de.featjar.gui.utils.CardinalityUtils.createCardinality;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.io.xml.AXMLParser;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.IFeatureTree.IMutableFeatureTree;
import de.featjar.formula.io.textual.ExpressionParser;
import de.featjar.formula.io.textual.ShortSymbols;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.gui.types.AttributeKeys;
import de.featjar.gui.types.FeatureImplementationTypes;
import de.featjar.gui.types.GroupNodeType;
import featJAR.Cardinality;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Parses a feature model from the FeatJAR EMF format.
 *
 */
public class EMFFeatureModelParser extends AXMLParser<IFeatureModel> {

    private IFeatureModel featureModel;

    @Override
    public IFeatureModel parseDocument(Document document) throws ParseException {
        featureModel = new FeatureModel(Identifiers.newCounterIdentifier());

        Element root = document.getDocumentElement();
        featureModel.mutate().setName(root.getAttribute(EMFFeatureModelFormat.NAME));

        for (Element child : getElements(root.getChildNodes())) {
            switch (child.getNodeName()) {
                case EMFFeatureModelFormat.ROOTS -> parseFeature(child, null);
                case EMFFeatureModelFormat.CONSTRAINTS -> parseConstraint(child);
                default -> {}
            }
        }

        return featureModel;
    }

    private IFeatureTree parseFeature(Element element, IFeatureTree parent) throws ParseException {
        IFeature feature = featureModel.mutate().addFeature(element.getAttribute(EMFFeatureModelFormat.NAME));
        IFeatureTree tree = parent == null
                ? featureModel.mutate().addFeatureTreeRoot(feature)
                : parent.mutate().addFeatureBelow(feature);

        for (Element child : getElements(element.getChildNodes())) {
            switch (child.getNodeName()) {
                case EMFFeatureModelFormat.ATTRIBUTES -> parseFeratureAttributes(child, feature);
                case EMFFeatureModelFormat.CARDINALITY -> {
                    Cardinality card = parseCardinality(child, false);
                    tree.mutate().setFeatureCardinality(Range.of(card.getLowerBound(), card.getUpperBound()));
                }
                case EMFFeatureModelFormat.GROUP_NODE_LIST -> parseGroupNode(child, tree);
                default -> {}
            }
        }

        return tree;
    }

    private void parseFeratureAttributes(Element element, IFeature feature) {
        String key = element.getAttribute(EMFFeatureModelFormat.KEY);
        String value = element.getAttribute(EMFFeatureModelFormat.VALUE);

        if (AttributeKeys.IMPLLEMENTATION.equals(key)) {
            if (FeatureImplementationTypes.ABSTRACT.value().equals(value)) {
                feature.mutate().setAbstract(true);
            } else if (FeatureImplementationTypes.CONCRETE.value().equals(value)) {
                feature.mutate().setAbstract(false);
            }
            // TODO If type "NONE" is present -> nothing should be set !
        }

        if (AttributeKeys.HIDDEN.equals(key)) {
            feature.mutate().setHidden(true);
        }
    }

    private Cardinality parseCardinality(Element e, boolean isGroup) {
        int lower = e.getAttribute(EMFFeatureModelFormat.LOWER_BOUND).isEmpty()
                ? 0
                : Integer.parseInt(e.getAttribute(EMFFeatureModelFormat.LOWER_BOUND));
        int upper;

        if (isGroup) {
            upper = e.getAttribute(EMFFeatureModelFormat.UPPER_BOUND).isEmpty()
                    ? OPEN
                    : Integer.parseInt(e.getAttribute(EMFFeatureModelFormat.UPPER_BOUND));
        } else {
            upper = e.getAttribute(EMFFeatureModelFormat.UPPER_BOUND).isEmpty()
                    ? 1
                    : Integer.parseInt(e.getAttribute(EMFFeatureModelFormat.UPPER_BOUND));
        }

        return createCardinality(lower, upper);
    }

    private void parseGroupNode(Element groupNode, IFeatureTree parentTree) throws ParseException {
        Cardinality cardinality = null;
        List<Element> childFeatures = new ArrayList<>();

        for (Element child : getElements(groupNode.getChildNodes())) {
            switch (child.getNodeName()) {
                case EMFFeatureModelFormat.CARDINALITY -> cardinality = parseCardinality(child, true);
                case EMFFeatureModelFormat.FEATURE_LIST -> childFeatures.add(child);
                default -> {}
            }
        }

        if (cardinality == null) {
            throw new ParseException("Missing cardinality element in group node");
        }
        if (childFeatures.size() == 0) {
            throw new ParseException("Missing features below group node");
        }

        final IMutableFeatureTree parent = parentTree.mutate();
        int groupID;
        if (parentTree.getChildrenCount() == 0) {
            groupID = 0;
            switch (GroupNodeType.of(cardinality)) {
                case OR_NODE -> parent.toOrGroup();
                case XOR_NODE -> parent.toAlternativeGroup();
                case CARDINALITY_NODE ->
                    parent.toCardinalityGroup(cardinality.getLowerBound(), cardinality.getUpperBound());
                case AND_NODE -> parent.toAndGroup();
                default -> throw new ParseException("Unknown group type");
            }
        } else {
            groupID = switch (GroupNodeType.of(cardinality)) {
                case OR_NODE -> parent.addOrGroup();
                case XOR_NODE -> parent.addAlternativeGroup();
                case CARDINALITY_NODE ->
                    parent.addCardinalityGroup(cardinality.getLowerBound(), cardinality.getUpperBound());
                case AND_NODE -> parent.addAndGroup();
                default -> throw new ParseException("Unknown group type");
            };
        }

        for (Element child : childFeatures) {
            parseFeature(child, parentTree).mutate().setParentGroupID(groupID);
        }
    }

    @SuppressWarnings("deprecation")
    private void parseConstraint(Element element) {
        String name = element.getAttribute(EMFFeatureModelFormat.NAME);

        // TODO replace when anther option is available
        ExpressionParser parser = new ExpressionParser();
        parser.setSymbols(ShortSymbols.INSTANCE);

        Result<IExpression> result = parser.parse(name);

        if (result.isPresent()) {
            IFormula formula = (IFormula) result.get();
            featureModel.mutate().addConstraint(formula);
        } else {
            result.getProblems().forEach(FeatJAR.log()::problem);
        }
    }
}
