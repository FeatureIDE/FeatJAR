package de.featjar.analysis.javasmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.composition.ExpressionParser;
import de.featjar.formula.io.textual.ShortSymbols;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.predicate.GreaterEqual;
import de.featjar.formula.structure.predicate.GreaterThan;
import de.featjar.formula.structure.predicate.LessEqual;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Tests preprocessor analysis for non-Boolean annotations.
 *
 * <p>In particular, this verifies that expressions such as {@code a > 5}
 */
public class PreprocessorAnalyzerTest {

    @Test
    void parsesGreaterThanExpression() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a > 5").get();

        assertTrue(expression instanceof GreaterThan);

        GreaterThan greaterThan = (GreaterThan) expression;

        assertTrue(greaterThan.getLeftExpression() instanceof Variable);
        assertTrue(greaterThan.getRightExpression() instanceof Constant);

        Variable variable = (Variable) greaterThan.getLeftExpression();

        Constant constant = (Constant) greaterThan.getRightExpression();

        assertEquals("a", variable.getName());
        assertEquals(Long.class, variable.getType());

        assertEquals(5L, constant.getValue());
        assertEquals(Long.class, constant.getType());
    }

    @Test
    void parsesLessThanExpression() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a < 5").get();

        assertTrue(expression instanceof LessThan);
    }

    @Test
    void parsesGreaterEqualExpression() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a >= 5").get();

        assertTrue(expression instanceof GreaterEqual);
    }

    @Test
    void parsesLessEqualExpression() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a <= 5").get();

        assertTrue(expression instanceof LessEqual);
    }

    @Test
    void parsesComparisonBetweenVariables() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("a > b").get();

        assertTrue(expression instanceof GreaterThan);

        GreaterThan greaterThan = (GreaterThan) expression;

        assertTrue(greaterThan.getLeftExpression() instanceof Variable);
        assertTrue(greaterThan.getRightExpression() instanceof Variable);

        Variable left = (Variable) greaterThan.getLeftExpression();

        Variable right = (Variable) greaterThan.getRightExpression();

        assertEquals("a", left.getName());
        assertEquals("b", right.getName());

        assertEquals(Long.class, left.getType());
        assertEquals(Long.class, right.getType());
    }

    @Test
    void detectsDeadCodeWithNonBooleanCondition() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("a <= 10").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> result = analyzer.findDeadCode(Stream.of("#if a > 20", "foo();", "#endif"), featureModel);

        assertEquals(1, result.size());
    }

    @Test
    void doesNotDetectReachableNonBooleanCodeAsDead() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("a <= 10").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> result = analyzer.findDeadCode(Stream.of("#if a > 5", "foo();", "#endif"), featureModel);

        assertTrue(result.isEmpty());
    }

    @Test
    void detectsSuperfluousNonBooleanAnnotation() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("a >= 10").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> result =
                analyzer.findSuperfluousAnnotations(Stream.of("#if a > 5", "foo();", "#endif"), featureModel);

        assertEquals(1, result.size());
    }

    @Test
    void doesNotDetectMeaningfulNonBooleanAnnotationAsSuperfluous() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("(a >= 0) & (a <= 10)").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> result =
                analyzer.findSuperfluousAnnotations(Stream.of("#if a > 5", "foo();", "#endif"), featureModel);

        assertTrue(result.isEmpty());
    }

    @Test
    void validNonBooleanConditionIsNeitherDeadNorSuperfluous() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("(a >= 0) & (a <= 10)").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> dead = analyzer.findDeadCode(Stream.of("#if a > 5", "foo();", "#endif"), featureModel);

        List<String> superfluous =
                analyzer.findSuperfluousAnnotations(Stream.of("#if a > 5", "foo();", "#endif"), featureModel);

        assertTrue(dead.isEmpty());
        assertTrue(superfluous.isEmpty());
    }

    @Test
    void detectsDeadElseBranchWhenIfConditionIsAlwaysTrue() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("a >= 10").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> dead =
                analyzer.findDeadCode(Stream.of("#if a > 5", "first();", "#else", "second();", "#endif"), featureModel);

        assertEquals(1, dead.size());
    }

    @Test
    void detectsDeadIfBranchWhenConditionCannotHold() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("a <= 10").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> dead = analyzer.findDeadCode(
                Stream.of("#if a > 20", "first();", "#else", "second();", "#endif"), featureModel);

        assertEquals(1, dead.size());
    }

    @Test
    void parsesBooleanAndNonBooleanCondition() {
        ExpressionParser parser = new ExpressionParser();

        IExpression expression = parser.parse("A & (a > 5)").get();

        assertTrue(expression instanceof IFormula);
    }

    @Test
    void mixedConditionCanBeAnalyzed() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("A & (a >= 10)").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> superfluous =
                analyzer.findSuperfluousAnnotations(Stream.of("#if A & (a > 5)", "foo();", "#endif"), featureModel);

        assertEquals(1, superfluous.size());
    }
}
