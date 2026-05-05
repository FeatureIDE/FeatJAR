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
package de.featjar.formula.io.binary;

import de.featjar.base.data.Result;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.output.AOutputMapper;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.IBooleanAssignmentListFormat;
import java.io.IOException;

/**
 * Reads / Writes a list of assignments.
 *
 * @author Sebastian Krieter
 */
public class BooleanAssignmentListSimpleBinaryFormat extends ASimpleAssignmentBinaryFormat<BooleanAssignmentList>
        implements IBooleanAssignmentListFormat {

    public static final String ID = BooleanAssignmentListSimpleBinaryFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public BooleanAssignmentListSimpleBinaryFormat getInstance() {
        return this;
    }

    @Override
    public void write(BooleanAssignmentList assignmentList, AOutputMapper outputMapper) throws IOException {
        writeList(assignmentList, outputMapper);
    }

    @Override
    public Result<BooleanAssignmentList> parse(AInputMapper inputMapper) {
        return parseList(inputMapper);
    }
}
