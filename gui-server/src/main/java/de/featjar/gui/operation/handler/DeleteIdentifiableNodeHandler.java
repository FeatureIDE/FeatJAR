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
import de.featjar.gui.utils.IdentifiableResolver;
import featJAR.Identifiable;
import java.util.List;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.glsp.server.emf.EMFModelState;
import org.eclipse.glsp.server.emf.EMFOperationHandler;
import org.eclipse.glsp.server.operations.DeleteOperation;

/**
 * The handler deletes the elements that are currently selected. The selection state of the server
 * {@link DeleteIdentifiableNodeHandler#modelState} is afterwards cleared.
 */
public class DeleteIdentifiableNodeHandler extends EMFOperationHandler<DeleteOperation> {

    @Inject
    protected IdentifiableResolver resolver;

    @Inject
    protected EMFModelState modelState;

    @Override
    public Optional<Command> createCommand(final DeleteOperation operation) {

        List<String> gModelIds = operation.getElementIds();
        CompoundCommand command = new CompoundCommand();

        for (String id : gModelIds) {
            command.append(delete(id));
        }

        modelState.clearProperty("currentSelection");

        return Optional.of(command);
    }

    protected Command delete(final String gModelId) {

        Identifiable element = resolver.findById(gModelId).orElseThrow();

        EditingDomain editingDomain = modelState.getEditingDomain();
        return RemoveCommand.create(
                editingDomain, EObject.class.cast(element.eContainer()), element.eContainingFeature(), element);
    }
}
