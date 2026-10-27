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

import de.featjar.base.data.Problem.Severity;
import de.featjar.base.io.format.ParseProblem;
import de.featjar.composition.PreprocessorStream.Line;
import de.featjar.formula.assignment.Assignment;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Not;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 * Analyzes preprocessor annotations to detect dead and core code and superfluous annotations.
 *
 * @author Sebastian Krieter
 * @author Mohammed Al-Saih
 * @author Mirza Adnan Baig
 * @author Malek Lahyami
 * @author Ines Zenkri
 */
public abstract class APreprocessorAnalyzer {

    public Stream<Line> detectDeadCode(Stream<Line> stream, IFormula featureModel, Assignment assignment) {
        // TODO cache pcs, exploit nesting
        // TODO take assignment into account
        return stream.peek(line -> {
            if (!isSatisfiable(featureModel, line.getPresenceCondition())) {
                if (line.problems == null) {
                    line.problems = new LinkedList<>();
                }
                line.problems.add(new ParseProblem("Dead code", Severity.WARNING, line.number));
            }
        });
    }

    public Stream<Line> detectSuperfluous(Stream<Line> stream, IFormula featureModel, Assignment assignment) {
        // TODO cache pcs, exploit nesting
        // TODO take assignment into account
        return stream.filter(line -> line.isAnnotation).peek(line -> {
            if (!isSatisfiable(
                    featureModel, new And(line.annotation.getPriorCondition(), new Not(line.annotation.condition)))) {
                if (line.problems == null) {
                    line.problems = new LinkedList<>();
                }
                line.problems.add(new ParseProblem("Superflous annotation", Severity.WARNING, line.number));
            }
        });
    }

    public Stream<Line> detectCoreCode(Stream<Line> stream, IFormula featureModel, Assignment assignment) {
        // TODO cache pcs, exploit nesting
        // TODO take assignment into account
        return stream.peek(line -> {
            if (!isSatisfiable(featureModel, new Not(line.getPresenceCondition()))) {
                if (line.problems == null) {
                    line.problems = new LinkedList<>();
                }
                line.problems.add(new ParseProblem("Core code", Severity.WARNING, line.number));
            }
        });
    }

    protected abstract boolean isSatisfiable(IFormula featureModel, IFormula presenceCondition);
}
