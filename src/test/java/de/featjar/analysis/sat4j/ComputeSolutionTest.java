/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j;

import static de.featjar.formula.structure.Expressions.and;
import static de.featjar.formula.structure.Expressions.biImplies;
import static de.featjar.formula.structure.Expressions.literal;
import static de.featjar.formula.structure.Expressions.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.Common;
import de.featjar.analysis.sat4j.computation.ComputeSolutionSAT4J;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.ComputePresence;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ComputeSolutionTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    public boolean hasSolution(IFormula formula) {
        return Computations.of(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .set(ComputeCNFFormula.IS_PLAISTED_GREENBAUM, Boolean.TRUE)
                .map(ComputeBooleanClauseList::new)
                .map(ComputeSolutionSAT4J::new)
                .map(ComputePresence<BooleanSolution>::new)
                .compute();
    }

    @Test
    void satisfiableFormulaInCNFHasSolution() {
        assertTrue(hasSolution(and(literal("x"), literal(false, "y"))));
    }

    @Test
    void unsatisfiableFormulaInCNFHasNoSolution() {
        assertFalse(hasSolution(and(literal("x"), literal(false, "x"))));
    }

    @Test
    void satisfiableArbitraryFormulaHasSolution() {
        assertTrue(hasSolution(biImplies(literal("a"), literal("b"))));
    }

    @Test
    void unsatisfiableArbitraryFormulaHasNoSolution() {
        assertFalse(hasSolution(biImplies(literal("a"), not(literal("a")))));
    }

    @Test
    void gplIsSatisfiable() {
        assertTrue(hasSolution(loadFormula("GPL/model.xml")));
    }
}
