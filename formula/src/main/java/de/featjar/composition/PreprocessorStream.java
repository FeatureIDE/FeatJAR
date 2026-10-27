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
import de.featjar.base.data.CustomStream;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.format.RuntimeParseException;
import de.featjar.base.tree.Trees;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.ProblemFormula;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.value.Variable;
import de.featjar.formula.visitor.AndOrSimplifier;
import de.featjar.formula.visitor.EvaluationSimplifier;
import de.featjar.formula.visitor.ExpressionReplacer;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads a file or text line by line and processes preprocessor annotations.
 *
 * @author Sebastian Krieter
 * @author Mohammed Al-Saih
 * @author Mirza Adnan Baig
 * @author Malek Lahyami
 * @author Ines Zenkri
 */
public class PreprocessorStream extends CustomStream<de.featjar.composition.PreprocessorStream.Line> {

    public enum AnnotationType {
        ifType,
        elseType,
        elifType,
        endifType,
        coreType
    }

    public enum UndeterminedMode {
        /**
         * Keep annotations with undetermined conditions.
         */
        KEEP,
        /**
         * Remove annotations with undetermined conditions.
         */
        REMOVE,
        /**
         * Throw an {@link RuntimeParseException exception} if encountering an annotation with undetermined condition.
         */
        ERROR,
        /**
         * Keep annotations with undetermined conditions and add a warning to each.
         */
        WARNING,
        /**
         * Keep and simplify annotations with undetermined conditions.
         */
        SIMPLIIFY,
    }

    public static class Annotation {
        public Annotation parent;

        public AnnotationType type;

        public IFormula condition;

        public String prefix;
        public String keyword;
        public String suffix;

        public void reset() {
            parent = null;
            condition = null;
        }

        public Stream<Variable> getVariables() {
            return condition == null ? null : condition.getVariableStream();
        }

        public Stream<String> getVariableNames() {
            return condition == null ? null : condition.getVariableStream().map(Variable::getName);
        }

        public IFormula getPriorCondition() {
            Reference node = new Reference(getPriorCondition_rec());
            Trees.traverse(node, new EvaluationSimplifier());
            Trees.traverse(node, new AndOrSimplifier());
            return node.getExpression();
        }

        public IFormula getPresenceCondition() {
            Reference node = new Reference(getPresenceCondition_rec());
            Trees.traverse(node, new EvaluationSimplifier());
            Trees.traverse(node, new AndOrSimplifier());
            return node.getExpression();
        }

        private IFormula getPriorCondition_rec() {
            return switch (type) {
                case coreType -> True.INSTANCE;
                case elifType -> constructPriorCondition();
                case elseType -> constructPriorCondition();
                case endifType -> parent.getPresenceCondition_rec();
                case ifType -> parent.getPresenceCondition_rec();
                default -> throw new IllegalStateException(String.valueOf(type));
            };
        }

        private IFormula getPresenceCondition_rec() {
            return switch (type) {
                case coreType -> True.INSTANCE;
                case elifType -> new And(constructPriorCondition(), condition);
                case elseType -> constructPriorCondition();
                case endifType -> getParentIf().getPresenceCondition_rec();
                case ifType -> new And(parent.getPriorCondition_rec(), condition);
                default -> throw new IllegalStateException(String.valueOf(type));
            };
        }

        private Annotation getParentIf() {
            Annotation previousAnnotation = parent;
            while (previousAnnotation.type != AnnotationType.ifType) {
                previousAnnotation = previousAnnotation.parent;
            }
            return previousAnnotation;
        }

        private And constructPriorCondition() {
            List<IFormula> previousAnnotationConditions = new LinkedList<>();
            Annotation previousAnnotation = parent;
            previousAnnotationConditions.add(new Not(previousAnnotation.condition));
            while (previousAnnotation.type != AnnotationType.ifType) {
                previousAnnotation = previousAnnotation.parent;
                previousAnnotationConditions.addFirst(new Not(previousAnnotation.condition));
            }
            previousAnnotationConditions.addFirst(previousAnnotation.getPriorCondition_rec());
            return new And(previousAnnotationConditions);
        }
    }

    public static class Line {
        public final String content;
        public final int number;

        public boolean isAnnotation;
        public Annotation annotation;
        public List<ParseProblem> problems;

