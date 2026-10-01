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

import de.featjar.base.data.Attributes;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseException;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.FeatureModelAttributes;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.DefLiteral;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NotEquals;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.function.integer.IntegerDivide;
import de.featjar.formula.structure.term.function.integer.IntegerMultiply;
import de.featjar.formula.structure.term.function.string.StringLength;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import de.vill.model.Attribute;
import de.vill.model.Feature;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import de.vill.model.constraint.AndConstraint;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.EqualEquationConstraint;
import de.vill.model.constraint.EquivalenceConstraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.constraint.GreaterEqualsEquationConstraint;
import de.vill.model.constraint.GreaterEquationConstraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.constraint.LowerEqualsEquationConstraint;
import de.vill.model.constraint.LowerEquationConstraint;
import de.vill.model.constraint.NotConstraint;
import de.vill.model.constraint.NotEqualsEquationConstraint;
import de.vill.model.constraint.OrConstraint;
import de.vill.model.constraint.ParenthesisConstraint;
import de.vill.model.expression.AddExpression;
import de.vill.model.expression.DivExpression;
import de.vill.model.expression.Expression;
import de.vill.model.expression.LengthAggregateFunctionExpression;
import de.vill.model.expression.LiteralExpression;
import de.vill.model.expression.MulExpression;
import de.vill.model.expression.NumberExpression;
import de.vill.model.expression.ParenthesisExpression;
import de.vill.model.expression.StringExpression;
import de.vill.model.expression.SubExpression;
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
     */
    public static Result<IFeatureModel> toFeatureModel(de.vill.model.FeatureModel uvlFeatureModel) {
        try {
            IFeatureModel featureModel = new FeatureModel();

            convertFeatureTree(featureModel, uvlFeatureModel.getRootFeature());
            for (Constraint uvlConstraint : uvlFeatureModel.getConstraints()) {
                convertConstraint(featureModel, uvlConstraint);
            }

            return Result.of(featureModel);
        } catch (Exception e) {
            return Result.empty(e);
        }
    }

    private static void convertFeatureTree(IFeatureModel featureModel, Feature rootUVLFeature)
            throws UVLConversionException {
        IFeature root = createFeature(featureModel, rootUVLFeature);
        if (root.getAttributeValue(UVLFormulaFormat.PSEUDO_ROOT_ATTRIBUTE).valueEquals(Boolean.TRUE)) {
            featureModel.mutate().removeFeature(root);
            for (Group group : rootUVLFeature.getChildren()) {
                for (Feature childFeature : group.getFeatures()) {
                    IFeatureTree featureTreeRoot =
                            featureModel.mutate().addFeatureTreeRoot(createFeature(featureModel, childFeature));
                    convertFeatureTreeRoot(featureModel, childFeature, featureTreeRoot);
                }
            }
        } else {
            IFeatureTree featureTreeRoot = featureModel.mutate().addFeatureTreeRoot(root);
            convertFeatureTreeRoot(featureModel, rootUVLFeature, featureTreeRoot);
            featureTreeRoot.mutate().makeMandatory();
        }
    }

    private static void convertFeatureTreeRoot(
            IFeatureModel featureModel, Feature rootUVLFeature, IFeatureTree featureTree)
            throws UVLConversionException {
        LinkedList<Feature> featureStack = new LinkedList<>();
        LinkedList<IFeatureTree> featureTreeStack = new LinkedList<>();

        featureStack.push(rootUVLFeature);
        featureTreeStack.push(featureTree);

        while (!featureStack.isEmpty()) {
            Feature feature = featureStack.pop();
            IFeatureTree tree = featureTreeStack.pop();

            for (Group group : feature.getChildren()) {
                int groupID = tree.getChildrenGroups().size();
                tree.mutate().addCardinalityGroup(convertGroupCardinality(group));

                for (Feature childFeature : group.getFeatures()) {
                    IFeatureTree childTree = tree.mutate().addFeatureBelow(createFeature(featureModel, childFeature));
                    childTree.mutate().setParentGroupID(groupID);
                    childTree.mutate().setFeatureCardinality(convertFeatureCardinality(childFeature));

                    featureStack.push(childFeature);
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
    private static IFeature createFeature(IFeatureModel featureModel, Feature uvlFeature)
            throws UVLConversionException {
        IFeature feature = featureModel.mutate().addFeature(getName(uvlFeature));
        feature.mutate().setAbstract(getAttributeValue(uvlFeature, "abstract", Boolean.FALSE));
        Map<String, Attribute<?>> attributes = uvlFeature.getAttributes();
        for (Entry<String, Attribute<?>> entry : attributes.entrySet()) {
            String uvlAttributeName = entry.getValue().getName();
            Object uvlAttributeValue = Objects.requireNonNull(entry.getValue().getValue());

            de.featjar.base.data.Attribute<? extends Object> attribute;
            if (FeatureModelAttributes.ABSTRACT.getSimpleName().equals(uvlAttributeName)) {
                attribute = FeatureModelAttributes.ABSTRACT;
            } else if (FeatureModelAttributes.HIDDEN.getSimpleName().equals(uvlAttributeName)) {
                attribute = FeatureModelAttributes.HIDDEN;
            } else {
                uvlAttributeName =
                        uvlAttributeName.replaceAll("(?<!_)_(?!_)", ".").replaceAll("_(_+)", "$1");
                int nameSpaceSeparatorIndex = uvlAttributeName.lastIndexOf('.');
                attribute = (nameSpaceSeparatorIndex < 0)
                        ? Attributes.get(uvlAttributeName, uvlAttributeValue.getClass())
                        : Attributes.get(
                                uvlAttributeName.substring(0, nameSpaceSeparatorIndex),
                                uvlAttributeName.substring(nameSpaceSeparatorIndex + 1),
                                uvlAttributeValue.getClass());
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

    private static Range convertFeatureCardinality(Feature feature) {
        if (feature.getCardinality() != null) {
            if (feature.getCardinality().upper < Integer.MAX_VALUE) {
                return Range.of(feature.getCardinality().lower, feature.getCardinality().upper);
            } else {
                return Range.atLeast(feature.getCardinality().lower);
            }
        } else {
            if (feature.getParentGroup() != null && feature.getParentGroup().GROUPTYPE == Group.GroupType.MANDATORY) {
                return Range.exactly(1);
            } else {
                return Range.atMost(1);
            }
        }
    }

    private static Range convertGroupCardinality(Group group) throws UVLConversionException {
        switch (group.GROUPTYPE) {
            case MANDATORY:
            case OPTIONAL:
                return Range.atLeast(0);
            case ALTERNATIVE:
                return Range.exactly(1);
            case OR:
                return Range.atLeast(1);
            case GROUP_CARDINALITY:
                return Range.of(group.getCardinality().lower, group.getCardinality().upper);
            default:
                throw new UVLConversionException(String.valueOf(group.GROUPTYPE));
        }
    }

    private static void convertConstraint(IFeatureModel featureModel, Constraint uvlConstraint)
            throws UVLConversionException {
        List<DefLiteral> literalList = new ArrayList<>();
        IFormula convertedUVLConstraint = (IFormula) parseUVLConstraintRecursively(uvlConstraint, literalList);
        featureModel
                .mutate()
                .addConstraint(
                        literalList.isEmpty()
                                ? convertedUVLConstraint
                                : new Implies(new And(literalList), convertedUVLConstraint));
    }

    private static IExpression parseUVLConstraintRecursively(
            Constraint uvlConstraint, List<DefLiteral> dependenciesList) throws UVLConversionException {
        return switch (uvlConstraint) {
            case LiteralConstraint literalConstraint -> {
                if (literalConstraint.getReference() instanceof Feature uvlFeature) {
                    yield new Literal(uvlFeature.getFeatureName());
                }
                throw new UVLConversionException(
                        literalConstraint.getReference().getClass().getSimpleName() + " is not supported.");
            }
            case ParenthesisConstraint parenthesisConstraint ->
                parseUVLConstraintRecursively(parenthesisConstraint.getContent(), dependenciesList);
            case ImplicationConstraint implicationConstraint ->
                new Implies(parseBooleanChildren(dependenciesList, implicationConstraint));
            case NotConstraint notConstraint ->
                new Not((IFormula) parseUVLConstraintRecursively(notConstraint.getContent(), dependenciesList));
            case AndConstraint andConstraint -> new And(parseBooleanChildren(dependenciesList, andConstraint));
            case OrConstraint orConstraint -> new Or(parseBooleanChildren(dependenciesList, orConstraint));
            case EquivalenceConstraint equivalenceConstraint ->
                new BiImplies(parseBooleanChildren(dependenciesList, equivalenceConstraint));
            case EqualEquationConstraint equalConstraint ->
                new Equals(parseNonBooleanChildren(dependenciesList, equalConstraint));
            case LowerEqualsEquationConstraint lowerEqualsConstraint ->
                new LessEqual(parseNonBooleanChildren(dependenciesList, lowerEqualsConstraint));
            case GreaterEqualsEquationConstraint greaterEqualConstraint ->
                new GreaterEqual(parseNonBooleanChildren(dependenciesList, greaterEqualConstraint));
            case NotEqualsEquationConstraint notEqualsConstraint ->
                new NotEquals(parseNonBooleanChildren(dependenciesList, notEqualsConstraint));
            case LowerEquationConstraint lowerConstraint ->
                new LessThan(parseNonBooleanChildren(dependenciesList, lowerConstraint));
            case GreaterEquationConstraint greaterConstraint ->
                new GreaterThan(parseNonBooleanChildren(dependenciesList, greaterConstraint));
            default ->
                throw new UVLConversionException(uvlConstraint.getClass().getSimpleName() + " is not supported.");
        };
    }

    private static List<IFormula> parseBooleanChildren(List<DefLiteral> dependenciesList, Constraint constraint)
            throws UVLConversionException {
        List<Constraint> constraintSubParts = constraint.getConstraintSubParts();
        List<IFormula> children = new ArrayList<>(constraintSubParts.size());
        for (Constraint child : constraintSubParts) {
            children.add((IFormula) parseUVLConstraintRecursively(child, dependenciesList));
        }
        return children;
    }

    private static List<ITerm> parseNonBooleanChildren(
            List<DefLiteral> dependenciesList, ExpressionConstraint constraint) throws UVLConversionException {
        List<Expression> constraintSubParts = constraint.getExpressionSubParts();
        List<ITerm> children = new ArrayList<>(constraintSubParts.size());
        for (Expression child : constraintSubParts) {
            children.add(parseExpressionConstraint(child, dependenciesList));
        }
        return children;
    }

    private static ITerm parseExpressionConstraint(Expression expression, List<DefLiteral> dependenciesList)
            throws UVLConversionException {
        return switch (expression) {
            case ParenthesisExpression parenthesisExpression ->
                parseExpressionConstraint(parenthesisExpression.getContent(), dependenciesList);
            case NumberExpression numberExpression -> new Constant(numberExpression.getNumber());
            case StringExpression stringExpression -> new Constant(stringExpression.getString(), String.class);
            case LengthAggregateFunctionExpression lenghtAggregateExpression -> {
                Variable variable =
                        new Variable(lenghtAggregateExpression.getReference().getIdentifier(), String.class);
                dependenciesList.add(new DefLiteral(variable));
                yield new StringLength(variable);
            }
            case LiteralExpression literalExpression ->
                switch (literalExpression.getContent()) {
                    case Attribute<?> uvlAttribute -> new Constant(uvlAttribute.getValue());
                    case Feature uvlFeature -> {
                        Class<?> variableType = getFeatureType(uvlFeature);
                        Variable variable = new Variable(uvlFeature.getFeatureName(), variableType);
                        if (variableType != Boolean.class) {
                            dependenciesList.add(new DefLiteral(variable));
                        }
                        yield variable;
                    }
                    default -> throw new UVLConversionException(literalExpression.getContent() + " is not supported.");
                };
            case AddExpression addExpression ->
                new IntegerAdd(
                        parseExpressionConstraint(addExpression.getLeft(), dependenciesList),
                        parseExpressionConstraint(addExpression.getRight(), dependenciesList));
            case SubExpression subExpression ->
                new IntegerAdd(
                        parseExpressionConstraint(subExpression.getLeft(), dependenciesList),
                        new IntegerMultiply(
                                new Constant(-1l),
                                parseExpressionConstraint(subExpression.getRight(), dependenciesList)));
            case MulExpression mulExpression ->
                new IntegerMultiply(
                        parseExpressionConstraint(mulExpression.getLeft(), dependenciesList),
                        parseExpressionConstraint(mulExpression.getRight(), dependenciesList));
            case DivExpression divExpression ->
                new IntegerDivide(
                        parseExpressionConstraint(divExpression.getLeft(), dependenciesList),
                        parseExpressionConstraint(divExpression.getRight(), dependenciesList));
            default -> throw new UVLConversionException(expression.getClass().getSimpleName() + " is not supported.");
        };
    }

    /**
     * Converts UVL feature type to FeatJAR feature type.
     * @param uvlFeature UVL feature to retrieve the type.
     * @return FeatJAR feature type.
     * @throws ParseException if a parsing error occurs
     */
    private static Class<?> getFeatureType(Feature uvlFeature) throws UVLConversionException {
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
                    throw new UVLConversionException(String.valueOf(featureType));
            }
        }
    }

    /**
     * Retrieves name and namespace of a UVL feature.
     * @param feature UVL feature to retrieve the name and namespace.
     * @return Name of the feature. If the feature has a namespace, the return value will be in the following format: {@literal <namespace>::<feature name>}
     */
    private static String getName(Feature feature) {
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
    private static <T> T getAttributeValue(Feature feature, String key, T defaultValue) {
        return Optional.ofNullable(feature.getAttributes().get(key))
                .map(a -> (T) a.getValue())
                .orElse(defaultValue);
    }
}
