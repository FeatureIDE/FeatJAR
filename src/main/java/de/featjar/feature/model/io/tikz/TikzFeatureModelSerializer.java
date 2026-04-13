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
package de.featjar.feature.model.io.tikz;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.IAttribute;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.FeatureModelAttributes;
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.LaTexSymbols;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * This class generates the Tikz representation of a {@link IFeatureModel},
 * including the {@link IFeatureTree feature tree}, all {@link IConstraint constraints}, and a legend.
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 * @author Simon Wenk
 * @author Yang Liu
 * @author Sebastian Krieter
 */
public class TikzFeatureModelSerializer {

    /**
     * This class traverses a given {@link IFeatureTree} and generates the Tikz representation of the tree.
     *
     * @author Felix Behme
     * @author Lara Merza
     * @author Jonas Hanke
     * @author Sebastian Krieter
     */
    public class PrintVisitor implements ITreeVisitor<IFeatureTree, Void> {
        private final StringBuilder stringBuilder;

        private int depth = 0;

        /**
         * Creates a new print visitor.
         * @param stringBuilder the string builder to print into
         */
        public PrintVisitor(StringBuilder stringBuilder) {
            this.stringBuilder = stringBuilder;
        }

        @Override
        public TraversalAction firstVisit(List<IFeatureTree> path) {
            IFeatureTree featureTree = ITreeVisitor.getCurrentNode(path);

            depth++;
            stringBuilder
                    .append(System.lineSeparator())
                    .append("\t".repeat(depth))
                    .append("[");

            printFeatureAttributes(featureTree);
            printFeatureType(featureTree);
            printFeatureCardinality(featureTree);
            printGroupCardinality(featureTree);

            return TraversalAction.CONTINUE;
        }

        @Override
        public TraversalAction lastVisit(List<IFeatureTree> path) {
            stringBuilder.append("]");
            depth--;
            return TraversalAction.CONTINUE;
        }

        private void printFeatureAttributes(IFeatureTree featureTree) {
            IFeature feature = featureTree.getFeature();
            String featureName = feature.getName().orElse("???");

            Map<IAttribute<?>, Object> filteredAttributes = new LinkedHashMap<>();
            feature.getAttributes().ifPresent(m -> m.entrySet().stream()
                    .filter(e -> include.test((Attribute<?>) e.getKey()))
                    .filter(e -> !exclude.test((Attribute<?>) e.getKey()))
                    .sorted(Comparator.comparing(e -> e.getKey().getName()))
                    .forEach(e -> filteredAttributes.put(e.getKey(), e.getValue())));
            if (filteredAttributes.isEmpty()) {
                stringBuilder.append(featureName);
            } else {
                stringBuilder
                        .append("\\multicolumn{2}{c}{")
                        .append(featureName)
                        .append("} \\\\\\hline")
                        .append(System.lineSeparator());

                for (Entry<IAttribute<?>, Object> attribute : filteredAttributes.entrySet()) {
                    stringBuilder
                            .append("\t".repeat(depth + 1))
                            .append("\\small\\texttt{")
                            .append(attribute.getKey().getSimpleName())
                            .append(" (")
                            .append(attribute.getKey().getClassType().getSimpleName())
                            .append(")} &\\small\\texttt{= ")
                            .append(attribute.getValue())
                            .append("} \\\\")
                            .append(System.lineSeparator());
                }
                stringBuilder.append("\t".repeat(depth + 1)).append(",align=ll");
            }
        }

        private void printFeatureType(IFeatureTree featureTree) {
            IFeature feature = featureTree.getFeature();
            if (feature.isAbstract()) {
                stringBuilder.append(",abstract");
            } else if (feature.isConcrete()) {
                stringBuilder.append(",concrete");
            }
        }

        private void printFeatureCardinality(IFeatureTree featureTree) {
            if (featureTree.getParentGroup().map(FeatureTree.Group::isAnd).orElse(false)) {
                if (featureTree.isOptional()) {
                    stringBuilder.append(",optional");
                } else if (featureTree.isMandatory()) {
                    stringBuilder.append(",mandatory");
                } else {
                    stringBuilder
                            .append(",featurecardinality={")
                            .append(featureTree.getFeatureCardinalityLowerBound())
                            .append("}{")
                            .append(featureTree.getFeatureCardinalityUpperBound())
                            .append("}");
                }
            }
        }

