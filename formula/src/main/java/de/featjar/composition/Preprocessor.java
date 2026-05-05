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
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.io.textual.Symbols;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.term.value.Variable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Preprocessor {

    private final ExpressionParser annotationParser;

    private final Pattern annotationPattern;
    private final Pattern startAnnotationPattern;

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
                if (matcher.group(2) != null) {
                    if (expressionStack.isEmpty()) {
                        FeatJAR.log().warning("Line %d: no annotation to end", lineNumber);
                    } else {
                        expressionStack.pop();
                        evaluationStack.pop();
                    }
                    return false;
                } else if (matcher.group(3) != null) {
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
                } else if (matcher.group(6) != null) {
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
                    Result<IExpression> parse = annotationParser.parse(matcher.group(7));
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
                } else if (matcher.group(4) != null) {
                    Result<IExpression> parse = annotationParser.parse(matcher.group(5));
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
            Matcher matcher = startAnnotationPattern.matcher(line);
            if (matcher.matches()) {
                Result<IExpression> parse = annotationParser.parse(matcher.group(2));
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

    public Preprocessor(String annotationPrefix, Symbols symbols) {
        annotationParser = new ExpressionParser();
        annotationParser.setSymbols(symbols);
        String prefix = Pattern.quote(annotationPrefix);
        annotationPattern = Pattern.compile(prefix + "\\s*((endif\\s*)|(else\\s*)|(if\\s+(.+))|(elif\\s+(.+)))");

        startAnnotationPattern = Pattern.compile(prefix + "\\s*(if|elif)\\s+(.+)");
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

    public List<String> extractVariableNames(Stream<String> lines) {
        return lines.flatMap(new VariableNames())
                .distinct()
                .map(Variable::getName)
                .collect(Collectors.toList());
    }

    public List<String> extractAnnotations(Stream<String> lines) {
        return lines.filter(annotationPattern.asMatchPredicate()).collect(Collectors.toList());
    }
}
