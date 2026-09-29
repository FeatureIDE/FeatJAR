package de.featjar.analysis.javasmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.composition.ExpressionParser;
import de.featjar.formula.io.textual.ShortSymbols;
import de.featjar.formula.structure.IFormula;
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
    void mixedConditionCanBeAnalyzed() {
        ExpressionParser parser = new ExpressionParser();

        IFormula featureModel = (IFormula) parser.parse("A & (a >= 10)").get();

        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("#", ShortSymbols.INSTANCE);

        List<String> superfluous =
                analyzer.findSuperfluousAnnotations(Stream.of("#if A & (a > 5)", "foo();", "#endif"), featureModel);

        assertEquals(1, superfluous.size());
    }
}
