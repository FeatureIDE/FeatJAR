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
package de.featjar.composition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.base.FeatJAR;
import de.featjar.base.tree.Trees;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.JavaSymbols;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Preprocessor#computePresenceConditions(java.util.stream.Stream)}.
 */
public class PreprocessorTest {

    @BeforeAll
    public static void begin() {
        FeatJAR.testConfiguration().initialize();
    }

    @AfterAll
    public static void end() {
        FeatJAR.deinitialize();
    }

    @Test
    public void nestedAnnotations() {
        List<String> lines = List.of(
                "//#if A",
                "  System.out.println(\"\");",
                "//#if B",
                "  System.out.println(\"\");",
                "  System.out.println(\"\");",
                "//#else",
                "  System.out.println(\"\");",
                "//#endif",
                "  System.out.println(\"\");",
                "//#endif",
                "  System.out.println(\"\");");
        assertEquals(
                List.of("false", "A", "false", "A && B", "A && B", "false", "A && !B", "false", "A", "false", "true"),
                presenceConditions(lines));
    }

    @Test
    public void noAnnotations() {
        assertEquals(List.of("true", "true"), presenceConditions(List.of("int a;", "int b;")));
    }

    private static List<String> presenceConditions(List<String> lines) {
        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setSymbols(JavaSymbols.INSTANCE);
        return new Preprocessor("//#", JavaSymbols.INSTANCE)
                .computePresenceConditions(lines.stream()).stream()
                        .map(pc -> Trees.traverse(pc, serializer).orElseThrow())
                        .collect(Collectors.toList());
    }
}