        public Line(String content, int number) {
            this.content = content;
            this.number = number;
        }

        public IFormula getPresenceCondition() {
            return annotation.getPresenceCondition();
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public Stream<? extends Problem> getProblems() {
            return problems == null ? null : problems.stream();
        }
    }

    private abstract static class LineIterator implements Spliterator<Line>, AutoCloseable {
        private static final int iterator_characteristics = Spliterator.NONNULL;

        public Spliterator<Line> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return iterator_characteristics;
        }

        @Override
        public abstract void close() throws IOException;
    }

    public static class FileLineIterator extends LineIterator {
        private final LineNumberReader lineNumberReader;

        public FileLineIterator(LineNumberReader lineNumberReader) {
            this.lineNumberReader = lineNumberReader;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Line> action) {
            try {
                String line = lineNumberReader.readLine();
                if (line != null) {
                    action.accept(new Line(line, lineNumberReader.getLineNumber()));
                    return true;
                }
            } catch (IOException e) {
                FeatJAR.log().error(e);
            }
            return false;
        }

        @Override
        public void close() throws IOException {
            lineNumberReader.close();
        }
    }

    private static class TextLineIterator extends LineIterator {
        private final Scanner scanner;
        private int lineNumber;

        public TextLineIterator(String text) {
            this.scanner = new Scanner(text);
        }

