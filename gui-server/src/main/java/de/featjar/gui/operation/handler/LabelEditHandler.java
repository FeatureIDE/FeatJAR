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
import featJAR.FeatJARPackage;
import featJAR.Identifiable;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.glsp.server.emf.EMFModelState;
import org.eclipse.glsp.server.features.directediting.ApplyLabelEditOperation;
import org.eclipse.glsp.server.operations.GModelOperationHandler;

/**
 * The handler edits a label of underlying semantic element.
 *
 * Label IDs are derived from their owner as "semanticID_label", so the
 * suffix is stripped before the element is resolved.
 */
public class LabelEditHandler extends GModelOperationHandler<ApplyLabelEditOperation> {

    @Inject
    protected IdentifiableResolver resolver;

    @Override
    public Optional<Command> createCommand(final ApplyLabelEditOperation operation) {
        String labelId = operation.getLabelId();
        String semanticId =
                labelId.endsWith("_label") ? labelId.substring(0, labelId.length() - "_label".length()) : labelId;
        Identifiable element = resolver.findById(semanticId).orElseThrow();

        return Optional.of(SetCommand.create(
                ((EMFModelState) modelState).getEditingDomain(),
                element,
                FeatJARPackage.Literals.IDENTIFIABLE__NAME,
                operation.getText()));
    }
}
