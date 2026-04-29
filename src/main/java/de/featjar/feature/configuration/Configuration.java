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
package de.featjar.feature.configuration;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a configuration and provides operations for the configuration process.
 */
public class Configuration implements Cloneable {

    public static final class IllegalSelectionTypeException extends RuntimeException {
        private static final long serialVersionUID = 1793844229871267311L;

        public IllegalSelectionTypeException(Class<?> otherType, Class<?> thisType) {
            super(String.format(
                    "Trying to set values of type %s to a feature of type %s",
                    String.valueOf(otherType), String.valueOf(thisType)));
        }

        public IllegalSelectionTypeException(Selection<?> feature, Object selection) {
            super(String.format(
                    "Trying to set the value %s (of type %s) to a feature of type %s",
                    String.valueOf(selection), String.valueOf(selection.getClass()), feature.getType()));
        }
    }

    public static final class SelectionNotPossibleException extends RuntimeException {
        private static final long serialVersionUID = 1793844229871267311L;

        public SelectionNotPossibleException(Object selection) {
            super(String.format("Feature cannot be set to %s", String.valueOf(selection)));
        }
    }

    /**
     * The value of a variable.
     * Has an automatic and manual value, which can be set independently.
     *
     * @param <T> the type of possible values
     */
    public static class Selection<T> {

        private final Class<T> type;
        private T manual, automatic;

        /**
         * Constructs a new selection.
         * @param type the type of the selection
         */
        public Selection(Class<T> type) {
            this.type = type;
        }

        private Selection(Selection<T> oldSelectableFeature) {
            type = oldSelectableFeature.type;
        }

        /**
         * {@return type of this selection}
         */
        public Class<T> getType() {
            return type;
        }

        /**
         * {@return the combined automatic and manual selection}
         * If an automatic value is set, this is returned. Otherwise the manual value is returned.
         */
        public T getSelection() {
            return automatic == null ? manual : automatic;
        }

        /**
         * {@return the manual selection}
         */
        public T getManual() {
            return manual;
        }

        /**
         * {@return the automatic selection}
         */
        public T getAutomatic() {
            return automatic;
        }

        /**
         * {@return the whether manual is defined but automatic is not}
         */
        public boolean isManual() {
            return automatic == null && manual != null;
        }

        /**
         * {@return the whether automatic is defined}
         */
        public boolean isAutomatic() {
            return automatic != null;
        }

        /**
         * Sets the manual value.
         * @param selection the value
         *
         * @throws IllegalSelectionTypeException if the given value is of a different type than this selection
         * @throws SelectionNotPossibleException if the value contradicts the automatic value
         */
        @SuppressWarnings("unchecked")
        public void setManual(Object selection) {
            checkIfSelectionPossible(selection, automatic);
            manual = (T) selection;
        }

        /**
         * Sets the automatic value.
         * @param selection the value
         *
         * @throws IllegalSelectionTypeException if the given value is of a different type than this selection
         * @throws SelectionNotPossibleException if the value contradicts the manual value
         */
        @SuppressWarnings("unchecked")
        public void setAutomatic(Object selection) {
            checkIfSelectionPossible(selection, manual);
            automatic = (T) selection;
        }

        private void checkIfSelectionPossible(Object selection, Object current) {
            if (selection != null) {
                if (!type.isInstance(selection)) {
                    throw new IllegalSelectionTypeException(this, selection);
                }
                if ((current != null) && (current != selection)) {
                    throw new SelectionNotPossibleException(selection);
                }
            }
        }

        /**
         * Converts the automatic selection into a manual selection.
         */
        public void makeManual() {
            if (automatic != null) {
                manual = automatic;
                automatic = null;
            }
        }

        /**
         * Adopts the values from a given selection.
         * @param selection the other selection
         * @throws IllegalSelectionTypeException if the type of the given selection is different from this selection's type
         */
        @SuppressWarnings("unchecked")
        public void adopt(Selection<?> selection) {
            if (selection.type != type) {
                throw new IllegalSelectionTypeException(selection.type, type);
            }
            manual = (T) selection.manual;
            automatic = (T) selection.automatic;
        }

        /**
         * Sets manual and automatic values to {@code null}.
         */
        public void reset() {
            manual = null;
            automatic = null;
        }
        /**
         * Sets automatic value to {@code null}.
         */
        public void resetAutomatic() {
            automatic = null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Selection<T> clone() {
            if (!this.getClass().equals(Selection.class)) {
                try {
                    return (Selection<T>) super.clone();
                } catch (final CloneNotSupportedException e) {
                    FeatJAR.log().error(e);
                    throw new RuntimeException("Cloning is not supported for " + this.getClass());
                }
            }
            Selection<T> selectableFeature = new Selection<>(this);
            selectableFeature.manual = this.manual;
            selectableFeature.automatic = this.automatic;
            return selectableFeature;
        }
    }

