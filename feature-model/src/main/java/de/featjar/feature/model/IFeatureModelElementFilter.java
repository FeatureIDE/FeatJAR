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
package de.featjar.feature.model;

import java.util.Collection;
import java.util.function.Predicate;

public interface IFeatureModelElementFilter extends Predicate<IFeatureModelElement> {

    public static final IFeatureModelElementFilter ALL = e -> true;
    public static final IFeatureModelElementFilter NONE = e -> false;

    public static FeatureNameListFilter featuresByName(Collection<String> featureNames) {
        return new FeatureNameListFilter(featureNames);
    }
}
