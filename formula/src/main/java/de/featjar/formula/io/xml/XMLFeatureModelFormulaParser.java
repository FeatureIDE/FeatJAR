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

import static de.featjar.formula.io.xml.XMLFeatureModelConstants.CONSTRAINTS;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.EXT_FEATURE_MODEL;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.FEATURE_MODEL;
import static de.featjar.formula.io.xml.XMLFeatureModelConstants.STRUCT;

import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.base.io.format.ParseException;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Parses feature model formulas from FeatureIDE XML files.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class XMLFeatureModelFormulaParser extends AXMLFeatureModelParser<IFormula, Literal, IFormula> {

    private LinkedHashSet<String> feature = Sets.empty();
    private List<IFormula> constraints = new ArrayList<>();

    @Override
    protected IFormula parseDocument(Document document) throws ParseException {
        feature = Sets.empty();
        constraints = new ArrayList<>();
        final Element featureModelElement = getDocumentElement(document, FEATURE_MODEL, EXT_FEATURE_MODEL);

        parseFeatureTree(getElement(featureModelElement, STRUCT));

        Result<Element> constraintsElement = getElementResult(featureModelElement, CONSTRAINTS);
        if (constraintsElement.isPresent()) {
            parseConstraints(constraintsElement.get());
        }

        Reference reference = new Reference(constraints.size() == 1 ? constraints.get(0) : new And(constraints));
        reference.setFreeVariables(feature.stream().map(Variable::new).collect(Collectors.toList()));
        return reference;
    }

    @Override
    protected Literal newFeature(
            String name, Literal parentFeature, boolean mandatory, boolean _abstract, boolean hidden)
            throws ParseException {
        if (feature.contains(name)) {
            throw new ParseException("Duplicate feature name: " + name);
        } else {
            feature.add(name);
        }
        Literal literal = new Literal(name);
        if (parentFeature == null) {
            constraints.add(literal);
        } else {
            constraints.add(new Implies(literal, parentFeature));
            if (mandatory) {
                constraints.add(new Implies(parentFeature, literal));
            }
        }
        return literal;
    }

    @Override
    protected void addAndGroup(Literal feature, List<Literal> childFeature) {}

    @Override
    protected void addOrGroup(Literal feature, List<Literal> childFeatures) {
        if (childFeatures.size() == 1) {
            constraints.add(new Implies(feature, childFeatures.get(0)));
        } else {
            constraints.add(new Implies(feature, new Or(childFeatures)));
        }
    }

    @Override
    protected void addAlternativeGroup(Literal feature, List<Literal> childFeatures) {
        if (childFeatures.size() == 1) {
            constraints.add(new Implies(feature, childFeatures.get(0)));
        } else {
            constraints.add(new And(new Implies(feature, new Or(childFeatures)), new AtMost(1, childFeatures)));
        }
    }

    @Override
    protected IFormula newConstraint(IFormula formula) throws ParseException {
        constraints.add(formula);
        return formula;
    }
}
