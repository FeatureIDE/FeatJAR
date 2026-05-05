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
package de.featjar.formula.io.dimacs;

import de.featjar.base.data.Pair;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.Assignments;
import de.featjar.formula.io.IFormulaFormat;
import de.featjar.formula.structure.FormulaNormalForm;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads and writes formulas that are already in CNF to/from DIMACS.
 *
 * @author Sebastian Krieter
 */
public class FormulaDimacsFormat extends ADimacsFormat<IFormula> implements IFormulaFormat {

    /**
     * The identifier of this format.
     */
    public static final String ID = FormulaDimacsFormat.class.getCanonicalName();

    @Override
    public String getIdentifier() {
        return ID;
    }

    @Override
    public FormulaDimacsFormat getInstance() {
        return this;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public Result<String> serialize(IFormula formula) {
        IFormula cnfFormula = (formula instanceof Reference) ? ((Reference) formula).getExpression() : formula;
        if (!cnfFormula.isStrictNormalForm(FormulaNormalForm.CNF)) {
            return Result.empty(new IllegalArgumentException("Formula is not in CNF"));
        }
        VariableMap variableMap = new VariableMap(formula.getVariableMap().keySet());
        return Result.of(DimacsSerializer.serialize(
                variableMap, cnfFormula.getChildren(), c -> Assignments.toBooleanLiterals((IFormula) c, variableMap)));
    }

    @Override
    public Result<IFormula> parse(AInputMapper inputMapper) {
        final DimacsParser parser = new DimacsParser();
        parser.setReadingVariableDirectory(true);
        try {
            Pair<VariableMap, List<List<int[]>>> parsingResult = parser.parse(inputMapper);
            VariableMap variableMap = parsingResult.getKey();
            LinkedHashSet<String> unusedVariableNames = new LinkedHashSet<>(variableMap.getVariableNames());
            List<IFormula> clauses = new ArrayList<>();
            for (List<int[]> clauseGroup : parsingResult.getValue()) {
                for (int[] clauseLiterals : clauseGroup) {
                    List<Literal> literals = new ArrayList<>(clauseLiterals.length);
                    for (int l : clauseLiterals) {
                        String variableName = variableMap
                                .get(Math.abs(l))
                                .orElseThrow(p -> new IllegalArgumentException("No mapping for literal " + l));
                        unusedVariableNames.remove(variableName);
                        literals.add(new Literal(l > 0, variableName));
                    }
                    clauses.add(new Or(literals));
                }
            }
            Reference reference = new Reference(new And(clauses));
            reference.setFreeVariables(
                    unusedVariableNames.stream().map(Variable::new).collect(Collectors.toList()));
            return Result.of(reference);
        } catch (final ParseException e) {
            return Result.empty(new ParseProblem(e, e.getErrorOffset()));
        } catch (final Exception e) {
            return Result.empty(e);
        }
    }
}
