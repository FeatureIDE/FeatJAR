/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.mixins;

import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.IIdentifier;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureModel;
import java.util.Collection;
import java.util.Objects;

/**
 * Implements a {@link IFeatureModel} mixin for common operations on {@link IConstraint constraints}.
 *
 * @author Elias Kuiter
 */
public interface IHasConstraints {
    Collection<IConstraint> getConstraints();

    default Result<IConstraint> getConstraint(IIdentifier identifier) {
        Objects.requireNonNull(identifier);
        return Result.ofOptional(getConstraints().stream()
                .filter(constraint -> constraint.getIdentifier().equals(identifier))
                .findFirst());
    }

    default boolean hasConstraint(IIdentifier identifier) {
        return getConstraint(identifier).isPresent();
    }

    default boolean hasConstraint(IConstraint constraint) {
        return hasConstraint(constraint.getIdentifier());
    }

    default int getNumberOfConstraints() {
        return getConstraints().size();
    }
}
