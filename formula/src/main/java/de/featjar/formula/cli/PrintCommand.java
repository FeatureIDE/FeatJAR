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
package de.featjar.formula.cli;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.cli.Options;
import de.featjar.base.io.text.GenericTextFormat;
import de.featjar.base.tree.Trees;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.ShortSymbols;
import de.featjar.formula.io.textual.Symbols;
import java.util.Optional;

/**
 * Prints the formula in a readable format.
 *
 * @author Andreas Gerasimow
 */
public class PrintCommand extends ACommand {

    public enum WhitespaceString {
        TAB(ExpressionSerializer.STANDARD_TAB_STRING),
        NEWLINE(ExpressionSerializer.STANDARD_NEW_LINE),
        SPACE(" ");

        private final String whitespaceValue;

        WhitespaceString(String value) {
            this.whitespaceValue = value;
        }

        public String getWhitespaceValue() {
            return whitespaceValue;
        }
    }

    /**
     * Defines the tab string.
     */
    public static final Option<WhitespaceString> TAB_OPTION = Options.newEnumOption("tab", WhitespaceString.class)
            .setDescription("Defines the string used for tabs.")
            .setDefaultArgument(String.valueOf(WhitespaceString.TAB));

    /**
     * Defines the notation.
     */
    public static final Option<ExpressionSerializer.Notation> NOTATION_OPTION = Options.newEnumOption(
                    "notation", ExpressionSerializer.Notation.class)
            .setDescription("Defines the notation.")
            .setDefaultArgument(String.valueOf(ExpressionSerializer.STANDARD_NOTATION));

    // TODO Use predefined list of symbol classes
    /**
     * Defines the symbols.
     */
    public static final Option<Symbols> SYMBOLS_OPTION = Options.newOption("format", (arg) -> {
                try {
                    return (Symbols) Class.forName(arg).getField("INSTANCE").get(null);
                } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
                    FeatJAR.log().error(e);
                    return ExpressionSerializer.STANDARD_SYMBOLS;
                }
            })
            .setDescription("Defines the symbols.")
            .setDefaultArgument(ShortSymbols.class.getName());

    /**
     * Defines the new line string.
     */
    public static final Option<WhitespaceString> NEW_LINE_OPTION = Options.newEnumOption(
                    "newline", WhitespaceString.class)
            .setDescription("Defines the string used for newline.")
            .setDefaultArgument(WhitespaceString.NEWLINE.toString());

    /**
     * Enforces parentheses.
     */
    public static final Option<Boolean> ENFORCE_PARENTHESES_OPTION = Options.newFlag("enforce-parentheses")
            .setDescription("Enforces parentheses.")
            .setDefaultArgument(String.valueOf(ExpressionSerializer.STANDARD_ENFORCE_PARENTHESES));

    /**
     * Enquotes whitespace.
     */
    public static final Option<Boolean> ENQUOTE_WHITESPACE_OPTION = Options.newFlag("enquote-whitespace")
            .setDescription("Enquotes whitespace.")
            .setDefaultArgument(String.valueOf(ExpressionSerializer.STANDARD_ENQUOTE_WHITESPACE));

    @Override
    public int run(OptionList optionParser) {
        return writeResult(
                optionParser,
                readFromInput(optionParser, FormulaFormats.getInstance())
                        .mapResult(f -> Trees.traverse(f, getSerializer(optionParser))),
                new GenericTextFormat<>());
    }

    private ExpressionSerializer getSerializer(OptionList optionParser) {
        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setTab(optionParser.getResult(TAB_OPTION).get().getWhitespaceValue());
        serializer.setNotation(optionParser.getResult(NOTATION_OPTION).get());
        serializer.setSymbols(optionParser.getResult(SYMBOLS_OPTION).get());
        serializer.setNewLine(optionParser.getResult(NEW_LINE_OPTION).get().getWhitespaceValue());
        serializer.setEnforceParentheses(
                optionParser.getResult(ENFORCE_PARENTHESES_OPTION).get());
        serializer.setEnquoteWhitespace(
                optionParser.getResult(ENQUOTE_WHITESPACE_OPTION).get());
        return serializer;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints the formula in a readable format.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("print");
    }
}
