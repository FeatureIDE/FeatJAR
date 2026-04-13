/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.io.list;

import de.featjar.base.data.Result;
import de.featjar.base.data.Sets;
import de.featjar.base.io.input.AInputMapper;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Parses and serializes a list of strings line-by-line, skipping comment and
 * empty lines.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class StringListParser {
    private static final String MULTILINE_COMMENT = "###";
    private static final LinkedHashSet<String> COMMENTS = Sets.of("#", "\t");

    public Result<List<String>> parse(AInputMapper inputMapper, final List<String> entries) {
        try {
            boolean pause = false;
            for (final String line : inputMapper.get().readLines()) {
                if (!line.isBlank()) {
                    if (COMMENTS.stream().anyMatch(line::startsWith)) {
                        if (line.equals(MULTILINE_COMMENT)) {
                            pause = !pause;
                        }
                    } else if (!pause) {
                        entries.add(line.trim());
                    }
                }
            }
            return Result.of(entries);
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }
}
