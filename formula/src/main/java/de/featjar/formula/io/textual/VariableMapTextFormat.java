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
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.VariableMap;
import de.featjar.formula.io.IVariableMapFormat;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Parses and serializes a list of strings line-by-line, skipping comment and empty lines.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class VariableMapTextFormat implements IVariableMapFormat {

    /**
     * The identifier of this format.
     */
    public static final String ID = VariableMapTextFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public String getName() {
        return "text";
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    private static final Pattern namePattern = Pattern.compile("\\A\\s*(\"(.+)\"|(\\S+)|(.*\\S))\\s*");

    @Override
    public Result<VariableMap> parse(AInputMapper inputMapper) {
        return parseLines(inputMapper.get().getLineStream(), new VariableMap());
    }

    @Override
    public Result<VariableMap> parse(AInputMapper inputMapper, Supplier<VariableMap> supplier) {
        return parseLines(inputMapper.get().getLineStream(), supplier.get());
    }

    private static Result<VariableMap> parseLines(final Stream<String> lineStream, VariableMap variableMap) {
        lineStream.map(VariableMapTextFormat::clean).filter(l -> l != null).forEach(l -> variableMap.add(l));
        return Result.of(variableMap);
    }

    private static String clean(String line) {
        final Matcher matcher = namePattern.matcher(line);
        if (matcher.matches()) {
            String group = matcher.group(2);
            if (group != null) {
                return group;
            }
            group = matcher.group(3);
            if (group != null) {
                return group;
            }
            group = matcher.group(4);
            if (group != null) {
                return group;
            }
            throw new IllegalStateException(line);
        } else {
            return null;
        }
    }

    @Override
    public Result<String> serialize(VariableMap variableMap) {
        final StringBuilder sb = new StringBuilder();
        variableMap.stream().forEach(e -> {
            final String value = e.getValue();
            if (value != null) {
                sb.append(
                        value.startsWith(" ") || value.endsWith(" ")
                                ? "\"" + value + "\""
                                : value.startsWith("\"") && value.endsWith("\"") ? "\"\"" + value + "\"\"" : value);
                sb.append(System.lineSeparator());
            }
        });
        return Result.of(sb.toString());
    }
}
