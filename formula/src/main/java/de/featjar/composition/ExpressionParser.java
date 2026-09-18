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

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseException;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.formula.io.textual.ShortSymbols;
import de.featjar.formula.io.textual.Symbols;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Constant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses expressions.
 *
 * @author Sebastian Krieter
 */
public class ExpressionParser {
    private enum TokenClass {
        OPERATOR,
        QUOTED_IDENTIFIER,
        IDENTIFIER,
        NUMBER,
        OPEN,
        CLOSE,
        EXPRESSION
    }

    private static class Token {
        private final TokenClass type;
        private final Object value;
        private final int lineNumber;
        private final int position;

        public Token(TokenClass type, Object value, int lineNumber, int position) {
            this.type = type;
            this.value = value;
            this.lineNumber = lineNumber;
            this.position = position;
        }

        @Override
        public String toString() {
            return type + ": " + value;
        }
    }

    private static final char QUOTE = '\"';

    private static final char PARENTHESIS_OPEN = '(';
    private static final char PARENTHESIS_CLOSE = ')';

    private Symbols symbols = ShortSymbols.INSTANCE;

    private List<Problem> problemList;

    public Symbols getSymbols() {
        return symbols;
    }

    public void setSymbols(Symbols symbols) {
        this.symbols = symbols;
    }

    public Result<IExpression> parse(String formulaString) {
        problemList = new ArrayList<>();
        if (formulaString == null) {
            return Result.empty(new ParseProblem(new ParseException("empty string", 0), 0));
        }
        try {
            return Result.of(parseExpression(formulaString), problemList);
        } catch (final ParseException e) {
            problemList.add(new ParseProblem(e, 0));
            return Result.empty(problemList);
        }
    }

    private IExpression parseExpression(String formulaString) throws ParseException {
        LinkedList<LinkedList<Token>> stack = new LinkedList<>();
        stack.push(new LinkedList<>());
        LinkedList<Token> openTokens = new LinkedList<>();

        for (Token currentToken : tokenize(formulaString)) {
            switch (currentToken.type) {
                case CLOSE:
                    if (openTokens.isEmpty()) {
                        throw new ParseException(
                                "Closing parenthesis has no match.", currentToken.lineNumber, currentToken.position);
                    }
                    Token open = openTokens.pop();
                    Token subExpression = new Token(
                            TokenClass.EXPRESSION,
                            parseSubExpression(stack.pop(), open.position),
                            open.lineNumber,
                            open.position);
                    LinkedList<Token> l = stack.peek();
                    l.add(subExpression);
                    break;
                case OPEN:
                    openTokens.push(currentToken);
                    stack.push(new LinkedList<>());
                    break;
                default:
                    stack.peek().add(currentToken);
                    break;
            }
        }
        if (!openTokens.isEmpty()) {
            Token open = openTokens.peek();
            throw new ParseException("Open parenthesis has no match.", open.lineNumber, open.position);
        }
        return parseSubExpression(stack.pop(), 0);
    }

    private IExpression parseSubExpression(List<Token> tokenList, int position) throws ParseException {
        IExpression expression = parseSubExpression(tokenList.listIterator());
        if (expression == null) {
            throw new ParseException("Missing condition.", 1, position);
        }
        return expression;
    }

    @SuppressWarnings("unchecked")
    private IExpression parseSubExpression(ListIterator<Token> iterator) throws ParseException {
        IExpression expression = null;
        while (iterator.hasNext()) {
            Token token = iterator.next();
            if (token.type != TokenClass.OPERATOR || isUnaryOperator(token)) {
                if (expression != null) {
                    throw new ParseException(
                            String.format("Unexpected '%s', expected an operator.", text(token)),
                            token.lineNumber,
                            token.position);
                }
                expression = parseOperand(token, iterator);
                continue;
            }
            Class<? extends IExpression> value = (Class<? extends IExpression>) token.value;
            if (!(expression instanceof IFormula) || !iterator.hasNext()) {
                throw missingOperand(token);
            }
            IExpression right = parseSubExpression(iterator);
            if (!(right instanceof IFormula)) {
                throw missingOperand(token);
            }
            if (value == And.class) {
                expression = new And((IFormula) expression, (IFormula) right);
            } else if (value == Or.class) {
                expression = new Or((IFormula) expression, (IFormula) right);
            } else if (value == Implies.class) {
                expression = new Implies((IFormula) expression, (IFormula) right);
            } else if (value == BiImplies.class) {
                expression = new BiImplies((IFormula) expression, (IFormula) right);
            } else {
                throw new ParseException(
                        String.format("Unsupported operator '%s'.", text(token)), token.lineNumber, token.position);
            }
        }
        return expression;
    }

