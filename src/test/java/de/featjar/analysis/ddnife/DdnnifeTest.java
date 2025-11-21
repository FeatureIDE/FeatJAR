/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ddnnife.
 *
 * formula-analysis-ddnnife is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ddnnife is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ddnnife. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/formula-analysis-ddnnife> for further information.
 */
package de.featjar.analysis.ddnife;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.Common;
import de.featjar.analysis.ddnnife.computation.ComputeDdnnifeWrapper;
import de.featjar.analysis.ddnnife.computation.ComputeSolutionCountDdnnife;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.conversion.ComputeBooleanRepresentation;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import java.math.BigInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DdnnifeTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void count() {
        final Literal a = Expressions.literal("a");
        final Literal b = Expressions.literal("b");
        final Literal c = Expressions.literal("c");

        final Implies implies1 = new Implies(a, b);
        final Or or = new Or(implies1, c);
        final BiImplies equals = new BiImplies(a, b);
        final And and = new And(equals, c);
        final IFormula formula = new Implies(or, and);

        final Result<BigInteger> result = Computations.of(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanRepresentation::new)
                .map(ComputeDdnnifeWrapper::new)
                .map(ComputeSolutionCountDdnnife::new)
                .computeResult();
        assertTrue(result.isPresent(), result::printProblems);
        assertEquals(BigInteger.valueOf(3), result.get());
    }

    @Test
    public void count2() {
        final IFormula formula = loadFormula("testFeatureModels/gpl_medium_model.xml");
        final Result<BigInteger> result = Computations.of(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanRepresentation::new)
                .map(ComputeDdnnifeWrapper::new)
                .map(ComputeSolutionCountDdnnife::new)
                .computeResult();
        assertTrue(result.isPresent(), result::printProblems);
        assertEquals(BigInteger.valueOf(960), result.get());
    }
}