    private VariableMap variableMap;
    private ArrayList<Selection<?>> selections;

    /**
     * Creates an empty configuration.
     */
    public Configuration() {
        variableMap = new VariableMap();
        selections = new ArrayList<>();
    }

    /**
     * Creates a configuration with the same features as the given feature model.
     *
     * @param featureModel the underlying feature model.
     */
    public Configuration(IFeatureModel featureModel) {
        variableMap = new VariableMap();
        selections = new ArrayList<>(featureModel.getNumberOfFeatures());
        for (final IFeature child : featureModel.getFeatures()) {
            String featureName = child.getName().get();
            int index = variableMap.add(featureName);
            for (int i = selections.size(); i <= index; i++) {
                selections.add(null);
            }
            selections.add(index, new Selection<>(child.getType()));
        }
    }

    /**
     * Copy constructor. Copies the status of a given configuration.
     *
     * @param configuration The configuration to clone
     */
    protected Configuration(Configuration configuration) {
        variableMap = configuration.variableMap.clone();
        selections = new ArrayList<>(configuration.selections.size());
        for (Selection<?> selection : configuration.selections) {
            selections.add(selection != null ? selection.clone() : null);
        }
    }

    /**
     * Creates configuration from literal set.
     *
     * @param booleanAssignment contains literals with truth values.
     * @param variableMap mapping of variable names to indices. Is used to link a literal index in a {@link BooleanAssignment}.
     */
    public Configuration(BooleanAssignment booleanAssignment, VariableMap variableMap) {
        variableMap = variableMap.clone();
        selections = new ArrayList<>(variableMap.maxIndex());
        adopt(booleanAssignment, variableMap);
    }

    /**
     * {@return a list of all features that have a manual and no automatic value}
     */
    public List<String> getManual() {
        return variableMap.stream()
                .filter(e -> selection(e.getFirst()).isManual())
                .map(e -> e.getSecond())
                .collect(Collectors.toList());
    }

    /**
     * {@return a list of all features that have a automatic value}
     */
    public List<String> getAutomatic() {
        return variableMap.stream()
                .filter(e -> selection(e.getFirst()).isAutomatic())
                .map(e -> e.getSecond())
                .collect(Collectors.toList());
    }

    /**
     * {@return a list of all selected features}
     *
     * That is the name of all features of type {@code Boolean} with the value {@code true}
     */
    public List<String> getSelected() {
        return variableMap.stream()
                .filter(e -> selection(e.getFirst()).getSelection() == Boolean.TRUE)
                .map(e -> e.getSecond())
                .collect(Collectors.toList());
    }

    /**
     * {@return a list of all deselected features}
     *
     * That is the name of all features of type {@code Boolean} with the value {@code false}
     */
    public List<String> getDeselected() {
        return variableMap.stream()
                .filter(e -> selection(e.getFirst()).getSelection() == Boolean.TRUE)
                .map(e -> e.getSecond())
                .collect(Collectors.toList());
    }

    /**
     * {@return a list of all undefined features}
     *
     * That is the name of all features of any type that have no value.
     */
    public List<String> getUndefined() {
        return variableMap.stream()
                .filter(e -> selection(e.getFirst()).getSelection() == null)
                .map(e -> e.getSecond())
                .collect(Collectors.toList());
    }

    private Selection<?> selection(int index) {
        return selections.get(index);
    }

    private Stream<Selection<?>> getSelectionStream() {
        return selections.stream().filter(Objects::nonNull);
    }

    public Result<Selection<?>> getSelection(String name) {
        return Result.ofNullable(name).mapResult(variableMap::get).map(selections::get);
    }

    public Selection<?> get(String name) {
        return getSelection(name).orElseThrow();
    }

    public Result<Selection<?>> getSelection(IFeature feature) {
        return Result.ofNullable(feature)
                .mapResult(IFeature::getName)
                .mapResult(variableMap::get)
                .map(selections::get);
    }

    /**
     * Adopts the values from this assignment.
     *
     * @param assignment the assignment to adopt
     * @param variableMap maps the literals in the assignments to feature names
     */
    public void adopt(BooleanAssignment assignment, VariableMap variableMap) {
        for (int literal : assignment.get()) {
            if (literal != 0) {
                int adapedLiteral = variableMap.adapt(literal, this.variableMap, true);
                if (adapedLiteral != 0) {
                    int index = Math.abs(adapedLiteral);
                    for (int i = selections.size(); i <= index; i++) {
                        selections.add(null);
                    }
                    Selection<?> selection = selections.get(index);
                    if (selection == null) {
                        selection = new Selection<>(Boolean.class);
                        selections.add(index, selection);
                    }
                    selection.setManual(adapedLiteral > 0);
                }
            }
        }
    }

