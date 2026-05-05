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
package de.featjar.formula.visitor;

import static de.featjar.formula.structure.Expressions.*;
import static org.junit.jupiter.api.Assertions.*;

import de.featjar.formula.structure.FormulaNormalForm;
import org.junit.jupiter.api.Test;

class NNFTesterTest {
    @Test
    void testNNF() {
        assertFalse(not(not(literal("x"))).isNNF());
        assertFalse(not(not(literal("x"))).isStrictNormalForm(FormulaNormalForm.NNF));
        assertTrue(not(literal("x")).isNNF());
        assertFalse(not(literal("x")).isStrictNormalForm(FormulaNormalForm.NNF));
        assertTrue(literal(false, "x").isNNF());
        assertTrue(literal(false, "x").isStrictNormalForm(FormulaNormalForm.NNF));
        assertFalse(implies(literal("x"), literal("y")).isNNF());
        assertFalse(implies(literal("x"), literal("y")).isStrictNormalForm(FormulaNormalForm.NNF));
    }
}
