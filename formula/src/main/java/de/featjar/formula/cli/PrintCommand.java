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
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.tree.Trees;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.Symbols;
import de.featjar.formula.structure.IFormula;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;
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

    public static final String CUSTOM_STRING_PREFIX = "CUSTOM:";

    /**
     * Defines the tab string.
     */
    public static final Option<String> TAB_OPTION = Option.newOption("tab", Option.StringParser)
            .setDescription("Defines the tab value. Possible options: "
                    + Arrays.toString(WhitespaceString.values())
                    + ". For custom value, type " + CUSTOM_STRING_PREFIX + "<value>")
            .setDefaultValue(WhitespaceString.TAB.toString());

    /**
     * Defines the notation.
     */
    public static final Option<ExpressionSerializer.Notation> NOTATION_OPTION = Option.newOption(
                    "notation", (arg) -> ExpressionSerializer.Notation.valueOf(arg.toUpperCase(Locale.ENGLISH)))
            .setDescription("Defines the notation. Possible options: "
                    + Arrays.toString(ExpressionSerializer.Notation.values()))
            .setDefaultValue(ExpressionSerializer.STANDARD_NOTATION);

    /**
     * Defines the symbols.
     */
    public static final Option<Symbols> SYMBOLS_OPTION = Option.newOption("format", (arg) -> {
                try {
                    return (Symbols) Class.forName(arg).getField("INSTANCE").get(null);
                } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
                    FeatJAR.log().error(e);
                    return ExpressionSerializer.STANDARD_SYMBOLS;
                }
            })
            .setDescription("Defines the symbols.")
            .setDefaultValue(ExpressionSerializer.STANDARD_SYMBOLS);

    /**
     * Defines the new line string.
     */
    public static final Option<String> NEW_LINE_OPTION = Option.newOption("newline", Option.StringParser)
            .setDescription("Defines the new line value. Possible options: "
                    + Arrays.toString(WhitespaceString.values())
                    + ". For custom value, type " + CUSTOM_STRING_PREFIX + "<value>")
            .setDefaultValue(WhitespaceString.NEWLINE.toString());

    /**
     * Enforces parentheses.
     */
    public static final Option<Boolean> ENFORCE_PARENTHESES_OPTION = Option.newFlag("enforce-parentheses")
            .setDescription("Enforces parentheses.")
            .setDefaultValue(ExpressionSerializer.STANDARD_ENFORCE_PARENTHESES);

    /**
     * Enquotes whitespace.
     */
    public static final Option<Boolean> ENQUOTE_WHITESPACE_OPTION = Option.newFlag("enquote-whitespace")
            .setDescription("Enquotes whitespace.")
            .setDefaultValue(ExpressionSerializer.STANDARD_ENQUOTE_WHITESPACE);

    @Override
    public int run(OptionList optionParser) {
        String tab = optionParser.getResult(TAB_OPTION).get();
        ExpressionSerializer.Notation notation =
                optionParser.getResult(NOTATION_OPTION).get();
        Symbols symbols = optionParser.getResult(SYMBOLS_OPTION).get();
        String newLine = optionParser.getResult(NEW_LINE_OPTION).get();
        boolean ep = optionParser.getResult(ENFORCE_PARENTHESES_OPTION).get();
        boolean ew = optionParser.getResult(ENQUOTE_WHITESPACE_OPTION).get();

        Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElse(null);
        Result<IFormula> formula =
                optionParser.getResult(INPUT_OPTION).mapResult(p -> IO.load(p, FormulaFormats.getInstance()));

        if (formula.isEmpty()) {
            FeatJAR.log().problems(formula);
            return FeatJAR.ERROR_COMPUTING_RESULT;
        }

        ExpressionSerializer serializer = new ExpressionSerializer();

        tab = getWhitespaceString(tab);
        newLine = getWhitespaceString(newLine);

        if (tab == null || newLine == null) {
            return 0;
        }

        serializer.setTab(tab);
        serializer.setNotation(notation);
        serializer.setSymbols(symbols);
        serializer.setNewLine(newLine);
        serializer.setEnforceParentheses(ep);
        serializer.setEnquoteWhitespace(ew);

        String formulaString = Trees.traverse(formula.get(), serializer).orElse("");
        if (outputPath == null) {
            FeatJAR.log().message(formulaString);
        } else {
            try {
                if (Files.isDirectory(outputPath)) {
                    FeatJAR.log().error(new IOException(outputPath.toString() + " is a directory"));
                } else {
                    Files.write(
                            outputPath,
                            formulaString.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                }
            } catch (IOException e) {
                FeatJAR.log().error(e);
                return FeatJAR.ERROR_COMPUTING_RESULT;
            }
        }
        return 0;
    }

    public String getWhitespaceString(String input) {
        try {
            WhitespaceString ws = WhitespaceString.valueOf(input);
            return ws.getWhitespaceValue();
        } catch (IllegalArgumentException e) {
            if (!input.startsWith(CUSTOM_STRING_PREFIX)) {
                FeatJAR.log().error("Illegal string: " + input);
                return null;
            }
            return input.substring(CUSTOM_STRING_PREFIX.length());
        }
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
