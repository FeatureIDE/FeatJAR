/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-uvl.
 *
 * uvl is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * uvl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uvl. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-uvl> for further information.
 */
package de.featjar.feature.model.io.uvl;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Attributes;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseException;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.FeatureModelAttributes;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.io.textual.ExpressionParser;
import de.featjar.formula.io.textual.Symbols;
import de.featjar.formula.io.textual.UVLSymbols;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.vill.model.Attribute;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import de.vill.model.constraint.Constraint;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides helper functions for uvl parsing and serialization.
 *
 * @author Andreas Gerasimow
 * @author Sebastion Krieter
 */
public class UVLFeatureModelToFeatureTree {

    /**
     * Converts UVL feature model to FeatJAR feature model.
     * @param uvlFeatureModel The UVL feature model to convert.
     * @return A FeatJAR feature model.
     * @throws ParseException if a parsing error occurs
     */
    public static IFeatureModel createFeatureModel(de.vill.model.FeatureModel uvlFeatureModel) throws ParseException {
        IFeatureModel featureModel = new FeatureModel();
        de.vill.model.Feature rootFeature = uvlFeatureModel.getRootFeature();
        UVLFeatureModelToFeatureTree.createFeatureTree(featureModel, rootFeature);

        return featureModel;
    }

    /**
     * Converts a list of UVL constraints into a list of formulas by parsing each UVL constraint.
     * @param uvlConstraints the UVL constraints
     * @return the parsed constraints in the same order
     * @throws ClassNotFoundException if the class {@link Symbols} cannot be loaded.
     */
    public static List<IFormula> uvlConstraintToFormula(List<Constraint> uvlConstraints) throws ClassNotFoundException {
        List<IFormula> formulas = new ArrayList<>();
        for (Constraint constraint : uvlConstraints) {
            // TODO do not use raw constructor, get instance for
            final ExpressionParser nodeReader = new ExpressionParser();
            ClassLoader.getSystemClassLoader().loadClass("de.featjar.formula.io.textual.Symbols");
            nodeReader.setSymbols(UVLSymbols.INSTANCE);
            nodeReader.setIgnoreMissingFeatures(ExpressionParser.ErrorHandling.KEEP);
            nodeReader.setIgnoreUnparseableSubExpressions(ExpressionParser.ErrorHandling.KEEP);
            Result<IExpression> parse = nodeReader.parse(constraint.toString(false, ""));
            if (parse.isEmpty()) {
                FeatJAR.log().problems(parse.getProblems());
            } else {
                FeatJAR.log().debug(Expressions.print(parse.get()));
            }
            formulas.add((IFormula) parse.get());
        }

        return formulas;
    }

    /**
     * Builds a FeatJAR feature model from a UVL root feature.
     * @param featureModel FeatJAR feature model to build.
     * @param rootUVLFeature UVL root feature from a UVL feature model.
     * @throws ParseException if a parsing error occurs
     */
    private static void createFeatureTree(IFeatureModel featureModel, de.vill.model.Feature rootUVLFeature)
            throws ParseException {
        LinkedList<de.vill.model.Feature> featureStack = new LinkedList<>();
        LinkedList<IFeatureTree> featureTreeStack = new LinkedList<>();

        IFeature rootFeature = createFeature(featureModel, rootUVLFeature);
        IFeatureTree featureTree = featureModel.mutate().addFeatureTreeRoot(rootFeature);

        featureStack.push(rootUVLFeature);
        featureTreeStack.push(featureTree);

        while (!featureStack.isEmpty()) {
            de.vill.model.Feature feature = featureStack.pop();
            IFeatureTree tree = featureTreeStack.pop();

            if (feature.getParentGroup() != null && feature.getParentGroup().GROUPTYPE == Group.GroupType.MANDATORY) {
                tree.mutate().makeMandatory();
            } else if (feature.getParentGroup() != null
                    && feature.getParentGroup().GROUPTYPE == Group.GroupType.OPTIONAL) {
                tree.mutate().makeOptional();
            } else if (feature.getLowerBound() != null) {
                if (feature.getUpperBound() != null) {
                    tree.mutate()
                            .setFeatureCardinality(Range.of(
                                    Integer.parseInt(feature.getLowerBound()),
                                    Integer.parseInt(feature.getUpperBound())));
                } else {
                    tree.mutate().setFeatureCardinality(Range.atLeast(Integer.parseInt(feature.getLowerBound())));
                }
            } else {
                if (feature.getUpperBound() != null) {
                    tree.mutate().setFeatureCardinality(Range.atMost(Integer.parseInt(feature.getUpperBound())));
                } else {
                    tree.mutate().setFeatureCardinality(Range.atMost(1));
                }
            }

            List<de.vill.model.Group> children = feature.getChildren();
            for (de.vill.model.Group group : children) {
                Range groupRange;
                switch (group.GROUPTYPE) {
                    case MANDATORY:
                    case OPTIONAL:
                        groupRange = Range.atLeast(0);
                        break;
                    case ALTERNATIVE:
                        groupRange = Range.exactly(1);
                        break;
                    case OR:
                        groupRange = Range.atLeast(1);
                        break;
                    case GROUP_CARDINALITY:
                        groupRange = Range.of(
                                Integer.parseInt(feature.getLowerBound()), Integer.parseInt(feature.getUpperBound()));
                        break;
                    default:
                        throw new ParseException(String.valueOf(group.GROUPTYPE));
                }
                int groupID = tree.getChildrenGroups().size();
                tree.mutate().addCardinalityGroup(groupRange);
                for (de.vill.model.Feature childFeature : group.getFeatures()) {
                    featureStack.push(childFeature);
                    IFeature child = createFeature(featureModel, childFeature);
                    IFeatureTree childTree = tree.mutate().addFeatureBelow(child);
                    childTree.mutate().setParentGroupID(groupID);
                    featureTreeStack.push(childTree);
                }
            }
        }
    }

