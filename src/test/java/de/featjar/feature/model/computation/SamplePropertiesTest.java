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
package de.featjar.feature.model.computation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.tree.DataTree;
import de.featjar.feature.configuration.computation.ComputeNumberOfConfigurations;
import de.featjar.feature.configuration.computation.ComputeNumberOfSelectionsPerConfiguration;
import de.featjar.feature.configuration.computation.ComputeNumberOfSelectionsPerFeature;
import de.featjar.feature.configuration.computation.ComputeNumberOfVariables;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.structure.IFormula;
import java.util.LinkedList;
import org.junit.jupiter.api.Test;

public class SamplePropertiesTest {

    public BooleanAssignmentList createAssignmentList() {
        LinkedList<String> variableNames = new LinkedList<String>();
        variableNames.add("A");
        variableNames.add("B");
        variableNames.add("C");
        variableNames.add("D");
        variableNames.add("E");
        variableNames.add("F");
        variableNames.add("G");
        VariableMap variableMap = new VariableMap(variableNames);

        BooleanAssignmentList booleanAssignmentList = new BooleanAssignmentList(
                variableMap,
                new BooleanAssignment(1, -2, -5, -6),
                new BooleanAssignment(-1, -3, -6),
                new BooleanAssignment(1, 2, 4, 5),
                new BooleanAssignment(5, 6),
                new BooleanAssignment());
        return booleanAssignmentList;
    }

