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

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.log.Log.Verbosity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An optional object that may be present with or without a problem,
 * absent with intention, or absent due to some unintended problem.
 * Similar to Java's {@link Optional}, but also stores any {@link Problem} associated when trying to obtain an object.
 * Usually, a {@link Result} wraps the result of a {@link IComputation} or other potentially complex operation,
 * such as parsing a {@link IFormat}.
 * Instead of throwing exceptions, consider using a {@link Result} if there is some object to return.
 * For void methods, throwing checked exceptions may be more reasonable than returning a {@link Result} object.
 *
 * @param <T> the type of the result's object
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class Result<T> implements Supplier<T> {

    private final T object;

    private final List<Problem> problems = new LinkedList<>();

    /**
     * Constructs a new result object.
     * @param object the computed object to wrap
     * @param problems the problems occurred during computation
     */
    protected Result(T object, List<Problem> problems) {
        this.object = object;
        problems = problems == null
                ? null
                : problems.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (problems != null && !problems.isEmpty()) {
            this.problems.addAll(problems);
        }
    }

    /**
     * {@return a result of a non-null object}
     *
     * @param object   the non-null object
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> of(T object, Problem... problems) {
        return of(object, Arrays.asList(problems));
    }

    /**
     * {@return a result of a non-null object}
     *
     * @param object   the non-null object
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> of(T object, List<Problem> problems) {
        Objects.requireNonNull(object, "tried to create non-empty result with null");
        return new Result<>(object, problems);
    }

    /**
     * {@return a result of a nullable object}
     *
     * @param object   the nullable object
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> ofNullable(T object) {
        return new Result<>(object, null);
    }

    /**
     * {@return a result of a nullable object}
     *
     * @param object   the nullable object
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> ofNullable(T object, Problem... problems) {
        return ofNullable(object, Arrays.asList(problems));
    }

    /**
     * {@return a result of a nullable object}
     *
     * @param object   the nullable object
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> ofNullable(T object, List<Problem> problems) {
        return new Result<>(object, problems);
    }

    /**
     * {@return a result of an link Optional}
     *
     * @param optional the optional
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> ofOptional(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional) {
        return ofNullable(optional.orElse(null));
    }

    /**
     * {@return a result of an link Optional}
     *
     * @param optional the optional
     * @param <T>      the type of the result's object
     * @param potentialProblem a supplier of the problem in case the optional is empty
     */
    public static <T> Result<T> ofOptional(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional,
            Supplier<Problem> potentialProblem) {
        T value = optional.orElse(null);
        return value != null ? new Result<>(value, null) : empty(potentialProblem.get());
    }

    /**
     * {@return an empty result}
     *
     * @param exception the exception
     * @param <T>       the type of the result's object
     */
    public static <T> Result<T> empty(Exception exception) {
        return empty(new Problem(exception));
    }

    /**
     * {@return an empty result}
     *
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> empty(Problem... problems) {
        return empty(Arrays.asList(problems));
    }

    /**
     * {@return an empty result}
     *
     * @param problems the problems
     * @param <T>      the type of the result's object
     */
    public static <T> Result<T> empty(List<Problem> problems) {
        return new Result<>(null, problems);
    }

    /**
     * {@return a void result}
     *
     * @param exception the exception
     */
    public static Result<Void> ofVoid(Exception exception) {
        return of(Void.VOID, new Problem(exception));
    }

    /**
     * {@return a void result}
     *
     * @param problems the problems
     */
    public static Result<Void> ofVoid(Problem... problems) {
        return ofVoid(Arrays.asList(problems));
    }

    /**
     * {@return a void result}
     *
     * @param problems the problems
     */
    public static Result<Void> ofVoid(List<Problem> problems) {
        return of(Void.VOID, problems);
    }

    /**
     * {@return a list of all problems from the given results}
     *
     * @param results the results
     */
    public static List<Problem> getProblems(Result<?>... results) {
        return getProblems(Arrays.asList(results));
    }
    /**
     *
     * {@return a list of all problems from the given results}
     *
     * @param results the results
     */
    public static List<Problem> getProblems(List<? extends Result<?>> results) {
        return results.stream()
                .filter(Objects::nonNull)
                .flatMap(r -> r.getProblems().stream())
                .collect(Collectors.toList());
    }

    private static Stream<Result<?>> nonNull(List<? extends Result<?>> results) {
        return results.stream().map(result -> result == null ? Result.empty() : result);
    }

    /**
     * {@return for a given list of results, a result of a list with all wrapped objects}
     * Merges a list of n non-empty results into a non-empty result of a list of length n.
     * If any result in the given list is empty, returns an empty result.
     * The returned result contains all problems from the given results.
     *
     * @param <T> the type of the returned list
     * @param results the results
     * @param listFactory a supplier for the returned list
     */
    public static <T extends List<Object>> Result<T> mergeAll(
            List<? extends Result<?>> results, Supplier<T> listFactory) {
        List<Problem> problems = getProblems(results);
        return nonNull(results).noneMatch(Result::isEmpty)
                ? Result.of(results.stream().map(Result::get).collect(Collectors.toCollection(listFactory)), problems)
                : Result.empty(problems);
    }

    /**
     * {@return for a given list of results, a result of a list with all wrapped objects}
     * Merges a list of n non-empty results into a non-empty result of a list of length n.
     * If any result in the given list is empty, returns an empty result.
     * The returned result contains all problems from the given results.
     *
     * @param results the results
     */
    public static Result<ArrayList<Object>> mergeAll(List<? extends Result<?>> results) {
        return mergeAll(results, ArrayList::new);
    }

    /**
     * {@return for a given list of results, a result of a list with all wrapped objects}
     * Merges a list of n results into a non-empty result of a list of length n.
     * If any result in the given list is empty, the returned list contains null at the same position.
     * The returned result contains all problems from the given results.
     *
     * @param <T> the type of the returned list
     * @param results the results
     * @param listFactory a supplier for the returned list
     */
    public static <T extends List<Object>> Result<T> mergeAllNullable(
            List<? extends Result<?>> results, Supplier<T> listFactory) {
        return Result.of(
                nonNull(results).map(r -> r.orElse(null)).collect(Collectors.toCollection(listFactory)),
                getProblems(results));
    }

    /**
     * {@return for a given list of results, a result of a list with all wrapped objects}
     * Merges a list of n results into a non-empty result of a list of length n.
     * If any result in the given list is empty, the returned list contains null at the same position.
     * The returned result contains all problems from the given results.
     *
     * @param results the results
     */
    public static Result<ArrayList<Object>> mergeAllNullable(List<? extends Result<?>> results) {
        return mergeAllNullable(results, ArrayList::new);
    }

    /**
     * {@return A new result that does not wrap another result}
     * If this result contains another result, this method unwraps the inner object, such that the returned result does not contain another result.
     * @param <U> the type of the wrapped object
     */
    @SuppressWarnings("unchecked")
    public <U> Result<U> unwrap() {
        List<Problem> allProblems = new LinkedList<>(problems);
        Object innerObject = object;
        while (innerObject instanceof Result) {
            Result<?> innerResult = (Result<?>) innerObject;
            allProblems.addAll(innerResult.problems);
            innerObject = innerResult.orElse(null);
        }
        return (Result<U>) Result.ofNullable(innerObject, allProblems);
    }

    /**
     * {@return this result's object}
     * As a side effect, logs all problems that have occurred.
     *
     * @throws NoSuchElementException if no object is present
     */
    public T get() {
        if (object == null) {
            throw new NoSuchElementException("no object present");
        }
        return object;
    }

    /**
     * {@return whether this result's object is present}
     */
    public boolean isPresent() {
        return object != null;
    }

    /**
     * {@return whether this result is empty}
     */
    public boolean isEmpty() {
        return object == null;
    }

    /**
     * Consumes this result's object, if any.
     *
     * @param resultHandler the object consumer
     *
     * @return this result
     */
    public Result<T> ifPresent(Consumer<T> resultHandler) {
        if (object != null) {
            resultHandler.accept(object);
        }
        return this;
    }

    /**
     * Consumes the problem list of this result, if the object is {@code null}.
     * This list is guaranteed to be non-null and read-only.
     *
     * @param problemHandler the problem list consumer
     *
     * @return this result
     */
    public Result<T> ifEmpty(Consumer<List<Problem>> problemHandler) {
        if (object != null) {
            problemHandler.accept(Collections.unmodifiableList(problems));
        }
        return this;
    }

    /**
     * Maps the object in this result to another object using a mapper function.
     *
     * @param mapper the mapper function
     * @param <R>    the type of the mapped object
     * @return A new result with the mapped object or an empty result, if any
     * exceptions occur during the mapping or if this result was empty before.
     */
    public <R> Result<R> map(Function<T, R> mapper) {
        try {
            return object != null
                    ? mergeProblems(Result.ofNullable(mapper.apply(object), problems))
                    : Result.empty(problems);
        } catch (final Exception e) {
            return mergeProblems(Result.empty(e));
        }
    }

    /**
     * Maps the object in this result to another object using a mapper function that also returns a {@link Result}.
     * Merges the two problems lists.
     *
     * @param mapper the mapper function
     * @param <R>    the type of the mapped object
     * @return A new result with the mapped object or an empty result, if any
     * exceptions occur during the mapping or if this result was empty before.
     */
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        try {
            return object != null ? mergeProblems(mapper.apply(object)) : Result.empty(problems);
        } catch (final Exception e) {
            return mergeProblems(Result.empty(e));
        }
    }

    /**
     * Filters the object in this result using a given {@link Predicate}.
     * If the predicate evaluates to {@code false} the value of this result is set to {@code null}.
     *
     * @param predicate the predicate
     * @return A new result with the original object or an empty result.
     */
    public Result<T> filter(Predicate<T> predicate) {
        try {
            return object != null ? predicate.test(object) ? this : Result.empty(problems) : Result.empty(problems);
        } catch (final Exception e) {
            return mergeProblems(Result.empty(e));
        }
    }

    /**
     * {@return a new empty result, containing the problems from this result and the problem constructed from the given exception}
     * @param exception the exception
     * @param <U> the type of the new result
     */
    public <U> Result<U> nullify(Exception exception) {
        return mergeProblems(Result.empty(exception));
    }

    /**
     * {@return a new empty result, containing the problems from this result and the given problems}
     * @param problems the given problems
     * @param <U> the type of the new result
     */
    public <U> Result<U> nullify(Problem... problems) {
        return mergeProblems(Result.empty(problems));
    }

    /**
     * {@return a new empty result, containing the problems from this result and the given problems}
     * @param problems the given problems
     * @param <U> the type of the new result
     */
    public <U> Result<U> nullify(List<Problem> problems) {
        return mergeProblems(Result.empty(problems));
    }

    /**
     * {@return a new result containing the same object and problems as this results and the given problems}
     * Returns this result object if there are no new problems given.
     * @param problems the given problems
     */
    public Result<T> addProblemInformation(Problem... problems) {
        return problems.length == 0 ? this : mergeProblems(Result.ofNullable(object, problems));
    }

    /**
     * {@return a new result containing the same object and problems as this results and the given problems}
     * Returns this result object if there are no new problems given.
     * @param problems the given problems
     */
    public Result<T> addProblemInformation(List<Problem> problems) {
        return problems.isEmpty() ? this : mergeProblems(Result.ofNullable(object, problems));
    }

    /**
     * Filters the object in this result using a given {@link Predicate}.
     * If the predicate evaluates to {@code false} the value of this result is set to {@code null}.
     * Allows to provide a {@link Problem} to indicate why the object was filtered.
     *
     * @param predicate the predicate
     * @param problemSupplier a supplier to create a {@link Problem} when an object was filtered
     * @return A new result with the original object or an empty result.
     */
    public Result<T> filter(Predicate<T> predicate, Supplier<Problem> problemSupplier) {
        try {
            return object != null
                    ? predicate.test(object) ? this : mergeProblems(Result.empty(problemSupplier.get()))
                    : Result.empty(problems);
        } catch (final Exception e) {
            return mergeProblems(Result.empty(e));
        }
    }

    /**
     * {@return the given result with the problems from this result added to its problem list}
     * @param <R> the type of result
     * @param otherResult the given result
     */
    private <R> Result<R> mergeProblems(Result<R> otherResult) {
        otherResult.problems.addAll(0, problems);
        return otherResult;
    }

    /**
     * {@return a sequential stream containing only this result's object, if any}
     */
    public Stream<T> stream() {
        if (!isPresent()) {
            return Stream.empty();
        } else {
            return Stream.of(object);
        }
    }

    /**
     * {@return this result or an alternative result if this result is empty}
     * If this result is empty, the problems of this result are added to the alternative.
     *
     *
     * @param alternative the alternative result
     */
    public Result<T> or(Result<T> alternative) {
        Objects.requireNonNull(alternative);
        if (object != null) {
            return this;
        }
        alternative.problems.addAll(problems);
        return alternative;
    }

    /**
     * {@return this result or an alternative result if this result is empty}
     * If this result is empty, the problems of this result are added to the alternative.
     *
     *
     * @param alternativeSupplier the supplier for the alternative result
     */
    public Result<T> orGet(Supplier<? extends Result<T>> alternativeSupplier) {
        if (object != null) {
            return this;
        }
        Result<T> alternative = alternativeSupplier.get();
        alternative.problems.addAll(problems);
        return alternative;
    }

    /**
     * {@return this result's object or an alternative object}
     *
     * @param alternative the alternative object
     */
    public T orElse(T alternative) {
        return object != null ? object : alternative;
    }

    /**
     * {@return this result's object or an alternative object}
     *
     * @param alternativeSupplier the supplier function
     */
    public T orElseGet(Supplier<? extends T> alternativeSupplier) {
        return object != null ? object : alternativeSupplier.get();
    }

    /**
     * {@return this result's object or logs this result's problems and returns null}
     *
     * @param verbosity the verbosity with which to log the problems
     */
    public T orElseLog(Verbosity verbosity) {
        if (object != null) {
            return object;
        } else {
            FeatJAR.log().problems(problems, verbosity);
            return null;
        }
    }

    /**
     * {@return this result's object or throws this result's problems}
     *
     * @param errorHandler the error handler
     * @param <E> the type of the thrown exception
     * @throws E a customizable exception
     */
    public <E extends Exception> T orElseThrow(Function<List<Problem>, E> errorHandler) throws E {
        if (object != null) {
            return object;
        } else {
            throw errorHandler.apply(getProblems());
        }
    }

    /**
     * {@return this result's object or throws this result's problems}
     */
    public T orElseThrow() {
        return orElseThrow(problems -> {
            List<Problem> errorProblems = problems.stream()
                    .filter(problem -> problem.getSeverity().equals(Problem.Severity.ERROR))
                    .collect(Collectors.toList());
            if (errorProblems.size() == 0) {
                return new RuntimeException("an unknown error occurred");
            }
            Problem problem = errorProblems.get(0);
            Exception e = problem.getException();
            if (errorProblems.size() == 1) {
                if (e instanceof RuntimeException) {
                    return (RuntimeException) e;
                } else {
                    return new RuntimeException(problem.getMessage(), e);
                }
            } else {
                return new RuntimeException(
                        problem.getMessage() + " (and " + (errorProblems.size() - 1) + " other problems)", e);
            }
        });
    }

    /**
     * {@return this result's problems}
     * The returned list is guaranteed to be non-null and read-only.
     */
    public List<Problem> getProblems() {
        return Collections.unmodifiableList(problems);
    }

    /**
     * {@return a supplier that prints all problems of this result to a string}
     */
    public String printProblems() {
        return Problem.printProblems(problems);
    }

    /**
     * {@return whether this result has problems}
     */
    public boolean hasProblems() {
        return !problems.isEmpty();
    }

    /**
     * {@return an Optional of this result's object}
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(object);
    }

    /**
     * {@return a Result of a given index}. If the given index is negative, the returned result is empty.
     *
     * @param index the index
     */
    public static Result<Integer> ofIndex(int index) {
        return index < 0 ? empty(new IndexOutOfBoundsException(index)) : of(index);
    }

    /**
     * {@return a wrapped function that converts its results into an Optional}
     *
     * @param function the function
     */
    public static <U, V> Function<U, Result<V>> mapReturnValue(Function<U, V> function) {
        return t -> Result.of(function.apply(t));
    }

    @Override
    public boolean equals(Object o) {
        // the problem is ignored as it cannot be compared for equality, and it only carries metadata for the user
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(object, ((Result<?>) o).object);
    }

    @Override
    public int hashCode() {
        // the problem is ignored as it cannot be hashed, and it only carries metadata for the user
        return Objects.hash(object);
    }

    @Override
    public String toString() {
        return "Result{" + object + ", " + problems + "}";
    }

    /**
     * {@return true if a value is present and it equals otherValue, false otherwise}
     * @param otherValue the value that is being compared to
     */
    public boolean valueEquals(T otherValue) {
        return isPresent() && Objects.equals(get(), otherValue);
    }

    /**
     * {@return a constant Computation from this result}
     */
    public IComputation<T> toComputation() {
        return Computations.of(orElseThrow());
    }
}
