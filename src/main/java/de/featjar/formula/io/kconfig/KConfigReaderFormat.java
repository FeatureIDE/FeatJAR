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
package de.featjar.formula.io.kconfig;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.io.IExpressionFormat;
import de.featjar.formula.io.textual.ExpressionParser;
import de.featjar.formula.io.textual.PropositionalModelSymbols;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses feature-model formula files created by KConfigReader.
 * TODO: this is currently mostly a hack and should be parsed properly as first-order formulas
 *
 * @author Elias Kuiter
 * @author Andreas Gerasimow
 * @see <a href="https://github.com/ekuiter/torte-FeatJAR/blob/main/src/main/java/KConfigReaderFormat.java">KConfigReaderFormat</a>
 */
public class KConfigReaderFormat implements IExpressionFormat {
    private static final Pattern equivalencePattern = Pattern.compile("(?<!\\w)def\\(([^()]*?)==CONFIG_(.*?)\\)");
    private static final Pattern definePattern = Pattern.compile("(?<!\\w)def\\(([^()]+)\\)");

    private static String fixNonBooleanConstraints(String l) {
        Matcher matcher = equivalencePattern.matcher(l);
        l = matcher.replaceAll(matchResult -> String.format("(%s<eq>%s)", matchResult.group(1), matchResult.group(2)));
        return l.replace("=", "_")
                .replace("<eq>", "==")
                .replace(":", "_")
                .replace(".", "_")
                .replace(",", "_")
                .replace("/", "_")
                .replace("\\", "_")
                .replace(" ", "_")
                .replace("-", "_");
    }

    private static String findDef(String l) {
        Matcher matcher = definePattern.matcher(l);
        return matcher.find() ? matcher.replaceAll("$1") : null;
    }

    @Override
    public Result<IExpression> parse(AInputMapper inputMapper) {
        final ArrayList<Problem> problems = new ArrayList<>();
        final ExpressionParser expressionParser = new ExpressionParser();
        expressionParser.setSymbols(PropositionalModelSymbols.INSTANCE);

        List<IFormula> subformulas = inputMapper
                .get()
                .getLineStream()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .filter(l -> !l.startsWith("#"))
                .map(KConfigReaderFormat::fixNonBooleanConstraints)
                .map(KConfigReaderFormat::findDef)
                .filter(Objects::nonNull)
                .map(expressionParser::parse)
                .peek(r -> problems.addAll(r.getProblems()))
                .filter(Result::isPresent)
                .map(expressionResult -> (IFormula) expressionResult.get())
                .collect(Collectors.toList());

        return Result.of(new Reference(subformulas.size() == 1 ? subformulas.get(0) : new And(subformulas)), problems);
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsWrite() {
        return false;
    }

    @Override
    public String getFileExtension() {
        return "model";
    }

    @Override
    public String getName() {
        return "KConfigReader";
    }
}