    public BooleanAssignmentList createAssignmentListUniformity(FeatureModel featureModel) {
        IComputation<IFormula> iFormula =
                Computations.of((IFeatureModel) featureModel).map(ComputeFormula::new);
        IFormula fmFormula = iFormula.compute();
        VariableMap variableMap = new VariableMap(fmFormula);
        BooleanAssignmentList booleanAssignmentList = new BooleanAssignmentList(
                variableMap,
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("API").get(),
                        variableMap.get("Get").get(),
                        variableMap.get("Windows").get(),
                        -variableMap.get("Delete").get(),
                        -variableMap.get("Transactions").get(),
                        -variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("Get").get(),
                        -variableMap.get("Windows").get(),
                        variableMap.get("Put").get(),
                        variableMap.get("Delete").get(),
                        variableMap.get("Transactions").get(),
                        variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("Get").get(),
                        -variableMap.get("Windows").get(),
                        -variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        -variableMap.get("Transactions").get(),
                        -variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("Get").get(),
                        -variableMap.get("Windows").get(),
                        -variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        variableMap.get("Transactions").get(),
                        variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        -variableMap.get("API").get(),
                        variableMap.get("Get").get(),
                        variableMap.get("Windows").get(),
                        -variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        -variableMap.get("Transactions").get(),
                        -variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("API").get(),
                        variableMap.get("Get").get(),
                        variableMap.get("Windows").get(),
                        variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        -variableMap.get("Transactions").get(),
                        -variableMap.get("Linux").get()));
        return booleanAssignmentList;
    }

    @Test
    public void computeDistributionFeaturesSelectionsTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        DataTree<Long> compute = Computations.of(booleanAssignmentList)
                .map(ComputeNumberOfSelectionsPerConfiguration::new)
                .compute();

        assertEquals(13L, compute.getValue().orElseThrow());
        assertEquals(3, compute.getChildren().size());
        assertEquals(7L, compute.getChildren().get(0).getValue().orElseThrow());
        assertEquals(6L, compute.getChildren().get(1).getValue().orElseThrow());
        assertEquals(0L, compute.getChildren().get(2).getValue().orElseThrow());
    }

    @Test
    public void computeFeatureCounterTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        DataTree<Long> compute = Computations.of(booleanAssignmentList)
                .map(ComputeNumberOfSelectionsPerFeature::new)
                .compute();

        assertEquals(35L, compute.getValue().orElseThrow());
        assertEquals(3, compute.getChildren().size());
        assertEquals(7L, compute.getChildren().get(0).getValue().orElseThrow());
        assertEquals(6L, compute.getChildren().get(1).getValue().orElseThrow());
        assertEquals(22L, compute.getChildren().get(2).getValue().orElseThrow());

        assertEquals(7, compute.getChildren().get(0).getChildren().size());
        assertEquals(
                2L, compute.getChildren().get(0).getChildren().get(0).getValue().orElseThrow());
        assertEquals(
                1L, compute.getChildren().get(0).getChildren().get(1).getValue().orElseThrow());
        assertEquals(
                0L, compute.getChildren().get(0).getChildren().get(2).getValue().orElseThrow());
        assertEquals(
                1L, compute.getChildren().get(0).getChildren().get(3).getValue().orElseThrow());
        assertEquals(
                2L, compute.getChildren().get(0).getChildren().get(4).getValue().orElseThrow());
        assertEquals(
                1L, compute.getChildren().get(0).getChildren().get(5).getValue().orElseThrow());
        assertEquals(
                0L, compute.getChildren().get(0).getChildren().get(6).getValue().orElseThrow());

        assertEquals(7, compute.getChildren().get(1).getChildren().size());
        assertEquals(
                1L, compute.getChildren().get(1).getChildren().get(0).getValue().orElseThrow());
        assertEquals(
                1L, compute.getChildren().get(1).getChildren().get(1).getValue().orElseThrow());
        assertEquals(
                1L, compute.getChildren().get(1).getChildren().get(2).getValue().orElseThrow());
        assertEquals(
                0L, compute.getChildren().get(1).getChildren().get(3).getValue().orElseThrow());
        assertEquals(
                1L, compute.getChildren().get(1).getChildren().get(4).getValue().orElseThrow());
        assertEquals(
                2L, compute.getChildren().get(1).getChildren().get(5).getValue().orElseThrow());
        assertEquals(
                0L, compute.getChildren().get(1).getChildren().get(6).getValue().orElseThrow());

        assertEquals(7, compute.getChildren().get(2).getChildren().size());
        assertEquals(
                2L, compute.getChildren().get(2).getChildren().get(0).getValue().orElseThrow());
        assertEquals(
                3L, compute.getChildren().get(2).getChildren().get(1).getValue().orElseThrow());
        assertEquals(
                4L, compute.getChildren().get(2).getChildren().get(2).getValue().orElseThrow());
        assertEquals(
                4L, compute.getChildren().get(2).getChildren().get(3).getValue().orElseThrow());
        assertEquals(
                2L, compute.getChildren().get(2).getChildren().get(4).getValue().orElseThrow());
        assertEquals(
                2L, compute.getChildren().get(2).getChildren().get(5).getValue().orElseThrow());
        assertEquals(
                5L, compute.getChildren().get(2).getChildren().get(6).getValue().orElseThrow());

        //        assertEquals(2, featureCounter.get("A_selected"));
        //        assertEquals(1, featureCounter.get("B_selected"));
        //        assertEquals(0, featureCounter.get("C_selected"));
        //        assertEquals(1, featureCounter.get("D_selected"));
        //        assertEquals(2, featureCounter.get("E_selected"));
        //        assertEquals(1, featureCounter.get("F_selected"));
        //        assertEquals(0, featureCounter.get("G_selected"));
        //
        //        assertEquals(1, featureCounter.get("A_deselected"));
        //        assertEquals(1, featureCounter.get("B_deselected"));
        //        assertEquals(1, featureCounter.get("C_deselected"));
        //        assertEquals(0, featureCounter.get("D_deselected"));
        //        assertEquals(1, featureCounter.get("E_deselected"));
        //        assertEquals(2, featureCounter.get("F_deselected"));
        //        assertEquals(0, featureCounter.get("G_deselected"));
        //
        //        assertEquals(2, featureCounter.get("A_undefined"));
        //        assertEquals(3, featureCounter.get("B_undefined"));
        //        assertEquals(4, featureCounter.get("C_undefined"));
        //        assertEquals(4, featureCounter.get("D_undefined"));
        //        assertEquals(2, featureCounter.get("E_undefined"));
        //        assertEquals(2, featureCounter.get("F_undefined"));
        //        assertEquals(5, featureCounter.get("G_undefined"));
    }

    @Test
    public void computeNumberConfigurationTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        DataTree<Integer> compute = Computations.of(booleanAssignmentList)
                .map(ComputeNumberOfConfigurations::new)
                .compute();
        assertEquals(5, compute.getValue().orElseThrow());
        assertEquals(0, compute.getChildren().size());
    }

    @Test
    public void computeNumberVariablesTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        DataTree<Integer> compute = Computations.of(booleanAssignmentList)
                .map(ComputeNumberOfVariables::new)
                .compute();
        assertEquals(7, compute.getValue().orElseThrow());
        assertEquals(0, compute.getChildren().size());
    }
}
