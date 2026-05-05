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

import static de.featjar.base.computation.Computations.async;
import static de.featjar.base.computation.Computations.await;
import static de.featjar.formula.structure.Expressions.literal;
import static de.featjar.formula.structure.Expressions.or;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.featjar.AnalysisTest;
import de.featjar.analysis.sat4j.computation.ComputeAtomicSetsSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeCoreSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeSatisfiableSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeSolutionSAT4J;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import org.junit.jupiter.api.Test;

public class Sat4JAnalysesTest extends AnalysisTest {

    public void getTWiseSample(IFormula formula, int t) {
        ComputeBooleanClauseList cnf = async(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new);

        BooleanSolution solution = await(cnf.map(ComputeSolutionSAT4J::new));
        BooleanAssignmentList core = await(cnf.map(ComputeCoreSAT4J::new));
        BooleanAssignmentList atomicSets = await(cnf.map(ComputeAtomicSetsSAT4J::new));
        assertNotNull(solution);
        assertNotNull(core);
        assertNotNull(atomicSets);
    }

    @Test
    void tWiseSampleHasCorrectSize() {
        getTWiseSample(or(literal("x"), literal(false, "y"), literal(false, "z")), 2);
    }

    @Test
    void satisfiabilityIsCorrectlyComputed() {
        testSatisfiability(ComputeBooleanClauseList::new, ComputeSatisfiableSAT4J::new);
    }

    @Test
    void coreIsCorrectlyComputed() {
        testCore(ComputeBooleanClauseList::new, ComputeCoreSAT4J::new);
    }

    @Test
    void computedSolutionIsSatisfying() {
        testSolution(ComputeBooleanClauseList::new, ComputeSolutionSAT4J::new);
    }
}
