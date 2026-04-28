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
import de.featjar.base.tree.Trees;
import de.featjar.formula.io.IExpressionFormat;
import de.featjar.formula.io.textual.ExpressionSerializer.Notation;
import de.featjar.formula.structure.IExpression;

/**
 * Parses and serializes propositional and first-order expressions.
 *
 * @author Sebastian Krieter
 */
@Deprecated
public class ExpressionFormat extends ATextFormat<IExpression> implements IExpressionFormat {
    @Override
    public String getName() {
        return "Expression";
    }

    //    @Override
    //    public boolean supportsParse() {
    //        return true;
    //    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    //    @Override
    //    public Result<IExpression> parse(AInputMapper inputMapper) {
    //        return new ExpressionParser().parse(inputMapper.get().read().get());
    //    }

    @Override
    public Result<String> serialize(IExpression expression) {
        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setNotation(Notation.POSTFIX);
        return Trees.traverse(expression, serializer);
    }
}
