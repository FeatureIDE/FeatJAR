/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-sat4j.
 *
 * formula-analysis-sat4j is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-sat4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-sat4j. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-sat4j> for further information.
 */
package de.featjar.analysis.sat4j;

import de.featjar.analysis.sat4j.computation.ComputeSatisfiableSAT4J;
import de.featjar.base.computation.Computations;
import de.featjar.base.tree.Trees;
import de.featjar.composition.Preprocessor;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.assignment.conversion.ComputeBooleanClauseList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.Symbols;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.True;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public class PreprocessorAnalyzer extends Preprocessor {

    public PreprocessorAnalyzer(String annotationPrefix, Symbols symbols) {
        super(annotationPrefix, symbols);
    }

    /**
     * {@return one message per block of code lines whose presence condition is not satisfiable
     * together with the given feature model}
     *
     * @param lines the line stream
     * @param featureModel the feature model
     */
    public List<String> findDeadCode(Stream<String> lines, IFormula featureModel) {
        List<String> lineList = lines.toList();
        List<IFormula> presence = computePresenceConditions(lineList.stream());
        ExpressionSerializer serializer = new ExpressionSerializer();
        serializer.setSymbols(getSymbols());
        List<String> dead = new ArrayList<>();

        // Compute the CNF of the feature model once (ComputeNNFFormula deals with the reference)
        BooleanAssignmentList modelClauses = Computations.of(featureModel)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .compute();

        // A code block is a maximal run of lines between annotations. Annotations are
        // identified with the shared pattern and the named annotation groups so that
        // the intent is explicit and does not depend on the internal representation of
        // computePresenceConditions.
        for (int start = 0, i = 0; i <= presence.size(); i++) {
            boolean atAnnotation = i == presence.size() || isAnnotation(lineList.get(i));
            if (atAnnotation) {
                if (start < i) {
                    IFormula presenceCondition = presence.get(start);
                    if (!isSatisfiable(modelClauses, presenceCondition)) {
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

    /** Returns annotations whose effective condition holds in every valid configuration. */
    public List<String> findSuperfluousAnnotations(Stream<String> lines, IFormula featureModel) {
        List<String> lineList = lines.toList();
        List<IFormula> presence = computePresenceConditions(lineList.stream());
        BooleanAssignmentList modelClauses = Computations.of(featureModel)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeBooleanClauseList::new)
                .compute();
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

            if (nextLine < presence.size() && !isSatisfiable(modelClauses, new Not(presence.get(nextLine)))) {
                result.add("Line " + (i + 1) + ": " + lineList.get(i));
            }
        }
        return result;
    }

    /**
     * {@return whether the given line is a presence annotation such as {@code #if},
     * {@code #elif}, {@code #else}, or {@code #endif}}
     *
     * Uses the compiled annotation pattern and the named annotation groups of
     * {@link Preprocessor} instead of relying on the internal representation of the
     * presence conditions for annotation lines.
     */
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

    /**
     * {@return whether the given presence condition is satisfiable together with the model}
     *
     * Uses {@link ComputeSatisfiableSAT4J} with the model's clause list as the base
     * and the presence condition's clause list as an assumed clause list.
     * Both clause lists use a shared {@link VariableMap} that also includes variables
     * appearing only in the presence condition.
     */
    private static boolean isSatisfiable(BooleanAssignmentList modelClauses, IFormula presenceCondition) {
        if (presenceCondition == True.INSTANCE) {
            return true;
        }
        if (presenceCondition == False.INSTANCE) {
            return false;
        }

        IFormula cnfPresence = presenceCondition.toCNF().orElseThrow();
        VariableMap variableMap = new VariableMap(modelClauses.getVariableMap(), new VariableMap(cnfPresence));
        BooleanAssignmentList presenceClauses = ComputeBooleanClauseList.toBooleanAssignmentList(
                        cnfPresence, variableMap)
                .orElseThrow();

        return Computations.of(modelClauses.remap(variableMap))
                .map(ComputeSatisfiableSAT4J::new)
                .set(ComputeSatisfiableSAT4J.ASSUMED_CLAUSE_LIST, presenceClauses)
                .compute();
    }
}
