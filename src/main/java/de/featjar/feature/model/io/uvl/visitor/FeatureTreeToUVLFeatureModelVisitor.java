/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.FeatureModelAttributes;
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import de.vill.model.Attribute;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts a {@link IFeatureTree} to a {@link de.vill.model.FeatureModel}.
 *
 * @author Andreas Gerasimow
 */
public class FeatureTreeToUVLFeatureModelVisitor implements ITreeVisitor<IFeatureTree, de.vill.model.FeatureModel> {

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
            String name;
            String namespace = "";
            if (namespaceAndName.length == 1) {
                name = namespaceAndName[0];
            } else if (namespaceAndName.length == 2) {
                namespace = namespaceAndName[0];
                name = namespaceAndName[1];
            } else {
                problemList.add(
                        new Problem("Feature " + node.getFeature().getName().get() + " has an illegal name."));
                return TraversalAction.FAIL;
            }

            de.vill.model.Feature uvlFeature = new de.vill.model.Feature(name);

            uvlFeature.setNameSpace(namespace);
            try {
                uvlFeature.setFeatureType(getUVLFeatureType(node.getFeature()));
            } catch (ParseException e) {
                problemList.add(new Problem(
                        "Type of feature " + node.getFeature().getName().get() + " cannot be parsed."));
                return TraversalAction.FAIL;
            }

            node.getFeature().getAttributes().orElseThrow().entrySet().stream()
                    .filter((entry) -> !entry.getKey().equals(FeatureModelAttributes.NAME))
                    .forEach(entry -> {
                        Name attributeName = entry.getKey().getName();
                        String uvlAttributeName = (entry.getKey().equals(FeatureModelAttributes.ABSTRACT))
                                ? escapeSeparator(attributeName.getName())
                                : escapeSeparator(attributeName.getNamespace()) + ":"
                                        + escapeSeparator(attributeName.getName());
                        uvlFeature
                                .getAttributes()
                                .put(uvlAttributeName, new Attribute<>(uvlAttributeName, entry.getValue()));
                    });

            List<FeatureTree.Group> groups = node.getChildrenGroups();

            for (int i = 0; i < groups.size(); i++) {
                List<IFeatureTree> children = node.getChildren(i);
                if (children.isEmpty()) {
                    continue;
                }

                FeatureTree.Group group = groups.get(i);
                Group.GroupType groupType = getUVLGroupType(group);

                if (groupType == null) {
                    List<IFeatureTree> mandatoryChildren =
                            children.stream().filter(IFeatureTree::isMandatory).collect(Collectors.toList());
                    List<IFeatureTree> optionalChildren =
                            children.stream().filter(IFeatureTree::isOptional).collect(Collectors.toList());
                    if (!mandatoryChildren.isEmpty()) {
                        de.vill.model.Group mandatoryGroup = new de.vill.model.Group(Group.GroupType.MANDATORY);
                        mandatoryGroup.setParentFeature(uvlFeature);
                        mandatoryGroup.getFeatures().addAll(getUVLChildrenFeatures(mandatoryChildren));
                        uvlFeature.addChildren(mandatoryGroup);
                    }
                    if (!optionalChildren.isEmpty()) {
                        de.vill.model.Group optionalGroup = new de.vill.model.Group(Group.GroupType.OPTIONAL);
                        optionalGroup.setParentFeature(uvlFeature);
                        optionalGroup.getFeatures().addAll(getUVLChildrenFeatures(optionalChildren));
                        uvlFeature.addChildren(optionalGroup);
                    }
                } else {
                    de.vill.model.Group uvlGroup = new de.vill.model.Group(groupType);
                    uvlGroup.setParentFeature(uvlFeature);
                    uvlGroup.setLowerBound(String.valueOf(group.getLowerBound()));
                    uvlGroup.setUpperBound(String.valueOf(group.getUpperBound()));
                    uvlGroup.getFeatures().addAll(getUVLChildrenFeatures(children));
                    uvlFeature.addChildren(uvlGroup);
                }
            }

            uvlModel.getFeatureMap().put(name, uvlFeature);
            if (node.getParent().isEmpty()) {
                uvlModel.setRootFeature(uvlFeature);
            }
        } catch (Exception e) {
            problemList.add(new Problem(e.getMessage()));
            return TraversalAction.FAIL;
        }

        return TraversalAction.CONTINUE;
    }

    private String escapeSeparator(String name) {
        return name.replace(":", "::");
    }

    private List<de.vill.model.Feature> getUVLChildrenFeatures(List<? extends IFeatureTree> features) throws Exception {
        List<de.vill.model.Feature> children = new ArrayList<>();
        for (IFeatureTree feature : features) {
            if (feature.getFeature().getName().isEmpty()) throw new Exception("Feature has no name.");
            de.vill.model.Feature uvlFeature =
                    uvlModel.getFeatureMap().get(feature.getFeature().getName().get());
            children.add(uvlFeature);
        }
        return children;
    }

    private String[] getUVLNamespaceAndName(IFeature feature) throws Exception {
        if (feature.getName().isEmpty()) throw new Exception("Feature has no name.");
        return feature.getName().get().split("::");
    }

    private Group.GroupType getUVLGroupType(FeatureTree.Group group) {
        if (group.isOr()) {
            return Group.GroupType.OR;
        }
        if (group.isAnd()) {
            return null;
        }
        if (group.isAlternative()) {
            return Group.GroupType.ALTERNATIVE;
        }
        if (group.isCardinalityGroup()) {
            return Group.GroupType.GROUP_CARDINALITY;
        }

        return Group.GroupType.OPTIONAL;
    }

    private de.vill.model.FeatureType getUVLFeatureType(IFeature feature) throws ParseException {
        Class<?> featureType = feature.getType();
        if (featureType == null) return BOOL;
        else if (featureType == Boolean.class) return BOOL;
        else if (featureType == Integer.class) return INT;
        else if (featureType == Double.class) return REAL;
        else if (featureType == String.class) return STRING;
        else throw new ParseException(featureType.getName());
    }
}
