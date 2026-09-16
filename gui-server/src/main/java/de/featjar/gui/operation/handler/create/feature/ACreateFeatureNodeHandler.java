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
package de.featjar.gui.operation.handler.create.feature;

import com.google.inject.Inject;
import de.featjar.base.data.Result;
import de.featjar.gui.types.CardinalityType;
import de.featjar.gui.types.FeatureImplementationTypes;
import de.featjar.gui.utils.AttributeKeysUtils;
import de.featjar.gui.utils.CardinalityUtils;
import de.featjar.gui.utils.HandlerUtils;
import featJAR.FeatJARFactory;
import featJAR.FeatJARPackage;
import featJAR.Feature;
import featJAR.FeatureModel;
import featJAR.GroupNode;
import featJAR.Identifiable;
import java.util.List;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.IdentityCommand;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.SelectAction;
import org.eclipse.glsp.server.emf.EMFCreateOperationHandler;
import org.eclipse.glsp.server.emf.EMFIdGenerator;
import org.eclipse.glsp.server.emf.notation.EMFNotationModelState;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

/**
 * The handler creates features that are attached to group nodes. The parent is taken from the current
 * selection. If a feature is selected instead, an AND group is inserted between
 * the selected feature and the new one.
 *
 * The concrete subclasses supply the element type id, which determines the
 * cardinality of the new feature (mandatory, optional or multiple).
 *
 * TODO Does currently not support to create a root feature. Maybe this should be done in another handler.
 */
public abstract class ACreateFeatureNodeHandler extends EMFCreateOperationHandler<CreateNodeOperation> {

    @Inject
    protected EMFNotationModelState modelState;

    @Inject
    protected EMFIdGenerator idGenerator;

    @Inject
    protected ActionDispatcher actionDispatcher;

    protected static int featureCounter = 1;

    @Override
    public Optional<Command> createCommand(final CreateNodeOperation operation) {
        return Optional.of(createFeatureCommand());
    }

    protected ACreateFeatureNodeHandler(final String elementTypeId) {
        super(elementTypeId);
    }

    protected Optional<Feature> getRoot() {
        return modelState.getSemanticModel(FeatureModel.class).flatMap(model -> model.getRoots().stream()
                .findFirst());
    }

    @Override
    public String getLabel() {
        return "New Feature";
    }

    protected FeatureModel getFeatureModel() {
        return modelState.getSemanticModel(FeatureModel.class).get();
    }

    protected Command createFeatureCommand() {
        Optional<Identifiable> selection = modelState.getProperty("currentSelection", Identifiable.class);
        Command command;

        if (selection.isEmpty()) {
            selection = this.getRoot().map(Identifiable.class::cast);
            if (selection.isEmpty()) {
                return IdentityCommand.INSTANCE;
            }
        }

        Feature newFeature =
                createFeature(getLabel(), FeatureImplementationTypes.CONCRETE).orElseThrow();

        if (selection.get() instanceof GroupNode groupNode) {
            // A group node was selected and the feature gets directly added
            command = AddCommand.create(
                    modelState.getEditingDomain(),
                    groupNode,
                    FeatJARPackage.Literals.GROUP_NODE__FEATURE_LIST,
                    newFeature);
        } else if (selection.get() instanceof Feature parentFeature) {
            // A feature was selected, insert an AND group between parent and new feature
            GroupNode andGroup = parentFeature.getGroupNodeList().stream()
                    .filter(g -> CardinalityUtils.isAnd(g.getCardinality()))
                    .findFirst()
                    .orElse(FeatJARFactory.eINSTANCE.createGroupNode());

            idGenerator.getOrCreateId(andGroup);
            andGroup.setName("");
            andGroup.setCardinality(CardinalityUtils.createAndCardinality());
            andGroup.getFeatureList().add(newFeature);

            command = AddCommand.create(
                    modelState.getEditingDomain(),
                    parentFeature,
                    FeatJARPackage.Literals.FEATURE__GROUP_NODE_LIST,
                    andGroup);
        } else {
            return IdentityCommand.INSTANCE;
        }
        selection.ifPresent(element ->
                actionDispatcher.dispatchAfterNextUpdate(SelectAction.setSelection(List.of(element.getId()))));

        return command;
    }

    private Result<Feature> createFeature(final String label, FeatureImplementationTypes featureType) {
        Feature newFeature = FeatJARFactory.eINSTANCE.createFeature();

        AttributeKeysUtils.setFeatureImplementationType(newFeature, featureType);

        idGenerator.getOrCreateId(newFeature); // sets ID if not already set
        newFeature.setName(label + "-" + featureCounter++);

        HandlerUtils.logIdentifiableCreation(newFeature);

        Result<CardinalityType> t =
                CardinalityType.fromValue(getHandledElementTypeIds().get(0));
        if (t.isEmpty()) {
            return Result.empty(t.getProblems());
        }

        CardinalityType type = t.get();

        switch (type) {
            case MANDATORY_FEATURE:
                newFeature.setCardinality(CardinalityUtils.createCardinality(1, 1));
                break;
            case OPTIONAL_FEATURE:
                newFeature.setCardinality(CardinalityUtils.createCardinality(0, 1));
                break;
            case MULTIPLE_FEATURE:
                newFeature.setCardinality(CardinalityUtils.createCardinality(0, CardinalityUtils.OPEN));
                break;
            default:
                break;
        }
        return Result.of(newFeature);
    }
}
