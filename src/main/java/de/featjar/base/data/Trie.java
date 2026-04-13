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
package de.featjar.base.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Basic implementation of a Trie data structure.
 *
 * @author Sebastian Krieter
 */
public class Trie {

    private static class TrieNode {
        LinkedHashMap<Character, TrieNode> children = new LinkedHashMap<>();
        String element;
    }

    private final TrieNode root = new TrieNode();

    public void add(String element) {
        TrieNode node = root;
        for (char c : element.toCharArray()) {
            TrieNode child = node.children.get(c);
            if (child == null) {
                child = new TrieNode();
                node.children.put(c, child);
            }
            node = child;
        }
        node.element = element;
    }

    public boolean has(String element) {
        final TrieNode prefixNode = getPrefixNode(element);
        return prefixNode != null && element.equals(prefixNode.element);
    }

    public boolean hasPrefix(String element) {
        return getPrefixNode(element) != null;
    }

    public void remove(String element) {
        if (element == null) {
            return;
        }
        if (element.isEmpty()) {
            root.element = null;
            return;
        }
        ArrayList<TrieNode> nodePath = new ArrayList<>();
        TrieNode node = root;
        final char[] charArray = element.toCharArray();
        for (char c : charArray) {
            nodePath.add(node);
            node = node.children.get(c);
            if (node == null) {
                return;
            }
        }
        node.element = null;
        for (int i = nodePath.size() - 1; i > 0; i++) {
            final TrieNode trieNode = nodePath.get(i);
            if (trieNode.children.isEmpty()) {
                nodePath.get(i - 1).children.remove(charArray[i]);
            } else {
                break;
            }
        }
    }

    private TrieNode getPrefixNode(String element) {
        if (element == null) {
            return null;
        }
        TrieNode node = root;
        for (char c : element.toCharArray()) {
            node = node.children.get(c);
            if (node == null) {
                return null;
            }
        }
        return node;
    }
}