        private void printGroupCardinality(IFeatureTree featureTree) {
            if (featureTree.hasParent()) {
                int previousChildrenCount = 1;
                for (int i = 0; i < featureTree.getChildrenGroups().size(); i++) {
                    if (featureTree.getChildrenGroup(i).isPresent()) {
                        int childrenCount = featureTree.getChildren(i).size();
                        FeatureTree.Group group =
                                featureTree.getChildrenGroup(i).get();
                        if (group.isOr()) {
                            stringBuilder.append(String.format(
                                    ",or={%d}{%d}{%d}",
                                    previousChildrenCount,
                                    previousChildrenCount + childrenCount - 1,
                                    (2 * previousChildrenCount + childrenCount - 1) / 2));
                        } else if (group.isAlternative()) {
                            stringBuilder.append(String.format(
                                    ",alternative={%d}{%d}{%d}",
                                    previousChildrenCount,
                                    previousChildrenCount + childrenCount - 1,
                                    (2 * previousChildrenCount + childrenCount - 1) / 2));
                        } else if (group.isCardinalityGroup()) {
                            stringBuilder.append(String.format(
                                    ",groupcardinality={%d}{%d}{%d}{%d}{%d}",
                                    previousChildrenCount,
                                    previousChildrenCount + childrenCount - 1,
                                    (2 * previousChildrenCount + childrenCount - 1) / 2,
                                    group.getLowerBound(),
                                    group.getUpperBound()));
                        }

                        previousChildrenCount += childrenCount;
                    }
                }
            }
        }
    }

    private Predicate<Attribute<?>> include = a -> true;
    private Predicate<Attribute<?>> exclude =
            a -> FeatureModelAttributes.FM_PROPERTY_NAMESPACE.equals(a.getNamespace());

    /**
     * Set the list of attributes that are shown in the Tikz output.
     * If no inclusion list is provided, all attributes will be shown that are not explicitly {@link #setAttributeExclusionList(List) excluded}.
     * @param attributeNames the list of attribute names
     */
    public void setAttributeInclusion(Predicate<Attribute<?>> include) {
        this.include = Objects.requireNonNull(include);
    }

    /**
     * Set the list of attributes that are **not** shown in the Tikz output.
     * @param attributeNames the list of attribute names
     */
    public void setAttributeExclusionList(Predicate<Attribute<?>> exclude) {
        this.exclude = Objects.requireNonNull(exclude);
    }

