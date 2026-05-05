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
 * A clause; that is, a disjunction of literals or equalities.
 * Implemented as an assignment of variables to values, which are, by convention, interpreted as a disjunction.
 * That is, a clause is fulfilled if at least one variable matches its assigned value.
 * Thus, this class generalizes the notion of a clause in propositional logic to also account for first-order logic.
 * For a propositional implementation, see {@link BooleanClause},
 * for a first-order implementation, see {@link ValueClause}.
 * If you need a conjunction of literals or equalities, consider using a {@link ISolution} instead.
 *
 * @param <T> the index type of the variables
 * @param <R> the value type of the variables
 * @author Elias Kuiter
 */
public interface IClause<T, R> extends IAssignment<T, R> {}
