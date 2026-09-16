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
package de.featjar.gui.policy;

import de.featjar.gui.types.EdgeType;
import java.util.List;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.server.diagram.BaseDiagramConfiguration;
import org.eclipse.glsp.server.layout.ServerLayoutKind;
import org.eclipse.glsp.server.types.EdgeTypeHint;
import org.eclipse.glsp.server.types.ShapeTypeHint;

/**
 * Determines how the diagram behaves.
 */
public class FeatureModelDiagramConfiguration extends BaseDiagramConfiguration {

    @Override
    public List<ShapeTypeHint> getShapeTypeHints() {
        // tasks can be moved, deleted and resized
        return List.of(
                new ShapeTypeHint("constraint-box", false, false, false, false),
                // all other elements (constraints, features, group nodes)
                new ShapeTypeHint(DefaultTypes.NODE, false, true, false, false));
    }

    @Override
    public List<EdgeTypeHint> getEdgeTypeHints() {
        return List.of(
                new EdgeTypeHint(EdgeType.BASIC_EDGE.value(), false, false, false, false, null, null),
                new EdgeTypeHint(EdgeType.MANDATORY_EDGE.value(), false, false, false, false, null, null),
                new EdgeTypeHint(EdgeType.OPTIONAL_EDGE.value(), false, false, false, false, null, null));
    }
    /* Because the engine is used, ServerLayoutKind needs to be AUTOMATIC*/
    @Override
    public ServerLayoutKind getLayoutKind() {
        return ServerLayoutKind.AUTOMATIC;
    }
}