    private IExpression parseOperand(Token token, ListIterator<Token> iterator) throws ParseException {
        switch (token.type) {
            case QUOTED_IDENTIFIER:
            case IDENTIFIER:
                return new Literal((String) token.value);
            case NUMBER:
                return new Constant(token.value);
            case EXPRESSION:
                return (IExpression) token.value;
            default:
                if (isUnaryOperator(token) && iterator.hasNext()) {
                    IExpression operand = parseOperand(iterator.next(), iterator);
                    if (operand instanceof IFormula) {
                        return token.value == Not.class ? new Not((IFormula) operand) : operand;
                    }
                }
                throw missingOperand(token);
        }
    }

    private boolean isUnaryOperator(Token token) {
        return token.value == Not.class || token.value == Literal.class;
    }

    private ParseException missingOperand(Token operator) {
        return new ParseException(
                String.format("Missing operand for '%s'.", text(operator)), operator.lineNumber, operator.position);
    }

    @SuppressWarnings("unchecked")
    private String text(Token token) {
        switch (token.type) {
            case OPERATOR:
                return symbols.getSymbolResult((Class<? extends IExpression>) token.value)
                        .orElse(String.valueOf(token.value));
            case EXPRESSION:
                return String.valueOf(PARENTHESIS_OPEN);
            default:
                return String.valueOf(token.value);
        }
    }

    private List<Token> tokenize(String expression) {
        final ArrayList<Token> tokens = new ArrayList<>();
        final StringBuilder tokenBuilder = new StringBuilder();
        boolean quoted = false;
        int lineNumber = 1;

        final char[] chars = expression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            final char curChar = chars[i];
            if (quoted) {
                switch (curChar) {
                    case QUOTE:
                        if (tokenBuilder.length() != 0) {
                            tokens.add(new Token(TokenClass.QUOTED_IDENTIFIER, tokenBuilder.toString(), lineNumber, i));
                            tokenBuilder.delete(0, tokenBuilder.length());
                        }
                        quoted = !quoted;
                        break;
                    default:
                        tokenBuilder.append(curChar);
                }
            }
            switch (curChar) {
                case QUOTE:
                    addToken(tokens, tokenBuilder, lineNumber, i);
                    quoted = !quoted;
                    break;
                case '\n':
                    lineNumber++;
                case ' ':
                case '\t':
                case '\r':
                    addToken(tokens, tokenBuilder, lineNumber, i);
                    break;
                case PARENTHESIS_OPEN:
                    addToken(tokens, tokenBuilder, lineNumber, i);
                    tokens.add(new Token(TokenClass.OPEN, String.valueOf(PARENTHESIS_OPEN), lineNumber, i));
                    break;
                case PARENTHESIS_CLOSE:
                    addToken(tokens, tokenBuilder, lineNumber, i);
                    tokens.add(new Token(TokenClass.CLOSE, String.valueOf(PARENTHESIS_CLOSE), lineNumber, i));
                    break;
                default:
                    tokenBuilder.append(curChar);
            }
        }
        addToken(tokens, tokenBuilder, lineNumber, chars.length);
        return tokens;
    }

    private void addToken(final List<Token> tokens, final StringBuilder tokenBuilder, int lineNumber, int position) {
        if (tokenBuilder.length() != 0) {
            String tokenString = tokenBuilder.toString();
            tokenBuilder.delete(0, tokenBuilder.length());
            int startPosition = position - tokenString.length();

            if (Pattern.compile("(-)?[0-9]+").matcher(tokenString).matches()) {
                tokens.add(new Token(TokenClass.NUMBER, Integer.parseInt(tokenString), lineNumber, startPosition));
            } else {
                Matcher matcher = Pattern.compile("\\w+").matcher(tokenString);
                int startIndex = 0;
                while (matcher.find()) {
                    createToken(
                            tokens,
                            lineNumber,
                            startPosition + startIndex,
                            tokenString.substring(startIndex, matcher.start()));
                    createToken(tokens, lineNumber, startPosition + matcher.start(), matcher.group());
                    startIndex = matcher.end();
                }
                createToken(tokens, lineNumber, startPosition + startIndex, tokenString.substring(startIndex));
            }
        }
    }

    private void createToken(final List<Token> tokens, int lineNumber, int position, String tokenString) {
        if (tokenString.isEmpty()) {
            return;
        }
        Result<Class<? extends IExpression>> parseSymbol = symbols.parseSymbol(tokenString);
        if (parseSymbol.isPresent()) {
            tokens.add(new Token(TokenClass.OPERATOR, parseSymbol.get(), lineNumber, position));
        } else {
            tokens.add(new Token(TokenClass.IDENTIFIER, tokenString, lineNumber, position));
        }
    }
}
