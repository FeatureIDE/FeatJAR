/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-kissat.
 *
 * formula-analysis-kissat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-kissat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-kissat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-cadical> for further information.
 */
package de.featjar.analysis.kissat.solver;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.Common;
import de.featjar.analysis.kissat.computation.ComputeGetSolutionKissat;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class KissatSolverTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void formulaHasSolution() {
        final Literal a = Expressions.literal("a");
        final Literal b = Expressions.literal("b");
        final Literal c = Expressions.literal("c");

        final Implies implies1 = new Implies(a, b);
        final Or or = new Or(implies1, c);
        final BiImplies equals = new BiImplies(a, b);
        final And and = new And(equals, c);
        final Implies formula = new Implies(or, and);

        checkSolution(formula);
    }

    @Test
    public void gplHasSolution() {
        IFormula formula = loadFormula("testFeatureModels/gpl_medium_model.xml");
        checkSolution(formula);
    }

    private void checkSolution(final IFormula formula) {
        IFormula cnf = formula.toCNF().orElseThrow();
        final Result<BooleanSolution> result = Computations.of(cnf)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeGetSolutionKissat::new)
                .computeResult();
        assertTrue(result.isPresent(), result::printProblems);
    }
}
