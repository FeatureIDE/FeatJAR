/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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

import static de.featjar.formula.io.xml.XMLFeatureModelConstants.ABSTRACT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.ALT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.AND;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.ATMOST1;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.CONJ;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.CONSTRAINTS;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.DATA_TYPE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.DESCRIPTION;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.DISJ;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.EQ;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.FEATURE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.FEATURE_MODEL;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.HIDDEN;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.IMP;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.KEY;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.MANDATORY;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.NAME;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.NAMESPACE_TAG;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.NOT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.OR;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.PROPERTY;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.RULE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.STRUCT;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.TAGS;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.TRUE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.VALUE;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.VAR;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.IAttribute;
import de.featjar.base.io.xml.AXMLWriter;
import de.featjar.feature.model.FeatureModelAttributes;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.AttributeIO;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Parses and writes feature models from and to FeatureIDE XML files.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class XMLFeatureModelWriter extends AXMLWriter<IFeatureModel> {

    @Override
    public void writeDocument(IFeatureModel featureModel, Document doc) {
        final Element root = doc.createElement(FEATURE_MODEL);
        doc.appendChild(root);

        writeFeatures(doc, root, featureModel);
        writeConstraints(doc, root, featureModel);
    }

    protected void writeFeatures(Document doc, final Element root, IFeatureModel featureModel) {
        final Element struct = doc.createElement(STRUCT);
        root.appendChild(struct);
        writeFeatureTreeRec(doc, struct, featureModel.getRoots().get(0));
    }

    protected void writeConstraints(Document doc, final Element root, IFeatureModel featureModel) {
        Collection<IConstraint> constraintList = featureModel.getConstraints();
        if (!constraintList.isEmpty()) {
            final Element constraints = doc.createElement(CONSTRAINTS);
            root.appendChild(constraints);
            for (final IConstraint constraint : constraintList) {
                Element rule;
                rule = doc.createElement(RULE);

                constraints.appendChild(rule);
                addDescription(doc, constraint.getDescription().orElse(null), rule);
                addProperties(doc, constraint.getAttributes().get(), rule);
                addTags(doc, constraint.getTags(), rule);
                createPropositionalConstraints(doc, rule, constraint.getFormula());
            }
        }
    }

    /**
     * Inserts the tags concerning propositional constraints into the DOM document
     * representation
     *
     * @param doc
     * @param node Parent node for the propositional nodes
     */
    protected void createPropositionalConstraints(Document doc, Element xmlNode, IFormula node) {
        if (node == null) {
            return;
        }

        final Element op;
        if (node instanceof Literal) {
            final Literal literal = (Literal) node;
            if (!literal.isPositive()) {
                final Element opNot = doc.createElement(NOT);
                xmlNode.appendChild(opNot);
                xmlNode = opNot;
            }
            op = doc.createElement(VAR);
            op.appendChild(doc.createTextNode(literal.getFirstChild().get().getName()));
            xmlNode.appendChild(op);
            return;
        } else if (node instanceof Or) {
            op = doc.createElement(DISJ);
        } else if (node instanceof BiImplies) {
            op = doc.createElement(EQ);
        } else if (node instanceof Implies) {
            op = doc.createElement(IMP);
        } else if (node instanceof And) {
            op = doc.createElement(CONJ);
        } else if (node instanceof Not) {
            op = doc.createElement(NOT);
        } else if (node instanceof AtMost) {
            op = doc.createElement(ATMOST1);
        } else {
            FeatJAR.log().error("Unsupported element %s", node);
            return;
        }
        xmlNode.appendChild(op);

        for (final IExpression child : node.getChildren()) {
            createPropositionalConstraints(doc, op, (IFormula) child);
        }
    }

    /**
     * Creates document based on feature model step by step
     *
     * @param doc  document to write
     * @param node parent node
     * @param feat current feature
     */
    protected void writeFeatureTreeRec(Document doc, Element node, IFeatureTree feat) {
        if (feat == null) {
            return;
        }

        final List<? extends IFeatureTree> children = feat.getChildren();

        final Element fnod;
        if (children.isEmpty()) {
            fnod = doc.createElement(FEATURE);
            writeFeatureProperties(doc, node, feat, fnod);
        } else {
            if (feat.getChildrenGroups().get(0).isAnd()) {
                fnod = doc.createElement(AND);
            } else if (feat.getChildrenGroups().get(0).isOr()) {
                fnod = doc.createElement(OR);
            } else if (feat.getChildrenGroups().get(0).isAlternative()) {
                fnod = doc.createElement(ALT);
            } else {
                FeatJAR.log().error("Unkown group %s", feat.getParentGroup());
                return;
            }

            writeFeatureProperties(doc, node, feat, fnod);

            for (final IFeatureTree feature : children) {
                writeFeatureTreeRec(doc, fnod, feature);
            }
        }
    }

    protected void writeFeatureProperties(Document doc, Element node, IFeatureTree feat, final Element fnod) {
        addDescription(doc, feat.getFeature().getDescription().orElse(null), fnod);
        if (feat.getAttributes().isPresent()) {
            addProperties(doc, feat.getAttributes().get(), fnod);
        }
        writeAttributes(node, fnod, feat);
    }

    protected void addDescription(Document doc, String description, Element fnod) {
        if ((description != null) && !description.trim().isEmpty()) {
            final Element descr = doc.createElement(DESCRIPTION);
            descr.setTextContent(description);
            fnod.appendChild(descr);
        }
    }

    protected void addProperties(Document doc, Map<IAttribute<?>, Object> attributes, Element fnod) {
        for (final Entry<IAttribute<?>, Object> property : attributes.entrySet()) {
            IAttribute<?> attribute = property.getKey();
            if (FeatureModelAttributes.ATTRIBUTE_NAMESPACE.equals(attribute.getNamespace())) {
                final Element propNode;
                propNode = doc.createElement(PROPERTY);
                propNode.setAttribute(NAMESPACE_TAG, attribute.getNamespace());
                propNode.setAttribute(
                        DATA_TYPE,
                        AttributeIO.getTypeString(attribute.getType())
                                .orElseThrow(p -> new IllegalArgumentException()));
                propNode.setAttribute(KEY, attribute.getSimpleName());
                propNode.setAttribute(VALUE, attribute.serialize(property.getValue()));
                fnod.appendChild(propNode);
            }
        }
    }

    private void addTags(Document doc, Set<String> tags, Element fnod) {
        if ((tags != null) && !tags.isEmpty()) {
            StringBuilder tagStrings = new StringBuilder();
            for (final String tagString : tags) {
                tagStrings.append(tagString);
                tagStrings.append(',');
            }
            if (tagStrings.length() > 0) {
                tagStrings.deleteCharAt(tagStrings.length() - 1);
            }
            final Element tag = doc.createElement(TAGS);
            tag.setTextContent(tagStrings.toString());
            fnod.appendChild(tag);
        }
    }

    protected void writeAttributes(Element node, Element fnod, IFeatureTree feat) {
        fnod.setAttribute(NAME, feat.getFeature().getName().get());
        if (feat.getFeature().isHidden()) {
            fnod.setAttribute(HIDDEN, TRUE);
        }
        if (feat.isMandatory() || feat.getParent().isEmpty()) {
            if ((feat.getParent().isPresent())
                    && feat.getParent().get().getChildrenGroups().get(0).isAnd()) {
                fnod.setAttribute(MANDATORY, TRUE);
            } else if (feat.getParent().isEmpty()) {
                fnod.setAttribute(MANDATORY, TRUE);
            }
        }
        if (feat.getFeature().isAbstract()) {
            fnod.setAttribute(ABSTRACT, TRUE);
        }

        node.appendChild(fnod);
    }
}
