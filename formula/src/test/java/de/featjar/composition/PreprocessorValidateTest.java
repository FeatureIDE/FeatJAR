package de.featjar.composition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.formula.io.textual.JavaSymbols;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PreprocessorValidateTest {

    private Preprocessor preprocessor;

    @BeforeEach
    void setUp() {
        preprocessor = new Preprocessor("//#", JavaSymbols.INSTANCE);
    }

    private List<String> validate(String... lines) {
        return preprocessor.validate(Stream.of(lines));
    }

    @Test
    void emptyInputProducesNoDiagnostics() {
        assertTrue(validate().isEmpty());
    }

    @Test
    void plainSourceCodeProducesNoDiagnostics() {
        assertTrue(validate(
                "int x = 1;",
                "System.out.println(x);"
        ).isEmpty());
    }

    @Test
    void wellFormedIfElseEndifProducesNoDiagnostics() {
        assertTrue(validate(
                "//# if A && B",
                "int x = 1;",
                "//# else",
                "int x = 2;",
                "//# endif"
        ).isEmpty());
    }

    @Test
    void wellFormedIfElifElseEndifProducesNoDiagnostics() {
        assertTrue(validate(
                "//# if A",
                "int x = 1;",
                "//# elif B || C",
                "int x = 2;",
                "//# else",
                "int x = 3;",
                "//# endif"
        ).isEmpty());
    }

    @Test
    void elseAndEndifHaveNothingToParse() {
        assertTrue(validate(
                "//# if A",
                "//# else",
                "//# endif"
        ).isEmpty());
    }

    @Test
    void unparseableIfConditionIsReported() {
        List<String> problems = validate(
                "//# if A oder B",
                "System.out.println(\"\");",
                "//# endif"
        );

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).startsWith("line 1:"),
                   "expected line 1, got: " + problems.get(0));
    }

    @Test
    void unparseableElifConditionIsReported() {
        List<String> problems = validate(
                "//# if A",
                "//# elif B oder C",
                "//# endif"
        );

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).startsWith("line 2:"),
                   "expected line 2, got: " + problems.get(0));
    }

    @Test
    void unbalancedParenthesisIsReported() {
        List<String> problems = validate(
                "//# if (A && B",
                "//# endif"
        );

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).startsWith("line 1:"));
    }

    @Test
    void danglingOperatorIsReported() {
        List<String> problems = validate(
                "//# if A &&",
                "//# endif"
        );

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).startsWith("line 1:"));
    }

    @Test
    void allProblemsInFileAreReported() {
        List<String> problems = validate(
                "//# if A oder B",   
                "int x = 1;",
                "//# elif C &&", 
                "int x = 2;",
                "//# endif"
        );

        assertEquals(2, problems.size());
        assertTrue(problems.get(0).startsWith("line 1:"));
        assertTrue(problems.get(1).startsWith("line 3:"));
    }

    @Test
    void validateDoesNotStopAtFirstProblem() {
        List<String> problems = validate(
                "//# if A oder B",
                "//# endif",
                "//# if C oder D",
                "//# endif"
        );

        assertEquals(2, problems.size());
    }

    @Test
    void numericConditionIsReported() {
        List<String> problems = validate(
                "//# if 5",
                "//# endif"
        );

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).contains("boolean"),
                   "expected a 'not boolean' message, got: " + problems.get(0));
    }

    @Test
    void lineNumbersAreOneBased() {
        List<String> problems = validate("//# if A oder B");

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).startsWith("line 1:"));
    }

    @Test
    void lineNumbersAdvanceThroughTheFile() {
        List<String> problems = validate(
                "",
                "",
                "",
                "//# if A oder B"  
        );

        assertEquals(1, problems.size());
        assertTrue(problems.get(0).startsWith("line 4:"));
    }

    @Test
    void nonAnnotationLinesAreNeverParsed() {
        List<String> problems = validate(
                "// this is a normal comment",
                "int A = 1;",
                "if (A) {  }"          

        assertTrue(problems.isEmpty());
    }
}