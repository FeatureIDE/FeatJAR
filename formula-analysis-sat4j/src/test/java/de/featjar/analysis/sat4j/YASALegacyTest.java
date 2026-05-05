/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j;

import static de.featjar.base.computation.Computations.async;
import static de.featjar.formula.structure.Expressions.literal;
import static de.featjar.formula.structure.Expressions.or;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.Common;
import de.featjar.analysis.sat4j.computation.ComputeAtomicSetsSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeConstraintedTWiseCoverage;
import de.featjar.analysis.sat4j.computation.ComputeCoreSAT4J;
import de.featjar.analysis.sat4j.computation.ComputeSolutionsSAT4J;
import de.featjar.analysis.sat4j.computation.YASALegacy;
import de.featjar.analysis.sat4j.solver.ISelectionStrategy;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.computation.IComputation;
import de.featjar.formula.CoverageStatistic;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.combination.VariableCombinationSpecification;
import de.featjar.formula.combination.VariableCombinationSpecification.VariableCombinationSpecificationComputation;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.computation.ComputeRelativeTWiseCoverage;
import de.featjar.formula.structure.IFormula;
import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class YASALegacyTest extends Common {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    void formulaHas1WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(or(literal("x"), literal(false, "y"), literal(false, "z")), 1);
    }

    @Test
    void gplHas1WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(loadFormula("GPL/model.xml"), 1);
    }

    @Test
    void formulaHas2WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(or(literal("x"), literal(false, "y"), literal(false, "z")), 2);
    }

    @Test
    void gplHas2WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(loadFormula("GPL/model.xml"), 2);
    }

    @Test
    void formulaHas3WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(or(literal("x"), literal(false, "y"), literal(false, "z")), 3);
    }

    @Test
    void gplHas3WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(loadFormula("GPL/model.xml"), 3);
    }

    @Test
    void modelWithFreeVariablesHas1WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(loadFormula("testFeatureModels/model_with_free_variables.dimacs"), 1);
    }

    @Test
    void modelWithFreeVariablesHas2WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(loadFormula("testFeatureModels/model_with_free_variables.dimacs"), 2);
    }

    @Test
    void modelWithFreeVariablesHas3WiseCoverage() {
        assertFullCoverageWithAllAlgorithms(loadFormula("testFeatureModels/model_with_free_variables.dimacs"), 3);
    }

    @Test
    public void variants() {
        compareVariants(loadFormula("models_stability_light/busybox_monthlySnapshot/2007-05-20_17-12-43/clean.dimacs"));
    }

    @Test
    void gplRunsUntilTimeout() {
        testTimeout(loadFormula("GPL/model.xml"), 10);
    }

    private void testTimeout(IFormula formula, int timeoutSeconds) {
        IComputation<BooleanAssignmentList> clauses = getClauses(formula);
        BooleanAssignmentList sample = clauses.map(YASALegacy::new)
                .set(YASALegacy.T, 3)
                .set(YASALegacy.ITERATIONS, Integer.MAX_VALUE)
                .computeResult(Duration.ofSeconds(timeoutSeconds))
                .orElseThrow();
        FeatJAR.log().info("Sample Size: %d", sample.size());

        long time = System.currentTimeMillis();
        CoverageStatistic statistic1 = computeCoverageNew(3, clauses, sample);
        assertEquals(1.0, statistic1.coverage());
        FeatJAR.log().info((System.currentTimeMillis() - time) / 1000.0);
    }

    void onlyNew(IFormula formula) {
        IComputation<BooleanAssignmentList> clauses = getClauses(formula);
        BooleanAssignmentList sample = computeSample(2, clauses);
        computeCoverageNew(2, clauses, sample);
    }

    void compareVariants(IFormula formula) {
        IComputation<BooleanAssignmentList> clauses = getClauses(formula);
        BooleanAssignmentList sample = computeRandomSample(clauses, 10);
        computeCoverageVariants(2, clauses, sample);
    }

    void onlyNewRandom(IFormula formula) {
        IComputation<BooleanAssignmentList> clauses = getClauses(formula);
        BooleanAssignmentList sample = computeRandomSample(clauses, 10);
        computeCoverageNew(2, clauses, sample);
    }

    private BooleanAssignmentList computeRandomSample(IComputation<BooleanAssignmentList> clauses, int size) {
        BooleanAssignmentList sample = clauses.map(ComputeSolutionsSAT4J::new)
                .set(ComputeSolutionsSAT4J.SELECTION_STRATEGY, ISelectionStrategy.NonParameterStrategy.FAST_RANDOM)
                .set(ComputeSolutionsSAT4J.LIMIT, size)
                .set(ComputeSolutionsSAT4J.RANDOM_SEED, 1L)
                .compute();
        return sample;
    }

    public void assertFullCoverageWithAllAlgorithms(IFormula formula, int t) {
        IComputation<BooleanAssignmentList> clauses = getClauses(formula);
        BooleanAssignmentList sample = computeSample(t, clauses);

        CoverageStatistic statistic1 = computeCoverageNew(t, clauses, sample);
        CoverageStatistic statistic2 = computeCoverageRel(t, clauses, sample);
        CoverageStatistic statistic4 = computeCoverageRel2(t, clauses, sample);

        FeatJAR.log().info("total     %d | %d", statistic1.total(), statistic2.total());
        FeatJAR.log().info("covered   %d | %d", statistic1.covered(), statistic2.covered());
        FeatJAR.log().info("uncovered %d | %d", statistic1.uncovered(), statistic2.uncovered());
        FeatJAR.log().info("invalid   %d | %d", statistic1.invalid(), statistic2.invalid());

        assertEquals(1.0, statistic1.coverage());
        assertEquals(1.0, statistic2.coverage());
        assertEquals(1.0, statistic4.coverage());

        assertEquals(statistic1.covered(), statistic2.covered());
        assertEquals(statistic1.uncovered(), statistic2.uncovered());
        assertEquals(statistic1.invalid(), statistic2.invalid());
        assertEquals(statistic1.covered(), statistic4.covered());
        assertEquals(statistic1.uncovered(), statistic4.uncovered());
        assertEquals(statistic1.invalid(), statistic4.invalid());
    }

    private BooleanAssignmentList computeSample(int t, IComputation<BooleanAssignmentList> clauses) {
        BooleanAssignmentList sample =
                clauses.map(YASALegacy::new).set(YASALegacy.T, t).compute();
        FeatJAR.log().info("Sample Size: %d", sample.size());
        return sample;
    }

    private CoverageStatistic computeCoverageRel(
            int t, IComputation<BooleanAssignmentList> clauses, BooleanAssignmentList sample) {
        CoverageStatistic statistic = Computations.of(sample)
                .map(ComputeRelativeTWiseCoverage::new)
                .set(ComputeRelativeTWiseCoverage.REFERENCE_SAMPLE, clauses.map(ComputeSolutionsSAT4J::new))
                .set(
                        ComputeRelativeTWiseCoverage.COMBINATION_SET,
                        clauses.map(VariableCombinationSpecificationComputation::new)
                                .set(VariableCombinationSpecificationComputation.T, t))
                .compute();
        FeatJAR.log().info("Computed Coverage (RelativeTWiseCoverageComputation)");
        return statistic;
    }

    private CoverageStatistic computeCoverageRel2(
            int t, IComputation<BooleanAssignmentList> clauses, BooleanAssignmentList sample) {
        CoverageStatistic statistic = Computations.of(sample)
                .map(ComputeRelativeTWiseCoverage::new)
                .set(ComputeRelativeTWiseCoverage.REFERENCE_SAMPLE, clauses.map(ComputeSolutionsSAT4J::new))
                .set(
                        ComputeRelativeTWiseCoverage.COMBINATION_SET,
                        clauses.map(VariableCombinationSpecificationComputation::new)
                                .set(VariableCombinationSpecificationComputation.T, t))
                .compute();
        FeatJAR.log().info("Computed Coverage (RelativeTWiseCoverageComputation2)");
        return statistic;
    }

    private CoverageStatistic computeCoverageNew(
            int t, IComputation<BooleanAssignmentList> clauses, BooleanAssignmentList sample) {
        CoverageStatistic statistic = Computations.of(sample)
                .map(ComputeConstraintedTWiseCoverage::new)
                .set(ComputeConstraintedTWiseCoverage.BOOLEAN_CLAUSE_LIST, clauses)
                .set(
                        ComputeConstraintedTWiseCoverage.COMBINATION_SET,
                        clauses.map(VariableCombinationSpecificationComputation::new)
                                .set(VariableCombinationSpecificationComputation.T, t))
                .compute();
        FeatJAR.log().info("Computed Coverage (TWiseCoverageComputation)");
        return statistic;
    }

    private void computeCoverageVariants(
            int t, IComputation<BooleanAssignmentList> clauses, BooleanAssignmentList sample) {
        BooleanAssignmentList cnf = clauses.compute();
        VariableMap variableMap = cnf.getVariableMap();
        BooleanAssignment variables = variableMap.getVariables();

        BooleanAssignmentList core =
                Computations.of(cnf).map(ComputeCoreSAT4J::new).compute();
        BooleanAssignment atomic =
                new BooleanAssignment(Computations.of(cnf).map(ComputeAtomicSetsSAT4J::new).compute().stream()
                        .skip(1)
                        .flatMapToInt(l -> Arrays.stream(l.get(), 1, l.get().length))
                        .toArray());

        ComputeConstant<BooleanAssignmentList> of = Computations.of(sample);
        CoverageStatistic statisticNone = of.map(ComputeConstraintedTWiseCoverage::new)
                .set(ComputeConstraintedTWiseCoverage.BOOLEAN_CLAUSE_LIST, clauses)
                .set(
                        ComputeConstraintedTWiseCoverage.COMBINATION_SET,
                        clauses.map(VariableCombinationSpecificationComputation::new)
                                .set(VariableCombinationSpecificationComputation.T, t))
                .compute();

        CoverageStatistic statisticCore = of.map(ComputeConstraintedTWiseCoverage::new)
                .set(ComputeConstraintedTWiseCoverage.BOOLEAN_CLAUSE_LIST, clauses)
                .set(
                        ComputeConstraintedTWiseCoverage.COMBINATION_SET,
                        new VariableCombinationSpecification(t, variables.removeAll(core.getFirst()), variableMap))
                .compute();

        CoverageStatistic statisticAtomic = of.map(ComputeConstraintedTWiseCoverage::new)
                .set(ComputeConstraintedTWiseCoverage.BOOLEAN_CLAUSE_LIST, clauses)
                .set(
                        ComputeConstraintedTWiseCoverage.COMBINATION_SET,
                        new VariableCombinationSpecification(t, variables.removeAll(atomic), variableMap))
                .compute();

        CoverageStatistic statisticCoreAtomic = of.map(ComputeConstraintedTWiseCoverage::new)
                .set(ComputeConstraintedTWiseCoverage.BOOLEAN_CLAUSE_LIST, clauses)
                .set(
                        ComputeConstraintedTWiseCoverage.COMBINATION_SET,
                        new VariableCombinationSpecification(
                                t, variables.removeAll(core.getFirst()).removeAll(atomic), variableMap))
                .compute();
        FeatJAR.log().info("Coverage statisticNone: %f", statisticNone.coverage());
        FeatJAR.log().info("Coverage statisticCore: %f", statisticCore.coverage());
        FeatJAR.log().info("Coverage statisticAtomic: %f", statisticAtomic.coverage());
        FeatJAR.log().info("Coverage statisticCoreAtomic: %f", statisticCoreAtomic.coverage());
    }

    private IComputation<BooleanAssignmentList> getClauses(IFormula formula) {
        return async(formula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new);
    }
}
