/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.tree.structure;

import static org.junit.jupiter.api.Assertions.*;

import de.featjar.base.tree.Trees;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LabeledTreeTest {

    LabeledTree<String> emptyRoot, root, childA, childB, childC, childD, childE, childF;
    List<LabeledTree<String>> firstChildren, secondChildren, thirdChildren, fourthChildren, fifthChildren;

    @BeforeEach
    public void setUp() {
        emptyRoot = new LabeledTree<>("EmptyRoot");
        root = new LabeledTree<>("Root");
        childA = new LabeledTree<>("A");
        childB = new LabeledTree<>("B");
        childC = new LabeledTree<>("C");
        childD = new LabeledTree<>("D");
        childE = new LabeledTree<>("E");
        childF = new LabeledTree<>("F");

        firstChildren = List.of(childA);
        secondChildren = Arrays.asList(childB, childC);
        thirdChildren = Arrays.asList(childA, childB, childC);
        fourthChildren = List.of(childD);
        fifthChildren = Arrays.asList(childE, childF);

        root.setChildren(thirdChildren);
    }

    @Test
    public void createTreeWithoutData() {
        final String treeToString = "LabeledTree[null]";

        final LabeledTree<String> newRoot = new LabeledTree<>();
        assertNull(newRoot.getLabel());
        assertFalse(newRoot.hasChildren());
        assertNotNull(newRoot.getChildren());
        assertTrue(newRoot.getChildren().isEmpty());
        assertEquals(treeToString, newRoot.toString());
    }

    @Test
    public void createTreeWithData() {
        final String newData = "NewRoot";
        final String treeToString = "LabeledTree[" + newData + "]";

        final LabeledTree<String> newRoot = new LabeledTree<>(newData);
        assertEquals(newData, newRoot.getLabel());
        assertFalse(newRoot.hasChildren());
        assertNotNull(newRoot.getChildren());
        assertTrue(newRoot.getChildren().isEmpty());
        assertEquals(treeToString, newRoot.toString());
    }

    @Test
    public void setData() {
        final String exampleData = "Example";
        final String treeToString = "LabeledTree[" + exampleData + "]";

        final LabeledTree<String> newRoot = new LabeledTree<>();
        newRoot.setLabel(exampleData);
        assertEquals(exampleData, newRoot.getLabel());
        assertEquals(treeToString, newRoot.toString());
    }

    @Test
    public void replaceData() {
        final String exampleData = "Example";
        final String treeToString = "LabeledTree[" + exampleData + "]";

        root.setLabel(exampleData);
        assertEquals(exampleData, root.getLabel());
        assertEquals(treeToString, root.toString());
    }

    @Test
    public void setChildrenOnce() {
        emptyRoot.setChildren(firstChildren);
        assertTrue(emptyRoot.hasChildren());
        assertEquals(firstChildren, emptyRoot.getChildren());
    }

    @Test
    public void setChildrenTwice() {
        emptyRoot.setChildren(firstChildren);
        emptyRoot.setChildren(secondChildren);
        assertTrue(emptyRoot.hasChildren());
        assertEquals(secondChildren, emptyRoot.getChildren());
    }

    @Test
    public void setChildrenThrice() {
        emptyRoot.setChildren(firstChildren);
        emptyRoot.setChildren(secondChildren);
        emptyRoot.setChildren(thirdChildren);
        assertTrue(emptyRoot.hasChildren());
        assertEquals(thirdChildren, emptyRoot.getChildren());
    }

    @Test
    public void removeChildren() {
        root.setChildren(Collections.emptyList());
        assertFalse(root.hasChildren());
        assertEquals(Collections.emptyList(), root.getChildren());
    }

    @Test
    public void removeChildrenIllegally() {
        assertThrows(NullPointerException.class, () -> emptyRoot.setChildren(null));
        emptyRoot.setChildren(firstChildren);
        assertThrows(NullPointerException.class, () -> emptyRoot.setChildren(null));
    }

    @Test
    public void cloneEmptyTree() {
        final LabeledTree<String> clonedRoot = Trees.clone(emptyRoot);

        assertEquals(emptyRoot.getClass(), clonedRoot.getClass());
        assertEquals(0, emptyRoot.getChildrenCount());
        assertEquals(0, clonedRoot.getChildrenCount());
    }

    @Test
    public void cloneTree() {
        final LabeledTree<String> clonedRoot = Trees.clone(root);

        assertEquals(root.getLabel(), clonedRoot.getLabel());
        assertEquals(thirdChildren.size(), root.getChildrenCount());
        assertEquals(thirdChildren.size(), clonedRoot.getChildrenCount());
        final Iterator<? extends LabeledTree<String>> iterator =
                root.getChildren().iterator();
        final Iterator<? extends LabeledTree<String>> clonedIterator =
                clonedRoot.getChildren().iterator();
        while (iterator.hasNext()) {
            assertEquals(iterator.next().getLabel(), clonedIterator.next().getLabel());
        }
    }

    @Test
    public void replaceChildrenInEmptyTree() {
        final List<?> children = emptyRoot.getChildren();

        emptyRoot.replaceChildren(oldChild -> new LabeledTree<>());

        assertSame(children, emptyRoot.getChildren());
        assertTrue(emptyRoot.getChildren().isEmpty());
    }

    @Test
    public void replaceAllChildrenWithNull() {
        final List<?> children = root.getChildren();
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.replaceChildren(oldChild -> null);

        assertSame(children, root.getChildren());
        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildrenCount());
        assertEquals(childA, iterator.next());
        assertEquals(childB, iterator.next());
        assertEquals(childC, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceAllChildrenWithThemselves() {
        final List<?> children = root.getChildren();
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.replaceChildren(oldChild -> oldChild);

        assertSame(children, root.getChildren());
        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildrenCount());
        assertEquals(childA, iterator.next());
        assertEquals(childB, iterator.next());
        assertEquals(childC, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceAllChildrenWithOneOtherChild() {
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.replaceChildren(oldChild -> childD);

        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildrenCount());
        assertEquals(childD, iterator.next());
        assertEquals(childD, iterator.next());
        assertEquals(childD, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceOnlyOneChildWithAnother() {
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.replaceChildren(oldChild -> oldChild == childB ? childD : null);

        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildrenCount());
        assertEquals(childA, iterator.next());
        assertEquals(childD, iterator.next());
        assertEquals(childC, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceChildrenInEmptyTreeWithList() {
        final List<?> children = emptyRoot.getChildren();

        emptyRoot.flatReplaceChildren(oldChild -> List.of(new LabeledTree<>()));

        assertSame(children, emptyRoot.getChildren());
        assertTrue(emptyRoot.getChildren().isEmpty());
    }

    @Test
    public void replaceAllChildrenWithNullList() {
        final List<?> children = root.getChildren();
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.flatReplaceChildren(oldChild -> null);

        assertSame(children, root.getChildren());
        assertDoesNotThrow(() -> {
            failFastIterator.next();
        });
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals(thirdChildren.size(), root.getChildrenCount());
        assertEquals(childA, iterator.next());
        assertEquals(childB, iterator.next());
        assertEquals(childC, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void replaceAllChildrenWithEmptyList() {
        final List<?> children = root.getChildren();
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.flatReplaceChildren(oldChild -> Collections.emptyList());

        assertSame(children, root.getChildren());
        assertThrows(ConcurrentModificationException.class, failFastIterator::next);
        assertTrue(emptyRoot.getChildren().isEmpty());
    }

    @Test
    public void replaceAllChildrenWithOtherLists() {
        final Iterator<?> failFastIterator = root.getChildren().iterator();

        root.flatReplaceChildren(oldChild -> oldChild == childB ? fifthChildren : fourthChildren);

        assertThrows(ConcurrentModificationException.class, failFastIterator::next);
        final Iterator<?> iterator = root.getChildren().iterator();
        assertEquals((2 * fourthChildren.size()) + fifthChildren.size(), root.getChildrenCount());
        assertEquals(childD, iterator.next());
        assertEquals(childE, iterator.next());
        assertEquals(childF, iterator.next());
        assertEquals(childD, iterator.next());
        assertFalse(iterator.hasNext());
    }
}
