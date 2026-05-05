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

import static de.featjar.formula.structure.Expressions.and;
import static de.featjar.formula.structure.Expressions.literal;
import static de.featjar.formula.structure.Expressions.or;
import static de.featjar.formula.structure.Expressions.reference;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.featjar.Common;
import de.featjar.FormatTest;
import de.featjar.base.FeatJAR;
import de.featjar.base.io.IO;
import de.featjar.formula.io.dimacs.FormulaDimacsFormat;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link de.featjar.formula.io.dimacs.FormulaDimacsFormat DIMACS} format.
 *
 * @author Sebastian Krieter
 */
public class DIMACSFormatTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    void testFixtures() {
        FormatTest.testParseAndSerialize("DIMACS/ABC-nAnBnC", new FormulaDimacsFormat());
        FormatTest.testParseAndSerialize("DIMACS/nA", new FormulaDimacsFormat());
        FormatTest.testParseAndSerialize("DIMACS/nAB", new FormulaDimacsFormat());
        FormatTest.testParse(reference(and(or(literal(false, "A")))), "DIMACS/nA", 1, new FormulaDimacsFormat());
    }

    // TODO: something is wrong with the Dimacs Serializer, go figure :-)
    @Test
    public void DIMACS_123_n1n2n3() {
        test("123-n1n2n3");
    }

    @Test
    public void DIMACS_ABC_nAnBnC() {
        testException("ABC-nAnBnC");
    }

    @Test
    public void DIMACS_empty() {
        test("empty");
    }

    //        @Test
    //        public void DIMACS_empty_1() {
    //            test("empty-1");
    //        }
    //
    //        @Test
    //        public void DIMACS_empty_A() {
    //            test("empty-A");
    //        }
    //
    //        @Test
    //        public void DIMACS_empty_ABC() {
    //            test("empty-ABC");
    //        }
    //
    //        @Test
    //        public void DIMACS_empty_A2C() {
    //            test("empty-A2C");
    //        }

    @Test
    public void DIMACS_nA() {
        testException("nA");
    }

    @Test
    public void DIMACS_nAB() {
        testException("nAB");
    }

    @Test
    public void DIMACS_faulty() {
        test("faulty");
    }

    @Test
    public void DIMACS_void() {
        test("void");
    }

    private static void test(String name) {
        FormatTest.testSerializeAndParse(getFormula(name), new FormulaDimacsFormat());
    }

    private void testException(final String name) {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    try {
                        IO.save(getFormula(name), OutputStream.nullOutputStream(), new FormulaDimacsFormat());
                    } catch (IOException e) {
                        throw e.getCause();
                    }
                },
                "Formula is not in CNF");
    }
}
