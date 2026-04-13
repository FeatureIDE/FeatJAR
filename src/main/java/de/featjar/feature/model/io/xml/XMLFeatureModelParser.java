/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.io.xml;

import static de.featjar.formula.io.xml.XMLFeatureModelConstants.COMMENT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.COMMENTS;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.CONSTRAINTS;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.DATA_TYPE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.DESCRIPTION;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.EXT_FEATURE_MODEL;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.FEATURE_MODEL;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.KEY;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.NAMESPACE_TAG;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.PROPERTIES;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.PROPERTY;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.STRUCT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.TAGS;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.VALUE;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Name;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.format.ParseException;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureModelElement;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.AttributeIO;
import de.featjar.formula.io.xml.AXMLFeatureModelParser;
import de.featjar.formula.structure.IFormula;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parses and writes feature models from and to FeatureIDE XML files.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class XMLFeatureModelParser extends AXMLFeatureModelParser<IFeatureModel, IFeatureTree, IConstraint> {

    private IFeatureModel featureModel;
    private HashSet<String> featureNames;

    @Override
    public IFeatureModel parseDocument(Document document) throws ParseException {
        featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        featureNames = Sets.empty();
        final Element featureModelElement = getDocumentElement(document, FEATURE_MODEL, EXT_FEATURE_MODEL);

        parseFeatureTree(getElement(featureModelElement, STRUCT));

        Result<Element> element = getElementResult(featureModelElement, CONSTRAINTS);
        if (element.isPresent()) {
            parseConstraints(element.get());
        }

        element = getElementResult(featureModelElement, COMMENTS);
        if (element.isPresent()) {
            parseComments(element.get());
        }

        element = getElementResult(featureModelElement, PROPERTIES);
        if (element.isPresent()) {
            parseFeatureModelProperties(element.get());
        }

        return featureModel;
    }

    @Override
    protected IFeatureTree newFeature(
            String name, IFeatureTree parentFeature, boolean mandatory, boolean _abstract, boolean hidden)
            throws ParseException {
        if (!featureNames.add(name)) {
            throw new ParseException("Duplicate feature name!");
        }
        IFeature feature = featureModel.mutate().addFeature(name);
        IFeatureTree featureTree;
        if (parentFeature == null) {
            featureTree = featureModel.mutate().addFeatureTreeRoot(feature);
        } else {
            featureTree = parentFeature.mutate().addFeatureBelow(feature);
        }
        feature.mutate().setAbstract(_abstract);
        feature.mutate().setHidden(hidden);
        if (mandatory || parentFeature == null) {
            featureTree.mutate().makeMandatory();
        } else {
            featureTree.mutate().makeOptional();
        }
        return featureTree;
    }

    @Override
    protected void addAndGroup(IFeatureTree featureTree, List<IFeatureTree> children) {
        featureTree.mutate().toAndGroup();
    }

    @Override
    protected void addOrGroup(IFeatureTree featureTree, List<IFeatureTree> children) {
        featureTree.mutate().toOrGroup();
    }

    @Override
    protected void addAlternativeGroup(IFeatureTree featureTree, List<IFeatureTree> children) {
        featureTree.mutate().toAlternativeGroup();
    }

    @Override
    protected void addFeatureMetadata(IFeatureTree feature, Element e) throws ParseException {
        String nodeName = e.getNodeName();
        switch (nodeName) {
            case DESCRIPTION:
                feature.getFeature().mutate().setDescription(getDescription(e));
                break;
            case PROPERTY:
                parseProperty(feature.getFeature(), e);
                break;
            default:
                FeatJAR.log().warning("Unkown node name %s", nodeName);
        }
    }

    @Override
    protected IConstraint newConstraint(IFormula formula) {
        return featureModel.mutate().addConstraint(formula);
    }

    @Override
    protected void addConstraintMetadata(IConstraint constraint, Element e) throws ParseException {
        String nodeName = e.getNodeName();
        switch (nodeName) {
            case DESCRIPTION:
                constraint.mutate().setDescription(getDescription(e));
                break;
            case PROPERTY:
                parseProperty(constraint, e);
                break;
            case TAGS:
                constraint.mutate().setTags(getTags(e));
                break;
            default:
                FeatJAR.log().warning("Unkown node name %s", nodeName);
        }
    }

    protected void parseComments(Element element) throws ParseException {
        for (final Element e1 : getElements(element.getChildNodes())) {
            if (e1.getNodeName().equals(COMMENT)) {
                featureModel
                        .mutate()
                        .setDescription(featureModel.getDescription().orElse("") + "\n" + e1.getTextContent());
            } else {
                addParseProblem("Unknown comment attribute: " + e1.getNodeName(), e1, Problem.Severity.WARNING);
            }
        }
    }

    protected static String getDescription(Node e) {
        String description = e.getTextContent();
        // NOTE: The following code is used for backwards compatibility. It replaces
        // spaces and tabs that were added to the XML for indentation, but don't
        // belong to the actual description.
        return description == null
                ? null
                : description.replaceAll("(\r\n|\r|\n)\\s*", "\n").replaceAll("\\A\n|\n\\Z", "");
    }

    protected static LinkedHashSet<String> getTags(final Node e) {
        return new LinkedHashSet<>(Arrays.asList(e.getTextContent().split(",")));
    }

    protected void parseProperty(IFeatureModelElement featureModelElement, Element e) throws ParseException {
        if (!e.hasAttribute(KEY)) {
            addParseProblem("Missing required attributes " + KEY, e, Problem.Severity.WARNING);
        } else if (!e.hasAttribute(VALUE)) {
            addParseProblem("Missing required attributes: " + VALUE, e, Problem.Severity.WARNING);
        } else {
            String typeString = e.hasAttribute(DATA_TYPE) ? e.getAttribute(DATA_TYPE) : "string";
            final Name name = new Name(
                    e.hasAttribute(NAMESPACE_TAG) ? e.getAttribute(NAMESPACE_TAG) : Name.DEFAULT_NAMESPACE,
                    e.getAttribute(KEY));
            final String valueString = e.getAttribute(VALUE);
            parseProblems.addAll(
                    AttributeIO.parseAndSetAttributeValue(featureModelElement, name, typeString, valueString));
        }
    }

    protected void parseFeatureModelProperties(Element e) throws ParseException {
        for (final Element propertyElement : getElements(e.getChildNodes())) {
            final String nodeName = propertyElement.getNodeName();
            switch (nodeName) {
                case PROPERTY:
                    parseProperty(featureModel, propertyElement);
                    break;
                default:
                    FeatJAR.log().warning("Unkown node name %s", nodeName);
            }
        }
    }
}
