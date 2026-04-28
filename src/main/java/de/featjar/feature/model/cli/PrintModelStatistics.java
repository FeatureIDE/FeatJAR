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
package de.featjar.feature.model.cli;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.Computations;
import de.featjar.base.io.DataTreeFormats;
import de.featjar.base.io.text.DataTreeTextFormat;
import de.featjar.base.tree.DataTree;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.computation.ComputeConstraintNumberOfAtoms;
import de.featjar.feature.model.computation.ComputeConstraintNumberOfConnectives;
import de.featjar.feature.model.computation.ComputeConstraintNumberOfDistinctVariables;
import de.featjar.feature.model.computation.ComputeFeatureTreeMaxDepth;
import de.featjar.feature.model.computation.ComputeFeatureTreeNumberOfBranches;
import de.featjar.feature.model.computation.ComputeFeatureTreeNumberOfGroups;
import de.featjar.feature.model.computation.ComputeFeatureTreeNumberOfLeaves;
import de.featjar.feature.model.computation.ComputeFeatureTreeNumberOfTopNodes;
import de.featjar.feature.model.io.FeatureModelFormats;
import java.io.IOException;
import java.util.Optional;

/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Kilian Hüppe
 * @author Knut Köhnlein
 * @author Benjamin von Holt
 * @author Sebastian Krieter
 */
public class PrintModelStatistics extends ACommand {

    public enum AnalysesScope {
        ALL,
        TREE_RELATED,
        CONSTRAINT_RELATED
    }

    public static final Option<AnalysesScope> ANALYSES_SCOPE = Option.newEnumOption("scope", AnalysesScope.class)
            .setDefaultValue(AnalysesScope.ALL)
            .setDescription("Specifies scope of statistics");

    public static final Option<String> OUTPUT_FORMAT = Option.newStringEnumOption(
                    "format", DataTreeFormats.getInstance().getNames())
            .setDefaultValue(new DataTreeTextFormat().getName())
            .setDescription("Format of the output");

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints statistics about a given feature model.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("print-model-stats");
    }

    @Override
    public int run(OptionList optionParser) {
        DataTree<?> data = collectStats(
                readFromInput(optionParser, FeatureModelFormats.getInstance()).orElseThrow(),
                optionParser.get(ANALYSES_SCOPE));

        try {
            writeToOutput(
                    data,
                    DataTreeFormats.getInstance()
                            .getFormatByName(optionParser.get(OUTPUT_FORMAT))
                            .orElse(null),
                    optionParser);
            return 0;
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return FeatJAR.ERROR_WRITING_RESULT;
        }
    }

    /**
     * method for collecting statistics of the provided feature model depending on specified scope of information (all, constraint related, tree related)
     * @param model: a feature model from which statistics will be collected
     * @param scope: describes whether only constraint-related, only tree-related, or both kinds of statistics are to be collected
     * @return data tree with statistics data
     */
    private DataTree<?> collectStats(IFeatureModel model, AnalysesScope scope) {
        DataTree<?> data = DataTree.of("FeatureModelStatistics");
        if ((scope == AnalysesScope.ALL || scope == AnalysesScope.TREE_RELATED)) {
            DataTree<?> treeData = DataTree.of("FeatureTree");
            data.addChild(treeData);
            treeData.addChild(
                    Computations.of(model).map(ComputeFeatureTreeMaxDepth::new).compute());
            treeData.addChild(Computations.of(model)
                    .map(ComputeFeatureTreeNumberOfBranches::new)
                    .compute());
            treeData.addChild(Computations.of(model)
                    .map(ComputeFeatureTreeNumberOfLeaves::new)
                    .compute());
            treeData.addChild(Computations.of(model)
                    .map(ComputeFeatureTreeNumberOfTopNodes::new)
                    .compute());
            treeData.addChild(Computations.of(model)
                    .map(ComputeFeatureTreeNumberOfGroups::new)
                    .compute());
        }
        if (scope == AnalysesScope.ALL || scope == AnalysesScope.CONSTRAINT_RELATED) {
            DataTree<?> constraintData = DataTree.of("CrossTreeConstraints");
            data.addChild(constraintData);
            constraintData.addChild(Computations.of(model)
                    .map(ComputeConstraintNumberOfAtoms::new)
                    .compute());
            constraintData.addChild(Computations.of(model)
                    .map(ComputeConstraintNumberOfDistinctVariables::new)
                    .compute());
            constraintData.addChild(Computations.of(model)
                    .map(ComputeConstraintNumberOfConnectives::new)
                    .compute());
        }
        return data;
    }
}
