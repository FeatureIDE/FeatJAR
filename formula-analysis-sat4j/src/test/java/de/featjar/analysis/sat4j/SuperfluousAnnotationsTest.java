package de.featjar.analysis.sat4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.AnalysisTest;
import de.featjar.formula.io.textual.JavaSymbols;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.predicate.Literal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SuperfluousAnnotationsTest extends AnalysisTest {

    @Test
    public void findsTautologyAndMandatoryFeature() {
        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList(
                "//#if A || !A",
                "always",
                "//#endif",
                "//#if Base",
                "also always",
                "//#endif",
                "//#if Directed",
                "optional",
                "//#endif");

        List<String> found = analyzer.findSuperfluousAnnotations(lines.stream(), loadFormula("GPL/model.xml"));

        assertEquals(Arrays.asList("Line 1: //#if A || !A", "Line 4: //#if Base"), found);
    }

    @Test
    public void considersEarlierBranchesAndParentCondition() {
        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList(
                "//#if Directed",
                "first",
                "//#elif Undirected",
                "second",
                "//#else",
                "never",
                "//#endif",
                "//#if Directed",
                "//#if Base",
                "nested",
                "//#endif",
                "//#else",
                "//#if Base",
                "nested always",
                "//#endif",
                "//#endif");

        List<String> found = analyzer.findSuperfluousAnnotations(lines.stream(), loadFormula("GPL/model.xml"));

        assertEquals(Arrays.asList(), found);
    }

    @Test
    public void reportsElifAndNestedBranchWhenAlwaysSelected() {
        PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer("//#", JavaSymbols.INSTANCE);
        List<String> lines = Arrays.asList(
                "//#if B",
                "optional",
                "//#elif !B",
                "always",
                "//#endif",
                "//#if B",
                "optional",
                "//#else",
                "//#if A",
                "always",
                "//#endif",
                "//#endif");

        List<String> found =
                analyzer.findSuperfluousAnnotations(lines.stream(), new And(new Literal("A"), new Literal(false, "B")));

        assertEquals(Arrays.asList("Line 3: //#elif !B", "Line 8: //#else", "Line 9: //#if A"), found);
    }
}
