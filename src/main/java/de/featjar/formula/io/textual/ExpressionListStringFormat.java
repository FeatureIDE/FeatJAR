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

import de.featjar.base.data.Result;
import de.featjar.base.io.text.ATextFormat;
import de.featjar.base.tree.structure.ITree;
import de.featjar.formula.structure.IExpression;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes a list of expressions to a string representation using {@link ITree#print()}.
 *
 * @author Niclas Kleinert
 * @author Klara Surmeier
 * @author Sebastian Krieter
 */
public class ExpressionListStringFormat extends ATextFormat<List<IExpression>> {

    @Override
    public ExpressionListStringFormat getInstance() {
        return this;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> serialize(List<IExpression> formula) {
        return Result.of(formula.stream().map(IExpression::print).collect(Collectors.joining("\n")));
    }
}
