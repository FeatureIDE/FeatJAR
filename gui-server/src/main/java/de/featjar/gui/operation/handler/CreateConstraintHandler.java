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
import de.featjar.gui.types.FeatureModelLables;
import de.featjar.gui.utils.HandlerUtils;
import featJAR.Constraint;
import featJAR.FeatJARFactory;
import featJAR.FeatJARPackage;
import featJAR.FeatureModel;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.glsp.server.emf.EMFCreateOperationHandler;
import org.eclipse.glsp.server.emf.EMFIdGenerator;
import org.eclipse.glsp.server.emf.notation.EMFNotationModelState;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

/**
 * The handler creates {@link Constraint} objects.
 */
public class CreateConstraintHandler extends EMFCreateOperationHandler<CreateNodeOperation> {

    @Inject
    protected EMFNotationModelState modelState;

    @Inject
    protected EMFIdGenerator idGenerator;

    static int constraintCounter = 1;

    public CreateConstraintHandler() {
        super(FeatureModelLables.CONSTRAINT_NODE);
    }

    @Override
    public Optional<Command> createCommand(final CreateNodeOperation operation) {
        return Optional.of(createConstraint());
    }

    @Override
    public String getLabel() {
        return "New Constraint";
    }

    public Command createConstraint() {

        String label = getLabel();
        Constraint newConstraint = FeatJARFactory.eINSTANCE.createConstraint();

        idGenerator.getOrCreateId(newConstraint); // sets ID if not already set
        newConstraint.setName(label + "_" + constraintCounter++);
        HandlerUtils.logIdentifiableCreation(newConstraint);

        EObject parentElement = modelState.getSemanticModel(FeatureModel.class).get();

        return AddCommand.create(
                modelState.getEditingDomain(),
                parentElement, // where to add
                FeatJARPackage.Literals.FEATURE_MODEL__CONSTRAINTS, // the containment reference
                newConstraint // what to add
                );
    }
}
