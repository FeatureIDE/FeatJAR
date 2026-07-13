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
package de.featjar.formula.io.dimacs;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.Assignments;
import de.featjar.formula.io.IVariableMapFormat;
import de.featjar.formula.structure.IFormula;
import java.text.ParseException;
import java.util.List;

/**
 * Parses and serializes a list of strings line-by-line, skipping comment and empty lines.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class VariableMapDimacsFormat extends ADimacsFormat<VariableMap> implements IVariableMapFormat {

    /**
     * The identifier of this format.
     */
    public static final String ID = VariableMapDimacsFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> serialize(VariableMap variableMap) {
        return Result.of(DimacsSerializer.serialize(
                variableMap, List.of(), c -> Assignments.toBooleanLiterals((IFormula) c, variableMap)));
    }

    @Override
    public Result<VariableMap> parse(AInputMapper inputMapper) {
        final DimacsParser parser = new DimacsParser();
        parser.setReadingVariableDirectory(true);
        try {
            return Result.of(parser.parse(inputMapper).getKey());
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e, e.getErrorOffset()));
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }
}
