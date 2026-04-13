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
package de.featjar.feature.model.io.xml;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes feature models to GraphViz DOT files.
 *
 * @author Elias Kuiter
 */
public class GraphVizFeatureModelFormat implements IFormat<IFeatureModel> {
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
    public Result<String> serialize(IFeatureModel featureModel) {
        // TODO take multiple roots into account
        List<IFeatureTree> features = featureModel.getFeatureTreeStream().collect(Collectors.toList());
        return Result.of(String.format(
                "digraph {%n  graph%s;%n  node%s;%n  edge%s;%n%s%n%s%n}",
                options(option("splines", "false"), option("ranksep", "0.2")),
                options(
                        option("fontname", "Arial"),
                        option("style", "filled"),
                        option("fillcolor", "#ccccff"),
                        option("shape", "box")),
                options(option("arrowhead", "none")),
                features.stream().map(this::getNode).collect(Collectors.joining("\n")),
                features.stream().map(this::getEdge).filter(s -> !s.isEmpty()).collect(Collectors.joining("\n"))));
    }

    public String getNode(IFeatureTree feature) {
        String nodeString = "";
        nodeString += String.format(
                "  %s%s;",
                quote(feature.getFeature().getIdentifier().toString()),
                options(
                        option("label", feature.getFeature().getName().orElse("")),
                        option("fillcolor", feature.getFeature().isAbstract() ? "#f2f2ff" : null)));
        if (feature.hasParent()) {
            nodeString += String.format(
                    "%n  %s%s;",
                    quote(feature.getFeature().getIdentifier().toString() + "_group"),
                    options(
                            option("shape", "diamond"),
                            option(
                                    "style",
                                    !feature.getParentGroup().get().isAnd()
                                            ? "invis"
                                            : feature.getParentGroup().get().isAlternative() ? "" : null),
                            option("fillcolor", feature.getParentGroup().get().isOr() ? "#000000" : null),
                            option("label", ""),
                            option("width", ".15"),
                            option("height", ".15")));
        }
        return nodeString;
    }

    public String getEdge(IFeatureTree feature) {
        String edgeString = "";
        if (feature.hasParent()) {
            String parentNode =
                    feature.getParent().get().getFeature().getIdentifier().toString();
            edgeString += getEdge(
                    parentNode + "_group",
                    feature,
                    option("style", feature.getParentGroup().get().isAnd() ? null : "invis"));
            if (!feature.getParentGroup().get().isAnd())
                edgeString +=
                        getEdge(parentNode + (feature.getParentGroup().get().isAnd() ? "_group" : ""), feature, "");
            edgeString += String.format(
                    "  %s:s -> %s:n%s;",
                    quote(feature.getFeature().getIdentifier().toString()),
                    quote(feature.getFeature().getIdentifier().toString() + "_group"),
                    options(option("style", feature.getParentGroup().get().isAnd() ? null : "invis")));
        }
        return edgeString;
    }

    public String getEdge(String parentNode, IFeatureTree childFeature, String option) {
        return String.format(
                "  %s:s -> %s:n%s;%n",
                quote(parentNode),
                quote(childFeature.getFeature().getIdentifier().toString()),
                options(
                        option(
                                "arrowhead",
                                childFeature.getParentGroup().get().isAnd()
                                        ? null
                                        : childFeature.isMandatory() ? "dot" : "odot"),
                        option));
    }

    protected String quote(String str) {
        return String.format("\"%s\"", str.replace("\"", "\\\""));
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
