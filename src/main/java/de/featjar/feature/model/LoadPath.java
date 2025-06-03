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

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.Commands;
import de.featjar.base.shell.ShellSession;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Loads a Path into the shell. That path can later be used to run {@link Commands}.
 *
 * @author Niclas Kleinert
 */
public class LoadPath extends ALoadShellCommand {

    @Override
    public void execute(ShellSession session, List<String> cmdParams) {
        String name = setVariableName(session, cmdParams);
        String path = setPath(cmdParams);

        File file = new File(path);

        if (file.exists()) {
            session.put(name, Paths.get(path), Path.class);
        } else {
            FeatJAR.log().error("'%s' is no valid path to a file", path);
        }
    }

    public Optional<String> getShortName() {
        return Optional.of("LoadPath");
    }

    public Optional<String> getDescription() {
        return Optional.of(
                "<var> <path> - load just a path into the shell such that it can be used by non-shell commands");
    }

    @Override
    public Optional<String> getFormatName() {
        return Optional.of("Path");
    }

    @Override
    public Optional<String> getDefaultName() {
        return Optional.of("path");
    }
}
