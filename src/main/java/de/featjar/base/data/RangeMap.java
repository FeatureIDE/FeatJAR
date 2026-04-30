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
package de.featjar.base.data;

import de.featjar.base.log.IndentFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps a collection of at most n objects to the range of natural numbers [1,
 * n]. Typically maps n objects one-to-one onto the range [1, n], but can
 * contain definition gaps if needed. TODO: currently, the edge case of an empty
 * range is not handled gracefully (with optionals everywhere). maybe this can
 * be solved in a better way.
 *
 * @param <T> the type of the elements in the map
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class RangeMap<T> implements Cloneable {

    /**
     * Indexed list of all elements.
     */
    protected final ArrayList<T> indexToObject = new ArrayList<>();

    /**
     * Element to index map.
     */
    protected final LinkedHashMap<T, Integer> objectToIndex = Maps.empty();

    /**
     * Creates an empty range map.
     */
    public RangeMap() {
        indexToObject.add(null);
    }

    /**
     * Creates a range map for [1, n] from a collection with n elements.
     *
     * @param collection the collection
     */
    public RangeMap(Collection<T> collection) {
        indexToObject.add(null);
        indexToObject.addAll(collection);
        updateObjectToIndex();
    }

    /**
     * Copies a range map.
     *
     * @param rangeMap the map
     */
    protected RangeMap(RangeMap<T> rangeMap) {
        this(rangeMap.getObjects(true));
    }

    /**
     * Merges two or more range maps into this range map. Joins on common objects and does
     * not necessarily preserve indices.
     *
     * @param rangeMapList the list of maps to merge
     */
    public RangeMap(List<? extends RangeMap<T>> rangeMapList) {
        SortedSet<T> objects = new TreeSet<>();
        for (RangeMap<T> m : rangeMapList) {
            objects.addAll(m.getObjects(false));
        }
        indexToObject.add(null);
        indexToObject.addAll(objects);
        updateObjectToIndex();
    }

    /**
     * {@return the number of mapped objects}
     */
    public int size() {
        return objectToIndex.size();
    }

    /**
     * {@return the maximum index in the element list}
     * Returns 0 for an empty map.
     */
    public int maxIndex() {
        return indexToObject.size() - 1;
    }

    /**
     * {@return the range of valid indices in this range map}
     * Returns an empty result for an empty map.
     */
    public Result<Range> getValidIndexRange() {
        return indexToObject.size() == 1 ? Result.empty() : Result.of(Range.of(1, indexToObject.size() - 1));
    }
    /**
     * {@return the minimum index of the map}
     * Returns an empty result for an empty map.
     */
    protected Result<Integer> getMinimumIndex() {
        return getValidIndexRange().map(Range::getLowerBound);
    }
    /**
     * {@return the maximum index of the map}
     * Returns an empty result for an empty map.
     */
    protected Result<Integer> getMaximumIndex() {
        return getValidIndexRange().map(Range::getUpperBound);
    }

    /**
     * {@return whether the given index is between the minimum and maximum index (inclusive) of this map}
     * @param index the index
     */
    protected boolean isValidIndex(int index) {
        return getValidIndexRange().map(range -> range.test(index)).orElse(false);
    }

    /**
     * {@return whether the given index is mapped by this range map}
     *
     * @param index the index
     */
    public boolean has(int index) {
        return isValidIndex(index) && indexToObject.get(index) != null;
    }

    /**
     * {@return whether the given object is mapped by this range map}
     *
     * @param object the object
     */
    public boolean has(T object) {
        return object != null && objectToIndex.containsKey(object);
    }

    /**
     * Sets a new object for an index mapped by this range map.
     *
     * @param index     the index
     * @param newObject the new object
     */
    public void setNewObject(int index, T newObject) {
        Objects.requireNonNull(newObject);
        if (isValidIndex(index)) {
            T object = indexToObject.get(index);
            if (object != null) {
                indexToObject.set(index, newObject);
                objectToIndex.remove(object);
                objectToIndex.put(newObject, index);
            } else {
                throw new NoSuchElementException(String.valueOf(index));
            }
        } else {
            throw new NoSuchElementException(String.valueOf(index));
        }
    }

    /**
     * Sets a new object for an object mapped by this range map.
     *
     * @param oldObject the old object
     * @param newObject the new object
     */
    public void setNewObject(T oldObject, T newObject) {
        Objects.requireNonNull(oldObject);
        Objects.requireNonNull(newObject);
        Integer index = objectToIndex.get(oldObject);
        if (index != null) {
            indexToObject.set(index, newObject);
            objectToIndex.remove(oldObject);
            objectToIndex.put(newObject, index);
        } else {
            throw new NoSuchElementException(String.valueOf(oldObject));
        }
    }

    /**
     * Maps an index to an object.
     *
     * @param index  the index, if this parameter equals -1, the next free index is determined.
     * @param object the object
     * @throws IllegalArgumentException if the index or object are invalid or
     *                                  already mapped
     * @return the index used for the object
     */
    public int add(int index, T object) {
        if (index < -1 || index == 0) {
            throw new IllegalArgumentException("index is invalid");
        } else if (index == -1) {
            index = getMaximumIndex().map(i -> i + 1).orElse(1);
        } else if (isValidIndex(index) && indexToObject.get(index) != null) {
            throw new IllegalArgumentException("element with the index " + index + " already mapped");
        }
        if (objectToIndex.containsKey(object)) {
            throw new IllegalArgumentException("element with the object " + object + " already mapped");
        }
        for (int i = getMaximumIndex().orElse(0); i < index; i++) {
            indexToObject.add(null);
        }
        objectToIndex.put(object, index);
        indexToObject.set(index, object);
        return index;
    }

    /**
     * Maps the next free index to an object.
     *
     * @param object the object
     *
     * @return the new index of the object
     */
    public int add(T object) {
        return add(-1, object);
    }

    /**
     * Adds objects of another map to this map, excluding duplicates.
     * @param other the range map to be added
     */
    public void addAll(RangeMap<T> other) {
        for (T variable : other.getObjects(true)) {
            if (!has(variable)) {
                add(variable);
            }
        }
    }

    /**
     * Removes an object mapped by this range map.
     *
     * @param object the object
     * @return whether the object was removed from this range map
     */
    public boolean remove(T object) {
        Integer index = objectToIndex.get(object);
        if (index != null) {
            if (index.equals(getMaximumIndex().get())) {
                indexToObject.remove(index.intValue());
            } else {
                indexToObject.set(index, null);
            }
            objectToIndex.remove(object);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes an index mapped by this range map.
     *
     * @param index the index
     * @return whether the index was removed from this range map
     */
    public boolean remove(int index) {
        if (isValidIndex(index)) {
            T object = indexToObject.get(index);
            if (object != null) {
                objectToIndex.remove(object);
            }
            if (index == getMaximumIndex().get()) {
                indexToObject.remove(index);
            } else {
                indexToObject.set(index, null);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@return whether this range map has gaps} That is, whether not all indices in
     * {@link #getValidIndexRange()} are mapped.
     */
    public boolean hasGaps() {
        return objectToIndex.size() != indexToObject.size();
    }

    /**
     * {@return whether this range map also maps all objects mapped by a given range
     * map}
     *
     * @param rangeMap the range map
     */
    public boolean containsAllObjects(RangeMap<T> rangeMap) {
        return objectToIndex.keySet().containsAll(rangeMap.objectToIndex.keySet());
    }

    /**
     * {@return all objects mapped by this range map}
     *
     * If the map has gaps, the return list does not contain {@code null} objects.
     *
     * @see #getObjects(boolean)
     */
    public List<T> getObjects() {
        return getObjects(false);
    }

    /**
     * {@return all objects mapped by this range map}
     * @param includeGaps if {@code true} and this map has gaps, the returned list contains {@code null} if there is no mapped object.
     */
    public List<T> getObjects(boolean includeGaps) {
        return includeGaps
                ? Collections.unmodifiableList(indexToObject.subList(1, indexToObject.size()))
                : indexToObject.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    /**
     * {@return an unmodifiable list of the objects mapped to the given list of indices}
     * The objects are in the same order as the given indices, but the list does not contain a object for any invalid index.
     * @param indices the list of indices
     */
    public List<T> getObjects(IntegerList indices) {
        return getObjects(indices, false);
    }

    /**
     * {@return an unmodifiable list of the objects mapped to the given list of indices}
     * The objects are in the same order as the given indices, but the list does not contain a object for any invalid index.
     * @param indices the list of indices
     * @param includeGaps if {@code true} and any given index is invalid, the returned list contains {@code null} at this position.
     */
    public List<T> getObjects(IntegerList indices, boolean includeGaps) {
        return (includeGaps
                        ? indices.stream().mapToObj(i -> isValidIndex(Math.abs(i)) ? indexToObject.get(i) : null)
                        : indices.stream()
                                .filter(i -> isValidIndex(Math.abs(i)))
                                .mapToObj(i -> indexToObject.get(i)))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * {@return the object an index is mapped to by this range map}
     *
     * @param index the index
     */
    public Result<T> get(int index) {
        return isValidIndex(index)
                ? Result.ofNullable(indexToObject.get(index))
                : Result.empty(new IndexOutOfBoundsException(index));
    }

    /**
     * {@return the index an object is mapped to by this range map}
     *
     * @param object the object
     */
    public Result<Integer> get(T object) {
        return Result.ofNullable(objectToIndex.get(object));
    }

    /**
     * {@return an unmodifiable list of all indices in this maps}
     */
    public List<Integer> getIndices() {
        return entryStream().map(e -> e.getValue()).collect(Collectors.toUnmodifiableList());
    }

    /**
     * {@return an unmodifiable list of objects that are mapped to the given objects}
     *
     * @param objects a list of objects
     */
    public List<Integer> getIndices(List<T> objects) {
        return stream(objects).collect(Collectors.toUnmodifiableList());
    }

    /**
     * {@return a human readable mapping}
     */
    public String print() {
        return stream()
                .map(pair -> String.format("%d <-> %s", pair.getKey(), pair.getValue()))
                .collect(Collectors.joining(", "));
    }

    /**
     * {@return a stream of the element to index map}
     */
    protected Stream<Entry<T, Integer>> entryStream() {
        return objectToIndex.entrySet().stream();
    }

    /**
     * {@return all objects mapped by this range map}
     */
    public Stream<Pair<Integer, T>> stream() {
        return entryStream().map(Pair::of).map(Pair::flip);
    }

    /**
     * {@return a stream of objects that are mapped to the given indices}
     *
     * @param indices a list of indices
     */
    public Stream<T> stream(IntegerList indices) {
        return indices.stream().filter(this::isValidIndex).mapToObj(indexToObject::get);
    }

    /**
     * {@return a stream of indices that are mapped to the given objects}
     *
     * @param objects a list of objects
     */
    public Stream<Integer> stream(Collection<T> objects) {
        return objects.stream().map(objectToIndex::get);
    }

    /**
     * Clears this range map.
     */
    public void clear() {
        objectToIndex.clear();
        indexToObject.clear();
        indexToObject.add(null);
    }

    private void updateObjectToIndex() {
        if (!isEmpty()) {
            int min = getMinimumIndex().get();
            int max = getMaximumIndex().get();
            objectToIndex.clear();
            for (int i = min; i <= max; i++) {
                T object = indexToObject.get(i);
                if (object != null) {
                    objectToIndex.put(object, i);
                }
            }
        }
    }

    /**
     * Randomizes the indices of this range map.
     *
     * @param random the random number generator
     */
    public void randomize(Random random) {
        if (!isEmpty()) {
            Collections.shuffle(
                    indexToObject.subList(
                            getMinimumIndex().get(), getMaximumIndex().get()),
                    random);
            updateObjectToIndex();
        }
    }

    /**
     * Normalizes the indices of this range map by removing any gaps.
     */
    public void normalize() {
        if (!isEmpty()) {
            int normalizedIndex = 1;
            for (int i = 1; i < indexToObject.size(); i++) {
                T o = indexToObject.get(i);
                if (o != null) {
                    indexToObject.set(normalizedIndex, o);
                    normalizedIndex++;
                }
            }
            if (indexToObject.size() > normalizedIndex) {
                indexToObject.subList(normalizedIndex, indexToObject.size()).clear();
            }
            updateObjectToIndex();
        }
    }

    /**
     * {@return whether this map contains no elements}
     */
    public boolean isEmpty() {
        return getValidIndexRange().isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(indexToObject);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        return Objects.equals(indexToObject, ((RangeMap) obj).indexToObject);
    }

    @Override
    public String toString() {
        return IndentFormatter.formatList("RangeMap", indexToObject.subList(1, indexToObject.size()));
    }

    @Override
    public RangeMap<T> clone() {
        return new RangeMap<>(this);
    }

    /**
     * Adapts each element from {@code oldIndices} from its index in this map to its index in {@code newMap}.
     * The adapted indices are stored in {@code newIndices}.
     * Caller must ensure that {@code newIndices} is at least as large as {@code oldIndices}.
     * Indices may be negative. In this case, the absolute value is used for adapting the mapping and the return value will also be negative.
     *
     * @param oldIndices the indices to adapt
     * @param newIndices the space for the adapted indices
     * @param newMap the range map to adapt to
     * @param integrateOldObjects if {@code true} objects that do not occur in {@code newMap} are added to it, otherwise an exception is thrown in this case.
     */
    public void adapt(int[] oldIndices, int[] newIndices, RangeMap<T> newMap, boolean integrateOldObjects) {
        for (int i = 0; i < oldIndices.length; i++) {
            newIndices[i] = adapt(oldIndices[i], newMap, integrateOldObjects);
        }
    }

    /**
     * Adapt {@code oldIndex} from its index in this map to its index in {@code newMap}.
     * The index may be negative. In this case, the absolute value is used for adapting the mapping and the return value will also be negative.
     *
     * @param oldIndex the index to adapt
     * @param newMap the range map to adapt to
     * @param integrateOldObject if {@code true} an object that does not occur in {@code newMap} is added to it, otherwise an exception is thrown in this case.
     * @return the adapted index
     */
    public int adapt(int oldIndex, RangeMap<T> newMap, boolean integrateOldObject) {
        if (oldIndex == 0) {
            return 0;
        } else {
            final Result<T> name = get(Math.abs(oldIndex));
            if (name.isPresent()) {
                T variableName = name.get();
                final int newLiteral;
                Result<Integer> index = newMap.get(variableName);
                if (index.isEmpty()) {
                    if (integrateOldObject) {
                        newLiteral = newMap.add(variableName);
                    } else {
                        throw new IllegalArgumentException("No variable named " + variableName);
                    }
                } else {
                    newLiteral = index.get();
                }
                return oldIndex < 0 ? -newLiteral : newLiteral;
            } else {
                throw new IllegalArgumentException("No variable with index " + oldIndex);
            }
        }
    }
}
