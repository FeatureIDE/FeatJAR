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

import de.featjar.base.computation.Computations;
import de.featjar.base.data.Result;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.io.output.AOutputMapper;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.Assignments;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.IFormulaFormat;
import de.featjar.formula.structure.IFormula;
import java.io.IOException;

/**
 * Reads / Writes a formula. Transforms it into CNF for writing.
 *
 * @author Sebastian Krieter
 */
public class CNFFormulaBinaryFormat extends ASimpleAssignmentBinaryFormat<IFormula> implements IFormulaFormat {

    public static final String ID = CNFFormulaBinaryFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public CNFFormulaBinaryFormat getInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "CNF-Binary";
    }

    @Override
    public void write(IFormula formula, AOutputMapper outputMapper) throws IOException {
        IFormula cnfFormula = Computations.of(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .set(ComputeCNFFormula.IS_STRICT, true)
                .compute();
        writeList(
                Assignments.toBooleanAssignmentList(
                        cnfFormula, new VariableMap(formula.getVariableMap().keySet())),
                outputMapper);
    }

    @Override
    public Result<IFormula> parse(AInputMapper inputMapper) {
        return parseList(inputMapper).map(Assignments::toCNFFormula);
    }
}
