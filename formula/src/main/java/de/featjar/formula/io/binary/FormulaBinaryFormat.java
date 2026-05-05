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
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.Assignments;
import de.featjar.formula.io.IFormulaFormat;
import de.featjar.formula.structure.FormulaNormalForm;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.Reference;
import java.io.IOException;

/**
 * Reads / Writes a formula already in CNF.
 *
 * @author Sebastian Krieter
 */
public class FormulaBinaryFormat extends ASimpleAssignmentBinaryFormat<IFormula> implements IFormulaFormat {

    public static final String ID = FormulaBinaryFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public FormulaBinaryFormat getInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "Binary";
    }

    @Override
    public void write(IFormula formula, AOutputMapper outputMapper) throws IOException {
        IFormula cnfFormula = (formula instanceof Reference) ? ((Reference) formula).getExpression() : formula;
        if (!cnfFormula.isStrictNormalForm(FormulaNormalForm.CNF)) {
            throw new IllegalArgumentException("Formula is not in CNF");
        }
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
