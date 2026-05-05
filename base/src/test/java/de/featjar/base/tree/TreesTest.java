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
package de.featjar.base.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.LabeledTree;
import de.featjar.base.tree.visitor.IInOrderTreeVisitor;
import de.featjar.base.tree.visitor.ITreeVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TreesTest {

    LabeledTree<String> emptyRoot, root1, root2, root3;
    List<LabeledTree<String>> preOrderList, postOrderList, innerOrderList;

    @BeforeEach
    public void setUp() {
        emptyRoot = new LabeledTree<>("EmptyRoot");
        root1 = new LabeledTree<>("Root");
        LabeledTree<String> a = new LabeledTree<>("A");
        LabeledTree<String> b = new LabeledTree<>("B");
        LabeledTree<String> c = new LabeledTree<>("C");
        LabeledTree<String> b1 = new LabeledTree<>("B1");
        LabeledTree<String> b2 = new LabeledTree<>("B2");
        LabeledTree<String> b3 = new LabeledTree<>("B3");
        LabeledTree<String> b1a = new LabeledTree<>("B1A");
        LabeledTree<String> b1b = new LabeledTree<>("B1B");
        LabeledTree<String> b1c = new LabeledTree<>("B1C");
        LabeledTree<String> b3a = new LabeledTree<>("B3A");
        LabeledTree<String> b3b = new LabeledTree<>("B3B");
        LabeledTree<String> c1 = new LabeledTree<>("C1");
        LabeledTree<String> c1a = new LabeledTree<>("C1A");
        LabeledTree<String> c1b = new LabeledTree<>("C1B");
        LabeledTree<String> c1c = new LabeledTree<>("C1C");
        LabeledTree<String> c1d = new LabeledTree<>("C1D");

        root1.setChildren(Arrays.asList(a, b, c));
        b.setChildren(Arrays.asList(b1, b2, b3));
        c.setChildren(List.of(c1));
        b1.setChildren(Arrays.asList(b1a, b1b, b1c));
        b3.setChildren(Arrays.asList(b3a, b3b));
        c1.setChildren(Arrays.asList(c1a, c1b, c1c, c1d));

        preOrderList = Arrays.asList(root1, a, b, b1, b1a, b1b, b1c, b2, b3, b3a, b3b, c, c1, c1a, c1b, c1c, c1d);
        postOrderList = Arrays.asList(a, b1a, b1b, b1c, b1, b2, b3a, b3b, b3, b, c1a, c1b, c1c, c1d, c1, c, root1);
        innerOrderList = Arrays.asList(
                a, root1, b1a, b1, b1b, b1, b1c, b, b2, b, b3a, b3, b3b, root1, c, c1a, c1, c1b, c1, c1c, c1, c1d);

        root2 = new LabeledTree<>("Root");
        a = new LabeledTree<>("A");
        b = new LabeledTree<>("B");
        c = new LabeledTree<>("C");
        b1 = new LabeledTree<>("B1");
        b2 = new LabeledTree<>("B2");
        b3 = new LabeledTree<>("B3");
        b1a = new LabeledTree<>("B1A");
        b1b = new LabeledTree<>("B1B");
        b1c = new LabeledTree<>("B1C");
        b3a = new LabeledTree<>("B3A");
        b3b = new LabeledTree<>("B3B");
        c1 = new LabeledTree<>("C1");
        c1a = new LabeledTree<>("C1A");
        c1b = new LabeledTree<>("C1B");
        c1c = new LabeledTree<>("C1C");
        c1d = new LabeledTree<>("C1D");

        root2.setChildren(Arrays.asList(a, b, c));
        b.setChildren(Arrays.asList(b1, b2, b3));
        c.setChildren(List.of(c1));
        b1.setChildren(Arrays.asList(b1a, b1b, b1c));
        b3.setChildren(Arrays.asList(b3a, b3b));
        c1.setChildren(Arrays.asList(c1a, c1b, c1c, c1d));

        root3 = new LabeledTree<>("Root");
        a = new LabeledTree<>("A");
        b = new LabeledTree<>("B");
        c = new LabeledTree<>("C");
        b1 = new LabeledTree<>("B1");
        b2 = new LabeledTree<>("B2");
        b3 = new LabeledTree<>("B3");
        b1a = new LabeledTree<>("B1A");
        b1b = new LabeledTree<>("B1B");
        b1c = new LabeledTree<>("B1C");
        b3a = new LabeledTree<>("B3A");
        b3b = new LabeledTree<>("B3B");
        c1 = new LabeledTree<>("C1");
        c1a = new LabeledTree<>("C1A");
        c1b = new LabeledTree<>("C1B");
        c1c = new LabeledTree<>("C1C");
        c1d = new LabeledTree<>("C1D");

        root3.setChildren(Arrays.asList(a, b, c));
        b.setChildren(Arrays.asList(b3, b2, b1));
        c.setChildren(List.of(c1));
        b1.setChildren(Arrays.asList(b1a, b1b, b1c));
        b3.setChildren(Arrays.asList(b3a, b3b));
        c1.setChildren(Arrays.asList(c1a, c1b, c1c, c1d));
    }

    @Test
    public void preOrderList() {
        assertEquals(preOrderList, root1.getDescendantsAsPreOrder());
    }

    @Test
    public void preOrderStream() {
        assertEquals(preOrderList, root1.preOrderStream().collect(Collectors.toList()));
        assertEquals(Collections.emptyList(), Trees.preOrderStream(null).collect(Collectors.toList()));
    }

    @Test
    public void postOrderStream() {
        assertEquals(postOrderList, root1.postOrderStream().collect(Collectors.toList()));
        assertEquals(Collections.emptyList(), Trees.postOrderStream(null).collect(Collectors.toList()));
    }

    @Test
    public void innerOrderStream() {
        assertEquals(innerOrderList, root1.innerOrderStream().collect(Collectors.toList()));
        assertEquals(Collections.emptyList(), Trees.innerOrderStream(null).collect(Collectors.toList()));
    }

    @Test
    public void equals() {
        assertTrue(Trees.equals(null, null));
        assertTrue(Trees.equals(emptyRoot, emptyRoot));
        assertTrue(Trees.equals(root1, root1));
        assertTrue(Trees.equals(root1, root2));
        assertTrue(Trees.equals(root2, root1));
        assertFalse(Trees.equals(root1, root3));
        assertFalse(Trees.equals(root3, root1));
        assertFalse(Trees.equals(root1, emptyRoot));
        assertFalse(Trees.equals(root3, emptyRoot));
        assertFalse(Trees.equals(emptyRoot, root1));
        assertFalse(Trees.equals(emptyRoot, root3));
        assertFalse(Trees.equals(emptyRoot, null));
        assertFalse(Trees.equals(null, emptyRoot));
    }

    @Test
    public void cloneTree() {
        assertTrue(Trees.equals(emptyRoot, Trees.clone(emptyRoot)));
        assertTrue(Trees.equals(root1, Trees.clone(root1)));
    }

    @Test
    public void traversePrePost() {
        final LinkedHashSet<String> actualCallOrder = new LinkedHashSet<>();
        final LinkedHashSet<String> expectedCallOrder = new LinkedHashSet<>();
        expectedCallOrder.add("reset");
        expectedCallOrder.add("first");
        expectedCallOrder.add("last");
        expectedCallOrder.add("result");

        final ArrayList<LabeledTree<String>> preOrderCollect = new ArrayList<>();
        final ArrayList<LabeledTree<String>> postOrderCollect = new ArrayList<>();

        final Result<Void> result = Trees.traverse(root1, new ITreeVisitor<>() {
            @Override
            public Result<Void> getResult() {
                final Result<Void> result = ITreeVisitor.super.getResult();
                assertTrue(result.isEmpty());
                actualCallOrder.add("result");
                return result;
            }

            @Override
            public void reset() {
                ITreeVisitor.super.reset();
                actualCallOrder.add("reset");
            }

            @Override
            public TraversalAction firstVisit(List<LabeledTree<String>> path) {
                assertEquals(TraversalAction.CONTINUE, ITreeVisitor.super.firstVisit(path));
                actualCallOrder.add("first");
                preOrderCollect.add(ITreeVisitor.getCurrentNode(path));
                return TraversalAction.CONTINUE;
            }

            @Override
            public TraversalAction lastVisit(List<LabeledTree<String>> path) {
                assertEquals(TraversalAction.CONTINUE, ITreeVisitor.super.lastVisit(path));
                actualCallOrder.add("last");
                postOrderCollect.add(ITreeVisitor.getCurrentNode(path));
                return TraversalAction.CONTINUE;
            }
        });

        assertFalse(result.isPresent());
        assertEquals(preOrderList, preOrderCollect);
        assertEquals(postOrderList, postOrderCollect);
        assertEquals(expectedCallOrder, actualCallOrder);

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction firstVisit(List<LabeledTree<?>> path) {
                        throw new RuntimeException();
                    }
                }));

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction lastVisit(List<LabeledTree<?>> path) {
                        throw new RuntimeException();
                    }
                }));

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public Result<Void> getResult() {
                        throw new RuntimeException();
                    }
                }));

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public void reset() {
                        throw new RuntimeException();
                    }
                }));

        assertFalse(Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction firstVisit(List<LabeledTree<?>> path) {
                        return TraversalAction.FAIL;
                    }
                })
                .isPresent());

        assertFalse(Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction lastVisit(List<LabeledTree<?>> path) {
                        return TraversalAction.FAIL;
                    }
                })
                .isPresent());
    }

    @Test
    public void traverseDfs() {
        final LinkedHashSet<String> actualCallOrder = new LinkedHashSet<>();
        final LinkedHashSet<String> expectedCallOrder = new LinkedHashSet<>();
        expectedCallOrder.add("reset");
        expectedCallOrder.add("first");
        expectedCallOrder.add("visit");
        expectedCallOrder.add("last");
        expectedCallOrder.add("result");

        final ArrayList<LabeledTree<String>> preOrderCollect = new ArrayList<>();
        final ArrayList<LabeledTree<String>> postOrderCollect = new ArrayList<>();

        final Result<Void> result = Trees.traverse(root1, new IInOrderTreeVisitor<>() {
            @Override
            public Result<Void> getResult() {
                final Result<Void> result = IInOrderTreeVisitor.super.getResult();
                assertTrue(result.isEmpty());
                actualCallOrder.add("result");
                return result;
            }

            @Override
            public void reset() {
                IInOrderTreeVisitor.super.reset();
                actualCallOrder.add("reset");
            }

            @Override
            public TraversalAction firstVisit(List<LabeledTree<String>> path) {
                assertEquals(TraversalAction.CONTINUE, IInOrderTreeVisitor.super.firstVisit(path));
                actualCallOrder.add("first");
                preOrderCollect.add(ITreeVisitor.getCurrentNode(path));
                return TraversalAction.CONTINUE;
            }

            @Override
            public TraversalAction visit(List<LabeledTree<String>> path) {
                assertEquals(TraversalAction.CONTINUE, IInOrderTreeVisitor.super.visit(path));
                actualCallOrder.add("visit");
                return TraversalAction.CONTINUE;
            }

            @Override
            public TraversalAction lastVisit(List<LabeledTree<String>> path) {
                assertEquals(TraversalAction.CONTINUE, IInOrderTreeVisitor.super.lastVisit(path));
                actualCallOrder.add("last");
                postOrderCollect.add(ITreeVisitor.getCurrentNode(path));
                return TraversalAction.CONTINUE;
            }
        });

        assertFalse(result.isPresent());
        assertEquals(preOrderList, preOrderCollect);
        assertEquals(postOrderList, postOrderCollect);
        assertEquals(expectedCallOrder, actualCallOrder);

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction firstVisit(List<LabeledTree<?>> path) {
                        throw new RuntimeException();
                    }
                }));

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new IInOrderTreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction visit(List<LabeledTree<?>> path) {
                        throw new RuntimeException();
                    }
                }));

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction lastVisit(List<LabeledTree<?>> path) {
                        throw new RuntimeException();
                    }
                }));

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public Result<Void> getResult() {
                        throw new RuntimeException();
                    }
                }));

        assertThrows(
                RuntimeException.class,
                () -> Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public void reset() {
                        throw new RuntimeException();
                    }
                }));

        assertFalse(Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction firstVisit(List<LabeledTree<?>> path) {
                        return TraversalAction.FAIL;
                    }
                })
                .isPresent());

        assertFalse(Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction visit(List<LabeledTree<?>> path) {
                        return TraversalAction.FAIL;
                    }
                })
                .isPresent());

        assertFalse(Trees.traverse(root1, new ITreeVisitor<LabeledTree<?>, Void>() {
                    @Override
                    public TraversalAction lastVisit(List<LabeledTree<?>> path) {
                        return TraversalAction.FAIL;
                    }
                })
                .isPresent());
    }
}
