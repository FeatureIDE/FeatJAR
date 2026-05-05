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
package de.featjar.base.computation;

import de.featjar.base.FeatJAR;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A dependency of a computation. Describes the dependency without storing its
 * actual value, which is passed in a dependency list to
 * {@link IComputation#compute(List, Progress)}.
 *
 * @param <U> the type of the dependency's computation result
 * @author Elias Kuiter
 */
public class Dependency<U> {

    private static Map<Class<?>, List<Dependency<?>>> map = new LinkedHashMap<>();

    public static Dependency<Object> newDependency() {
        return addDependency(getCallingClass(), Object.class);
    }

    public static <U> Dependency<U> newDependency(Class<U> type) {
        return addDependency(getCallingClass(), type);
    }

    private static Class<?> getCallingClass() {
        try {
            Class<?> callingClass =
                    Class.forName(Thread.currentThread().getStackTrace()[3].getClassName());
            assert isComputation(callingClass);
            return callingClass;
        } catch (ClassNotFoundException e) {
            FeatJAR.log().error(e);
            throw new RuntimeException(e);
        }
    }

    private static boolean isComputation(Class<?> callingClass) {
        if (callingClass == null) {
            return false;
        }
        if (callingClass == AComputation.class) {
            return true;
        }
        return isComputation(callingClass.getSuperclass());
    }

    private static <U> Dependency<U> addDependency(Class<?> clazz, Class<U> type) {
        final List<Dependency<?>> list = getDependencyList(clazz);
        Dependency<U> newDependency = new Dependency<>(type, list.size());
        list.add(newDependency);
        return newDependency;
    }

    public static void deleteAllDependencies() {
        map.clear();
        map = null;
    }

    public static int getDependencyCount(Class<?> clazz) {
        return getDependencyList(clazz).size();
    }

    public static List<Dependency<?>> getDependencyList(Class<?> clazz) {
        final List<Dependency<?>> list = map.get(clazz);
        if (list != null) {
            return list;
        } else {
            final Class<?> superClazz = clazz.getSuperclass();
            ArrayList<Dependency<?>> clazzDependencyList =
                    (superClazz == null) ? new ArrayList<>() : new ArrayList<>(getDependencyList(superClazz));
            map.put(clazz, clazzDependencyList);
            return clazzDependencyList;
        }
    }

    private final Class<U> type;
    private final int index;

    private Dependency(Class<U> type, int index) {
        this.type = type;
        this.index = index;
    }

    public Class<U> getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public U getValue(List<?> values) {
        final Object obj = values.get(index);
        assert obj == null || type.isInstance(obj)
                : String.format(
                        "Value for dependency %s is of type %s",
                        toString(), obj.getClass().toString());
        return type.cast(obj);
    }

    public U get(List<?> values) {
        return getValue(values);
    }

    @Override
    public String toString() {
        return "Dependency [type=" + type + ", index=" + index + "]";
    }
}
