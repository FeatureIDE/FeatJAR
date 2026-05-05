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

import de.featjar.base.computation.Computations;
import de.featjar.base.data.Result;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.Assignments;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.IFormulaFormat;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.Reference;

/**
 * Reads and writes feature models in the DIMACS CNF format.
 *
 * @author Sebastian Krieter
 */
public class CNFFormulaDimacsFormat extends ADimacsFormat<IFormula> implements IFormulaFormat {
    /**
     * The identifier of this format.
     */
    public static final String ID = CNFFormulaDimacsFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public CNFFormulaDimacsFormat getInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "CNF-DIMACS";
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> serialize(IFormula formula) {
        IFormula cnfFormula = Computations.of(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .set(ComputeCNFFormula.IS_STRICT, true)
                .compute();
        VariableMap variableMap = new VariableMap(formula.getVariableMap().keySet());
        return Result.of(DimacsSerializer.serialize(
                variableMap,
                ((Reference) cnfFormula).getExpression().getChildren(),
                c -> Assignments.toBooleanLiterals((IFormula) c, variableMap)));
    }
}
