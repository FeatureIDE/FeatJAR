/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-cadical.
 *
 * formula-analysis-cadical is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-cadical is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-cadical. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-cadical> for further information.
 */
package de.featjar.analysis.cadical.solver;

import de.featjar.AnalysisTest;
import de.featjar.analysis.cadical.computation.ComputeCoreCadiCal;
import de.featjar.analysis.cadical.computation.ComputeGetSolutionCadiCal;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import org.junit.jupiter.api.Test;

public class CadiCalAnalysesTest extends AnalysisTest {

    @Test
    void coreIsCorrectlyComputed() {
        testCore(ComputeBooleanClauseList::new, ComputeCoreCadiCal::new);
    }

    @Test
    void computedSolutionIsSatisfying() {
        testSolution(ComputeBooleanClauseList::new, ComputeGetSolutionCadiCal::new);
    }
}
