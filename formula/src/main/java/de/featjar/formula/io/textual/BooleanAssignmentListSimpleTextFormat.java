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
package de.featjar.formula.io.textual;

import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.IBooleanAssignmentListFormat;

/**
 * Writes a list of configuration.
 *
 * @author Sebastian Krieter
 */
public class BooleanAssignmentListSimpleTextFormat implements IBooleanAssignmentListFormat {

    private static final String LINE_SEPARATOR = "\n";
    private static final String VALUE_SEPARATOR = ",";

    public static final String ID = BooleanAssignmentListSimpleTextFormat.class.getCanonicalName();

    @Override
    public Result<String> serialize(BooleanAssignmentList booleanAssignmentList) {
        final StringBuilder lines = new StringBuilder();
        for (final BooleanAssignment configuration : booleanAssignmentList) {
            final int[] literals = configuration.get();
            for (int l : literals) {
                if (l > 0) {
                    lines.append(booleanAssignmentList.getVariableMap().get(l).orElseThrow());
                    lines.append(VALUE_SEPARATOR);
                }
            }
            int lastIndex = lines.length() - VALUE_SEPARATOR.length();
            if (lastIndex >= 0 && VALUE_SEPARATOR.equals(lines.substring(lastIndex))) {
                lines.replace(lastIndex, lines.length(), LINE_SEPARATOR);
            } else {
                lines.append(LINE_SEPARATOR);
            }
        }
        return Result.of(lines.toString());
    }

    @Override
    public String getFileExtension() {
        return "list";
    }

    @Override
    public BooleanAssignmentListSimpleTextFormat getInstance() {
        return this;
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public String getName() {
        return "VariableList";
    }
}
