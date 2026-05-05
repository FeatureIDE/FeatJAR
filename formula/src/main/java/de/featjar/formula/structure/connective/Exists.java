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
package de.featjar.formula.structure.connective;

import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.term.value.Variable;

/**
 * Expresses "exists X such that A" constraints (i.e., existential quantification).
 * Evaluates to {@code true} iff there is a value of the bound variable such that the formula evaluates to {@code true}.
 * TODO: not supported by SAT solvers, but there is no error handling implemented, so
 *  it may cause undefined behavior. same goes for {@link ForAll}.
 *
 * @author Sebastian Krieter
 */
public class Exists extends AQuantifier {
    // TODO: rewrite CNFSlicer to work on a QBF
    protected Exists(Exists exists) {
        super(exists);
    }

    public Exists(Variable boundVariable, IFormula formula) {
        super(boundVariable, formula);
    }

    @Override
    public String getName() {
        return "exists-" + boundVariable;
    }

    @Override
    public Exists cloneNode() {
        return new Exists(this);
    }
}
