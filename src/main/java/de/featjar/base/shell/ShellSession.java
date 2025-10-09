/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.base.shell;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.shell.type.IVariableType;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The session in which all loaded formats are stored.
 *
 * @author Niclas Kleinert
 */
public class ShellSession {

    public static final class StoredElement<T> {
        private final IVariableType<T> type;
        private final T element;

        public StoredElement(IVariableType<T> type, T element) {
            this.type = type;
            this.element = element;
        }

        public IVariableType<T> getType() {
            return type;
        }

        public String getTypeName() {
            return type.getName();
        }

        public Class<T> getClassType() {
            return type.getClassType();
        }

        public T getElement() {
            return element;
        }
    }

    private final Map<String, StoredElement<?>> elements;

    public ShellSession() {
        elements = new LinkedHashMap<>();
    }

    /**
     * Returns the element if the key is present in the session
     * and casts the element to the known type.
     *
     * @param <T> generic type of element
     * @param key the elements' key
     * @param kownType the elements' type
     * @return the element of the shell session or an empty result if the element is not present
     */
    @SuppressWarnings("unchecked")
    public <T> Result<T> getValue(String key, Class<T> kownType) {
        StoredElement<?> storedElement = elements.get(key);

        if (storedElement == null) {
            return notPresent(key);
        }
        if (storedElement.getClassType() == kownType) {
            return Result.of((T) storedElement.getClassType().cast(storedElement.element));
        } else {
            throw new RuntimeException("Wrong Type");
        }
    }

    /**
     * Returns the element if the key is present in the session or
     * ,otherwise, an empty result containing an error message.
     *
     * @param key the elements' key
     * @return the element that is mapped to the key or an empty result if it is not present
     */
    public Result<Object> getValue(String key) {
        return elements.get(key) != null ? Result.of(elements.get(key)).map(e -> e.element) : notPresent(key);
    }

    /**
     * Returns the type of an element or
     * ,otherwise, an empty result containing an error message.
     *
     * @param key the elements' key
     * @return the type of the element or an empty result if the element is not present
     */
    public Result<Class<?>> getType(String key) {
        return elements.get(key) != null
                ? Result.of(elements.get(key)).map(StoredElement::getClassType)
                : notPresent(key);
    }

    /**
     * Returns the stored element with the given key or an empty result if no such element exists.
     *
     * @param key the elements' key
     * @return the type of the element or an empty result if the element is not present
     */
    public Result<StoredElement<?>> getElement(String key) {
        return Result.ofNullable(elements.get(key));
    }

    /**
     * Removes a single element of the shell session.
     *
     * @param key the elements' key
     * @return non-null previous value if the removal was successful
     */
    public Result<?> remove(String key) {
        return Result.of(elements.remove(key)).or(notPresent(key));
    }

    private <T> Result<T> notPresent(String key) {
        return Result.empty(new Problem(
                String.format("A variable named '%s' is not present in the session!", key), Severity.ERROR));
    }

    /**
     * Puts an element into the session.
     *
     * @param <T> generic type of element
     * @param key the elements' key
     * @param element the element of the shell session
     * @param type the elements' type
     */
    public <T> boolean put(String key, Object element, IVariableType<T> type) {
        if (elements.containsKey(key)) {
            FeatJAR.log().error("Session already contains a variable named %s", key);
            return false;
        }
        Class<T> classType = type.getClassType();
        if (!classType.isInstance(element)) {
            FeatJAR.log().error("Object is not of type %s", type.getName());
            return false;
        }
        elements.put(key, new StoredElement<>(type, classType.cast(element)));
        return true;
    }

    /**
     * Removes all elements of the session.
     */
    public void clear() {
        elements.clear();
    }

    /**
     * {@return the number of elements in the session}
     */
    public int getSize() {
        return elements.size();
    }

    /**
     * Checks if the shell session contains a element with a specific key.
     *
     * @param key the elements' key
     * @return true if a variable with given key is present
     */
    public boolean containsKey(String key) {
        return elements.containsKey(key);
    }

    /**
     * Checks if the shell session is empty.
     *
     * @return true if no element is present
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Prints everything present in the session.
     */
    public void printAll() {
        elements.entrySet().forEach(m -> FeatJAR.log()
                .message(m.getKey() + "   (" + m.getValue().getTypeName() + ")"));
    }

    @SuppressWarnings({"unused"})
    public void printSortedByVarNames() {
        elements.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(m -> FeatJAR.log()
                .message(m.getKey() + " " + m.getValue().getTypeName()));
    }

    @SuppressWarnings({"unused"})
    public void printSortedByType() {
        elements.entrySet().stream()
                .sorted(Comparator.comparing(e -> String.valueOf(e.getValue().type)))
                .forEach(m ->
                        FeatJAR.log().message(m.getKey() + " " + m.getValue().getTypeName()));
    }
}
