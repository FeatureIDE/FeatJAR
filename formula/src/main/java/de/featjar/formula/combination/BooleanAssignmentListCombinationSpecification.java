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
package de.featjar.formula.combination;

import de.featjar.base.data.IntegerList;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.Collections;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Specification that describes combinations by a predefined list.
 */
public class BooleanAssignmentListCombinationSpecification implements ICombinationSpecification {

    public BooleanAssignmentList booleanAssignmentList;

    public BooleanAssignmentListCombinationSpecification(BooleanAssignmentList booleanAssignmentList) {
        this.booleanAssignmentList = new BooleanAssignmentList(booleanAssignmentList);
    }

    @Override
    public long loopCount() {
        return booleanAssignmentList.size();
    }

    @Override
    public void shuffleElements(Random random) {
        Collections.shuffle(booleanAssignmentList.getAll(), random);
    }

    @Override
    public void remap(VariableMap variableMap) {
        booleanAssignmentList = booleanAssignmentList.remap(variableMap);
    }

    @Override
    public void forEach(Consumer<int[]> consumer) {
        booleanAssignmentList.stream().map(IntegerList::get).forEach(consumer);
    }

    @Override
    public <V> void forEach(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        booleanAssignmentList.stream()
                .map(IntegerList::get)
                .forEach(assignment -> consumer.accept(environmentCreator.get(), assignment));
    }

    @Override
    public void forEachParallel(Consumer<int[]> consumer) {
        booleanAssignmentList.stream().parallel().map(IntegerList::get).forEach(consumer);
    }

    @Override
    public <V> void forEachParallel(BiConsumer<V, int[]> consumer, Supplier<V> environmentCreator) {
        booleanAssignmentList.stream()
                .parallel()
                .map(IntegerList::get)
                .forEach(assignment -> consumer.accept(environmentCreator.get(), assignment));
    }

    @Override
    public VariableMap variableMap() {
        return booleanAssignmentList.getVariableMap();
    }

    @Override
    public int maxT() {
        return booleanAssignmentList.stream()
                .map(IntegerList::size)
                .max(Integer::compareTo)
                .orElse(0);
    }

    @Override
    public ICombinationSpecification reduceTTo(int newT) {
        return new BooleanAssignmentListCombinationSpecification(new BooleanAssignmentList(
                booleanAssignmentList.getVariableMap(),
                booleanAssignmentList.stream().filter(assignment -> assignment.size() <= newT)));
    }
}
