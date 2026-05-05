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
package de.featjar.formula.transform;

import static org.junit.jupiter.api.Assertions.*;

import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import java.util.function.Function;

class TransformationTest {
    public static void traverseAndAssertSameFormula(
            IFormula oldFormula,
            Function<IComputation<IFormula>, IComputation<? extends IFormula>> formulaComputationFunction) {
        Result<? extends IFormula> result =
                formulaComputationFunction.apply(Computations.of(oldFormula)).get();
        assertTrue(result.isPresent());
        assertEquals(oldFormula, result.get());
    }

    public static void traverseAndAssertFormulaEquals(
            IFormula oldFormula,
            Function<IComputation<IFormula>, IComputation<? extends IFormula>> formulaComputationFunction,
            IFormula assertFormula) {
        Result<? extends IFormula> result =
                formulaComputationFunction.apply(Computations.of(oldFormula)).get();
        assertTrue(result.isPresent());
        assertNotEquals(
                oldFormula, result.get(), Expressions.print(oldFormula) + "!=\n" + Expressions.print(result.get()));
        assertEquals(
                assertFormula,
                result.get(),
                Expressions.print(assertFormula) + "!=\n" + Expressions.print(result.get()));
    }

    public static void traverseAndAssertFail(
            IFormula oldFormula, Function<IComputation<IFormula>, IComputation<IFormula>> formulaComputationFunction) {
        Result<IFormula> result =
                formulaComputationFunction.apply(Computations.of(oldFormula)).get();
        assertFalse(result.isPresent());
    }
}
