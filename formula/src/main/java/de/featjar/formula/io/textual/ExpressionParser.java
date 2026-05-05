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
package de.featjar.formula.io.textual;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.ProblemFormula;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses expressions.
 * Currently only supports a subset of expressions involving {@link And}, {@link Or}, {@link Not}, {@link Implies},
 * and {@link BiImplies}.
 * TODO: write new parser for all expression types
 *
 * @author Dariusz Krolikowski
 * @author David Broneske
 * @author Fabian Benduhn
 * @author Thomas Thüm
 * @author Florian Proksch
 * @author Stefan Krueger
 * @author Sebastian Krieter
 * @deprecated does not work reliably at the moment
 */
@Deprecated
public class ExpressionParser {

    private static final char SPACE = ' ';
    private static final char QUOTE = '\"';
    private static final char PARENTHESIS_OPEN = '(';
    private static final char PARENTHESIS_CLOSE = ')';

    public enum ErrorMessage {
        INVALID_FEATURE_NAME("'%s' is no valid feature name."), //
        NULL_CONSTRAINT("Constraint is null."), //
        EMPTY_CONSTRAINT("Constraint is empty."),
        PARENTHESES_IN_FEATURE_NAMES("Parenthesis are not allowed in feature names."),
        INVALID_CLOSING_PARENTHESES("To many closing parentheses."),
        INVALID_NUMBER_OF_QUOTATION_MARKS("Invalid number of quotation marks."),
        INVALID_OPENING_PARENTHESES("There are unclosed opening parentheses."),
        EMPTY_EXPRESSION("Sub expression is empty."),
        MISSING_NAME("Missing feature name or expression: %s"),
        MISSING_NAME_LEFT("Missing feature name or expression on left side: %s"),
        MISSING_NAME_RIGHT("Missing feature name or expression on right side: %s"),
        MISSING_OPERATOR("Missing operator: %s");

        private final String message;

        ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private void throwParsingError(ErrorMessage message, int offset, Object... context) throws ParseException {
        throw new ParseException(String.format(message.getMessage(), context), offset);
    }

    public enum ErrorHandling {
        THROW,
        REMOVE,
        KEEP
    }

    private static final String featureNameMarker = "#";
    private static final String subExpressionMarker = "$";
    private static final String replacedFeatureNameMarker = featureNameMarker + "_";
    private static final String replacedSubExpressionMarker = subExpressionMarker + "_";

    private static final Pattern subExpressionPattern = Pattern.compile(Pattern.quote(subExpressionMarker) + "\\d+");
    private static final Pattern featureNamePattern = Pattern.compile(Pattern.quote(featureNameMarker) + "\\d+");

    private static final Pattern parenthesisPattern = Pattern.compile("\\(([^()]*)\\)");
    private static final Pattern quotePattern = Pattern.compile("\"(.*?)\"");

    private static final String symbolPatternString = "\\s*(%s)\\s*";

    private Symbols symbols = ShortSymbols.INSTANCE;
    private List<String> symbolList = symbols.getSortedSymbols();

    private ErrorHandling ignoreMissingFeatures = ErrorHandling.THROW;
    private ErrorHandling ignoreUnparseableSubExpressions = ErrorHandling.THROW;
    private List<Problem> problemList;

    public Symbols getSymbols() {
        return symbols;
    }

    public void setSymbols(Symbols symbols) {
        this.symbols = symbols;
        symbolList = symbols.getSortedSymbols();
    }

    public ErrorHandling ignoresMissingFeatures() {
        return ignoreMissingFeatures;
    }

    public void setIgnoreMissingFeatures(ErrorHandling ignoreMissingFeatures) {
        this.ignoreMissingFeatures = Objects.requireNonNull(ignoreMissingFeatures);
    }

    public ErrorHandling isIgnoreUnparseableSubExpressions() {
        return ignoreUnparseableSubExpressions;
    }

    public void setIgnoreUnparseableSubExpressions(ErrorHandling ignoreUnparseableSubExpressions) {
        this.ignoreUnparseableSubExpressions = Objects.requireNonNull(ignoreUnparseableSubExpressions);
    }

