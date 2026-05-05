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
package de.featjar.base.env;

import de.featjar.base.data.Result;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A stack trace of a thread.
 *
 * @author Elias Kuiter
 */
public class StackTrace {
    private List<StackTraceElement> stackTraceElements;

    /**
     * Creates a new stack trace for the current thread.
     */
    public StackTrace() {
        this(Thread.currentThread());
    }

    /**
     * Creates a new stack trace for a given thread.
     *
     * @param thread the thread
     */
    public StackTrace(Thread thread) {
        stackTraceElements = new ArrayList<>(Arrays.asList(thread.getStackTrace()));
        removeClassNamePrefix(getClass().getName());
    }

    /**
     * {@return this stack trace without its most recent element}
     * Useful for removing the entry for {@link Thread#getStackTrace()} from a stack trace.
     */
    public StackTrace removeTop() {
        if (!stackTraceElements.isEmpty()) stackTraceElements.remove(0);
        return this;
    }

    /**
     * {@return this stack trace without elements having a class name starting with a given prefix}
     *
     * @param classNamePrefix the class name prefix
     */
    public StackTrace removeClassNamePrefix(String classNamePrefix) {
        stackTraceElements.removeIf(
                stackTraceElement -> stackTraceElement.getClassName().startsWith(classNamePrefix));
        return this;
    }

    /**
     * {@return whether the given stack trace element originates from a given method in a given class}
     * Returns an empty result if the given stack trace element originates from an anonymous function (e.g., a lambda)
     * or its class cannot be found for a different reason.
     *
     * @param stackTraceElement the stack trace element
     * @param klass             the class
     * @param methodName        the method name
     */
    public static Result<Boolean> isMethodCall(StackTraceElement stackTraceElement, Class<?> klass, String methodName) {
        try {
            return Result.of(klass.isAssignableFrom(Class.forName(stackTraceElement.getClassName()))
                    && stackTraceElement.getMethodName().equals(methodName));
        } catch (ClassNotFoundException e) {
            return Result.empty();
        }
    }

    /**
     * {@return whether this stack trace contains a call to a given method name in a given class or any subclass}
     * Does not detect calls on anonymous functions (e.g., lambdas).
     *
     * @param klass      the class
     * @param methodName the method name
     */
    public boolean containsMethodCall(Class<?> klass, String methodName) {
        return stackTraceElements.stream()
                .anyMatch(stackTraceElement ->
                        isMethodCall(stackTraceElement, klass, methodName).orElse(false));
    }

    /**
     * {@return all stack trace elements of this stack trace}
     */
    public List<StackTraceElement> getAll() {
        return stackTraceElements;
    }

    /**
     * {@return the top stack trace element of this stack trace, if any}
     */
    public Result<StackTraceElement> getTop() {
        return stackTraceElements.isEmpty() ? Result.empty() : Result.of(stackTraceElements.get(0));
    }

    @Override
    public String toString() {
        return stackTraceElements.stream().map(StackTraceElement::toString).collect(Collectors.joining("\n"));
    }
}