        @Override
        public boolean tryAdvance(Consumer<? super Line> action) {
            if (scanner.hasNextLine()) {
                action.accept(new Line(scanner.nextLine(), lineNumber++));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void close() throws IOException {
            scanner.close();
        }
    }

    private static class Serializer implements Function<Line, Line> {
        final ExpressionSerializer conditionSerializer;
        final Style style;

        public Serializer(Style style) {
            this.style = style;
            conditionSerializer = new ExpressionSerializer();
            conditionSerializer.setSymbols(style.getSymbols());
        }

        @Override
        public Line apply(Line line) {
            if (line.isAnnotation) {
                StringBuilder text = new StringBuilder();
                return new Line(
                        text //
                                .append(line.annotation.prefix) //
                                .append(line.annotation.keyword) //
                                .append(conditionText(line)) //
                                .append(line.annotation.suffix) //
                                .toString(),
                        line.number);
            } else {
                return new Line(line.content, line.number);
            }
        }

        private String conditionText(Line line) {
            IFormula condition = line.annotation.condition;
            if (condition != null) {
                return serializeCondition(condition);
            } else if (style.conditionAfterElseAndEndif) {
                Annotation previousAnnotation = line.annotation.parent;
                while (previousAnnotation != null && previousAnnotation.type != AnnotationType.ifType) {
                    previousAnnotation = previousAnnotation.parent;
                }
                if (previousAnnotation != null) {
                    return serializeCondition(previousAnnotation.condition);
                }
            }
            return "";
        }

        private String serializeCondition(IFormula condition) {
            return new StringBuilder() //
                    .append(style.conditionStart == null ? " " : style.conditionStart) //
                    .append(Trees.traverse(condition, conditionSerializer).get()) //
                    .append(style.conditionEnd == null ? "" : style.conditionEnd)
                    .toString();
        }
    }

    private static class Parser implements Function<Line, Line> {
        final Style style;
        final Pattern annotationPattern;
        final ExpressionParser conditionParser;
        final LinkedList<Annotation> annotationStack = new LinkedList<>();
        final Annotation root = new Annotation();

        public Parser(Style style) {
            this.style = style;
            annotationPattern = style.toPattern();
            conditionParser = new ExpressionParser();
            conditionParser.setSymbols(style.getSymbols());

            root.condition = True.INSTANCE;
            root.parent = null;
            root.type = AnnotationType.coreType;
            annotationStack.push(root);
        }

        @Override
        public Line apply(Line line) {
            line.problems = null;
            Matcher matcher = annotationPattern.matcher(line.content);
            return (matcher.matches()) ? handleAnnotation(line, matcher) : handleNonAnnotation(line);
        }

        private Line handleNonAnnotation(Line line) {
            line.isAnnotation = false;
            line.annotation = annotationStack.peek();
            return line;
        }

        private Line handleAnnotation(Line line, Matcher matcher) {
            line.isAnnotation = true;
            line.annotation = new Annotation();
            line.annotation.parent = annotationStack.peek();
            line.annotation.prefix = matcher.group(Style.ANNOTATION_START);
            line.annotation.keyword = matcher.group(Style.KEYWORD_GROUP);
            line.annotation.suffix = matcher.group(Style.REMAINDER_GROUP);

            List<ParseProblem> problems = new LinkedList<>();
            String annotation;
            if ((annotation = matcher.group(Style.IF_GROUP)) != null) {
                handleIf(line, matcher, problems);
            } else if ((annotation = matcher.group(Style.ENDIF_GROUP)) != null) {
                handleEndif(line, matcher, problems);
            } else if ((annotation = matcher.group(Style.ELSE_GROUP)) != null) {
                handleElse(line, matcher, problems);
            } else if ((annotation = matcher.group(Style.ELIF_GROUP)) != null) {
                handleElif(line, matcher, problems);
            } else if ((annotation = matcher.group(Style.UNKNOWN_KEYWORD_GROUP)) != null) {
                handleUnknown(line, matcher, annotation, problems);
            }
            line.problems = problems.isEmpty() ? null : problems;
            return line;
        }

        private void handleIf(Line line, Matcher matcher, List<ParseProblem> problems) {
            line.annotation.type = AnnotationType.ifType;
            line.annotation.condition = parseCondition(line, matcher, problems);

            String remainder = matcher.group(Style.REMAINDER_GROUP);
            if (remainder != null && !remainder.isBlank()) {
                problems.add(new ParseProblem(
                        "Unexpected remainder after condition: " + remainder, Problem.Severity.WARNING, line.number));
            }

            annotationStack.push(line.annotation);
        }

        private void handleEndif(Line line, Matcher matcher, List<ParseProblem> problems) {
            if (line.annotation.parent.type == AnnotationType.ifType
                    || line.annotation.parent.type == AnnotationType.elifType
                    || line.annotation.parent.type == AnnotationType.elseType) {
                Annotation parentIfAnnotation = annotationStack.pop();
                while (parentIfAnnotation.type != AnnotationType.ifType) {
                    if (parentIfAnnotation.type == AnnotationType.coreType) {
                        throw new IllegalStateException();
                    }
                    parentIfAnnotation = annotationStack.pop();
                }

                line.annotation.type = AnnotationType.endifType;
                line.annotation.condition = null;

                parseRemainder(line, matcher, problems, "endif");
            } else {
                handleInvalidStructure(line, problems, "Invalid annotation syntax: unmatched endif");
            }
        }

        private void handleElse(Line line, Matcher matcher, List<ParseProblem> problems) {
            if (line.annotation.parent.type == AnnotationType.ifType
                    || line.annotation.parent.type == AnnotationType.elifType) {
                line.annotation.type = AnnotationType.elseType;
                line.annotation.condition = null;

                parseRemainder(line, matcher, problems, "else");

                annotationStack.push(line.annotation);
            } else {
                handleInvalidStructure(line, problems, "Invalid annotation syntax: unmatched else");
            }
        }

        private void handleElif(Line line, Matcher matcher, List<ParseProblem> problems) {
            if (line.annotation.parent.type == AnnotationType.ifType
                    || line.annotation.parent.type == AnnotationType.elifType) {
                line.annotation.type = AnnotationType.elifType;
                line.annotation.condition = parseCondition(line, matcher, problems);

                String remainder = matcher.group(Style.REMAINDER_GROUP);
                if (remainder != null && !remainder.isBlank()) {
                    problems.add(new ParseProblem(
                            "Unexpected remainder after condition: " + remainder,
                            Problem.Severity.WARNING,
                            line.number));
                }

                annotationStack.push(line.annotation);
            } else {
                handleInvalidStructure(line, problems, "Invalid annotation syntax: unmatched elif");
            }
        }

        private void handleUnknown(Line line, Matcher matcher, String annotation, List<ParseProblem> problems) {
            String keyword = matcher.group(Style.KEYWORD_GROUP);
            handleInvalidStructure(
                    line,
                    problems, //
                    !keyword.isBlank() //
                            ? "Invalid annotation syntax: unexpected keyword: " + keyword //
                            : annotation.isBlank() //
                                    ? "Invalid annotation syntax: missing annotation" //
                                    : "Invalid annotation syntax: malformed annotation: " + annotation);
        }

        private void handleInvalidStructure(Line line, List<ParseProblem> problems, String message) {
            handleNonAnnotation(line);
            problems.add(new ParseProblem(message, Severity.ERROR, line.number));
        }

        private IFormula parseCondition(Line line, Matcher matcher, List<ParseProblem> problems) {
            String conditionText = matcher.group(Style.CONDITION_GROUP);

            if (conditionText == null || conditionText.isBlank()) {
                ParseProblem problem = new ParseProblem(
                        "Invalid annotation syntax: missing condition", Problem.Severity.ERROR, line.number);
                problems.add(problem);
                return new ProblemFormula(problem);
            }
            Result<IFormula> condition = conditionParser
                    .parse(conditionText)
                    .mapResult(expression -> (expression instanceof IFormula formula)
                            ? Result.of(formula)
                            : Result.empty(new ParseProblem(
                                    "Invalid annotation syntax: condition is not a boolean formula: " + conditionText,
                                    Problem.Severity.ERROR,
                                    line.number)));
            condition.getProblems().stream()
                    .forEach(p -> problems.add(new ParseProblem(p.getMessage(), p.getSeverity(), line.number)));
            if (condition.isEmpty()) {
                return new ProblemFormula(
                        Problem.getFirstError(condition.getProblems()).get());
            }
            return condition.get();
        }

        private void parseRemainder(Line line, Matcher matcher, List<ParseProblem> problems, String keyword) {
            String condition = matcher.group(Style.CONDITION_GROUP);
            if (condition != null && !condition.isBlank()) {
                if (!style.conditionAfterElseAndEndif) {
                    problems.add(new ParseProblem(
                            "Unexpected remainder after " + keyword + ": " + condition,
                            Problem.Severity.WARNING,
                            line.number));
                }
            } else {
                if (style.conditionAfterElseAndEndif) {
                    problems.add(new ParseProblem(
                            "Missing condition after " + keyword, Problem.Severity.WARNING, line.number));
                }
            }
            String remainder = matcher.group(Style.REMAINDER_GROUP);
            if (remainder != null && !remainder.isBlank()) {
                problems.add(new ParseProblem(
                        "Unexpected remainder after " + keyword + ": " + remainder,
                        Problem.Severity.WARNING,
                        line.number));
            }
        }
    }

    private final Style style;
    private final LineIterator iterator;

    private PreprocessorStream(Stream<Line> stream, LineIterator iterator, Style style) {
        super(stream);
        this.iterator = iterator;
        this.style = style;
    }

    private PreprocessorStream(LineIterator iterator, Style style) {
        this(StreamSupport.stream(iterator, false).map(new Parser(style)), iterator, style);
    }

    /**
     * Attempts to open the given file and constructs a stream of {@link Line lines} from it.
     * The stream needs to be closed by the caller.
     *
     * @param path the path to the file.
     * @param style the preprocessor style.
     * @throws IOException if the file cannot be read.
     */
    public static PreprocessorStream create(Path path, Style style) throws IOException {
        return new PreprocessorStream(new FileLineIterator(new LineNumberReader(Files.newBufferedReader(path))), style);
    }

    /**
     * Constructs a stream of {@link Line lines} from a given text.
     *
     * @param text the text.
     * @param style the preprocessor style.
     * @return A stream of lines from a text.
     */
    public static PreprocessorStream create(String text, Style style) {
        return new PreprocessorStream(new TextLineIterator(text), style);
    }

    private PreprocessorStream newStream(Stream<Line> stream) {
        return new PreprocessorStream(stream, iterator, style);
    }

    @Override
    public void close() {
        super.close();
        try {
            iterator.close();
        } catch (IOException e) {
            FeatJAR.log().error(e);
        }
    }

    /**
     * {@return a filtered stream that contains only lines that remain after preprocessing with the given variable assignment}
     *
     * <b>Note</b>: The return stream is <b>not state less</b>.
     * It is not suitable for parallel consumption.
     *
     * @param assignment the variable assignment
     * @param processType how to handle annotations with undetermined conditions
     */
    public PreprocessorStream process(Assignment assignment, UndeterminedMode processType) {
        return newStream((switch (processType) {
                    case ERROR ->
                        stream.map(line -> {
                            Optional<Object> evaluate =
                                    line.annotation.getPresenceCondition().evaluate(assignment);
                            if (evaluate.isEmpty()) {
                                throw new RuntimeParseException("Could not evaluate condition: ", line.number);
                            } else {
                                return evaluate.get() == Boolean.TRUE ? line : null;
                            }
                        });
                    case KEEP ->
                        stream.map(line -> {
                            Optional<Object> evaluate =
                                    line.annotation.getPresenceCondition().evaluate(assignment);
                            return evaluate.isEmpty() || evaluate.get() == Boolean.TRUE ? line : null;
                        });
                    case REMOVE ->
                        stream.map(line -> {
                            Optional<Object> evaluate =
                                    line.annotation.getPresenceCondition().evaluate(assignment);
                            return evaluate.isPresent() && evaluate.get() == Boolean.TRUE ? line : null;
                        });
                    case SIMPLIIFY ->
                        stream.map(line -> {
                                    Assignment combinedAssingment =
                                            line.annotation.getPriorCondition().getImpliedAssignment(assignment);
                                    Optional<Object> evaluate = line.annotation
                                            .getPresenceCondition()
                                            .evaluate(combinedAssingment);
                                    if (line.annotation.condition != null) {
                                        if (evaluate.isPresent()) {
                                            line.annotation.condition =
                                                    evaluate.get() == Boolean.TRUE ? True.INSTANCE : False.INSTANCE;
                                        } else {
                                            IFormula cloneTree = line.annotation.condition.cloneTree();
                                            Trees.traverse(
                                                    cloneTree,
                                                    ExpressionReplacer.insertKnownVariables(combinedAssingment));
                                            Trees.traverse(cloneTree, new EvaluationSimplifier());
                                            line.annotation.condition = cloneTree;
                                        }
                                    }
                                    if (evaluate.isPresent() && evaluate.get() == Boolean.FALSE) {
                                        return null;
                                    }
                                    return line;
                                })
                                .filter(Objects::nonNull);
                    case WARNING ->
                        stream.map(line -> {
                            Optional<Object> evaluate =
                                    line.annotation.getPresenceCondition().evaluate(assignment);
                            if (evaluate.isEmpty()) {
                                if (line.problems == null) {
                                    line.problems = new LinkedList<>();
                                }
                                line.problems.add(
                                        new ParseProblem("Could not evaluate line.", Severity.WARNING, line.number));
                                return line;
                            } else {
                                return evaluate.get() == Boolean.TRUE ? line : null;
                            }
                        });
                    default -> throw new IllegalStateException(String.valueOf(processType));
                })
                .filter(Objects::nonNull));
    }

    /**
     * {@return a stream with simplified annotations}
     * Shorthand for {@code process(new Assignment(), UndeterminedMode.SIMPLIIFY)}.
     * <br/>
     * <b>Note</b>: The return stream is <b>not</b> state less.
     * It is not suitable for parallel consumption.
     *
     * @param processType how to handle undetermined conditions
     */
    public PreprocessorStream simplify() {
        return process(new Assignment(), UndeterminedMode.SIMPLIIFY);
    }

    public PreprocessorStream renameVariables(Map<Variable, Variable> mapping) {
        return newStream(stream.peek(line -> {
                    if (line.isAnnotation) {
                        Trees.traverse(line.annotation.condition, ExpressionReplacer.substituteVariables(mapping));
                    }
                })
                .map(new Serializer(style))
                .map(new Parser(style)));
    }

    public PreprocessorStream keepAnnotations() {
        return newStream(stream.filter(line -> line.isAnnotation));
    }

    public PreprocessorStream keepNonAnnotations() {
        return newStream(stream.filter(line -> !line.isAnnotation));
    }

    public Stream<String> mapToString() {
        return stream.map(new Serializer(style)).map(line -> line.content);
    }

    public Stream<IFormula> mapToPresenceConditions() {
        return stream.map(line -> line.annotation.getPresenceCondition());
    }

    public Stream<String> mapToVariableNames() {
        return stream.filter(line -> line.isAnnotation)
                .map(line -> line.annotation.condition)
                .filter(Objects::nonNull)
                .flatMap(condition -> condition.getVariableStream())
                .distinct()
                .map(Variable::getName);
    }

    public Stream<ParseProblem> mapToProblems() {
        return stream.map(line -> line.problems).filter(Objects::nonNull).flatMap(List::stream);
    }
}
