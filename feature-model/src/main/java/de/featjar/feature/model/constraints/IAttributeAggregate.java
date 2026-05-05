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
package de.featjar.feature.model.constraints;

import de.featjar.base.data.Result;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.transformer.FeatureToFormula;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.term.ITerm;
import java.util.Collection;

/**
 * Interface for modeling attribute aggregate functionality. Attribute aggregates are placeholders which
 * will be translated into the actual formula.
 *
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 */
public interface IAttributeAggregate extends ITerm {

    Result<IExpression> translate(Collection<IFeature> elements, FeatureToFormula featureToFormula);
}
