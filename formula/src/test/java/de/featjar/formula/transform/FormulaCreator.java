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
package de.featjar.formula.transform;

import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.predicate.Literal;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FormulaCreator {

    public static IFormula getFormula01() {
        final Literal p = Expressions.literal("p");
        final Literal q = Expressions.literal("q");
        final Literal r = Expressions.literal("r");
        final Literal s = Expressions.literal("s");

        return Expressions.implies(Expressions.and(Expressions.or(p, q), r), s.invert());
    }

    public static IFormula getFormula02() {
        final Literal p = Expressions.literal("p");
        final Literal q = Expressions.literal("q");
        final Literal r = Expressions.literal("r");
        final Literal s = Expressions.literal("s");

        return Expressions.and(
                Expressions.implies(r, Expressions.and(p, q)),
                Expressions.implies(s, Expressions.and(q, p)),
                Expressions.or(Expressions.and(s.invert(), r), Expressions.and(s, r.invert())));
    }

    public static Stream<BooleanAssignment> streamAllAssignments(int numVariables) {
        return IntStream.range(0, (int) Math.pow(2, numVariables)).mapToObj(i -> {
            final int[] literals = new int[numVariables];
            for (int j = 0; j < numVariables; j++) {
                literals[j] = (((i >> j) & 1) == 1) ? (j + 1) : -(j + 1);
            }
            return new BooleanAssignment(literals);
        });
    }
}
