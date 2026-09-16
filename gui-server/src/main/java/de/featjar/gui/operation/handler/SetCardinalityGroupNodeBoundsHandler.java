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
import de.featjar.gui.operation.SetCardinalityGroupNodeBoundsOperation;
import de.featjar.gui.utils.IdentifiableResolver;
import featJAR.Cardinality;
import featJAR.GroupNode;
import featJAR.Identifiable;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.operations.GModelOperationHandler;

/**
 * The handler sets the {@link Cardinality} bounds of a {@link GroupNode}
 * and is triggered by the underlying operation {@link SetCardinalityGroupNodeBoundsOperation}.
 */
public class SetCardinalityGroupNodeBoundsHandler
        extends GModelOperationHandler<SetCardinalityGroupNodeBoundsOperation> {

    @Inject
    protected IdentifiableResolver resolver;

    @Override
    public Optional<Command> createCommand(SetCardinalityGroupNodeBoundsOperation operation) {
        Identifiable element = resolver.findById((operation.getElementId())).orElseThrow();

        if (!(element instanceof GroupNode groupNode)) {
            return Optional.empty();
        }

        int lower = operation.getLowerBound();
        int upper = operation.getUpperBound();

        if (lower < 0 || (upper != -1 && upper < lower)) {
            return Optional.empty();
        }

        return commandOf(() -> {
            Cardinality c = groupNode.getCardinality();
            c.setLowerBound(lower);
            c.setUpperBound(upper);
        });
    }
}
