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
package de.featjar.base.io.graphviz;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.tree.structure.ITree;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serializes trees to GraphViz DOT files.
 *
 * @param <T> the type of the read/written object
 *
 * @author Elias Kuiter
 */
public class GraphVizTreeFormat<T extends ITree<T>> implements IFormat<T> {
    protected boolean includeRoot = true;

    public boolean isIncludeRoot() {
        return includeRoot;
    }

    public void setIncludeRoot(boolean includeRoot) {
        this.includeRoot = includeRoot;
    }

    @Override
    public String getFileExtension() {
        return "dot";
    }

    @Override
    public String getName() {
        return "GraphViz";
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> serialize(T tree) {
        List<? extends T> descendants = tree.getDescendantsAsLevelOrder();
        return Result.of(String.format(
                "digraph {%n%s%s%s%n%s%n%s%n}",
                globalOptions("graph"),
                globalOptions(
                        "node",
                        option("fontname", "Helvetica"),
                        option("style", "filled"),
                        option("fillcolor", "beige"),
                        option("shape", "record")),
                globalOptions("edge", option("fontname", "Helvetica"), option("fontsize", "10")),
                descendants.stream()
                        .skip(includeRoot ? 0 : 1)
                        .distinct()
                        .map(this::getNode)
                        .collect(Collectors.joining("\n")),
                descendants.stream()
                        .skip(includeRoot ? 0 : 1)
                        .distinct()
                        .map(this::getEdge)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining("\n"))));
    }

    protected String getNodeIdentifier(T tree) {
        return String.valueOf(tree.hashCode());
        // todo: technically, two objects can be different but have the same hash code.
        //  so node identifiers should also take .equals into account.
    }

    protected String getNodeOptions(T tree) {
        return options(option("label", tree.toString()));
    }

    protected String getEdgeOptions(T parent, T child, int idx) {
        return "";
    }

    protected String getNode(T tree) {
        return String.format("  %s%s;", quote(getNodeIdentifier(tree)), getNodeOptions(tree));
    }

    protected String getEdge(T parent) {
        StringBuilder sb = new StringBuilder();
        List<? extends T> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            T child = children.get(i);
            String edge = getEdge(parent, child, getEdgeOptions(parent, child, i));
            sb.append(edge);
        }
        return sb.toString();
    }

    protected String getEdge(T parent, T child, String option) {
        return String.format(
                "  %s -> %s%s;%n", quote(getNodeIdentifier(parent)), quote(getNodeIdentifier(child)), option);
    }

    protected String quote(String str) {
        return String.format(
                "\"%s\"", str.replace("\"", "\\\"").replace("<", "&lt;").replace(">", "&gt;"));
    }

    protected String globalOptions(String type, String... options) {
        if (options.length == 0) return "";
        else return String.format("%s%s;", type, options(options));
    }

    protected String options(String... options) {
        List<String> optionsList =
                Arrays.stream(options).filter(o -> !o.isEmpty()).collect(Collectors.toList());
        if (String.join("", optionsList).trim().isEmpty()) return "";
        return String.format(" [%s]", String.join(" ", optionsList));
    }

    protected String option(String name, String value) {
        return value != null ? String.format("%s=%s", name, quote(value)) : "";
    }
}
