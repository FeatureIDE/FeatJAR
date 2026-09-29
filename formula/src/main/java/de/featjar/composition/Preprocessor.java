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

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.io.textual.JavaSymbols;
import de.featjar.formula.io.textual.Symbols;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Preprocessor {

    protected static final String IF_GROUP = "if";
    protected static final String ELIF_GROUP = "elif";
    protected static final String ELSE_GROUP = "else";
    protected static final String ENDIF_GROUP = "endif";
    protected static final String IF_CONDITION_GROUP = "ifCondition";
    protected static final String ELIF_CONDITION_GROUP = "elifCondition";

    /**
     * Describes the syntax of annotations, i.e., their prefix and suffix, their keywords, how conditions are delimited,
     * and the symbols used within conditions.
     * All strings are matched literally, including whitespace within them.
     * Presets for common preprocessors are available as {@link #CPP}, {@link #ANTENNA}, and {@link #MUNGE}.
     */
    public static final class Style {

        /**
         * The style of the C preprocessor, e.g., {@code #if A && B}, {@code #elif C}, {@code #else}, {@code #endif}.
         */
        public static final Style CPP =
                new Style("#", "", "if", "elif", "else", "endif", " ", "", false, JavaSymbols.INSTANCE);

        /**
         * The style of Antenna, e.g., {@code //#if A && B}, {@code //#elif C}, {@code //#else}, {@code //#endif}.
         */
        public static final Style ANTENNA =
                new Style("//#", "", "if", "elif", "else", "endif", " ", "", false, JavaSymbols.INSTANCE);

        /**
         * The style of Munge, e.g., {@code /*if[A]*}{@code /}, {@code /*else[A]*}{@code /}, {@code /*end[A]*}{@code /}.
         * Munge has no elif, and the condition repeated after else and end is ignored.
         * Annotations must be placed on their own line.
         */
        public static final Style MUNGE =
                new Style("/*", "*/", "if", null, "else", "end", "[", "]", true, JavaSymbols.INSTANCE);

        private final String prefix;
        private final String suffix;
        private final String ifKeyword;
        private final String elifKeyword;
        private final String elseKeyword;
        private final String endifKeyword;
        private final String conditionStart;
        private final String conditionEnd;
        private final boolean conditionAfterElseAndEndif;
        private final Symbols symbols;

        /**
         * Creates a new annotation style.
         *
         * @param prefix the string that starts each annotation, must not be empty
         * @param suffix the string that ends each annotation, may be empty
         * @param ifKeyword the keyword that opens a block
         * @param elifKeyword the keyword for an alternative block with a condition, {@code null} if not supported
         * @param elseKeyword the keyword for an alternative block without a condition
         * @param endifKeyword the keyword that closes a block
         * @param conditionStart the string between the keyword and the condition
         * @param conditionEnd the string after the condition, may be empty
         * @param conditionAfterElseAndEndif whether else and endif may repeat a condition (which is ignored)
         * @param symbols the symbols used within conditions
         * @throws IllegalArgumentException if the prefix is empty
         */
        public Style(
                String prefix,
                String suffix,
                String ifKeyword,
                String elifKeyword,
                String elseKeyword,
                String endifKeyword,
                String conditionStart,
                String conditionEnd,
                boolean conditionAfterElseAndEndif,
                Symbols symbols) {
            if (Objects.requireNonNull(prefix).isEmpty()) {
                throw new IllegalArgumentException("annotation prefix must not be empty");
            }
            this.prefix = prefix;
            this.suffix = Objects.requireNonNull(suffix);
            this.ifKeyword = Objects.requireNonNull(ifKeyword);
            this.elifKeyword = elifKeyword;
            this.elseKeyword = Objects.requireNonNull(elseKeyword);
            this.endifKeyword = Objects.requireNonNull(endifKeyword);
            this.conditionStart = Objects.requireNonNull(conditionStart);
            this.conditionEnd = Objects.requireNonNull(conditionEnd);
            this.conditionAfterElseAndEndif = conditionAfterElseAndEndif;
            this.symbols = Objects.requireNonNull(symbols);
        }

        /**
         * {@return a copy of this style with the given prefix}
         *
         * @param prefix the string that starts each annotation, must not be empty
         * @throws IllegalArgumentException if the prefix is empty
         */
        public Style withPrefix(String prefix) {
            return new Style(
                    prefix,
                    suffix,
                    ifKeyword,
                    elifKeyword,
                    elseKeyword,
                    endifKeyword,
                    conditionStart,
                    conditionEnd,
                    conditionAfterElseAndEndif,
                    symbols);
        }

        /**
         * {@return a copy of this style with the given symbols}
         *
         * @param symbols the symbols used within conditions
         */
        public Style withSymbols(Symbols symbols) {
            return new Style(
                    prefix,
                    suffix,
                    ifKeyword,
                    elifKeyword,
                    elseKeyword,
                    endifKeyword,
                    conditionStart,
                    conditionEnd,
                    conditionAfterElseAndEndif,
                    symbols);
        }

        public String getPrefix() {
            return prefix;
        }

        public Symbols getSymbols() {
            return symbols;
        }

        private Pattern toPattern() {
            String ignoredCondition = conditionAfterElseAndEndif
                    ? "(?:" + Pattern.quote(conditionStart) + ".*" + Pattern.quote(conditionEnd) + ")?"
                    : "";
            String elif = elifKeyword == null ? "(?!)" : Pattern.quote(elifKeyword);
            String regex = Pattern.quote(prefix)
                    + "\\s*(?:"
                    + group(ENDIF_GROUP, Pattern.quote(endifKeyword) + ignoredCondition + "\\s*")
                    + "|" + group(ELSE_GROUP, Pattern.quote(elseKeyword) + ignoredCondition + "\\s*")
                    + "|" + group(IF_GROUP, Pattern.quote(ifKeyword) + condition(IF_CONDITION_GROUP))
                    + "|" + group(ELIF_GROUP, elif + condition(ELIF_CONDITION_GROUP))
                    + ")"
                    + (suffix.isEmpty() ? "" : "\\s*" + Pattern.quote(suffix) + "\\s*");
            FeatJAR.log().debug("annotation pattern: %s", regex);
            return Pattern.compile(regex);
        }

        private String condition(String groupName) {
            return Pattern.quote(conditionStart) + group(groupName, ".+") + Pattern.quote(conditionEnd);
        }

        private static String group(String name, String regex) {
            return "(?<" + name + ">" + regex + ")";
        }
    }

    private final ExpressionParser annotationParser;

    protected final Pattern annotationPattern;
    private final Pattern annotationPrefixPattern;
    private final String annotationPrefix;

    /**
     * {@return the symbols used to parse annotation conditions}
     */
    public Symbols getSymbols() {
        return annotationParser.getSymbols();
    }
    // -------------------------------------------------------------------------

    private class Filter implements Predicate<String> {

        private final Assignment assignment;

        private final LinkedList<IExpression> expressionStack = new LinkedList<>();
        private final LinkedList<Boolean> evaluationStack = new LinkedList<>();

        private int lineNumber;

        public Filter(Assignment assignment) {
            this.assignment = assignment;
        }

        @Override
        public boolean test(String line) {
            lineNumber++;
            Matcher matcher = annotationPattern.matcher(line);
            if (matcher.matches()) {
                if (matcher.group(ENDIF_GROUP) != null) {
                    if (expressionStack.isEmpty()) {
                        FeatJAR.log().warning("Line %d: no annotation to end", lineNumber);
                    } else {
                        expressionStack.pop();
                        evaluationStack.pop();
                    }
                    return false;
                } else if (matcher.group(ELSE_GROUP) != null) {
                    if (expressionStack.isEmpty()) {
                        FeatJAR.log().warning("Line %d: no annotation for else", lineNumber);
                    } else {
                        Boolean eval = evaluationStack.pop();
                        if (evaluationStack.isEmpty() || evaluationStack.peek()) {
                            evaluationStack.push(!eval);
                        } else {
                            evaluationStack.push(Boolean.FALSE);
                        }
                    }
                    return false;
                } else if (matcher.group(ELIF_GROUP) != null) {
                    if (expressionStack.isEmpty()) {
                        FeatJAR.log().warning("Line %d: no annotation for elif", lineNumber);
                    } else {
                        Boolean eval = evaluationStack.pop();
                        if (evaluationStack.isEmpty() || evaluationStack.peek()) {
                            evaluationStack.push(!eval);
                        } else {
                            evaluationStack.push(Boolean.FALSE);
                        }
                    }
                    Result<IExpression> parse = annotationParser.parse(matcher.group(ELIF_CONDITION_GROUP));
                    if (parse.isPresent()) {
                        IExpression annotationExpression = parse.get();
                        expressionStack.push(annotationExpression);
                        if (evaluationStack.isEmpty() || evaluationStack.peek()) {
                            Object evaluation =
                                    annotationExpression.evaluate(assignment).orElse(null);
                            if (evaluation instanceof Boolean) {
                                evaluationStack.push((Boolean) evaluation);
                            } else {
                                FeatJAR.log().warning("Line %d: could not evaluate annotation: %s", lineNumber, line);
                                evaluationStack.push(Boolean.FALSE);
                            }
                        } else {
                            evaluationStack.push(Boolean.FALSE);
                        }
                    } else {
                        FeatJAR.log().warning("Line %d: could not parse annotation: %s", lineNumber, line);
                        return true;
                    }
                    return false;
                } else if (matcher.group(IF_GROUP) != null) {
                    Result<IExpression> parse = annotationParser.parse(matcher.group(IF_CONDITION_GROUP));
                    if (parse.isPresent()) {
                        IExpression annotationExpression = parse.get();
                        expressionStack.push(annotationExpression);
                        if (evaluationStack.isEmpty() || evaluationStack.peek()) {
                            Object evaluation =
                                    annotationExpression.evaluate(assignment).orElse(null);
                            if (evaluation instanceof Boolean) {
                                evaluationStack.push((Boolean) evaluation);
                            } else {
                                FeatJAR.log().warning("Line %d: could not evaluate annotation: %s", lineNumber, line);
                                evaluationStack.push(Boolean.FALSE);
                            }
                        } else {
                            evaluationStack.push(Boolean.FALSE);
                        }
                        return false;
                    } else {
                        FeatJAR.log().warning("Line %d: could not parse annotation: %s", lineNumber, line);
                        return true;
                    }
                } else {
                    FeatJAR.log().warning("Line %d: syntax error: %s", lineNumber, line);
                    return true;
                }
            } else {
                return evaluationStack.isEmpty() || evaluationStack.peek();
            }
        }
    }

    private class VariableNames implements Function<String, Stream<Variable>> {

        private int lineNumber;

        @Override
        public Stream<Variable> apply(String line) {
            lineNumber++;
            Matcher matcher = annotationPattern.matcher(line);
            if (matcher.matches() && (matcher.group(IF_GROUP) != null || matcher.group(ELIF_GROUP) != null)) {
                String condition = matcher.group(IF_GROUP) != null
                        ? matcher.group(IF_CONDITION_GROUP)
                        : matcher.group(ELIF_CONDITION_GROUP);
                Result<IExpression> parse = annotationParser.parse(condition);
                if (parse.isPresent()) {
                    return parse.get().getVariableStream();
                } else {
                    FeatJAR.log().warning("Line %d: could not parse annotation: %s", lineNumber, line);
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Creates a preprocessor for annotations in the style of the C preprocessor with the given prefix and symbols.
     *
     * @param annotationPrefix the string that starts each annotation, must not be empty
     * @param symbols the symbols used within conditions
     * @throws IllegalArgumentException if the prefix is empty
     */
    public Preprocessor(String annotationPrefix, Symbols symbols) {
        this(Style.CPP.withPrefix(annotationPrefix).withSymbols(symbols));
    }

    /**
     * Creates a preprocessor for annotations in the given style.
     *
     * @param style the annotation style, e.g., {@link Style#CPP}, {@link Style#ANTENNA}, or {@link Style#MUNGE}
     */
    public Preprocessor(Style style) {
        annotationParser = new ExpressionParser();
        annotationParser.setSymbols(style.getSymbols());
        annotationPattern = style.toPattern();
        annotationPrefix = style.getPrefix();
        annotationPrefixPattern = Pattern.compile("^" + Pattern.quote(annotationPrefix));
    }

    /**
     * {@return a filtered stream that contains only lines that remain after preprocessing with the given variable assignment}
     *
     * <b>Note</b>: The return stream is <b>not state less</b>.
     * It is not suitable for parallel consumption.
     *
     * @param lines the line stream
     * @param assignment the variable assignment
     */
    public Stream<String> preprocess(Stream<String> lines, Assignment assignment) {
        return lines.sequential().filter(new Filter(assignment));
    }

    /**
     * {@return the presence condition of each line, in order}
     *
     * @param lines the line stream
     */
    public List<IFormula> computePresenceConditions(Stream<String> lines) {
        LinkedList<IFormula> stack = new LinkedList<>();
        LinkedList<Integer> elifCounts = new LinkedList<>(); // each elif adds one extra stack entry to its if
        return lines.sequential()
                .map(line -> {
                    Matcher matcher = annotationPattern.matcher(line);
                    if (!matcher.matches()) {
                        if (stack.isEmpty()) {
                            return (IFormula) True.INSTANCE;
                        }
                        List<IFormula> conjuncts = new ArrayList<>();
                        stack.descendingIterator().forEachRemaining(conjuncts::add);
                        return conjuncts.size() == 1 ? conjuncts.get(0) : new And(conjuncts);
                    }
                    if (matcher.group(IF_GROUP) != null) {
                        stack.push((IFormula) annotationParser
                                .parse(matcher.group(IF_CONDITION_GROUP))
                                .orElseThrow());
                        elifCounts.push(0);
                    } else if (matcher.group(ELSE_GROUP) != null) {
                        stack.push(new Not(popChecked(stack, line)));
                    } else if (matcher.group(ENDIF_GROUP) != null) {
                        popChecked(stack, line);
                        for (int i = elifCounts.pop(); i > 0; i--) stack.pop();
                    } else if (matcher.group(ELIF_GROUP) != null) {
                        stack.push(new Not(popChecked(stack, line)));
                        elifCounts.push(elifCounts.pop() + 1);
                        stack.push((IFormula) annotationParser
                                .parse(matcher.group(ELIF_CONDITION_GROUP))
                                .orElseThrow());
                    }
                    return (IFormula) False.INSTANCE;
                })
                .collect(Collectors.toList());
    }

    private IFormula popChecked(LinkedList<IFormula> stack, String line) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Unbalanced presence annotation (empty stack): " + line);
        }
        return stack.pop();
    }

    /**
     * Checks matching if/endif annotations
     */
    public List<Problem> checkStructure(Stream<String> lines) {
        LinkedList<Integer> stack = new LinkedList<>();
        List<Problem> problems = new ArrayList<>();
        List<Integer> ifLines = new ArrayList<>();
        List<String> lineList = lines.toList();
        int lineNumber = 0;
        int lastEndifLine = 0;

        for (String line : lineList) {
            lineNumber++;
            Matcher matcher = annotationPattern.matcher(line);

            if (matcher.matches()) {
                if (matcher.group(IF_GROUP) != null) { // this line is an #if (check notes.md file for more)
                    stack.push(lineNumber);
                    ifLines.add(lineNumber);
                } else if (matcher.group(ENDIF_GROUP) != null) { // this is an #endif
                    if (stack.isEmpty()) {
                        String addIfSuggestion = lastEndifLine == 0
                                ? "add a matching #if before line 1"
                                : "add a matching #if on line " + (lastEndifLine + 1);
                        problems.add(new ParseProblem(
                                "#endif without #if. Suggestion: remove the #endif or " + addIfSuggestion + ".",
                                Severity.ERROR,
                                lineNumber));
                    } else {
                        stack.pop();
                    }
                    lastEndifLine = lineNumber;
                }
            }
        }

        // the remaining #if lines have no matching #endif
        ListIterator<Integer> iterator = ifLines.listIterator();
        while (!stack.isEmpty()) {
            int startLine = stack.removeLast();
            int nextIfLine = 0;
            while (iterator.hasNext()) {
                int next = iterator.next();
                if (next > startLine) {
                    nextIfLine = next;
                    iterator.previous();
                    break;
                }
            }

            String suggestion;
            if (nextIfLine > 0) {
                suggestion = "add a matching #endif before line " + nextIfLine;
            } else if (startLine == lineList.size()) {
                suggestion = "remove the #if";
            } else {
                suggestion = "add a matching #endif at the end of the file";
            }
            problems.add(new ParseProblem(
                    "#if has no matching #endif. Suggestion: " + suggestion + ".", Severity.ERROR, startLine));
        }
        return problems;
    }

    public List<String> extractVariableNames(Stream<String> lines) {
        return lines.flatMap(new VariableNames())
                .distinct()
                .map(Variable::getName)
                .collect(Collectors.toList());
    }

    /**
     * {@return a problem for each annotation with a syntactically invalid condition, including its line number}
     *
     * @param lines the line stream
     */
    public List<ParseProblem> validate(Stream<String> lines) {
        List<ParseProblem> problems = new ArrayList<>();

        Iterator<String> it = lines.iterator();
        int lineNumber = 0;
        while (it.hasNext()) {
            String line = it.next();
            lineNumber++;

            Matcher matcher = annotationPattern.matcher(line);
            if (!matcher.matches()) continue;

            if (matcher.group(IF_GROUP) != null) {
                problems.addAll(checkCondition(matcher.group(IF_CONDITION_GROUP), lineNumber));
            } else if (matcher.group(ELIF_GROUP) != null) {
                problems.addAll(checkCondition(matcher.group(ELIF_CONDITION_GROUP), lineNumber));
            }
        }

        return problems;
    }

    /**
     * {@return a problem for each annotation with a syntactically invalid condition, including its line number}
     *
     * @param lines the line stream
     */
    public List<ParseProblem> checkSyntax(Stream<String> lines) {
        List<ParseProblem> problems = new ArrayList<>();

        Iterator<String> it = lines.iterator();
        int lineNumber = 0;

        while (it.hasNext()) {
            String line = it.next();
            lineNumber++;

            if (!annotationPrefixPattern.matcher(line).find()) {
                continue;
            }
            Matcher matcher = annotationPattern.matcher(line);

            if (!matcher.matches()) {
                problems.add(new ParseProblem(describeInvalidAnnotation(line), Problem.Severity.ERROR, lineNumber));
                continue;
            }
        }
        return problems;
    }

    private String describeInvalidAnnotation(String line) {
        String annotation = line.substring(annotationPrefix.length()).trim();
        if (annotation.isEmpty()) {
            return "Invalid annotation syntax: missing annotation keyword";
        }

        String[] parts = annotation.split("\\s+", 2);
        String keyword = parts[0];
        String remainder = parts.length > 1 ? parts[1].trim() : "";

        if (!keyword.equals("if") && !keyword.equals("elif") && !keyword.equals("else") && !keyword.equals("endif")) {
            return "Invalid annotation syntax: unknown annotation keyword '" + keyword + "'";
        }
        if ((keyword.equals("if") || keyword.equals("elif")) && remainder.isEmpty()) {
            return "Invalid annotation syntax: missing condition after '" + keyword + "'";
        }
        if ((keyword.equals("else") || keyword.equals("endif")) && !remainder.isEmpty()) {
            return "Invalid annotation syntax: unexpected content after '" + keyword + "'";
        }
        return "Invalid annotation syntax: malformed '" + keyword + "' annotation";
    }

    private List<ParseProblem> checkCondition(String condition, int lineNumber) {
        Result<IExpression> parse = annotationParser.parse(condition);

        if (!parse.isPresent()) {
            return parse.getProblems().stream()
                    .map(p -> new ParseProblem(p.getMessage(), p.getSeverity(), lineNumber))
                    .toList();
        } else if (!(parse.get() instanceof IFormula)) {
            return List.of(new ParseProblem(
                    String.format("condition is not a boolean formula: \"%s\"", condition),
                    Problem.Severity.ERROR,
                    lineNumber));
        }
        return List.of();
    }

    /**
     * {@return a problem for each feature used in an annotation that does not occur in the feature model, including its line number}
     *
     * @param lines the line stream
     * @param featureModel the feature model
     */
    public List<ParseProblem> findUnknownFeatures(Stream<String> lines, IFormula featureModel) {
        Set<String> features = featureModel.getVariableNames();
        List<ParseProblem> problems = new ArrayList<>();
        List<String> lineList = lines.toList();
        for (int i = 0; i < lineList.size(); i++) {
            Matcher matcher = annotationPattern.matcher(lineList.get(i));
            if (matcher.matches()) {
                int lineNumber = i + 1;
                String condition = matcher.group(IF_GROUP) != null
                        ? matcher.group(IF_CONDITION_GROUP)
                        : matcher.group(ELIF_CONDITION_GROUP);
                if (condition != null) {
                    annotationParser.parse(condition).ifPresent(expression -> expression.getVariableNames().stream()
                            .filter(name -> !features.contains(name))
                            .forEach(name -> problems.add(new ParseProblem(
                                    String.format("unknown feature \"%s\"", name), Severity.ERROR, lineNumber))));
                }
            }
        }
        return problems;
    }

    public List<String> extractAnnotations(Stream<String> lines) {
        return lines.filter(annotationPattern.asMatchPredicate()).collect(Collectors.toList());
    }
}