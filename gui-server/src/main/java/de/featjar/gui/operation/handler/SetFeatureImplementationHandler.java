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
import de.featjar.gui.operation.SetFeatureImplementationTypeOperation;
import de.featjar.gui.types.AttributeKeys;
import de.featjar.gui.types.FeatureImplementationTypes;
import de.featjar.gui.utils.AttributeKeysUtils;
import de.featjar.gui.utils.IdentifiableResolver;
import featJAR.Feature;
import featJAR.Identifiable;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.operations.GModelOperationHandler;

/**
 * The handler sets the {@link AttributeKeys#IMPLLEMENTATION} type of a {@link Feature}
 * and is triggered by the underlying operation {@link SetFeatureImplementationTypeOperation}.
 */
public class SetFeatureImplementationHandler extends GModelOperationHandler<SetFeatureImplementationTypeOperation> {

    @Inject
    protected IdentifiableResolver resolver;

    @Override
    public Optional<Command> createCommand(SetFeatureImplementationTypeOperation operation) {
        Identifiable element = resolver.findById((operation.getElementId())).orElseThrow();
        if (!(element instanceof Feature feature)) {
            return Optional.empty();
        }

        Result<FeatureImplementationTypes> type = operation.getFeatureImpementationType();
        if (type.isEmpty()) {
            return Optional.empty();
        }

        return commandOf(() -> AttributeKeysUtils.setFeatureImplementationType(feature, type.get()));
    }
}
