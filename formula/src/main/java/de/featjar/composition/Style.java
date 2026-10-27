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
import de.featjar.formula.io.textual.JavaSymbols;
import de.featjar.formula.io.textual.Symbols;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Describes the syntax of annotations, i.e., their prefix and suffix, their keywords, how conditions are delimited,
 * and the symbols used within conditions.
 * All strings are matched literally, including whitespace within them.
 * Presets for common preprocessors are available as {@link #CPP}, {@link #ANTENNA}, and {@link #MUNGE}.
 */
public class Style {

    protected static final String ANNOTATION_START = "annotation";
    protected static final String KEYWORD_GROUP = "keyword";
    protected static final String IF_GROUP = "if";
    protected static final String ELIF_GROUP = "elif";
    protected static final String ELSE_GROUP = "else";
    protected static final String ENDIF_GROUP = "endif";
    protected static final String UNKNOWN_KEYWORD_GROUP = "unknown";

    protected static final String CONDITION_GROUP = "condition";
    protected static final String REMAINDER_GROUP = "remainder";

    private static final String ANYTHING = ".*";
    private static final String SPACE = "\\s*";

    private static final String NON_GROUP_START = "(?:";
    private static final String NAMED_GROUP_START = "(?<";
    private static final String NAMED_GROUP_MIDDLE = ">";
    private static final String OR = "|";
    private static final String OPTIONAL = "?";
    private static final String GROUP_END = ")";

    /**
     * The style of the C preprocessor, e.g., {@code #if A && B}, {@code #elif C}, {@code #else}, {@code #endif}.
     */
    public static final Style CPP =
            new Style("#", "", "if", "elif", "else", "endif", null, null, false, JavaSymbols.INSTANCE);

    /**
     * The style of Antenna, e.g., {@code //#if A && B}, {@code //#elif C}, {@code //#else}, {@code //#endif}.
     */
    public static final Style ANTENNA =
            new Style("//#", "", "if", "elif", "else", "endif", null, null, false, JavaSymbols.INSTANCE);

    /**
     * The style of Munge, e.g., {@code /*if[A]*}{@code /}, {@code /*else[A]*}{@code /}, {@code /*end[A]*}{@code /}.
     * Munge has no elif, and the condition repeated after else and end is ignored.
     * Annotations must be placed on their own line.
     */
    public static final Style MUNGE =
            new Style("/*", "*/", "if", null, "else", "end", "[", "]", true, JavaSymbols.INSTANCE);

    final String annotationPrefix;
    final String annotationSuffix;
    final String ifKeyword;
    final String elifKeyword;
    final String elseKeyword;
    final String endifKeyword;
    final String conditionStart;
    final String conditionEnd;
    final Symbols expressionSymbols;

    final boolean conditionAfterElseAndEndif;

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
        this.annotationPrefix = prefix;
        this.annotationSuffix = Objects.requireNonNull(suffix);
        this.ifKeyword = Objects.requireNonNull(ifKeyword);
        this.elifKeyword = elifKeyword;
        this.elseKeyword = elseKeyword;
        this.endifKeyword = Objects.requireNonNull(endifKeyword);
        this.conditionStart = conditionStart;
        this.conditionEnd = conditionEnd;
        this.conditionAfterElseAndEndif = conditionAfterElseAndEndif;
        this.expressionSymbols = Objects.requireNonNull(symbols);
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
                annotationSuffix,
                ifKeyword,
                elifKeyword,
                elseKeyword,
                endifKeyword,
                conditionStart,
                conditionEnd,
                conditionAfterElseAndEndif,
                expressionSymbols);
    }

    /**
     * {@return a copy of this style with the given symbols}
     *
     * @param symbols the symbols used within conditions
     */
    public Style withSymbols(Symbols symbols) {
        return new Style(
                annotationPrefix,
                annotationSuffix,
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
        return annotationPrefix;
    }

    public Symbols getSymbols() {
        return expressionSymbols;
    }

    public Pattern toPattern() {
        String patternString = annotation().toString();
        FeatJAR.log().debug("annotation pattern: %s", patternString);
        return Pattern.compile(patternString);
    }

    private StringBuilder annotation() {
        return new StringBuilder()
                .append("\\A")
                .append(group(ANNOTATION_START, prefix()))
                .append(group(KEYWORD_GROUP, keywords()))
                .append(group(condition()))
                .append(OPTIONAL)
                .append(group(REMAINDER_GROUP, ANYTHING))
                .append(OPTIONAL)
                .append("\\Z");
    }

    private StringBuilder group(String groupName, CharSequence content) {
        return new StringBuilder()
                .append(NAMED_GROUP_START)
                .append(groupName)
                .append(NAMED_GROUP_MIDDLE)
                .append(content)
                .append(GROUP_END);
    }

    private StringBuilder group(CharSequence content) {
        return new StringBuilder().append(NON_GROUP_START).append(content).append(GROUP_END);
    }

    private StringBuilder keywords() {
        return new StringBuilder()
                .append(group(IF_GROUP, Pattern.quote(ifKeyword)))
                .append(OR)
                .append(group(ELIF_GROUP, Pattern.quote(elifKeyword)))
                .append(OR)
                .append(group(ELSE_GROUP, Pattern.quote(elseKeyword)))
                .append(OR)
                .append(group(ENDIF_GROUP, Pattern.quote(endifKeyword)))
                .append(OR)
                .append(group(UNKNOWN_KEYWORD_GROUP, ANYTHING));
    }

    private StringBuilder prefix() {
        return new StringBuilder()
                .append(SPACE)
                .append(Pattern.quote(annotationPrefix))
                .append(SPACE);
    }

    private StringBuilder condition() {
        return new StringBuilder()
                .append(conditionStart == null ? "\\s" : Pattern.quote(conditionStart))
                .append(group(CONDITION_GROUP, ANYTHING))
                .append(conditionEnd == null ? "" : Pattern.quote(conditionEnd));
    }
}
