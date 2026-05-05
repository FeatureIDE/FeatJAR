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
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.assignment.BooleanAssignmentGroups;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.IBooleanAssignmentListFormat;

/**
 * Reads / Writes a list of assignments.
 *
 * @author Sebastian Krieter
 */
public class BooleanAssignmentListDimacsFormat extends AAssignmentDimacsFormat<BooleanAssignmentList>
        implements IBooleanAssignmentListFormat {

    /**
     * The identifier of this format.
     */
    public static final String ID = BooleanAssignmentListDimacsFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public BooleanAssignmentListDimacsFormat getInstance() {
        return this;
    }

    @Override
    public Result<String> serialize(BooleanAssignmentList assignmentList) {
        return serializeGroups(new BooleanAssignmentGroups(assignmentList));
    }

    @Override
    public Result<BooleanAssignmentList> parse(AInputMapper inputMapper) {
        return parseGroups(inputMapper).map(BooleanAssignmentGroups::getMergedGroups);
    }
}
