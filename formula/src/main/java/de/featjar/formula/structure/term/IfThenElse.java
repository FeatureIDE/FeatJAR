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
package de.featjar.formula.structure.term;

import de.featjar.base.tree.structure.ITree;
import de.featjar.formula.structure.ANonTerminalExpression;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import java.util.List;
import java.util.Optional;

/**
 * Node that chooses between two values depending on a condition.
 * First child of this node is a boolean expression. Second and third children are two expressions of the same type.
 * If the condition is met, the value of the second child is returned, otherwise the value of the third child is returned.
 *
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 * @author Sebastian Krieter
 */
public class IfThenElse extends ANonTerminalExpression implements ITerm {

    protected IfThenElse() {}

    public IfThenElse(IFormula formula, ITerm term1, ITerm term2) {
        super(formula, term1, term2);

        if (term1.getType() != term2.getType()) {
            throw new IllegalArgumentException("Term are not from the same type.");
        }
    }

    @Override
    public String getName() {
        return "IfThenElse";
    }

    @Override
    public Class<?> getType() {
        return getChildren().get(1).getType();
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        if (values.size() == 3) {
            Object condition = values.get(0);

            if (condition instanceof Boolean) {
                return Optional.ofNullable((Boolean) condition ? values.get(1) : values.get(2));
            }
        }
        return Optional.empty();
    }

    @Override
    public ITree<IExpression> cloneNode() {
        return new IfThenElse();
    }
}
