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

import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.IInOrderTreeVisitor;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.ATerminalExpression;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.BiImplies;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Exists;
import de.featjar.formula.structure.connective.ForAll;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import java.util.List;

/**
 * Serializes expressions as readable text.
 *
 * @author Sebastian Krieter
 */
public class ExpressionSerializer implements IInOrderTreeVisitor<IExpression, String> {

    public static final Symbols STANDARD_SYMBOLS = ShortSymbols.INSTANCE;
    public static final Notation STANDARD_NOTATION = Notation.INFIX;
    public static final boolean STANDARD_ENFORCE_PARENTHESES = false;
    public static final boolean STANDARD_ENQUOTE_WHITESPACE = false;
    public static final boolean STANDARD_ENQUOTE_ALWAYS = false;
    public static final String STANDARD_TAB_STRING = "\t";
    public static final String STANDARD_NEW_LINE = System.lineSeparator();

    /**
     * The type of notation of the formula.
     *
     * @author Timo Günther
     */
    public enum Notation {
        /**
         * <p>
         * The infix notation. Operators are written between operands where possible.
         *
         * <p>
         * Examples:
         * <ul>
         * <li><em>A &amp; B &amp; C</em></li>
         * <li><em>A =&gt; B &lt;=&gt; -A | B</em></li>
         * <li><em>atleast2(A, B, C, D) &amp; atmost3(A, B, C, D)</em></li>
         * </ul>
         */
        INFIX,
        /**
         * <p>
         * The prefix notation. Operators are written before the operands.
         *
         * <p>
         * Examples:
         * <ul>
         * <li><em>(&amp; A B C)</em></li>
         * <li><em>(&lt;=&gt; (=&gt; A B) (| (- A) B)</em></li>
         * <li><em>(&amp; (atleast2 A B C D) (atmost3 A B C D))</em></li>
         * </ul>
         */
        PREFIX,
        /**
         * <p>
         * The postfix notation. Operators are written after the operands.
         *
         * <p>
         * Examples:
         * <ul>
         * <li><em>(A B C &amp;)</em></li>
         * <li><em>((A B =&gt;) ((A -) B |) &lt;=&gt;)</em></li>
         * <li><em>((A B C D atleast2) (A B C D atmost3) &gt;)</em></li>
         * </ul>
         */
        POSTFIX,
        /**
         * The tree notation. Operators and operands are written in pre-order.
         * Each operator and operand is written in a single line with indentation corresponding to its nesting depth.
         */
        TREE,
    }

    /**
     * The symbols for the operations.
     *
     * @see #setSymbols(Symbols)
     */
    private Symbols symbols = STANDARD_SYMBOLS;
    /** The notation to use. */
    private Notation notation = STANDARD_NOTATION;
    /**
     * If true, this writer will always place brackets, even if they are
     * semantically irrelevant.
     */
    private boolean enforceParentheses = STANDARD_ENFORCE_PARENTHESES;
    /** If true, this writer will enquote variables if they contain whitespace. */
    private boolean enquoteWhitespace = STANDARD_ENQUOTE_WHITESPACE;
    /** If true, this writer will enquote all variables. */
    private boolean enquoteAlways = STANDARD_ENQUOTE_ALWAYS;

    private String tab = STANDARD_TAB_STRING;
    private String newLine = STANDARD_NEW_LINE;

    /**
     * Sets the symbols to use for the operations. These are:
     * <ul>
     * <li>{@link Not}</li>
     * <li>{@link And}</li>
     * <li>{@link Or}</li>
     * <li>{@link Implies}</li>
     * <li>{@link BiImplies}</li>
     * <li>{@link Choose}</li>
     * <li>{@link AtLeast}</li>
     * <li>{@link AtMost}</li>
     * <li>{@link Between}</li>
     * <li>{@link ForAll}</li>
     * <li>{@link Exists}</li>
     * </ul>
     * By default, the set of short symbols is used.
     *
     * @param symbols symbols for the operations; not null
     * @see LogicalSymbols
     * @see TextualSymbols
     * @see ShortSymbols
     * @see JavaSymbols
     */
    public void setSymbols(Symbols symbols) {
        this.symbols = symbols;
    }

    /**
     * {@return the symbols to use for the operations.}
     */
    protected Symbols getSymbols() {
        return symbols;
    }

    /**
     * Sets the notation to use. By default, this is the {@link Notation#INFIX
     * infix} notation.
     *
     * @param notation notation to use
     */
    public void setNotation(Notation notation) {
        this.notation = notation;
    }

    /**
     * {@return the notation to use.}
     */
    protected Notation getNotation() {
        return notation;
    }

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public String getNewLine() {
        return newLine;
    }

    public void setNewLine(String newLine) {
        this.newLine = newLine;
    }

    /**
     * Sets the enforcing brackets flag. If {@code true}, this writer will always
     * place brackets, even if they are semantically irrelevant.
     *
     * @param enforceParentheses if {@code true} the writer will use parentheses for
     *                        every sub-expression.
     */
    public void setEnforceParentheses(boolean enforceParentheses) {
        this.enforceParentheses = enforceParentheses;
    }

    /**
     * {@return the enforcing parentheses flag.}
     */
    protected boolean isEnforceParentheses() {
        return enforceParentheses;
    }

    /**
     * Sets the enquoting whitespace flag. If {@code true}, this writer will enquote
     * variables if they contain whitespace.
     *
     * @param enquoteWhitespace if {@code true} the writer will enquote all variable
     *                          names with white spaces in them.
     */
    public void setEnquoteWhitespace(boolean enquoteWhitespace) {
        this.enquoteWhitespace = enquoteWhitespace;
    }

