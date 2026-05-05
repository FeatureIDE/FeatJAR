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
package de.featjar.feature.model.transformer;

import de.featjar.feature.model.IFeature;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.predicate.ILiteral;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NonBooleanLiteral;
import de.featjar.formula.structure.predicate.NotEquals;
import de.featjar.formula.structure.term.value.Variable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Defines useful methods to wrap a bool or numeric feature into a IFormula:
 *      bool: {@link de.featjar.formula.structure.predicate.Literal}
 *      int: {@link NotEquals 0}
 *      float: {@link NotEquals 0}
 *
 * Numeric features are therefore selected, if there value is not 0.
 *
 * @author Jonas Hanke
 * @author Sebastian Krieter
 */
public class FeatureToFormula {

    private final Map<String, ILiteral> nameToLiteral = new LinkedHashMap<>();
    private final Map<String, List<String>> featureNameToNames = new LinkedHashMap<>();
    private final LinkedHashSet<Variable> variables = new LinkedHashSet<>();

    public ILiteral getFeatureFormula(String featureName) {
        return nameToLiteral.get(featureName);
    }

    public List<String> getNamesPerFeature(String featureName) {
        List<String> names = featureNameToNames.get(featureName);
        return names == null ? List.of(featureName) : Collections.unmodifiableList(names);
    }

    public IFormula createFeatureFormula(IFeature feature) {
        return createFeatureFormula(feature, feature.getName().orElse("???"));
    }

    public IFormula createFeatureFormula(IFeature feature, String featureName) {
        if (nameToLiteral.containsKey(featureName)) {
            throw new IllegalAccessError(String.format("Formula for feature %s already exists.", featureName));
        }
        ILiteral formula = newFeatureFormula(feature, featureName);
        nameToLiteral.put(featureName, formula);
        featureNameToNames
                .computeIfAbsent(feature.getName().orElse("???"), k -> new LinkedList<>())
                .add(featureName);
        return formula;
    }

    public void initFeatureNames(Collection<IFeature> features) {
        for (IFeature feature : features) {
            featureNameToNames.computeIfAbsent(feature.getName().orElse("???"), k -> new LinkedList<>());
        }
    }

    private ILiteral newFeatureFormula(IFeature feature, String featureName) {
        Class<?> type = feature.getType();
        Variable variable = new Variable(featureName, type);
        variables.add(variable);

        if (type.equals(Boolean.class)) {
            return new Literal(variable);
        } else {
            return new NonBooleanLiteral(variable);
        }
    }

    public Collection<Variable> getVariables() {
        return Collections.unmodifiableCollection(variables);
    }
}
