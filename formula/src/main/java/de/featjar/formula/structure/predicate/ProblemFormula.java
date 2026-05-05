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
package de.featjar.formula.structure.predicate;

import de.featjar.base.data.Problem;
import de.featjar.formula.structure.ATerminalExpression;
import de.featjar.formula.structure.IExpression;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A placeholder for a formula that wraps a problem.
 * May be used for example when an expression cannot be parsed.
 *
 * @author Sebastian Krieter
 */
public class ProblemFormula extends ATerminalExpression implements IPredicate {
    private final Problem problem;

    public ProblemFormula(Problem problem) {
        this.problem = problem;
    }

    public Problem getProblem() {
        return problem;
    }

    @Override
    public String getName() {
        return problem.toString();
    }

    @Override
    public ProblemFormula cloneNode() {
        return new ProblemFormula(problem);
    }

    @Override
    public boolean equalsNode(IExpression other) {
        return super.equalsNode(other) && Objects.equals(problem, ((ProblemFormula) other).problem);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(super.hashCodeNode(), problem);
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        return Optional.empty();
    }
}
