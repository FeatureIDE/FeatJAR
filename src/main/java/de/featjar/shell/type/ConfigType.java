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
package de.featjar.shell.type;

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.shell.type.IVariableType;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.io.BooleanAssignmentGroupsFormats;
import java.nio.file.Path;

public class ConfigType implements IVariableType<BooleanAssignment> {

    @Override
    public Class<BooleanAssignment> getClassType() {
        return BooleanAssignment.class;
    }

    @Override
    public Result<BooleanAssignment> load(Path path) {
        return IO.load(path, BooleanAssignmentGroupsFormats.getInstance())
                .map(g -> g.getMergedGroups().getFirst());
    }

    @Override
    public String getName() {
        return "config";
    }
}
