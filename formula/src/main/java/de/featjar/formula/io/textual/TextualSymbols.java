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
package de.featjar.formula.io.textual;

import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;

/**
 * Symbols for a long textual representation. These are best used for editing by
 * the user due to simplicity and ease of handling.
 *
 * @author Timo Günther
 * @author Sebastian Krieter
 */
public class TextualSymbols extends Symbols {

    public static final Symbols INSTANCE = new TextualSymbols();

    private TextualSymbols() {
        super(true);
        setSymbol(Not.class, "not");
        setSymbol(And.class, "and");
        setSymbol(Or.class, "or");
        setSymbol(Implies.class, "implies");
        setSymbol(BiImplies.class, "iff");
    }
}
