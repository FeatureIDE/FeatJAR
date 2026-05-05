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

import de.featjar.base.FeatJAR;
import de.featjar.base.io.format.AFormats;
import de.featjar.formula.structure.IExpression;
import java.util.List;

/**
 * Extension point for {@link AFormats formats} for {@link IExpression}.
 *
 * @author Sebastian Krieter
 */
public class ExpressionListFormats extends AFormats<List<IExpression>> {

    public static ExpressionListFormats getInstance() {
        return FeatJAR.extensionPoint(ExpressionListFormats.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<List<IExpression>> getType() {
        return (Class<List<IExpression>>) (Class<?>) List.class;
    }
}