    /**
     * {@return the Tikz output for a given feature model}
     * @param featureModel the feature model
     */
    public String serialize(IFeatureModel featureModel) {
        StringBuilder stringBuilder = new StringBuilder();
        for (IFeatureTree featureTree : featureModel.getRoots()) {
            stringBuilder.append("\\begin{forest}").append(System.lineSeparator());

            printForest(featureTree, stringBuilder);
            stringBuilder.append(System.lineSeparator()).append("%").append(System.lineSeparator());

            printConstraints(featureModel.getConstraints(), stringBuilder);
            stringBuilder.append("%").append(System.lineSeparator());

            printLegend(featureTree, stringBuilder);

            stringBuilder.append("\\end{forest}").append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    private void printForest(IFeatureTree featureTree, StringBuilder stringBuilder) {
        stringBuilder.append("\tfeatureDiagram");

        Trees.traverse(featureTree, new PrintVisitor(stringBuilder));
    }

    private void printLegend(IFeatureTree featureTree, StringBuilder stringBuilder) {
        // TODO use visitor to prevent multiple traversals
        boolean optional = featureTree.preOrderStream().anyMatch(f -> f.isOptional());
        boolean mandatory = featureTree.preOrderStream().anyMatch(f -> f.isMandatory());
        boolean concrete =
                featureTree.preOrderStream().anyMatch(f -> f.getFeature().isConcrete());
        boolean abstrakt =
                featureTree.preOrderStream().anyMatch(f -> !f.getFeature().isConcrete());
        boolean alternative = featureTree
                .preOrderStream()
                .anyMatch(f -> f.getParentGroup().map(g -> g.isAlternative()).orElse(false));
        boolean or = featureTree
                .preOrderStream()
                .anyMatch(f -> f.getParentGroup().map(g -> g.isOr()).orElse(false));

        stringBuilder
                .append("\t\\matrix [anchor=north west] at (current bounding box.north east) {")
                .append(System.lineSeparator())
                .append("\t\t\\node [placeholder] {}; \\\\")
                .append(System.lineSeparator())
                .append("\t};")
                .append(System.lineSeparator())
                .append("\t\\matrix [draw=drawColor,anchor=north west] at (current bounding box.north east) {")
                .append(System.lineSeparator())
                .append("\t\t\\node [label=center:\\underline{Legend:}] {}; \\\\")
                .append(System.lineSeparator());

        if (abstrakt && concrete) {
            stringBuilder
                    .append("\t\t\\node [abstract,label=right:Abstract Feature] {}; \\\\")
                    .append(System.lineSeparator())
                    .append("\t\t\\node [concrete,label=right:Concrete Feature] {}; \\\\")
                    .append(System.lineSeparator());
        } else if (abstrakt) {
            stringBuilder
                    .append("\t\t\\node [abstract,label=right:Feature] {}; \\\\")
                    .append(System.lineSeparator());
        } else if (concrete) {
            stringBuilder
                    .append("\t\t\\node [concrete,label=right:Feature] {}; \\\\")
                    .append(System.lineSeparator());
        }

        if (mandatory) {
            stringBuilder
                    .append("\t\t\\node [mandatory,label=right:Mandatory] {}; \\\\")
                    .append(System.lineSeparator());
        }

        if (optional) {
            stringBuilder
                    .append("\t\t\\node [optional,label=right:Optional] {}; \\\\")
                    .append(System.lineSeparator());
        }

        if (or) {
            stringBuilder
                    .append("\t\t\t\\filldraw[drawColor] (0.1,0) - +(-0,-0.2) - +(0.2,-0.2)- +(0.1,0);")
                    .append(System.lineSeparator())
                    .append("\t\t\t\\draw[drawColor] (0.1,0) -- +(-0.2, -0.4);")
                    .append(System.lineSeparator())
                    .append("\t\t\t\\draw[drawColor] (0.1,0) -- +(0.2,-0.4);")
                    .append(System.lineSeparator())
                    .append("\t\t\t\\fill[drawColor] (0,-0.2) arc (240:300:0.2);")
                    .append(System.lineSeparator())
                    .append("\t\t\\node [label=right:Or Group] {}; \\\\")
                    .append(System.lineSeparator());
        }

        if (alternative) {
            stringBuilder
                    .append("\t\t\t\\draw[drawColor] (0.1,0) -- +(-0.2, -0.4);")
                    .append(System.lineSeparator())
                    .append("\t\t\t\\draw[drawColor] (0.1,0) -- +(0.2,-0.4);")
                    .append(System.lineSeparator())
                    .append("\t\t\t\\draw[drawColor] (0,-0.2) arc (240:300:0.2);")
                    .append(System.lineSeparator())
                    .append("\t\t\\node [label=right:Alternative Group] {}; \\\\")
                    .append(System.lineSeparator());
        }

        stringBuilder.append("\t};").append(System.lineSeparator());
    }

    private void printConstraints(Collection<IConstraint> constraints, StringBuilder stringBuilder) {
        if (!constraints.isEmpty()) {
            ExpressionSerializer expressionSerializer = new ExpressionSerializer();
            expressionSerializer.setEnquoteAlways(true);
            expressionSerializer.setSymbols(LaTexSymbols.INSTANCE);

            stringBuilder
                    .append("\t\\matrix [below=1mm of current bounding box] {")
                    .append(System.lineSeparator());
            for (IConstraint constraint : constraints) {
                stringBuilder
                        .append("\t\t\\node {\\(")
                        .append(constraint
                                .getFormula()
                                .traverse(expressionSerializer)
                                .get()
                                .replaceAll("\\s+", " "))
                        .append("\\)}; \\\\")
                        .append(System.lineSeparator());
            }
            stringBuilder.append("\t};").append(System.lineSeparator());
        }
    }
}
