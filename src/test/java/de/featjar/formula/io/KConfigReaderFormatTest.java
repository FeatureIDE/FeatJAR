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
import de.featjar.formula.io.kconfig.KConfigReaderFormat;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link KConfigReaderFormat KConfigReader} format.
 *
 * @author Sebastian Krieter
 */
public class KConfigReaderFormatTest {

    @Test
    public void KConfigReader_ABC_nAnBnC() {
        // TODO: Fix expression parser
        // test("ABC-nAnBnC", 5);
    }

    @Test
    public void KConfigReader_empty() {
        test("empty", 2);
    }

    @Test
    public void KConfigReader_nA() {
        test("nA", 1);
    }

    @Test
    public void KConfigReader_nAB() {
        test("nAB", 1);
    }

    private static void test(String name, int count) {
        FormatTest.testParse(Common.getFormula(name), "KConfigReader/" + name, count, new KConfigReaderFormat());
    }
}