    /**
     * Returns the enquoting whitespace flag.
     *
     * @return the enquoting whitespace flag
     */
    protected boolean isEnquoteWhitespace() {
        return enquoteWhitespace;
    }

    /**
     * Sets the enquoting always flag. If {@code true}, this writer will enquote
     * all variables.
     *
     * @param enquoteAlways if {@code true} the writer will enquote all variable
     *                      names.
     */
    public void setEnquoteAlways(boolean enquoteAlways) {
        this.enquoteAlways = enquoteAlways;
    }

    /**
     * Returns the enquoting always flag.
     *
     * @return the enquoting always flag
     */
    protected boolean isEnquoteAlways() {
        return enquoteAlways;
    }

    private StringBuilder sb = new StringBuilder();

    @Override
    public TraversalAction firstVisit(List<IExpression> path) {
        final IExpression node = ITreeVisitor.getCurrentNode(path);
        if (node instanceof ATerminalExpression) {
            if (notation == Notation.TREE) {
                alignLine(path.size());
            }
            sb.append(variableToString(node));
        } else if (node instanceof Literal) {
            printLiteral(node);
        } else if (!(node instanceof Reference)) {
            switch (notation) {
                case TREE:
                    alignLine(path.size());
                    sb.append(symbols.getSymbol(node));
                    break;
                case PREFIX:
                    sb.append(symbols.getSymbol(node));
                    sb.append('(');
                    break;
                case INFIX:
                    if (!isInfix(node)) {
                        sb.append(symbols.getSymbol(node));
                        if (needsParentheses(path, node)) {
                            sb.append('(');
                        } else if (symbols.isTextual()) {
                            sb.append(' ');
                        }
                    } else {
                        if (needsParentheses(path, node)) {
                            sb.append('(');
                        }
                    }
                    break;
                case POSTFIX:
                    if (needsParentheses(path, node)) {
                        sb.append('(');
                    }
                    break;
                default:
                    break;
            }
        }
        return TraversalAction.CONTINUE;
    }

    @Override
    public TraversalAction visit(List<IExpression> path) {
        final IExpression node = ITreeVisitor.getCurrentNode(path);
        if (!(node instanceof Literal) && !(node instanceof Reference)) {
            switch (notation) {
                case TREE:
                    break;
                case PREFIX:
                case POSTFIX:
                    sb.append(' ');
                    break;
                case INFIX:
                    sb.append(' ');
                    if (isInfix(node)) {
                        sb.append(symbols.getSymbol(node));
                        sb.append(' ');
                    }
                    break;
                default:
                    break;
            }
        }
        return TraversalAction.CONTINUE;
    }

    @Override
    public TraversalAction lastVisit(List<IExpression> path) {
        final IExpression node = ITreeVisitor.getCurrentNode(path);
        if (!(node instanceof ATerminalExpression) && !(node instanceof Literal) && !(node instanceof Reference)) {
            switch (notation) {
                case TREE:
                    break;
                case PREFIX:
                    sb.append(')');
                    break;
                case INFIX:
                    if (needsParentheses(path, node)) {
                        sb.append(')');
                    }
                    break;
                case POSTFIX:
                    if (needsParentheses(path, node)) {
                        sb.append(')');
                    } else if (symbols.isTextual()) {
                        sb.append(' ');
                    }
                    sb.append(symbols.getSymbol(node));
                    break;
                default:
                    break;
            }
        }
        return TraversalAction.CONTINUE;
    }

    private boolean isInfix(final IExpression node) {
        return symbols.getInfix(node).orElse(false) && node.getChildrenCount() > 1;
    }

    private void printLiteral(final IExpression node) {
        if (!((Literal) node).isPositive()) {
            Result<String> notString = symbols.getSymbolResult(Not.class);
            sb.append(notString.get());
            if (symbols.isTextual()) {
                sb.append(' ');
            }
        }
    }

    private boolean needsParentheses(List<IExpression> path, final IExpression node) {
        return enforceParentheses
                || symbols.getPriority(node).orElse(-1)
                        <= ITreeVisitor.getParentNode(path)
                                .mapResult(symbols::getPriority)
                                .orElse(-1);
    }

    @Override
    public void reset() {
        sb = new StringBuilder();
    }

    @Override
    public Result<String> getResult() {
        return Result.of(sb.toString());
    }

    /**
     * Converts a variable into the specified textual representation.
     *
     * @param variable a variable to convert; not null
     * @return the textual representation; not null
     */
    private String variableToString(IExpression variable) {
        final String name = variable.getName();
        return enquoteAlways || (enquoteWhitespace && (containsWhitespace(name) || equalsSymbol(name)))
                ? symbols.quoteStart() + name + symbols.quoteEnd()
                : name;
    }

    private void alignLine(int depth) {
        if (depth > 1) {
            sb.append(newLine);
            sb.append(tab.repeat(depth));
        }
    }

    /**
     * Returns true iff the given string equals one of the symbols.
     *
     * @param s string potentially equaling a symbol; not null
     * @return whether the string equals one of the symbols
     */
    private boolean equalsSymbol(String s) {
        return getSymbols().parseSymbol(s).isPresent();
    }

    /**
     * Returns true iff the given string contains a whitespace character.
     *
     * @param s string potentially containing whitespace; not null
     * @return whether the string contains whitespace
     */
    private static boolean containsWhitespace(String s) {
        return s.matches(".*?\\s+.*");
    }
}