    public Result<IExpression> parse(String formulaString) {
        problemList = new ArrayList<>();
        if (formulaString == null) {
            return Result.empty(new ParseProblem(new ParseException(ErrorMessage.NULL_CONSTRAINT.getMessage(), 0), 0));
        }
        try {
            return Result.of(parseNode(formulaString), problemList);
        } catch (final ParseException e) {
            problemList.add(new ParseProblem(e, 0));
            switch (ignoreUnparseableSubExpressions) {
                case KEEP:
                    return Result.of(new ProblemFormula(new Problem(formulaString, Severity.ERROR)));
                case REMOVE:
                case THROW:
                    return Result.empty(problemList);
                default:
                    throw new IllegalStateException(String.valueOf(ignoreUnparseableSubExpressions));
            }
        }
    }

    private IExpression checkExpression(String source, List<String> quotedVariables, List<String> subExpressions)
            throws ParseException {
        if (source.isEmpty()) {
            return handleInvalidExpression(ErrorMessage.EMPTY_EXPRESSION, source);
        }
        source = SPACE + source + SPACE;
        // traverse all symbols
        for (final String symbol : symbolList) {
            final String symbolPattern = String.format(symbolPatternString, Pattern.quote(symbol));
            final Matcher matcher = Pattern.compile(symbolPattern).matcher(source);
            if (matcher.find()) {
                // 1st symbol occurrence
                final int index = matcher.start(1);

                // recursion for children nodes

                final List<IExpression> children = new ArrayList<>(2);
                String substring = source.substring(index + symbol.length());
                final Result<Class<? extends IExpression>> operator = symbols.parseSymbol(symbol);
                if (operator.valueEquals(Not.class)) {
                    final String rightSide = substring.trim();
                    IExpression subExpression;
                    if (rightSide.isEmpty()) {
                        subExpression = handleInvalidExpression(ErrorMessage.MISSING_NAME, source);
                    } else {
                        subExpression = checkExpression(rightSide, quotedVariables, subExpressions);
                    }
                    if (subExpression == null) {
                        return null;
                    }
                    children.add(subExpression);
                } else {
                    IExpression expression1, expression2;
                    final String leftSide = source.substring(0, index).trim();
                    if (leftSide.isEmpty()) {
                        expression1 = handleInvalidExpression(ErrorMessage.MISSING_NAME_LEFT, source);
                    } else {
                        expression1 = checkExpression(leftSide, quotedVariables, subExpressions);
                    }
                    if (expression1 == null) {
                        return null;
                    }
                    final String rightSide = substring.trim();
                    if (rightSide.isEmpty()) {
                        expression2 = handleInvalidExpression(ErrorMessage.MISSING_NAME_RIGHT, source);
                    } else {
                        expression2 = checkExpression(rightSide, quotedVariables, subExpressions);
                    }
                    if (expression2 == null) {
                        return null;
                    }
                    children.add(expression1);
                    children.add(expression2);
                }

                try {
                    return operator.orElseThrow().getConstructor(List.class).newInstance(children);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        source = source.trim();
        final Matcher subExpressionMatcher = subExpressionPattern.matcher(source);
        if (subExpressionMatcher.find()) {
            if ((subExpressionMatcher.start() == 0) && (subExpressionMatcher.end() == source.length())) {
                return checkExpression(
                        subExpressions
                                .get(Integer.parseInt(source.substring(1)))
                                .trim(),
                        quotedVariables,
                        subExpressions);
            } else {
                return handleInvalidExpression(ErrorMessage.MISSING_OPERATOR, source);
            }
        } else {
            String featureName;
            final Matcher featureNameMatcher = featureNamePattern.matcher(source);
            if (featureNameMatcher.find()) {
                if ((featureNameMatcher.start() == 0) && (featureNameMatcher.end() == source.length())) {
                    featureName = quotedVariables.get(Integer.parseInt(source.substring(1)));
                } else {
                    return handleInvalidExpression(ErrorMessage.MISSING_OPERATOR, source);
                }
            } else {
                if (source.contains(String.valueOf(SPACE))) {
                    return handleInvalidFeatureName(source);
                }
                featureName = source;
            }
            featureName = featureName
                    .replace(replacedFeatureNameMarker, featureNameMarker)
                    .replace(replacedSubExpressionMarker, subExpressionMarker);
            return new Literal(featureName);
        }
    }

    private IExpression handleInvalidFeatureName(String featureName) throws ParseException {
        return getInvalidLiteral(ErrorMessage.INVALID_FEATURE_NAME, ignoreMissingFeatures, featureName);
    }

    private IExpression handleInvalidExpression(ErrorMessage message, String constraint) throws ParseException {
        return getInvalidLiteral(message, ignoreUnparseableSubExpressions, constraint);
    }

    private IExpression getInvalidLiteral(ErrorMessage message, ErrorHandling handleError, String element)
            throws ParseException {
        switch (handleError) {
            case KEEP:
                problemList.add(new ParseProblem(message.getMessage(), Severity.WARNING, 0));
                return new ProblemFormula(new Problem(message.getMessage(), Severity.ERROR));
            case REMOVE:
                problemList.add(new ParseProblem(message.getMessage(), Severity.WARNING, 0));
                return null;
            case THROW:
            default:
                throwParsingError(message, 0, element);
                return null;
        }
    }

    private IExpression parseNode(String constraint) throws ParseException {
        constraint = constraint.trim();
        if (constraint.isEmpty()) {
            throwParsingError(ErrorMessage.EMPTY_CONSTRAINT, 0);
        }

        int parenthesisCounter = 0;
        boolean quoteSign = false;
        for (int i = 0; i < constraint.length(); i++) {
            final char curChar = constraint.charAt(i);
            switch (curChar) {
                case PARENTHESIS_OPEN:
                    if (quoteSign) {
                        throwParsingError(ErrorMessage.PARENTHESES_IN_FEATURE_NAMES, i);
                    }
                    parenthesisCounter++;
                    break;
                case QUOTE:
                    quoteSign = !quoteSign;
                    break;
                case PARENTHESIS_CLOSE:
                    if (quoteSign) {
                        throwParsingError(ErrorMessage.PARENTHESES_IN_FEATURE_NAMES, i);
                    }
                    if (--parenthesisCounter < 0) {
                        throwParsingError(ErrorMessage.INVALID_CLOSING_PARENTHESES, i);
                    }
                    break;
                default:
                    break;
            }
        }
        if (quoteSign) {
            throwParsingError(ErrorMessage.INVALID_NUMBER_OF_QUOTATION_MARKS, 0);
        }
        if (parenthesisCounter > 0) {
            throwParsingError(ErrorMessage.INVALID_OPENING_PARENTHESES, 0);
        }

        constraint = constraint.replace(featureNameMarker, replacedFeatureNameMarker);
        constraint = constraint.replace(subExpressionMarker, replacedSubExpressionMarker);

        final ArrayList<String> quotedFeatureNames = new ArrayList<>();
        final ArrayList<String> subExpressions = new ArrayList<>();
        if (constraint.contains(String.valueOf(QUOTE))) {
            constraint = replaceGroup(constraint, featureNameMarker, quotedFeatureNames, quotePattern);
        }
        while (constraint.contains(String.valueOf(PARENTHESIS_OPEN))) {
            constraint = replaceGroup(constraint, subExpressionMarker, subExpressions, parenthesisPattern);
        }

        return checkExpression(constraint, quotedFeatureNames, subExpressions);
    }

    private String replaceGroup(String constraint, String marker, final List<String> groupList, final Pattern pattern) {
        int counter = groupList.size();

        final ArrayList<Integer> positionList = new ArrayList<>();
        final Matcher matcher = pattern.matcher(constraint);
        positionList.add(0);
        while (matcher.find()) {
            groupList.add(matcher.group(1));
            positionList.add(matcher.start());
            positionList.add(matcher.end());
        }
        positionList.add(constraint.length());

        final StringBuilder sb = new StringBuilder(constraint.substring(positionList.get(0), positionList.get(1)));
        for (int i = 2; i < positionList.size(); i += 2) {
            sb.append(marker);
            sb.append(counter++);
            sb.append(constraint, positionList.get(i), positionList.get(i + 1));
        }
        return sb.toString();
    }
}
