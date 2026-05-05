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

import de.featjar.formula.structure.IBinaryExpression;
import java.util.List;
import java.util.Optional;

/**
 * A binary predicate.
 *
 * @author Elias Kuiter
 */
public interface IBinaryPredicate extends IPredicate, IBinaryExpression {
    @SuppressWarnings("rawtypes")
    @Override
    default Class<Comparable> getChildrenType() {
        return Comparable.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Optional<Boolean> evaluate(List<?> values) {
        final Comparable v1 = (Comparable) values.get(0);
        final Comparable v2 = (Comparable) values.get(1);
        return (v1 != null && v2 != null) ? Optional.of(compareDifference(v1.compareTo(v2))) : Optional.empty();
    }

    boolean compareDifference(int difference);
}
