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
package de.featjar.gui.operation.handler;

import com.google.inject.Inject;
import de.featjar.base.data.Result;
import de.featjar.gui.operation.SetGroupNodeTypeOperation;
import de.featjar.gui.types.GroupNodeType;
import de.featjar.gui.utils.CardinalityUtils;
import de.featjar.gui.utils.IdentifiableResolver;
import featJAR.GroupNode;
import featJAR.Identifiable;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.operations.GModelOperationHandler;

/**
 * The handler sets the {@link GroupNodeType} of a {@link GroupNode}
 * and is triggered by the underlying operation {@link SetGroupNodeTypeOperation}.
 */
public class SetGroupNodeTypeHandler extends GModelOperationHandler<SetGroupNodeTypeOperation> {

    @Inject
    protected IdentifiableResolver resolver;

    @Override
    public Optional<Command> createCommand(SetGroupNodeTypeOperation operation) {

        Identifiable element = resolver.findById((operation.getElementId())).orElseThrow();

        if (!(element instanceof GroupNode groupNode)) {
            return Optional.empty();
        }

        Result<GroupNodeType> groupNodeType = operation.getNodeType();

        if (groupNodeType.isEmpty()) {
            return Optional.empty();
        }

        GroupNodeType type = groupNodeType.get();

        return commandOf(() -> {
            groupNode.setCardinality(CardinalityUtils.createCardinality(type.lowerBound(), type.upperBound()));
        });
    }
}
