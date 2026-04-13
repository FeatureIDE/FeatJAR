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

import de.featjar.base.data.IAttributable;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ARootedTree;
import de.featjar.base.tree.structure.IRootedTree;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.mixins.IHasFeatureTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An ordered {@link ARootedTree} labeled with {@link Feature features}.
 * Implements some concepts from feature-oriented domain analysis, such as
 * mandatory/optional features and groups.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public interface IFeatureTree extends IRootedTree<IFeatureTree>, IAttributable, IHasFeatureTree {

    // TODO the FeatureTreeRoot has no feature, this should return an Optional
    IFeature getFeature();

    /**
     * {@return the groups of this feature's children.}
     * This list may contain {@code null}.
     */
    List<Group> getChildrenGroups();

    /**
     * {@return the group of this feature's children with the given id.}
     * @param groupID the groupID
     */
    default Optional<Group> getChildrenGroup(int groupID) {
        return isValidGroupID(groupID) ? Optional.of(getChildrenGroups().get(groupID)) : Optional.empty();
    }

    default List<Pair<Group, List<IFeatureTree>>> getGroupedChildren() {
        Map<Integer, List<IFeatureTree>> groupedFeatures =
                getChildren().stream().collect(Collectors.groupingBy(IFeatureTree::getParentGroupID));
        List<Pair<Group, List<IFeatureTree>>> featureGroups = new ArrayList<>(groupedFeatures.size());
        for (Entry<Integer, List<IFeatureTree>> entry : groupedFeatures.entrySet()) {
            featureGroups.add(new Pair<>(getChildrenGroup(entry.getKey()).get(), entry.getValue()));
        }
        return featureGroups;
    }

    /**
     * {@return the group IDs of this feature's children.}
     * Contains only {@link #isValidGroupID valid} group IDs.
     */
    int[] getChildrenGroupIDs();

    boolean isValidGroupID(int groupID);

    /**
     * {@return all children within the group with the given id.}
     * @param groupID the groupID
     */
    default List<IFeatureTree> getChildren(int groupID) {
        return getChildren().stream()
                .filter(c -> c.getParentGroupID() == groupID)
                .collect(Collectors.toList());
    }

    default IFeatureTree getFeatureTreeRoot() {
        IFeatureTree currentTree = null;
        IFeatureTree parentTree = (IFeatureTree) this;
        while (!(parentTree instanceof PseudoFeatureTreeRoot)) {
            currentTree = parentTree;
            if (currentTree.hasParent()) {
                parentTree = currentTree.getParent().get();
            } else {
                break;
            }
        }
        return currentTree;
    }

    /**
     * {@return the group of this feature. (The group of this feature's parent, in which this feature is contained.)}
     * @see #getParentGroupID()
     */
    Optional<Group> getParentGroup();

    /**
     * {@return the id of the group of this feature.}
     * @see #getParentGroup()
     */
    int getParentGroupID();

    int getFeatureCardinalityLowerBound();

    int getFeatureCardinalityUpperBound();

    default boolean isOptional() {
        return getFeatureCardinalityLowerBound() <= 0;
    }

    default boolean isMandatory() {
        return getFeatureCardinalityLowerBound() > 0;
    }

    default boolean isMultiple() {
        return getFeatureCardinalityUpperBound() > 1;
    }

    default IMutableFeatureTree mutate() {
        return (IMutableFeatureTree) this;
    }

    static interface IMutableFeatureTree extends IFeatureTree, IMutatableAttributable {

        default IFeatureTree addFeatureBelow(IFeature newFeature) {
            return addFeatureBelow(newFeature, getChildrenCount(), 0);
        }

        default IFeatureTree addFeatureBelow(IFeature newFeature, int index) {
            return addFeatureBelow(newFeature, index, 0);
        }

        default IFeatureTree addFeatureBelow(IFeature newFeature, int index, int groupID) {
            FeatureTree newTree = new FeatureTree(newFeature);
            addChild(index, newTree);
            newTree.setParentGroupID(groupID);
            return newTree;
        }

        default IFeatureTree addFeatureAbove(IFeature newFeature) {
            FeatureTree newTree = new FeatureTree(newFeature);
            Result<IFeatureTree> parent = getParent();
            if (parent.isPresent()) {
                parent.get().replaceChild(this, newTree);
            }
            newTree.addChild(this);
            setParentGroupID(0);
            return newTree;
        }

        default void removeFromTree() {
            IMutableFeatureTree parent = getParent()
                    .orElseThrow(p -> new IllegalStateException("Cannot remove root feature"))
                    .mutate();

            int childIndex = parent.getChildIndex(this).orElseThrow();
            Group group = getParentGroup().get();
            parent.removeChild(childIndex);

            for (int groupID : getChildrenGroupIDs()) {
                List<IFeatureTree> children = getChildren(groupID);
                if (children.isEmpty()) {
                    continue;
                }
                Group childrenGroup = getChildrenGroup(groupID).get();
                if (group.hasSameBoundaries(childrenGroup)
                        && group.getLowerBound() <= 1
                        && (group.getUpperBound() == 1 || group.getUpperBound() == Range.OPEN)) {
                    for (IFeatureTree child : children) {
                        parent.addChild(childIndex++, child);
                        child.mutate().setParentGroupID(getParentGroupID());
                    }
                } else {
                    parent.addCardinalityGroup(childrenGroup.getLowerBound(), childrenGroup.getUpperBound());
                    for (IFeatureTree child : children) {
                        parent.addChild(childIndex++, child);
                        child.mutate().setParentGroupID(groupID);
                    }
                    parent.toCardinalityGroup(getParentGroupID(), Range.atLeast(0));
                }
            }
        }

        void setParentGroupID(int groupID);

        void setFeatureCardinality(Range featureRange);

        void makeMandatory();

        void makeOptional();

        int addCardinalityGroup(int lowerBound, int upperBound);

        default int addCardinalityGroup(Range groupRange) {
            return addCardinalityGroup(groupRange.getLowerBound(), groupRange.getUpperBound());
        }

        default int addAndGroup() {
            return addCardinalityGroup(0, Range.OPEN);
        }

        default int addAlternativeGroup() {
            return addCardinalityGroup(1, 1);
        }

        default int addOrGroup() {
            return addCardinalityGroup(1, Range.OPEN);
        }

        /**
         * Removes a cardinality group and moves all features in this group to the group with the given substitute id.
         * The first group can never be removed.
         * The substitute id must be a {@link #isValidGroupID valid} group id in this feature tree node.
         *
         * @param groupID the group id of the group to remove
         * @param substituteGroupID the new group id of all features within the removed group
         *
         * @see #removeCardinalityGroup(int)
         */
        void removeCardinalityGroup(int groupID, int substituteGroupID);

        /**
         * Removes a cardinality group and moves all features in this group to the first group in this feature tree node.
         * The first group can never be removed.
         *
         * @param groupID the group id of the group to remove
         *
         * @see #removeCardinalityGroup(int, int)
         */
        default void removeCardinalityGroup(int groupID) {
            removeCardinalityGroup(groupID, 0);
        }

        /**
         * Changes the cardinality of the children group with the given id.
         *
         * @param groupID          the id of the group to change
         * @param lowerBound       the new lower bound
         * @param upperBound       the new upper bound
         */
        void toCardinalityGroup(int groupID, int lowerBound, int upperBound);

        /**
         * Changes the cardinality of the children group with the given id.
         *
         * @param groupID          the id of the group to change
         * @param groupCardinality the new cardinality
         */
        default void toCardinalityGroup(int groupID, Range groupCardinality) {
            toCardinalityGroup(groupID, groupCardinality.getLowerBound(), groupCardinality.getUpperBound());
        }

        /**
         * Change children group to and group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(groupID, 0, Range.OPEN)}.
         *
         * @param groupID the id of the group to change
         */
        default void toAndGroup(int groupID) {
            toCardinalityGroup(groupID, 0, Range.OPEN);
        }

        /**
         * Change children group to or group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(groupID, 1, Range.OPEN)}.
         *
         * @param groupID the id of the group to change
         */
        default void toOrGroup(int groupID) {
            toCardinalityGroup(groupID, 1, Range.OPEN);
        }

        /**
         * Change children group to alternative group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(groupID, 1, 1)}.
         *
         * @param groupID the id of the group to change
         */
        default void toAlternativeGroup(int groupID) {
            toCardinalityGroup(groupID, 1, 1);
        }

        /**
         * Changes the cardinality of the first children group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, groupCardinality)}.
         *
         * @param groupCardinality the new cardinality
         */
        default void toCardinalityGroup(Range groupCardinality) {
            toCardinalityGroup(0, groupCardinality);
        }

        /**
         * Change first children group to cardinality group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, lowerBound, upperBound)}.
         *
         * @param lowerBound       the new lower bound
         * @param upperBound       the new upper bound
         */
        default void toCardinalityGroup(int lowerBound, int upperBound) {
            toCardinalityGroup(0, lowerBound, upperBound);
        }

        /**
         * Change first children group to and group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, 0, Range.OPEN)}.
         *
         */
        default void toAndGroup() {
            toAndGroup(0);
        }

        /**
         * Change first children group to alternative group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, 1, Range.OPEN)}.
         */
        default void toAlternativeGroup() {
            toAlternativeGroup(0);
        }

        /**
         * Change first children group to or group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, 1, 1)}.
         */
        default void toOrGroup() {
            toOrGroup(0);
        }
    }
}
