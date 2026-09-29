/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt>
 * for further information.
 */
package de.featjar.analysis.javasmt;

import de.featjar.analysis.javasmt.computation.ComputeJavaSMTFormula;
import de.featjar.analysis.javasmt.computation.ComputeSatisfiability;
import de.featjar.base.computation.Computations;
import de.featjar.base.tree.Trees;
import de.featjar.composition.Preprocessor;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.Symbols;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.predicate.False;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

/**
 * Analyzes preprocessor annotations using JavaSMT.
 *
 * <p>In contrast to the SAT4J-based preprocessor analysis, this analyzer can
 * also handle non-Boolean expressions such as {@code #if a > 5}.
 */
public class PreprocessorAnalyzer extends Preprocessor {

    public PreprocessorAnalyzer(String annotationPrefix, Symbols symbols) {
        super(annotationPrefix, symbols);
    }

    public List<String> findDeadCode(Stream<String> lines, IFormula featureModel) {
        List<String> lineList = lines.toList();
        List<IFormula> presence = computePresenceConditions(lineList.stream());

        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setSymbols(getSymbols());

        List<String> dead = new ArrayList<>();
        for (int start = 0, i = 0; i <= presence.size(); i++) {
            boolean atAnnotation = i == presence.size() || isAnnotation(lineList.get(i));

            if (atAnnotation) {
                if (start < i) {
                    IFormula presenceCondition = presence.get(start);

                    if (!isSatisfiable(featureModel, presenceCondition)) {
                        dead.add(String.format(
                                "Dead code at lines %d-%d: %s",
                                start + 1,
                                i,
                                Trees.traverse(presenceCondition, serializer).orElseThrow()));
                    }
                }

                start = i + 1;
            }
        }

        return dead;
    }

    public List<String> findSuperfluousAnnotations(Stream<String> lines, IFormula featureModel) {

        List<String> lineList = lines.toList();
        List<IFormula> presence = computePresenceConditions(lineList.stream());
        List<String> result = new ArrayList<>();

        for (int i = 0; i < lineList.size(); i++) {
            Matcher matcher = annotationPattern.matcher(lineList.get(i));

            if (!matcher.matches() || matcher.group(ENDIF_GROUP) != null || i + 1 >= presence.size()) {
                continue;
            }
            int nextLine = i + 1;

            while (nextLine < presence.size() && presence.get(nextLine) == False.INSTANCE) {
                nextLine++;
            }

            if (nextLine < presence.size()) {
                IFormula presenceCondition = presence.get(nextLine);
                if (!isSatisfiable(featureModel, new Not(presenceCondition))) {

                    result.add("Line " + (i + 1) + ": " + lineList.get(i));
                }
            }
        }

        return result;
    }

    private boolean isAnnotation(String line) {
        Matcher matcher = annotationPattern.matcher(line);

        if (!matcher.matches()) {
            return false;
        }

        return matcher.group(IF_GROUP) != null
                || matcher.group(ELIF_GROUP) != null
                || matcher.group(ELSE_GROUP) != null
                || matcher.group(ENDIF_GROUP) != null;
    }

    private static boolean isSatisfiable(IFormula featureModel, IFormula presenceCondition) {

        IFormula combinedFormula = new And(featureModel, presenceCondition);

        return Computations.of(combinedFormula)
                .map(ComputeJavaSMTFormula::new)
                .set(ComputeJavaSMTFormula.SOLVER, Solvers.SMTINTERPOL)
                .map(ComputeSatisfiability::new)
                .compute();
    }
}