    /**
     * Converts UVL feature to FeatJAR feature.
     * @param featureModel The corresponding feature model of the feature.
     * @param uvlFeature The UVL feature to convert.
     * @return A FeatJAR feature.
     * @throws ParseException if a parsing error occurs
     */
    private static IFeature createFeature(IFeatureModel featureModel, de.vill.model.Feature uvlFeature)
            throws ParseException {
        IFeature feature = featureModel.mutate().addFeature(getName(uvlFeature));
        feature.mutate().setAbstract(getAttributeValue(uvlFeature, "abstract", Boolean.FALSE));
        Map<String, Attribute> attributes = uvlFeature.getAttributes();
        for (Entry<String, Attribute> entry : attributes.entrySet()) {
            String uvlAttributeName = entry.getValue().getName();
            Object uvlAttributeValue = Objects.requireNonNull(entry.getValue().getValue());

            de.featjar.base.data.Attribute<? extends Object> attribute;
            if (FeatureModelAttributes.ABSTRACT.getSimpleName().equals(uvlAttributeName)) {
                attribute = FeatureModelAttributes.ABSTRACT;
            } else if (FeatureModelAttributes.HIDDEN.getSimpleName().equals(uvlAttributeName)) {
                attribute = FeatureModelAttributes.HIDDEN;
            } else {
                String[] nameParts = uvlAttributeName.split("(?<!:):(?!:)");
                if (nameParts.length > 2) {
                    throw new ParseException(uvlAttributeName);
                }
                attribute = (nameParts.length == 2)
                        ? Attributes.get(
                                unescapeSeparator(nameParts[0]),
                                unescapeSeparator(nameParts[1]),
                                uvlAttributeValue.getClass())
                        : Attributes.get(unescapeSeparator(uvlAttributeName), uvlAttributeValue.getClass());
            }

            setAttribute(feature, attribute, uvlAttributeValue);
        }
        feature.mutate().setType(getFeatureType(uvlFeature));
        return feature;
    }

    private static <T> void setAttribute(
            IFeature feature, de.featjar.base.data.Attribute<T> attribute, Object uvlAttributeValue) {
        feature.mutate().setAttributeValue(attribute, attribute.cast(uvlAttributeValue));
    }

    private static String unescapeSeparator(String name) {
        return name.replaceAll(":(:+)", "$1");
    }

    /**
     * Converts UVL feature type to FeatJAR feature type.
     * @param uvlFeature UVL feature to retrieve the type.
     * @return FeatJAR feature type.
     * @throws ParseException if a parsing error occurs
     */
    private static Class<?> getFeatureType(de.vill.model.Feature uvlFeature) throws ParseException {
        FeatureType featureType = uvlFeature.getFeatureType();
        if (featureType == null) {
            return Boolean.class;
        } else {
            switch (featureType) {
                case BOOL:
                    return Boolean.class;
                case INT:
                    return Integer.class;
                case REAL:
                    return Double.class;
                case STRING:
                    return String.class;
                default:
                    throw new ParseException(String.valueOf(featureType));
            }
        }
    }

    /**
     * Retrieves name and namespace of a UVL feature.
     * @param feature UVL feature to retrieve the name and namespace.
     * @return Name of the feature. If the feature has a namespace, the return value will be in the following format: {@literal <namespace>::<feature name>}
     */
    private static String getName(de.vill.model.Feature feature) {
        String nameSpace = feature.getNameSpace();
        return (nameSpace != null && !nameSpace.isBlank() ? nameSpace + "::" : "") + feature.getFeatureName();
    }

    /**
     * Retrieves attribute value of a UVL feature.
     * @param feature UVL feature to retrieve the attribute value.
     * @param key Key name of the attribute.
     * @param defaultValue Default value if the attribute does not exist.
     * @return The attribute of the feature.
     * @param <T> the type of the attribute
     */
    @SuppressWarnings("unchecked")
    private static <T> T getAttributeValue(de.vill.model.Feature feature, String key, T defaultValue) {
        return Optional.ofNullable(feature.getAttributes().get(key))
                .map(a -> (T) a.getValue())
                .orElse(defaultValue);
    }
}
