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

import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.transformer.FeatureToFormula;
import de.featjar.formula.structure.ATerminalExpression;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.IfThenElse;
import de.featjar.formula.structure.term.function.AAdd;
import de.featjar.formula.structure.term.function.ADivide;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.function.integer.IntegerDivide;
import de.featjar.formula.structure.term.function.real.RealAdd;
import de.featjar.formula.structure.term.function.real.RealDivide;
import de.featjar.formula.structure.term.value.Constant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * The average aggregate placeholder sums attribute values from attributes with a specific attribute name and divides
 * the sum by the number of attributes.
 * Only boolean features which are selected ({@link de.featjar.formula.structure.term.value.Variable} with
 * type {@link Boolean} and value true) will be considered.
 *
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 * @author Sebastian Krieter
 */
public class AttributeAverage extends ATerminalExpression implements IAttributeAggregate {

    private final IAttribute<?> attribute;

    public AttributeAverage(IAttribute<?> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getName() {
        return "avg(" + attribute.getName().getFullName() + ")";
    }

    @Override
    public Class<?> getType() {
        return attribute.getClassType();
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        return Optional.empty();
    }

    @Override
    public ITree<IExpression> cloneNode() {
        return new AttributeAverage(attribute);
    }

    @Override
    public Result<IExpression> translate(Collection<IFeature> elements, FeatureToFormula featureToFormula) {
        Constant zero;
        Constant one;
        Class<?> type;
        Function<Object, Object> typeConverter;
        Function<List<ITerm>, AAdd> addNode;
        Function<List<ITerm>, ADivide> divideNode;
        if (attribute.getClassType() == Double.class || attribute.getClassType() == Float.class) {
            zero = new Constant(0.0, Double.class);
            one = new Constant(1.0, Double.class);
            type = Double.class;
            typeConverter = Function.identity();
            addNode = RealAdd::new;
            divideNode = RealDivide::new;
        } else if (attribute.getClassType() == Integer.class || attribute.getClassType() == Long.class) {
            zero = new Constant(0L, Long.class);
            one = new Constant(1L, Long.class);
            type = Long.class;
            typeConverter =
                    (attribute.getClassType() == Integer.class) ? i -> Long.valueOf((int) i) : Function.identity();
            addNode = IntegerAdd::new;
            divideNode = IntegerDivide::new;
        } else {
            return Result.empty(
                    new Problem(String.format("Unsupported type <%s> for attribute sum", attribute.getClassType())));
        }

        List<ITerm> termListSum = new ArrayList<>();
        List<ITerm> termListCount = new ArrayList<>();
        for (IFeature element : elements) {
            Optional<Object> optionalAttribute = element.getAttributes().map(list -> list.get(attribute));
            if (optionalAttribute.isPresent()) {
                Constant attributeValue = new Constant(typeConverter.apply(optionalAttribute.get()), type);
                for (String name :
                        featureToFormula.getNamesPerFeature(element.getName().orElse("???"))) {
                    IFormula formula = featureToFormula.getFeatureFormula(name);
                    termListSum.add(new IfThenElse(formula, attributeValue, zero));
                    termListCount.add(new IfThenElse(formula, one, zero));
                }
            }
        }
        return Result.of(divideNode.apply(Arrays.asList(addNode.apply(termListSum), addNode.apply(termListCount))));
    }
}
