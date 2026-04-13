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

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * A dynamic array of integer values.
 *
 * @author Sebastian Krieter
 */
public class ExpandableIntegerList implements Serializable {

    private static final long serialVersionUID = 1831680426776033300L;

    private int[] elements;
    private int size;

    public ExpandableIntegerList() {
        this(10);
    }

    public ExpandableIntegerList(int initialCapacity) {
        elements = new int[initialCapacity];
    }

    public ExpandableIntegerList(int... elements) {
        this.elements = elements;
        size = elements.length;
    }

    public ExpandableIntegerList(ExpandableIntegerList other) {
        this.elements = Arrays.copyOf(other.elements, other.elements.length);
        this.size = other.size;
    }

    public void clear() {
        size = 0;
    }

    public void ensure(int newSize) {
        if (newSize >= elements.length) {
            elements = Arrays.copyOf(elements, Math.max(newSize, elements.length * 2));
        }
    }

    public void add(int e) {
        ensure(size + 1);
        elements[size++] = e;
    }

    public void add(int... newElements) {
        ensure(size + newElements.length);
        for (int e : newElements) {
            elements[size++] = e;
        }
    }

    public void add(int e, int i) {
        if (i == size) {
            add(e);
        } else {
            if (size + 1 >= elements.length) {
                final int[] newElements = new int[Math.max(size + 1, elements.length * 2)];
                System.arraycopy(elements, 0, newElements, 0, i);
                System.arraycopy(elements, i, newElements, i + 1, (size + 1) - i);
                elements = newElements;
            } else {
                System.arraycopy(elements, i, elements, i + 1, (size + 1) - i);
            }
            elements[i] = e;
            size++;
        }
    }

    public void addUnsort(int e, int i) {
        ensure(size + 1);
        elements[size++] = elements[i];
        elements[i] = e;
    }

    public int getFirst() {
        return elements[0];
    }

    public int getLast() {
        return elements[size - 1];
    }

    public int get(int i) {
        return this.elements[i];
    }

    public void set(int i, int e) {
        this.elements[i] = e;
    }

    public int indexOf(int e) {
        for (int i = 0; i < size; i++) {
            if (elements[i] == e) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(int e) {
        for (int i = size - 1; i >= 0; i--) {
            if (elements[i] == e) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the given element
     * @param element the element to remove
     */
    public void remove(int element) {
        int index = indexOf(element);
        if (index >= 0) {
            removeAt(index);
        }
    }

    /**
     * Removes the last element
     */
    public void removeLast() {
        size--;
    }

    /**
     * Removes the element at the given index.
     * @param index the index to remove
     */
    public void removeAt(int index) {
        System.arraycopy(elements, index, elements, index - 1, size - index);
        size--;
    }

    public void removeAtUnsort(int i) {
        elements[i] = elements[--size];
    }

    public void sort() {
        Arrays.sort(elements, 0, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExpandableIntegerList other = (ExpandableIntegerList) obj;
        if (size != other.size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (elements[i] != other.elements[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (int i = 0; i < size; i++) {
            hash = 37 * hash + elements[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IntList [");
        if (size > 0) {
            sb.append(elements[0]);
        }
        for (int i = 1; i < size; i++) {
            sb.append(", ");
            sb.append(elements[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     *{@return The number of elements in this list}
     */
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * The internal array used to store the elements of this list.
     * Modifications will affect this instance.
     * Maybe larger than {@link #size()}.
     *
     * @return The internal array used to store the elements
     *
     * @see #toArray()
     * @see #toIntStream()
     */
    public int[] getInternalArray() {
        return elements;
    }

    /**
     * Creates an array containing all elements of this list.
     * Modifications will not affect this instance.
     * The array will be as large as {@link #size()}.
     * <br><br>
     * If modification is not necessary, but performance is important, consider using {@link #getInternalArray()}.
     *
     * @return An array containing all elements
     *
     * @see #getInternalArray()
     * @see #toIntStream()
     */
    public int[] toArray() {
        return Arrays.copyOf(elements, size);
    }

    /**
     * {@return A stream over all elements in this list}
     *
     * @see #getInternalArray()
     * @see #toArray()
     */
    public IntStream toIntStream() {
        return IntStream.range(0, size).map(i -> elements[i]);
    }
}