    /**
     * Adopts the values from this assignment.
     * Assumes that the variable map of the given assignment is the same as in this configuration.
     *
     * @param assignment the assignment to adopt
     */
    public void adopt(BooleanAssignment assignment) {
        for (int literal : assignment.get()) {
            if (literal != 0) {
                int index = Math.abs(literal);
                for (int i = selections.size(); i <= index; i++) {
                    selections.add(null);
                }
                Selection<?> selection = selections.get(index);
                if (selection == null) {
                    selection = new Selection<>(Boolean.class);
                    selections.add(index, selection);
                }
                selection.setManual(literal > 0);
            }
        }
    }

    /**
     * Adopts the values from the given configuration.
     * Features not
     *
     * @param configuration the configuration to adopt
     */
    public void adopt(Configuration configuration) {
        ListIterator<Selection<?>> it = configuration.selections.listIterator();
        while (it.hasNext()) {
            Selection<?> otherSelection = it.next();
            if (otherSelection != null) {
                int adapedIndex = configuration.variableMap.adapt(it.previousIndex(), variableMap, true);
                if (adapedIndex != 0) {
                    for (int i = selections.size(); i <= adapedIndex; i++) {
                        selections.add(null);
                    }
                    Selection<?> selection = selections.get(adapedIndex);
                    if (selection == null) {
                        selection = otherSelection.clone();
                        selections.add(adapedIndex, selection);
                    } else {
                        selection.adopt(otherSelection);
                    }
                }
            }
        }
    }

    public List<Selection<?>> select(Collection<String> features) {
        return select(features.stream()).collect(Collectors.toList());
    }

    public Stream<Selection<?>> select(Stream<String> features) {
        return features.map(this::getSelection).filter(Result::isPresent).map(Result::get);
    }

    public VariableMap getVariableMap() {
        return variableMap;
    }

    public void adapt(VariableMap newVariableMap) {
        ArrayList<Selection<?>> newSelections = new ArrayList<>(selections.size());
        ListIterator<Selection<?>> it = selections.listIterator();
        while (it.hasNext()) {
            Selection<?> otherSelection = it.next();
            if (otherSelection != null) {
                int adapedIndex = variableMap.adapt(it.previousIndex(), newVariableMap, true);
                if (adapedIndex != 0) {
                    for (int i = selections.size(); i <= adapedIndex; i++) {
                        newSelections.add(null);
                    }
                    newSelections.add(adapedIndex, otherSelection.clone());
                }
            }
        }
        selections.clear();
        selections.addAll(newSelections);
        newSelections.clear();
        this.variableMap = newVariableMap;
    }

    public List<Selection<?>> getSelections() {
        return Collections.unmodifiableList(selections);
    }

    /**
     * Turns all automatic into manual values.
     */
    public void makeManual() {
        getSelectionStream().forEach(Selection::makeManual);
    }

    /**
     * Resets all values to undefined.
     */
    public void reset() {
        getSelectionStream().forEach(Selection::reset);
    }

    /**
     * Resets all automatic values to undefined.
     */
    public void resetAutomatic() {
        getSelectionStream().forEach(Selection::resetAutomatic);
    }

    /**
     * Resets automatic values that equal the given selection.
     *
     * @param selection the selection to reset
     */
    public void resetAutomatic(Object selection) {
        getSelectionStream().filter(f -> f.getAutomatic() == selection).forEach(Selection::resetAutomatic);
    }

    /**
     * Creates and returns a copy of this configuration.
     *
     * @return configuration a clone of this configuration.
     */
    @Override
    public Configuration clone() {
        if (!this.getClass().equals(Configuration.class)) {
            try {
                return (Configuration) super.clone();
            } catch (final CloneNotSupportedException e) {
                FeatJAR.log().error(e);
                throw new RuntimeException("Cloning is not supported for " + this.getClass());
            }
        }
        return new Configuration(this);
    }

    public BooleanAssignment toBooleanAssignment() {
        return new BooleanAssignment(getVariableMap().stream()
                .filter(e -> selection(e.getFirst()).getType() == Boolean.class)
                .filter(e -> selection(e.getFirst()).getSelection() != null)
                .mapToInt(e -> selection(e.getFirst()).getSelection() == Boolean.TRUE ? e.getFirst() : -e.getFirst())
                .toArray());
    }

    @Override
    public String toString() {
        return getSelectionStream().map(Selection::toString).collect(Collectors.joining("\n"));
    }
}
