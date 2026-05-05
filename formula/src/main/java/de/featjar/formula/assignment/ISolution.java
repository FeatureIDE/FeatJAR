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
package de.featjar.formula.assignment;

/**
 * A solution; that is, a conjunction of generalized literals.
 * Implemented as an assignment of variables to values, which are, by convention, interpreted as a conjunction.
 * That is, a clause is fulfilled if all variables match their assigned values.
 * Thus, this class generalizes the notion of a solution (also known as satisfying assignment or model)
 * in propositional logic to also account for first-order logic.
 * For a propositional implementation, see {@link BooleanSolution},
 * for a first-order implementation, see {@link ValueSolution}.
 * If you need a disjunction of generalized literals, consider using a {@link IClause} instead.
 *
 * @param <T> the index type of the variables
 * @param <R> the value type of the variables
 * @author Elias Kuiter
 */
public interface ISolution<T, R> extends IAssignment<T, R> {}
