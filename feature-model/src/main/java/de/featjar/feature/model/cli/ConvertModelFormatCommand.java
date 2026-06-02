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

import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.IFormatSupplier;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import java.util.Optional;

/**
 * Reads and writes a {@link IFeatureModel feature model} in different formats.
 *
 * @author Kilian Hüppe
 * @author Knut Köhnlein
 * @author Sebastian Krieter
 */
public class ConvertModelFormatCommand extends ACommand {

    public static final Option<IFormatSupplier<IFeatureModel>> INPUT_FORMAT =
            Options.newInputFormatOption(FeatureModelFormats.class);

    public static final Option<IFormat<IFeatureModel>> OUTPUT_FORMAT =
            Options.newOutputFormatOption(FeatureModelFormats.class, null);

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Convert feature model into another format.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("convert-model");
    }

    @Override
    public int run(OptionList optionParser) {
        return writeResult(
                optionParser,
                readFromInput(optionParser, optionParser.getResult(INPUT_FORMAT).get()),
                optionParser.get(OUTPUT_FORMAT));
    }
}
