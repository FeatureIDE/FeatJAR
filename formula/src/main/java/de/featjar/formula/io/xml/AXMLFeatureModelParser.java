/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.io.xml;

import static de.featjar.formula.io.xml.XMLFeatureModelConstants.ABSTRACT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.ALT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.AND;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.ATMOST1;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.CONJ;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.DESCRIPTION;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.DISJ;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.EQ;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.FEATURE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.HIDDEN;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.IMP;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.MANDATORY;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.NAME;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.NOT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.OR;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.PROPERTY;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.RULE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.TAGS;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.TRUE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.VAR;

import de.featjar.base.data.Problem;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.io.xml.AXMLParser;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Abstract parser for FeatureIDE XML format.
 *
 * @param <T> the type of parsed data
 * @param <F> the type of feature
 * @param <C> the type of constraint
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public abstract class AXMLFeatureModelParser<T, F, C> extends AXMLParser<T> {
    public static final Pattern inputHeaderPattern =
            Pattern.compile("\\A\\s*(<[?]xml\\s.*[?]>\\s*)?<(extendedFeatureModel|featureModel)[\\s>]");

    protected abstract F newFeature(
            String name, F parentFeatureLabel, boolean mandatory, boolean _abstract, boolean hidden)
            throws ParseException;

    protected abstract void addAndGroup(F feature, List<F> childFeatures);

    protected abstract void addOrGroup(F feature, List<F> childFeatures);

    protected abstract void addAlternativeGroup(F feature, List<F> childFeatures);

    protected abstract C newConstraint(IFormula formula) throws ParseException;

    protected void addFeatureMetadata(F feature, Element e) throws ParseException {}

    protected void addConstraintMetadata(C constraint, Element e) throws ParseException {}

    protected void parseFeatureTree(Element element) throws ParseException {
        parseFeatureTree(element.getChildNodes(), null, false);
    }

    protected ArrayList<F> parseFeatureTree(NodeList nodeList, F parentFeature, boolean and) throws ParseException {
        final ArrayList<F> features = new ArrayList<>();
        final List<Element> elements = getElements(nodeList);
        if (parentFeature == null) {
            if (elements.isEmpty()) {
                throw new ParseException("No root feature!");
            }
            if (elements.size() > 1) {
                throw new ParseException("Multiple root features!");
            }
        } else {
            if (elements.isEmpty()) {
                addParseProblem("No feature in group!", null, Problem.Severity.WARNING);
            }
        }
        for (final Element e : elements) {
            final String nodeName = e.getNodeName();
            switch (nodeName) {
                case DESCRIPTION:
                case PROPERTY:
                    if (parentFeature != null) {
                        addFeatureMetadata(parentFeature, e);
                    } else {
                        addParseProblem("Misplaced metadata element " + nodeName, e, Problem.Severity.WARNING);
                    }
                    break;
                case AND:
                case OR:
                case ALT:
                case FEATURE:
                    features.add(parseFeature(parentFeature, e, nodeName, and));
                    break;
                default:
                    break;
            }
        }
        return features;
    }

    protected F parseFeature(F parentFeature, final Element e, final String nodeName, boolean and)
            throws ParseException {
        boolean _abstract = false, mandatory = false, hidden = false;
        String name = null;
        if (e.hasAttributes()) {
            final NamedNodeMap nodeMap = e.getAttributes();
            for (int i = 0; i < nodeMap.getLength(); i++) {
                final org.w3c.dom.Node node = nodeMap.item(i);
                final String attributeName = node.getNodeName();
                final String attributeValue = node.getNodeValue();
                switch (attributeName) {
                    case ABSTRACT:
                        _abstract = attributeValue.equals(TRUE);
                        break;
                    case MANDATORY:
                        mandatory = attributeValue.equals(TRUE);
                        break;
                    case NAME:
                        name = attributeValue;
                        break;
                    case HIDDEN:
                        hidden = attributeValue.equals(TRUE);
                        break;
                    default:
                        addParseProblem("Unknown feature attribute: " + attributeName, e, Problem.Severity.WARNING);
                        break;
                }
            }
        }

        F feature = newFeature(name, parentFeature, and && mandatory, _abstract, hidden);

        if (e.hasChildNodes()) {
            final ArrayList<F> featureLabels = parseFeatureTree(e.getChildNodes(), feature, nodeName.equals(AND));
            switch (nodeName) {
                case AND:
                    addAndGroup(feature, featureLabels);
                    break;
                case OR:
                    addOrGroup(feature, featureLabels);
                    break;
                case ALT:
                    addAlternativeGroup(feature, featureLabels);
                    break;
                default:
                    break;
            }
        } else if (!FEATURE.equals(nodeName)) {
            throw new ParseException("Empty group!");
        }

        return feature;
    }

    protected void parseConstraints(Element constraintsElement) throws ParseException {
        for (final Element child : getElements(constraintsElement.getChildNodes())) {
            final String nodeName = child.getNodeName();
            if (nodeName.equals(RULE)) {
                try {
                    final List<IFormula> formulaParts = parseConstraint(child.getChildNodes());
                    if (formulaParts.size() == 1) {
                        C constraint = newConstraint(formulaParts.get(0));
                        parseConstraintMetadata(child.getChildNodes(), constraint);
                    } else {
                        addParseProblem("could not parse constraint node " + nodeName, child, Problem.Severity.WARNING);
                    }
                } catch (final Exception exception) {
                    addParseProblem(exception.getMessage(), child, Problem.Severity.WARNING);
                }
            } else {
                addParseProblem("Encountered unknown node " + nodeName, child, Problem.Severity.WARNING);
            }
        }
    }

    protected List<IFormula> parseConstraint(NodeList nodeList) throws ParseException {
        final List<IFormula> formulaParts = new ArrayList<>();
        for (final Element e : getElements(nodeList)) {
            final String nodeName = e.getNodeName();
            switch (nodeName) {
                case DESCRIPTION:
                case PROPERTY:
                case TAGS:
                    break;
                case DISJ:
                    formulaParts.add(new Or(parseConstraint(e.getChildNodes())));
                    break;
                case CONJ:
                    formulaParts.add(new And(parseConstraint(e.getChildNodes())));
                    break;
                case EQ: {
                    List<IFormula> children = parseConstraint(e.getChildNodes());
                    if (children.size() == 2) {
                        formulaParts.add((IFormula) new BiImplies(children.get(0), children.get(1)));
                    } else {
                        addParseProblem("unexpected number of operands for equivalence", e, Problem.Severity.WARNING);
                    }
                    break;
                }
                case IMP: {
                    List<IFormula> children = parseConstraint(e.getChildNodes());
                    if (children.size() == 2) {
                        formulaParts.add(new Implies(children.get(0), children.get(1)));
                    } else {
                        addParseProblem("unexpected number of operands for implication", e, Problem.Severity.WARNING);
                    }
                    break;
                }
                case NOT: {
                    List<IFormula> children = parseConstraint(e.getChildNodes());
                    if (children.size() == 1) {
                        formulaParts.add(new Not(children.get(0)));
                    } else {
                        addParseProblem("unexpected number of operands for negation", e, Problem.Severity.WARNING);
                    }
                    break;
                }
                case ATMOST1:
                    formulaParts.add((IFormula) new AtMost(1, parseConstraint(e.getChildNodes())));
                    break;
                case VAR:
                    formulaParts.add(new Literal(e.getTextContent()));
                    break;
                default:
                    addParseProblem("Unknown constraint type: " + nodeName, e, Problem.Severity.WARNING);
            }
        }
        return formulaParts;
    }

    protected void parseConstraintMetadata(NodeList nodeList, C constraint) throws ParseException {
        for (final Element e : getElements(nodeList)) {
            final String nodeName = e.getNodeName();
            switch (nodeName) {
                case DESCRIPTION:
                case PROPERTY:
                case TAGS:
                    addConstraintMetadata(constraint, e);
                    break;
                default:
                    break;
            }
        }
    }

    protected IFormula implies(Literal f, List<? extends IFormula> parseFeatures) {
        return parseFeatures.size() == 1 ? new Implies(f, parseFeatures.get(0)) : new Implies(f, new Or(parseFeatures));
    }
}
