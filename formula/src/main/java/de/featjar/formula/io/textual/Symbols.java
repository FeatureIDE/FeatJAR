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
package de.featjar.formula.io.textual;

import de.featjar.base.data.Maps;
import de.featjar.base.data.Result;
import de.featjar.base.data.Trie;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Exists;
import de.featjar.formula.structure.connective.ForAll;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Symbols {

    private static class OperatorProperties {
        Boolean infix = null;
        String name = null;
        Integer priority = null;
    }

    private final LinkedHashMap<String, Class<? extends IExpression>> symbolToOperator = Maps.empty();
    private final LinkedHashMap<Class<? extends IExpression>, OperatorProperties> operatorToProperties = Maps.empty();
    private final Trie operatorNames = new Trie();

    private final boolean textual;
    private String quoteStart = "\"";
    private String quoteEnd = "\"";

    public Symbols(boolean textual) {
        this.textual = textual;
        setSymbol(Literal.class, "defined", 8, false);
        setSymbol(Not.class, "not", 7, false);
        setSymbol(And.class, "and", 6, true);
        setSymbol(Or.class, "or", 5, true);
        setSymbol(Implies.class, "implies", 4, true);
        setSymbol(BiImplies.class, "biimplies", 3, true);
        setSymbol(Choose.class, "choose", 2, false);
        setSymbol(AtLeast.class, "atleast", 2, false);
        setSymbol(Between.class, "between", 2, false);
        setSymbol(AtMost.class, "atmost", 2, false);
        setSymbol(Exists.class, "exists", 1, false);
        setSymbol(ForAll.class, "forall", 1, false);
    }

    private OperatorProperties getProperties(Class<? extends IExpression> operator) {
        OperatorProperties properties = operatorToProperties.get(operator);
        if (properties == null) {
            properties = new OperatorProperties();
            operatorToProperties.put(operator, properties);
        }
        return properties;
    }

    public void setSymbol(Class<? extends IExpression> operator, String name) {
        symbolToOperator.put(name, operator);
        final OperatorProperties properties = getProperties(operator);
        operatorNames.add(name);
        if (properties.name != null) {
            operatorNames.remove(properties.name);
        }
        properties.name = name;
    }

    public void setSymbol(Class<? extends IExpression> operator, String name, int priority) {
        symbolToOperator.put(name, operator);
        final OperatorProperties properties = getProperties(operator);
        operatorNames.add(name);
        if (properties.name != null) {
            operatorNames.remove(properties.name);
        }
        properties.name = name;
        properties.priority = priority;
    }

    public void setSymbol(Class<? extends IExpression> operator, String name, int priority, boolean infix) {
        symbolToOperator.put(name, operator);
        final OperatorProperties properties = getProperties(operator);
        operatorNames.add(name);
        if (properties.name != null) {
            operatorNames.remove(properties.name);
        }
        properties.name = name;
        properties.priority = priority;
        properties.infix = infix;
    }

    public void setQuoteStart(String quoteStart) {
        this.quoteStart = quoteStart;
    }

    public void setQuoteEnd(String quoteEnd) {
        this.quoteEnd = quoteEnd;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + symbolToOperator.keySet();
    }

    public boolean hasPrefix(String prefix) {
        return operatorNames.hasPrefix(prefix);
    }

    public Result<Class<? extends IExpression>> parseSymbol(String symbol) {
        return Result.ofNullable(symbolToOperator.get(symbol));
    }

    public boolean isTextual() {
        return textual;
    }

    public List<String> getSymbols() {
        return operatorToProperties.entrySet().stream()
                .map(e -> e.getValue().name)
                .collect(Collectors.toList());
    }

    public String getSymbol(IExpression operator) {
        return Result.ofNullable(operatorToProperties.get(operator.getClass()))
                .map(p -> p.name)
                .orElse(operator.getName());
    }

    public Result<String> getSymbolResult(Class<? extends IExpression> operator) {
        return Result.ofNullable(operatorToProperties.get(operator)).map(p -> p.name);
    }

    /**
     * Assigns a number to every operator. For instance, if {@link And} has a higher
     * order than {@link Or} then <em>(A and B or C)</em> is equal to <em>((A and B)
     * or C)</em>.
     *
     * @param operator operator type
     * @return the order assigned to the type of node
     */
    public Result<Integer> getPriority(Class<? extends IExpression> operator) {
        return Result.ofNullable(operatorToProperties.get(operator)).map(p -> p.priority);
    }

    public Result<Integer> getPriority(IExpression operator) {
        return getPriority(operator.getClass());
    }

    public Result<Boolean> getInfix(Class<? extends IExpression> operator) {
        return Result.ofNullable(operatorToProperties.get(operator)).map(p -> p.infix);
    }

    public Result<Boolean> getInfix(IExpression operator) {
        return getInfix(operator.getClass());
    }

    public List<String> getSortedSymbols() {
        return operatorToProperties.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().priority))
                .map(e -> e.getValue().name)
                .collect(Collectors.toList());
    }

    public String quoteStart() {
        return quoteStart;
    }

    public String quoteEnd() {
        return quoteEnd;
    }
}
