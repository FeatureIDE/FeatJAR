/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-gui-server.
 *
 * gui-server is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * gui-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gui-server. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE> for further information.
 */
package de.featjar.gui.utils;

import com.google.inject.Inject;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.formula.io.textual.ExpressionParser;
import de.featjar.formula.io.textual.ShortSymbols;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.gui.types.FeatureModelLables;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.server.features.directediting.LabelEditValidator;
import org.eclipse.glsp.server.features.directediting.ValidationStatus;
import org.eclipse.glsp.server.model.GModelState;

/**
 * Validates the editing of all labels.
 * The name of a feature must not be empty and must be unique. The text of a constraint must be
 * a correct formula that only refers to existing features, see {@link #findConstraintProblem(String, Set)}.
 * the validator is called while the user is typing, so problems are shown before the edit is applied (error message appears on the spot).
 */
public class FeatureModelLabelEditValidator implements LabelEditValidator {
    @Inject
    protected GModelState modelState;

    // gives us the names of all features, so we can check the names used in a constraint
    @Inject
    protected IdentifiableResolver resolver;

    @Override
    public ValidationStatus validate(final String label, final GModelElement element) {
        if (label.length() < 1) {
            return ValidationStatus.error("Name must not be empty");
        }
        // a constraint is a formula, not a name, so it gets its own checks.
        // The duplicate-name check further down does not apply to it.
        if (isConstraint(element)) {
            Result<IFormula> result = findConstraintProblem(label, resolver.findFeatureNames());
            if (result.isEmpty()) {
                return ValidationStatus.error(result.getProblems().stream()
                        .findFirst()
                        .map(Problem::getMessage)
                        .orElse("Constraint is not a valid formula"));
            }
            return ValidationStatus.ok();
        }

        Set<GNode> featureNodes = modelState.getIndex().getAllByClass(GNode.class);
        Stream<GLabel> otherLabels = featureNodes.stream()
                .filter(e -> !e.getId().equals(element.getId()))

                // The text of a constraint is a formula and never clashes with the name of a feature.
                // Otherwise a constraint "A" would be rejected because of the feature "A" (must not count as a
                // duplication).

                .filter(e -> !isConstraint(e))
                .flatMap(n -> n.getChildren().stream())
                .filter(c -> FeatureModelLables.EDITABLE_LABEL.equals(c.getType()))
                .filter(GLabel.class::isInstance)
                .map(GLabel.class::cast);

        boolean hasDuplicate = otherLabels.anyMatch(otherLabel -> Objects.equals(label, otherLabel.getText()));

        if (hasDuplicate) {
            return ValidationStatus.error("Name must be unique");
        }

        return ValidationStatus.ok();
    }
    /**
     *Tells whether the element is a constraint or a part of one such as its label.
     */
    protected boolean isConstraint(final GModelElement element) {

        // The element we get is the label but the marker "this is a constraint" (a CSS class)
        // sits on the node around it. Therefore the element and all of its parents are checked.
        for (GModelElement current = element; current != null; current = current.getParent()) {
            List<String> cssClasses = current.getCssClasses();
            if (cssClasses != null
                    && (cssClasses.contains(FeatureModelLables.CONSTRAINT_NODE)
                            || cssClasses.contains(FeatureModelLables.CONSTRAINT_LABEL))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks for a problem in the text of a constraint which is stored as the name of the constraint.
     * A constraint is valid if:
     *  its text is a syntactically correct formula and every feature it mentions exists in the feature model.
     *
     * @param text the text the user entered
     * @param featureNames the names of all features of the feature model
     * @return a {@link Result} holding the parsed formula if the constraint is valid, or holding the
     * problem(s) describing why it is not     */
    /**
     * I used an AI assistant (Claude) to help design this fix. Matching this code with the rest of the source code and
     * comments are done by me. I applied the changes by hand, tested them in the editor, and
     * debugged the environment myself.
     */
    @SuppressWarnings("deprecation")
    public static Result<IFormula> findConstraintProblem(final String text, final Set<String> featureNames) {
        // Same setup as in EMFFeatureModelParser#parseConstraint. Keep both in sync.
        ExpressionParser parser = new ExpressionParser();
        parser.setSymbols(ShortSymbols.INSTANCE);

        // Syntax: the parser owns Result already carries readable message like missing operands or open brackets
        Result<IExpression> result = parser.parse(text);
        if (result.isEmpty()) {
            return Result.empty(result.getProblems());
        }

        // The save-time parser casts the result to IFormula, so anything else would fail there
        if (!(result.get() instanceof IFormula)) {
            return Result.empty(new Problem("Constraint must be a formula"));
        }

        // References: the parser accepts any name so unknown features must be checked separately.
        // A LinkedHashSet keeps the names in the order they appear in the text.
        Set<String> unknown = new LinkedHashSet<>(result.get().getVariableNames());
        unknown.removeAll(featureNames);
        if (!unknown.isEmpty()) {
            return Result.empty(new Problem("Unknown feature: " + String.join(", ", unknown)));
        }

        return Result.of((IFormula) result.get());
    }
}
