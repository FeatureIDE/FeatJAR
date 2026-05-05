/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-evaluation.
 *
 * evaluation is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with evaluation. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-evaluation> for further information.
 */
package de.featjar.evaluation;

import de.featjar.base.FeatJAR;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Reset current output path.
 *
 * @author Sebastian Krieter
 */
public class OutputCleaner extends Evaluator {

    @Override
    public void runEvaluation() throws Exception {
        Files.deleteIfExists(outputRootPath.resolve(".current"));
        FeatJAR.log().info("Reset current output path.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("output-cleaner-evaluation");
    }
}
