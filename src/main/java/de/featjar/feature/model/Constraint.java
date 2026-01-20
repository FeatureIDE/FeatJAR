/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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

import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IConstraint.IMutableConstraint;
import de.featjar.formula.structure.IFormula;
import java.util.LinkedHashSet;

public class Constraint extends AFeatureModelElement implements IMutableConstraint {
    protected IFormula formula;

    protected Constraint(IFeatureModel featureModel, IFormula formula) {
        super(featureModel);
        setFormula(formula);
    }

    protected Constraint(Constraint otherConstraint) {
        this(otherConstraint, otherConstraint.featureModel);
    }

    protected Constraint(Constraint otherConstraint, IFeatureModel newFeatureModel) {
        super(otherConstraint, newFeatureModel);
        setFormula(Trees.clone(otherConstraint.formula));
    }

    @Override
    public Constraint clone() {
        return new Constraint(this);
    }

    @Override
    public Constraint clone(IFeatureModel newFeatureModel) {
        return new Constraint(this);
    }

    @Override
    public IFormula getFormula() {
        return formula;
    }

    @Override
    public LinkedHashSet<IFeature> getReferencedFeatures() {
        return IConstraint.getReferencedFeatures(formula, featureModel);
    }

    @Override
    public String toString() {
        return String.format("Constraint{formula=%s}", formula);
    }

    @Override
    public void setFormula(IFormula formula) {
        this.formula = formula;
    }

    @Override
    public void setName(String name) {
        attributeValues.put(FeatureModelAttributes.NAME, name);
    }

    @Override
    public void setDescription(String description) {
        attributeValues.put(FeatureModelAttributes.DESCRIPTION, description);
    }
}
