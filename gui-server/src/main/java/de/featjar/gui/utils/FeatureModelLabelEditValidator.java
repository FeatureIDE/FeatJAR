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
import de.featjar.gui.types.FeatureModelLables;
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
 */
public class FeatureModelLabelEditValidator implements LabelEditValidator {
    @Inject
    protected GModelState modelState;

    @Override
    public ValidationStatus validate(final String label, final GModelElement element) {
        if (label.length() < 1) {
            return ValidationStatus.error("Name must not be empty");
        }

        Set<GNode> featureNodes = modelState.getIndex().getAllByClass(GNode.class);
        Stream<GLabel> otherLabels = featureNodes.stream()
                .filter(e -> !e.getId().equals(element.getId()))
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
}
