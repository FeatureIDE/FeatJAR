/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.analysis;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.env.Process;
import de.featjar.base.env.TempFile;
import de.featjar.base.io.IO;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.io.dimacs.BooleanAssignmentGroupsDimacsFormat;
import java.nio.file.Path;

public class ExternalConfigurationTester implements IConfigurationTester {

    private final Path processPath;

    private VariableMap variableMap;

    public ExternalConfigurationTester(Path processPath) {
        this.processPath = processPath;
    }

    @Override
    public VariableMap getVariableMap() {
        return variableMap;
    }

    @Override
    public void setVariableMap(VariableMap variableMap) {
        this.variableMap = variableMap;
    }

    @Override
    public Result<Integer> test(BooleanAssignment configuration) {
        try (TempFile tempFile = new TempFile("verifier", ".dimacs")) {
            Path path = tempFile.getPath();
            IO.save(
                    new BooleanAssignmentGroups(variableMap, configuration),
                    path,
                    new BooleanAssignmentGroupsDimacsFormat());
            Process process = new Process(processPath, path.toString());

            return process.get().map(lines -> lines.isEmpty() ? null : "0".equals(lines.get(0)) ? 0 : 1);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }
}
