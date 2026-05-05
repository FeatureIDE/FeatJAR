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
import de.featjar.feature.configuration.computation.ComputeNumberOfConfigurations;
import de.featjar.feature.configuration.computation.ComputeNumberOfSelectionsPerConfiguration;
import de.featjar.feature.configuration.computation.ComputeNumberOfSelectionsPerFeature;
import de.featjar.feature.configuration.computation.ComputeNumberOfVariables;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import java.io.IOException;
import java.util.Optional;

/**
 * Prints statistics about given configuration(s)
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 * @author Sebastian Krieter
 */
public class PrintConfigurationStatistics extends ACommand {

    public static final Option<String> OUTPUT_FORMAT = Option.newStringEnumOption(
                    "format", DataTreeFormats.getInstance().getNames())
            .setDefaultValue(new DataTreeTextFormat().getName())
            .setDescription("Format of the output");

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints statistics about a given set of configurations.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("print-config-stats");
    }

    @Override
    public int run(OptionList optionParser) {
        DataTree<?> data = collectStats(readFromInput(optionParser, BooleanAssignmentListFormats.getInstance())
                .orElseThrow());
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

    private DataTree<?> collectStats(BooleanAssignmentList configurations) {
        DataTree<?> data = DataTree.of("ConfigurationStatistics");
        data.addChild(Computations.of(configurations)
                .map(ComputeNumberOfConfigurations::new)
                .compute());
        data.addChild(Computations.of(configurations)
                .map(ComputeNumberOfVariables::new)
                .compute());
        data.addChild(Computations.of(configurations)
                .map(ComputeNumberOfSelectionsPerConfiguration::new)
                .compute());
        data.addChild(Computations.of(configurations)
                .map(ComputeNumberOfSelectionsPerFeature::new)
                .compute());
        return data;
    }
}
