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

import de.featjar.base.data.Maps;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.assignment.Assignment;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Textual format for serializing and parsing an CPP variables to an {@link Assignment}
 *
 * @author Sebastian Krieter
 */
public class CPPAssignmentFormat implements IFormat<Assignment> {

    private static final Pattern cs = Pattern.compile("#define\\s+(\\S+)(\\s+(.*))?");

    @Override
    public Result<String> serialize(Assignment valueAssignment) {
        return Result.of(valueAssignment.getAll().entrySet().stream()
                .map(e -> e.getValue() == Boolean.TRUE ? String.format("#define %s", e.getKey()) : null)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n")));
    }

    @Override
    public Result<Assignment> parse(AInputMapper inputMapper) {
        LinkedHashMap<String, Object> variableValuePairs = Maps.empty();
        inputMapper
                .get()
                .getLineStream()
                .map(cs::matcher)
                .filter(m -> m.matches())
                .map(m -> m.group(1))
                .forEach(variable -> variableValuePairs.put(variable, Boolean.TRUE));
        return Result.of(new Assignment(variableValuePairs));
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public String getName() {
        return "CPP-Assignment";
    }
}
