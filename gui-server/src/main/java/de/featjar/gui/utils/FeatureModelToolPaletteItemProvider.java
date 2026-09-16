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

import de.featjar.gui.types.CardinalityType;
import de.featjar.gui.types.FeatureModelLables;
import de.featjar.gui.types.GroupNodeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.TriggerElementCreationAction;
import org.eclipse.glsp.server.actions.TriggerNodeCreationAction;
import org.eclipse.glsp.server.features.toolpalette.PaletteItem;
import org.eclipse.glsp.server.features.toolpalette.ToolPaletteItemProvider;

/**
 * The tool palette which can only encompass {@link TriggerElementCreationAction} not pure{@link Action} objects.
 */
public class FeatureModelToolPaletteItemProvider implements ToolPaletteItemProvider {

    @Override
    public List<PaletteItem> getItems(final Map<String, String> args) {
        return combinePalletes();
    }

    private PaletteItem featureCreation() {
        List<PaletteItem> nodes = new ArrayList<>();
        PaletteItem createOptionalFeature = node(CardinalityType.OPTIONAL_FEATURE.value(), "Optional Feature");
        PaletteItem createMandatoryFeature = node(CardinalityType.MANDATORY_FEATURE.value(), "Mandatory Feature");
        PaletteItem createCarinalityFeature = node(CardinalityType.MULTIPLE_FEATURE.value(), "Multiple Feature");

        nodes.add(createOptionalFeature);
        nodes.add(createMandatoryFeature);
        nodes.add(createCarinalityFeature);

        return PaletteItem.createPaletteGroup("nodes", "Add Features", nodes, "symbol-property");
    }

    private PaletteItem nodeCreation() {
        List<PaletteItem> nodes = new ArrayList<>();
        PaletteItem createOrGroupNode = node(GroupNodeType.OR_NODE.value(), "OR Node");
        PaletteItem createXorGroupNode = node(GroupNodeType.XOR_NODE.value(), "XOR Node");
        PaletteItem createAndGroupNode = node(GroupNodeType.AND_NODE.value(), "AND Node");

        nodes.add(createAndGroupNode);
        nodes.add(createOrGroupNode);
        nodes.add(createXorGroupNode);

        return PaletteItem.createPaletteGroup("nodes", "Add Nodes", nodes, "symbol-property");
    }

    private PaletteItem constraintCreation() {
        List<PaletteItem> nodes = new ArrayList<>();
        PaletteItem createConstraint = node(FeatureModelLables.CONSTRAINT_NODE, "Constraint");

        nodes.add(createConstraint);

        return PaletteItem.createPaletteGroup("nodes", "Add Constraints", nodes, "symbol-property");
    }

    private PaletteItem node(final String elementTypeId, final String label) {
        return new PaletteItem(elementTypeId, label, new TriggerNodeCreationAction(elementTypeId));
    }

    private List<PaletteItem> combinePalletes() {
        List<PaletteItem> palettes = new ArrayList<>();

        palettes.add(featureCreation());
        palettes.add(nodeCreation());
        palettes.add(constraintCreation());

        return palettes;
    }
}
