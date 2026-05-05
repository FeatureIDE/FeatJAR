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

import static de.featjar.formula.structure.Expressions.reference;
import static org.junit.jupiter.api.Assertions.*;

import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.IFormula;

public class VisitorTest {
    public static void traverseAndAssertSameFormula(IFormula oldFormula, ITreeVisitor<IFormula, ?> treeVisitor) {
        oldFormula = reference(oldFormula);
        IFormula newFormula = (IFormula) oldFormula.cloneTree();
        assertTrue(Trees.traverse(newFormula, treeVisitor).getProblems().isEmpty());
        assertEquals(oldFormula, newFormula);
    }

    public static void traverseAndAssertFormulaEquals(
            IFormula oldFormula, ITreeVisitor<IFormula, ?> treeVisitor, IFormula assertFormula) {
        oldFormula = reference(oldFormula);
        IFormula newFormula = (IFormula) oldFormula.cloneTree();
        assertTrue(Trees.traverse(newFormula, treeVisitor).getProblems().isEmpty());
        assertNotEquals(oldFormula, newFormula);
        assertEquals(reference(assertFormula), newFormula);
    }
}
