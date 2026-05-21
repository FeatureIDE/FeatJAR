/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.analysis.javasmt.computation.ComputeJavaSMTFormula;
import de.featjar.analysis.javasmt.computation.ComputeSolutionEnumeration;
import de.featjar.analysis.javasmt.solver.FormulaToJavaSMT;
import de.featjar.analysis.javasmt.solver.FormulaToJavaSMT.VariableReference;
import de.featjar.analysis.javasmt.solver.JavaSMTFormula;
import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.DefLiteral;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.function.integer.IntegerDivide;
import de.featjar.formula.structure.term.function.integer.IntegerMultiply;
import de.featjar.formula.structure.term.function.string.StringLength;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.visitors.DefaultBooleanFormulaVisitor;

public class ComputeSolutionEnumerationTest {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void formulaHasEightSatisfyingAssignments() {
        IFormula formula = new Reference(new And(
                new Implies(
                        new Literal("Veggies"),
                        new Or(
                                new Literal("Tomatoes"),
                                new Literal("Cucumber"),
                                new Literal("Fennel"),
                                new Literal("Beets"))),
                new Implies(new Literal("Fennel"), new And(new Literal("Beets"), new Not(new Literal("Cucumber")))),
                new GreaterEqual(new IntegerAdd(new Constant(90l), new Constant(100l)), new Constant(80d)),
                new LessThan(new IntegerMultiply(new Constant(80l), new Constant(100l)), new Constant(100000d)),
                new Equals(new Constant(100l), new Constant(100d)),
                new BiImplies(new Literal("Beets"), new Or(new Literal("Cucumber"), new Not(new Literal("Tomatoes")))),
                new LessEqual(
                        new IntegerAdd(new Constant(100l), new IntegerMultiply(new Constant(-1l), new Constant(80l))),
                        new Constant(30d)),
                new GreaterThan(new IntegerDivide(new Constant(100l), new Constant(25l)), new Constant(3d)),
                new Equals(new Constant("Cherry"), new Constant("Cherry")),
                new Implies(
                        new And(new DefLiteral(new Variable("Arugula", String.class))),
                        new Equals(new StringLength(new Variable("Arugula", String.class)), new Constant(7d)))));

        final Result<JavaSMTFormula> javaSMTFormulaResult = Computations.of(formula)
                .map(ComputeJavaSMTFormula::new)
                .set(ComputeJavaSMTFormula.SOLVER, Solvers.Z3)
                .computeResult();

        JavaSMTFormula javaSMTFormula = javaSMTFormulaResult.get();
        FormulaToJavaSMT formulaToJavaSmt = javaSMTFormula.getTranslator();

        formulaToJavaSmt.nodeToFormula(formula);
        FormulaManager formulaManager = formulaToJavaSmt.getCurrentFormulaManager();
        List<VariableReference> variableReferences = formulaToJavaSmt.getMappings();

        Result<List<List<BooleanFormula>>> booleanAssignmentsResult = Computations.of(javaSMTFormula)
                .map(ComputeSolutionEnumeration::new)
                .computeResult();

        assertTrue(
                booleanAssignmentsResult.isPresent(),
                () -> Problem.printProblems(booleanAssignmentsResult.getProblems()));
        List<List<BooleanFormula>> booleanAssignments = booleanAssignmentsResult.get();
        LinkedHashMap<String, Object> assignment = new LinkedHashMap<>();

        // define a BooleanFormulaVisitor which returns the index of the current atom
        DefaultBooleanFormulaVisitor<Void> booleanFormulaVisitor = new DefaultBooleanFormulaVisitor<Void>() {

            public Void visitNot(BooleanFormula operand) {
                assignment.put(getName(operand), Boolean.FALSE);
                return null;
            }

            public Void visitAtom(BooleanFormula atom, FunctionDeclaration<BooleanFormula> funcDecl) {
                assignment.put(getName(atom), Boolean.TRUE);
                return null;
            }

            private String getName(BooleanFormula atom) {
                return variableReferences.stream()
                        .filter(r -> r.getJavaSmtVariable().equals(atom))
                        .map(r -> r.getVariable().getName())
                        .findFirst()
                        .orElse("???");
            }

            public Void visitDefault() {
                return null;
            }
        };

        // create computed assignments
        List<Assignment> computedAssignments = new ArrayList<>();
        for (List<BooleanFormula> booleanAssignment : booleanAssignments) {
            assignment.clear();
            for (BooleanFormula booleanVariable : booleanAssignment) {
                formulaManager.getBooleanFormulaManager().visit(booleanVariable, booleanFormulaVisitor);
            }
            computedAssignments.add(new Assignment(new LinkedHashMap<>(assignment)));
        }

        List<Assignment> expectedAssignments = new ArrayList<>();
        expectedAssignments.add(assignment(List.of("Tomatoes"), List.of("Cucumber", "Fennel", "Beets")));
        expectedAssignments.add(assignment(List.of("Cucumber", "Beets"), List.of("Fennel")));
        expectedAssignments.add(assignment(List.of("Beets"), List.of("Tomatoes", "Cucumber", "Fennel")));
        expectedAssignments.add(assignment(List.of("Beets", "Fennel"), List.of("Tomatoes", "Cucumber")));

        Assertions.assertEquals(expectedAssignments, computedAssignments);
    }

    private Assignment assignment(List<String> positive, List<String> negative) {
        LinkedHashMap<String, Object> assignment = new LinkedHashMap<>();
        for (String name : positive) {
            assignment.put(name, Boolean.TRUE);
        }
        for (String name : negative) {
            assignment.put(name, Boolean.FALSE);
        }
        return new Assignment(assignment);
    }
}
