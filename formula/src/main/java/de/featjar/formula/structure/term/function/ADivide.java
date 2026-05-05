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
package de.featjar.formula.structure.term.function;

import de.featjar.formula.structure.ANonTerminalExpression;
import de.featjar.formula.structure.IBinaryExpression;
import de.featjar.formula.structure.term.ITerm;
import java.util.List;

/**
 * Divides the values of two terms.
 *
 * @author Sebastian Krieter
 */
public abstract class ADivide extends ANonTerminalExpression implements IFunction, IBinaryExpression {

    protected ADivide() {}

    protected ADivide(ITerm leftTerm, ITerm rightTerm) {
        super(leftTerm, rightTerm);
    }

    protected ADivide(List<ITerm> arguments) {
        super(arguments);
    }

    @Override
    public String getName() {
        return "/";
    }
}
