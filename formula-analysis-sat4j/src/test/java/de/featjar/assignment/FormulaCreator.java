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
package de.featjar.assignment;

public class FormulaCreator {

    //    public static IExpression getFormula01() {
    //        final TermMap map = new TermMap();
    //        final Literal p = map.createLiteral("p");
    //        final Literal q = map.createLiteral("q");
    //        final Literal r = map.createLiteral("r");
    //        final Literal s = map.createLiteral("s");
    //
    //        return new Implies(new And(new Or(p, q), r), s.invert());
    //    }
    //
    //    public static IExpression getFormula02() {
    //        final TermMap map = new TermMap();
    //        final Literal p = map.createLiteral("p");
    //        final Literal q = map.createLiteral("q");
    //        final Literal r = map.createLiteral("r");
    //        final Literal s = map.createLiteral("s");
    //
    //        return new And(
    //                new Implies(r, new And(p, q)),
    //                new Implies(s, new And(q, p)),
    //                new Or(new And(s.invert(), r), new And(s, r.invert())));
    //    }
    //
    //    public static void testAllAssignments(TermMap map, Consumer<Assumable> testFunction) {
    //        final Assumable assumable = new Assignment(map);
    //        final int numVariables = map.getVariableCount();
    //        final int numAssignments = (int) Math.pow(2, numVariables);
    //        for (int i = 0; i < numAssignments; i++) {
    //            for (int j = 0; j < numVariables; j++) {
    //                assumable.set(j + 1, ((i >> j) & 1) == 1);
    //            }
    //            testFunction.accept(assumable);
    //        }
    //    }
}
