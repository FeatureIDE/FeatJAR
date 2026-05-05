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
import de.featjar.formula.io.xml.XMLFeatureModelFormulaFormat;
import org.junit.jupiter.api.Test;

public class XMLFeatureModelFormatTest {

    @Test
    void testFixtures() {
        FormatTest.testParse(Common.getFormula("A"), "FeatureIDE/A", 2, new XMLFeatureModelFormulaFormat());
        FormatTest.testParse(
                Common.getFormula("Root-ABC-nAnBnC"),
                "FeatureIDE/Root-ABC-nAnBnC",
                2,
                new XMLFeatureModelFormulaFormat());
    }

    //    @Test
    //    public void FeatureIDE_ABC_nAnBnC() {
    //        test("ABC-nAnBnC");
    //    }
    //
    //    @Test
    //    public void FeatureIDE_A() {
    //        test("A");
    //    }
    //
    //    @Test
    //    public void FeatureIDE_CNF_SingleGroups() {
    //        test("SingleGroups");
    //    }
    //
    //    @Test
    //    public void FeatureIDE_faulty() {
    //        test("faulty");
    //    }
    //
    //    private static void test(String name) {
    //        FormatTest.testLoad(getFormula(name), name, new XMLFeatureModelFormat());
    //    }
    //
    //    private static Expression getFormula(String name) {
    //        switch (name) {
    //            case "faulty": {
    //                return null;
    //            }
    //            case "ABC-nAnBnC": {
    //                final TermMap map = new TermMap();
    //                final Literal root = map.createLiteral("Root");
    //                final Literal a = map.createLiteral("A");
    //                final Literal b = map.createLiteral("B");
    //                final Literal c = map.createLiteral("C");
    //                return new And(
    //                        root.cloneNode(),
    //                        new Implies(a.cloneNode(), root.cloneNode()),
    //                        new Implies(b.cloneNode(), root.cloneNode()),
    //                        new Implies(c.cloneNode(), root.cloneNode()),
    //                        new Implies(root.cloneNode(), new Or(a.cloneNode(), b.cloneNode(), c.cloneNode())),
    //                        new Or(new Not(a.cloneNode()), new Not(b.cloneNode()), new Not(c.cloneNode())));
    //            }
    //            case "SingleGroups": {
    //                final TermMap map = new TermMap();
    //                final Literal root = map.createLiteral("Root");
    //                final Literal a = map.createLiteral("A");
    //                final Literal a1 = map.createLiteral("A1");
    //                final Literal b = map.createLiteral("B");
    //                final Literal b1 = map.createLiteral("B1");
    //                return new And(
    //                        root.cloneNode(),
    //                        new Implies(a.cloneNode(), root.cloneNode()),
    //                        new Implies(root.cloneNode(), a.cloneNode()),
    //                        new Implies(a1.cloneNode(), a.cloneNode()),
    //                        new Implies(a.cloneNode(), a1.cloneNode()),
    //                        new Implies(b.cloneNode(), root.cloneNode()),
    //                        new Implies(root.cloneNode(), b.cloneNode()),
    //                        new Implies(b1.cloneNode(), b.cloneNode()),
    //                        new Implies(b.cloneNode(), b1.cloneNode()));
    //            }
    //            case "A": {
    //                final TermMap map = new TermMap();
    //                final Literal a = map.createLiteral("A");
    //                return new And(a.cloneNode());
    //            }
    //            default:
    //                fail(name);
    //                return null;
    //        }
    // }
}
