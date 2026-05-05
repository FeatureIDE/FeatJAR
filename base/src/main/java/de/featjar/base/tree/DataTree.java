/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.tree;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.Attributes;
import de.featjar.base.data.Result;
import de.featjar.base.data.ValueUtils;
import de.featjar.base.data.Void;
import de.featjar.base.tree.structure.ATree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * A tree of nodes with a given name and some data.
 *
 * @param <T> type of value this node holds
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class DataTree<T> extends ATree<DataTree<?>> {

    public static final String NAMESPACE = DataTree.class.getSimpleName();

    public static DataTree<Void> of(String name) {
        return new DataTree<>(Attributes.get(NAMESPACE, Objects.requireNonNull(name), Void.class), (Void) null);
    }

    public static DataTree<Void> of(String name, List<DataTree<?>> children) {
        return new DataTree<>(
                Attributes.get(NAMESPACE, Objects.requireNonNull(name), Void.class), (Void) null, children);
    }

    public static <R> DataTree<R> ofValue(String name, R value) {
        return ofValue(name, value, List.of());
    }

    @SuppressWarnings("unchecked")
    public static <R> DataTree<R> ofValue(String name, R value, List<DataTree<?>> children) {
        return new DataTree<>(
                Attributes.get(NAMESPACE, Objects.requireNonNull(name), (Class<R>) value.getClass()), value, children);
    }

    @SuppressWarnings("unchecked")
    public static <R> DataTree<R> ofAggregator(
            String name, R defaultValue, BinaryOperator<R> aggregator, List<DataTree<R>> children) {
        return new DataTree<>(
                Attributes.get(NAMESPACE, Objects.requireNonNull(name), (Class<R>) defaultValue.getClass()),
                aggregator,
                () -> defaultValue,
                children);
    }

    public static <R> DataTree<R> ofAggregator(
            String name,
            Class<R> type,
            BinaryOperator<R> aggregator,
            Supplier<R> defaultValue,
            List<DataTree<R>> children) {
        return new DataTree<>(
                Attributes.get(NAMESPACE, Objects.requireNonNull(name), type), aggregator, defaultValue, children);
    }

    public static <R> DataTree<R> ofMap(String name, Class<R> type, Map<String, R> dataMap) {
        return ofMap(name, type, dataMap, null, () -> null);
    }

    public static <R> DataTree<R> ofMap(
            String name,
            Class<R> type,
            Map<String, R> dataMap,
            BinaryOperator<R> aggregator,
            Supplier<R> defaultValue) {
        Attribute<R> attribute = Attributes.get(NAMESPACE, Objects.requireNonNull(name), type);
        List<DataTree<R>> children = new ArrayList<>(dataMap.size());
        for (Entry<String, R> dataEntry : dataMap.entrySet()) {
            children.add(new DataTree<>(Attributes.get(NAMESPACE, dataEntry.getKey(), type), dataEntry.getValue()));
        }
        Collections.sort(children, Comparator.comparing(DataTree::getAttribute));
        return new DataTree<>(attribute, aggregator, defaultValue, children);
    }

    private static <R> R aggregate(BinaryOperator<R> aggregator, Supplier<R> defaultValue, List<DataTree<R>> children) {
        Iterator<DataTree<R>> iterator = children.iterator();
        if (aggregator != null) {
            R aggregatedValue = null;
            while (iterator.hasNext()) {
                R value = iterator.next().value;
                if (aggregatedValue == null) {
                    aggregatedValue = value;
                } else if (value != null) {
                    aggregatedValue = aggregator.apply(aggregatedValue, value);
                }
            }
            if (aggregatedValue != null) {
                return aggregatedValue;
            }
        }
        return defaultValue.get();
    }

    private final Attribute<T> attribute;
    private final T value;

    public DataTree(Attribute<T> attribute) {
        this.attribute = Objects.requireNonNull(attribute);
        this.value = null;
    }

    public DataTree(Attribute<T> attribute, T value) {
        this.attribute = Objects.requireNonNull(attribute);
        this.value = value;
    }

    public DataTree(Attribute<T> attribute, DataTree<?>... children) {
        this(attribute, null, Arrays.asList(children));
    }

    public DataTree(Attribute<T> attribute, List<DataTree<?>> children) {
        this(attribute, null, children);
    }

    public DataTree(Attribute<T> attribute, T value, DataTree<?>... children) {
        this(attribute, value, Arrays.asList(children));
    }

    public DataTree(Attribute<T> attribute, T value, List<DataTree<?>> children) {
        super(children.size());
        this.attribute = Objects.requireNonNull(attribute);
        this.value = value;
        setChildren(children);
    }

    public DataTree(
            Attribute<T> attribute,
            BinaryOperator<T> aggregator,
            Supplier<T> defaultValue,
            List<DataTree<T>> children) {
        super(children.size());
        this.attribute = Objects.requireNonNull(attribute);
        this.value = aggregate(aggregator, defaultValue, children);
        setChildren(children);
    }

    protected DataTree(DataTree<T> analysisTree) {
        this.attribute = analysisTree.attribute;
        this.value = analysisTree.value;
    }

    public Attribute<T> getAttribute() {
        return attribute;
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    /**
     * {@return the child of this node with the specified attribute name, if any}
     *
     * @param name the simple name of the attribute
     */
    public Result<DataTree<?>> getChild(String name) {
        for (DataTree<?> child : getChildren()) {
            if (Objects.equals(name, child.attribute.getSimpleName())) {
                return Result.of(child);
            }
        }
        return Result.empty();
    }

    @Override
    public DataTree<T> cloneNode() {
        return new DataTree<>(this);
    }

    @Override
    public boolean equalsNode(DataTree<?> other) {
        return Objects.equals(attribute, other.attribute) && Objects.deepEquals(value, other.value);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(attribute, ValueUtils.hashValue(value));
    }

    @Override
    public String toString() {
        return String.format(
                "%s.%s = %s (%s)",
                attribute.getNamespace(),
                attribute.getSimpleName(),
                ValueUtils.toStringValue(value),
                attribute.getClassType());
    }
}
