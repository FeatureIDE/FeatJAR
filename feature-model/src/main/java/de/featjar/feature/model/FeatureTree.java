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
package de.featjar.feature.model;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ARootedTree;
import de.featjar.base.tree.structure.ITree;
import de.featjar.feature.model.IFeatureTree.IMutableFeatureTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FeatureTree extends ARootedTree<IFeatureTree> implements IMutableFeatureTree {

    public static final class Group {
        private Range groupCardinality;

        private Group(int lowerBound, int upperBound) {
            this.groupCardinality = Range.of(lowerBound, upperBound);
        }

        public Group(Range groupRange) {
            this.groupCardinality = Range.copy(groupRange);
        }

        private Group(Group otherGroup) {
            this.groupCardinality = Range.copy(otherGroup.groupCardinality);
        }

        private void setBounds(int lowerBound, int upperBound) {
            groupCardinality.setBounds(lowerBound, upperBound);
        }

        public int getLowerBound() {
            return groupCardinality.getLowerBound();
        }

        public int getUpperBound() {
            return groupCardinality.getUpperBound();
        }

        public boolean isCardinalityGroup() {
            return !isAlternative() && !isOr() && !isAnd();
        }

        public boolean isAlternative() {
            return groupCardinality.is(1, 1);
        }

        public boolean isOr() {
            return groupCardinality.is(1, Range.OPEN);
        }

        public boolean isAnd() {
            return groupCardinality.is(0, Range.OPEN);
        }

        public boolean allowsZero() {
            return groupCardinality.getLowerBound() <= 0;
        }

        public boolean hasSameBoundaries(Group group) {
            return groupCardinality.is(group.groupCardinality);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        protected Group clone() {
            return new Group(this);
        }

        @Override
        public String toString() {
            return groupCardinality.toString();
        }
    }

    protected final IFeature feature;

    protected int parentGroupID;

    protected Range cardinality;
    protected ArrayList<Group> childrenGroups;

    protected LinkedHashMap<IAttribute<?>, Object> attributeValues;

    protected FeatureTree() {
        this.feature = null;
        cardinality = Range.of(0, 1);
        childrenGroups = new ArrayList<>(1);
        childrenGroups.add(new Group(Range.atLeast(0)));
    }

    protected FeatureTree(IFeature feature) {
        this.feature = Objects.requireNonNull(feature);
        cardinality = Range.of(0, 1);
        childrenGroups = new ArrayList<>(1);
        childrenGroups.add(new Group(Range.atLeast(0)));
    }

    protected FeatureTree(FeatureTree otherFeatureTree) {
        feature = otherFeatureTree.feature;
        parentGroupID = otherFeatureTree.parentGroupID;
        cardinality = otherFeatureTree.cardinality.clone();
        childrenGroups = new ArrayList<>(otherFeatureTree.childrenGroups.size());
        otherFeatureTree.childrenGroups.stream().map(Group::clone).forEach(childrenGroups::add);
        attributeValues = otherFeatureTree.cloneAttributes();
    }

    @Override
    public IFeature getFeature() {
        return feature;
    }

    @Override
    public int getParentGroupID() {
        return parentGroupID;
    }

    @Override
    public Optional<Group> getParentGroup() {
        return parent == null ? Optional.empty() : parent.getChildrenGroup(parentGroupID);
    }

    @Override
    public List<Group> getChildrenGroups() {
        return Collections.unmodifiableList(childrenGroups);
    }

    @Override
    public int[] getChildrenGroupIDs() {
        return IntStream.range(0, childrenGroups.size())
                .filter(id -> childrenGroups.get(id) != null)
                .toArray();
    }

    @Override
    public Optional<Group> getChildrenGroup(int groupID) {
        return isValidGroupID(groupID) ? Optional.of(getChildrenGroups().get(groupID)) : Optional.empty();
    }

    @Override
    public List<IFeatureTree> getChildren(int groupID) {
        return getChildren().stream()
                .filter(c -> c.getParentGroupID() == groupID)
                .collect(Collectors.toList());
    }

    public List<IFeatureTree> getGroupSiblings() {
        return getParent().map(parent -> parent.getChildren(parentGroupID)).orElse(List.of());
    }

    @Override
    public String toString() {
        return Result.ofNullable(feature).mapResult(IFeature::getName).orElse("?");
    }

    @Override
    public Optional<Map<IAttribute<?>, Object>> getAttributes() {
        return attributeValues == null ? Optional.empty() : Optional.of(Collections.unmodifiableMap(attributeValues));
    }

    @Override
    public List<IFeatureTree> getRoots() {
        return List.of(this);
    }

    @Override
    public int getFeatureCardinalityLowerBound() {
        return cardinality.getLowerBound();
    }

    @Override
    public int getFeatureCardinalityUpperBound() {
        return cardinality.getUpperBound();
    }

    @Override
    public ITree<IFeatureTree> cloneNode() {
        return new FeatureTree(this);
    }

    @Override
    public boolean equalsNode(IFeatureTree other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        FeatureTree otherFeatureTree = (FeatureTree) other;
        return parentGroupID == otherFeatureTree.parentGroupID
                && Objects.equals(feature, otherFeatureTree.feature)
                && Objects.equals(childrenGroups, otherFeatureTree.childrenGroups);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(feature, parentGroupID, childrenGroups);
    }

    @Override
    public int addCardinalityGroup(int lowerBound, int upperBound) {
        return addGroup(new Group(lowerBound, upperBound));
    }

    @Override
    public int addCardinalityGroup(Range groupRange) {
        return addGroup(new Group(groupRange));
    }

    private int addGroup(Group newGroup) {
        for (int i = 1; i < childrenGroups.size(); i++) {
            if (childrenGroups.get(i) == null) {
                childrenGroups.set(i, newGroup);
                return i;
            }
        }
        childrenGroups.add(newGroup);
        return childrenGroups.size() - 1;
    }

    @Override
    public void removeCardinalityGroup(int groupID, int substituteGroupID) {
        if (groupID == 0) {
            throw new IllegalArgumentException("Cannot remove first group!");
        }
        if (groupID < 0) {
            throw new IllegalArgumentException("GroupID must be greater than 0!");
        }
        if (!isValidGroupID(substituteGroupID)) {
            throw new IllegalArgumentException(String.format("Invalid substitute group id %d!", substituteGroupID));
        }
        childrenGroups.set(groupID, null);
        getChildren().stream().filter(c -> c.getParentGroupID() == groupID).forEach(c -> c.mutate()
                .setParentGroupID(substituteGroupID));
    }

    public boolean isValidGroupID(int groupID) {
        return groupID >= 0 && groupID <= childrenGroups.size() && childrenGroups.get(groupID) != null;
    }

    public void setParentGroupID(int groupID) {
        if (parent == null) throw new IllegalArgumentException("Cannot set groupID for root feature!");
        if (groupID < 0) throw new IllegalArgumentException(String.format("groupID must be positive (%d)", groupID));
        if (groupID >= parent.getChildrenGroups().size())
            throw new IllegalArgumentException(
                    String.format("groupID must be smaller than number of groups in parent feature (%d)", groupID));
        this.parentGroupID = groupID;
    }

    @Override
    public void setFeatureCardinality(Range featureCardinality) {
        this.cardinality = Range.copy(featureCardinality);
    }

    @Override
    public void makeMandatory() {
        if (cardinality.getUpperBound() == 0) {
            cardinality = Range.exactly(1);
        } else {
            cardinality.setLowerBound(1);
        }
    }

    @Override
    public void makeOptional() {
        cardinality.setLowerBound(0);
    }

    @Override
    public <S> void setAttributeValue(Attribute<S> attribute, S value) {
        if (value == null) {
            removeAttributeValue(attribute);
            return;
        }
        checkType(attribute, value);
        validate(attribute, value);
        if (attributeValues == null) {
            attributeValues = new LinkedHashMap<>();
        }
        attributeValues.put(attribute, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> S removeAttributeValue(Attribute<S> attribute) {
        if (attributeValues == null) {
            attributeValues = new LinkedHashMap<>();
        }
        return (S) attributeValues.remove(attribute);
    }

    @Override
    public void toCardinalityGroup(int groupID, int lowerBound, int upperBound) {
        Group group = getChildrenGroups().get(groupID);
        if (group != null) {
            group.setBounds(lowerBound, upperBound);
        }
    }
}
