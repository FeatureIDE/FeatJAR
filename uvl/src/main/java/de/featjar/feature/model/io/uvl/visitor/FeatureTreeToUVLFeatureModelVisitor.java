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
package de.featjar.feature.model.io.uvl.visitor;

import static de.vill.model.FeatureType.BOOL;
import static de.vill.model.FeatureType.INT;
import static de.vill.model.FeatureType.REAL;
import static de.vill.model.FeatureType.STRING;

import de.featjar.base.data.Name;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.FeatureModelAttributes;
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import de.vill.model.Attribute;
import de.vill.model.Cardinality;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.FeatureType;
import de.vill.model.Group;
import de.vill.model.Group.GroupType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts a {@link IFeatureTree} to a {@link de.vill.model.FeatureModel}.
 *
 * @author Andreas Gerasimow
 */
public class FeatureTreeToUVLFeatureModelVisitor implements ITreeVisitor<IFeatureTree, de.vill.model.FeatureModel> {

    public static Feature newFeature(de.vill.model.FeatureModel uvlModel, String variableName) {
        Feature uvlFeature = new Feature(variableName);
        uvlModel.getFeatureMap().put(variableName, uvlFeature);
        return uvlFeature;
    }

    public static Attribute<?> setAttribute(Feature uvlFeature, Name name, Object value) {
        return setAttribute(uvlFeature, name.getNamespace(), name.getName(), value);
    }

    public static Attribute<?> setAttribute(Feature uvlFeature, String nameSpace, String name, Object value) {
        if (name.indexOf('.') >= 0) {
            throw new IllegalArgumentException("Invalid character '.' in attribute name");
        }
        String combinedName = nameSpace != null
                ? nameSpace.replaceAll("_+", "_$0").replace(".", "_") + "_" + name.replaceAll("_+", "_$0")
                : name;
        return uvlFeature.getAttributes().put(combinedName, new Attribute<>(combinedName, value, uvlFeature));
    }

    private de.vill.model.FeatureModel uvlModel;
    private List<Problem> problemList;

    /**
     * Constructs a new visitor.
     */
    public FeatureTreeToUVLFeatureModelVisitor() {
        reset();
    }

    @Override
    public void reset() {
        uvlModel = new de.vill.model.FeatureModel();
        problemList = new ArrayList<>();
    }

    @Override
    public Result<FeatureModel> getResult() {
        return Result.of(uvlModel, problemList);
    }

