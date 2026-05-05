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
package de.featjar.formula.io;

import de.featjar.Common;
import de.featjar.FormatTest;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.binary.FormulaBinaryFormat;
import de.featjar.formula.structure.IFormula;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ExpressionFormat Formula} format.
 *
 * @author Sebastian Krieter
 */
public class BinaryFormatTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void Formula_ABC_nAnBnC() {
        test("123-n1n2n3");
    }

    @Test
    public void Formula_nA() {
        test("nA");
    }

    @Test
    public void Formula_nAB() {
        test("nAB");
    }

    private static void test(String name) {
        final IFormula formula = Computations.of(getFormula(name))
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .compute();
        FormatTest.testParseAndSerialize("binary/" + name, new FormulaBinaryFormat());
        FormatTest.testSerializeAndParse(formula, new FormulaBinaryFormat());
    }
}
