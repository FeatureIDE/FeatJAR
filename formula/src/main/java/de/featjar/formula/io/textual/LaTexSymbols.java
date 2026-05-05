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

import de.featjar.formula.structure.connective.*;

/**
 * This class represents symbols for LaTex formulas.
 *
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 */
public class LaTexSymbols extends Symbols {

    public static final Symbols INSTANCE = new LaTexSymbols();

    public LaTexSymbols() {
        super(false);
        setSymbol(Not.class, "\\lnot{}");
        setSymbol(And.class, "\\land{}");
        setSymbol(Or.class, "\\lor{}");
        setSymbol(Implies.class, "\\Rightarrow{}");
        setSymbol(BiImplies.class, "\\Leftrightarrow{}");
        setSymbol(ForAll.class, "\\forall{}");
        setSymbol(Exists.class, "\\exists{}");
        setQuoteStart("\\text{");
        setQuoteEnd("}");
    }
}