    @Override
    public TraversalAction lastVisit(List<IFeatureTree> path) {
        final IFeatureTree node = ITreeVisitor.getCurrentNode(path);

        try {
            String[] namespaceAndName = getUVLNamespaceAndName(node.getFeature());
            String namespace, name;
            if (namespaceAndName.length == 1) {
                namespace = null;
                name = namespaceAndName[0];
            } else if (namespaceAndName.length == 2) {
                namespace = namespaceAndName[0];
                name = namespaceAndName[1];
            } else {
                problemList.add(
                        new Problem("Feature " + node.getFeature().getName().get() + " has an illegal name."));
                return TraversalAction.FAIL;
            }

            Feature uvlFeature = newFeature(uvlModel, name);
            if (namespace != null) {
                uvlFeature.setNameSpace(namespace);
            }
            if (path.size() == 1) {
                uvlModel.setRootFeature(uvlFeature);
            }

            try {
                FeatureType uvlFeatureType = getUVLFeatureType(node.getFeature());
                if (uvlFeatureType != BOOL) {
                    uvlFeature.setFeatureType(uvlFeatureType);
                }
            } catch (ParseException e) {
                problemList.add(new Problem(
                        "Type of feature " + node.getFeature().getName().get() + " cannot be parsed."));
                return TraversalAction.FAIL;
            }

            node.getFeature().getAttributes().orElseThrow().entrySet().stream()
                    .filter((entry) -> !entry.getKey().equals(FeatureModelAttributes.NAME))
                    .forEach(entry -> {
                        if (entry.getKey().equals(FeatureModelAttributes.ABSTRACT)) {
                            if (entry.getValue() == Boolean.TRUE) {
                                setAttribute(uvlFeature, null, "abstract", Boolean.TRUE);
                            }
                        } else if (entry.getKey().equals(FeatureModelAttributes.HIDDEN)) {
                            if (entry.getValue() == Boolean.TRUE) {
                                setAttribute(uvlFeature, null, "hidden", Boolean.TRUE);
                            }
                        } else {
                            setAttribute(uvlFeature, entry.getKey().getName(), entry.getValue());
                        }
                    });

            List<FeatureTree.Group> groups = node.getChildrenGroups();

            for (int i = 0; i < groups.size(); i++) {
                List<IFeatureTree> children = node.getChildren(i);
                if (children.isEmpty()) {
                    continue;
                }

                FeatureTree.Group group = groups.get(i);
                GroupType groupType = getUVLGroupType(group);
                if (groupType == null) {
                    List<IFeatureTree> mandatoryChildren =
                            children.stream().filter(IFeatureTree::isMandatory).collect(Collectors.toList());
                    List<IFeatureTree> optionalChildren =
                            children.stream().filter(IFeatureTree::isOptional).collect(Collectors.toList());
                    if (!mandatoryChildren.isEmpty()) {
                        Group mandatoryGroup = new Group(GroupType.MANDATORY);
                        mandatoryGroup.setParentFeature(uvlFeature);
                        mandatoryGroup.getFeatures().addAll(getUVLChildrenFeatures(mandatoryChildren));
                        uvlFeature.addChildren(mandatoryGroup);
                    }
                    if (!optionalChildren.isEmpty()) {
                        Group optionalGroup = new Group(GroupType.OPTIONAL);
                        optionalGroup.setParentFeature(uvlFeature);
                        optionalGroup.getFeatures().addAll(getUVLChildrenFeatures(optionalChildren));
                        uvlFeature.addChildren(optionalGroup);
                    }
                } else if (groupType == GroupType.GROUP_CARDINALITY) {
                    Group uvlGroup = new Group(groupType);
                    uvlGroup.setParentFeature(uvlFeature);
                    uvlGroup.setCardinality(new Cardinality(
                            group.getLowerBound(),
                            group.getUpperBound() == Range.OPEN ? Integer.MAX_VALUE : group.getUpperBound()));
                    uvlGroup.getFeatures().addAll(getUVLChildrenFeatures(children));
                    uvlFeature.addChildren(uvlGroup);
                } else {
                    Group uvlGroup = new Group(groupType);
                    uvlGroup.setParentFeature(uvlFeature);
                    uvlGroup.getFeatures().addAll(getUVLChildrenFeatures(children));
                    uvlFeature.addChildren(uvlGroup);
                }
            }
        } catch (Exception e) {
            problemList.add(new Problem(e.getMessage()));
            return TraversalAction.FAIL;
        }

        return TraversalAction.CONTINUE;
    }

    private List<Feature> getUVLChildrenFeatures(List<? extends IFeatureTree> features) throws Exception {
        List<Feature> children = new ArrayList<>();
        for (IFeatureTree feature : features) {
            if (feature.getFeature().getName().isEmpty()) throw new Exception("Feature has no name.");
            Feature uvlFeature =
                    uvlModel.getFeatureMap().get(feature.getFeature().getName().get());
            children.add(uvlFeature);
        }
        return children;
    }

    private String[] getUVLNamespaceAndName(IFeature feature) throws Exception {
        if (feature.getName().isEmpty()) throw new Exception("Feature has no name.");
        return feature.getName().get().split("::");
    }

    private static GroupType getUVLGroupType(FeatureTree.Group group) {
        if (group.isOr()) {
            return GroupType.OR;
        }
        if (group.isAnd()) {
            return null;
        }
        if (group.isAlternative()) {
            return GroupType.ALTERNATIVE;
        }
        if (group.isCardinalityGroup()) {
            return GroupType.GROUP_CARDINALITY;
        }

        return GroupType.OPTIONAL;
    }

    private static de.vill.model.FeatureType getUVLFeatureType(IFeature feature) throws ParseException {
        Class<?> featureType = feature.getType();
        if (featureType == null) return BOOL;
        else if (featureType == Boolean.class) return BOOL;
        else if (featureType == Integer.class) return INT;
        else if (featureType == Double.class) return REAL;
        else if (featureType == String.class) return STRING;
        else throw new ParseException(featureType.getName());
    }
}
