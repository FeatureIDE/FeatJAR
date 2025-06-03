/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.feature.model;

import de.featjar.base.shell.ShellSession;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import java.util.List;
import java.util.Optional;

/**
 * Loads a configuration {@link BooleanAssignment} into the shell.
 *
 * @author Niclas Kleinert
 */
public class LoadConfig extends ALoadShellCommand {

    @Override
    public void execute(ShellSession session, List<String> cmdParams) {
        parseArguments(session, cmdParams, BooleanAssignmentGroupsFormats.getInstance());
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("loadConfig");
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("<var> <path> - load a configuration");
    }

    @Override
    public Optional<String> getFormatName() {
        return Optional.of("Configuration");
    }

    @Override
    public Optional<String> getDefaultName() {
        return Optional.of("config");
    }
}
