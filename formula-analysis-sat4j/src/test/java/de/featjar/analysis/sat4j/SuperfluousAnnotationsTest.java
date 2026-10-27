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
