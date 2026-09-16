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
package de.featjar.gui.operation.handler.create.group;

import com.google.inject.Inject;
import de.featjar.base.data.Result;
import de.featjar.gui.types.GroupNodeType;
import de.featjar.gui.utils.CardinalityUtils;
import de.featjar.gui.utils.HandlerUtils;
import de.featjar.gui.utils.IdentifiableResolver;
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
 * The handler creates a group node which is always attached to a feature. The parent is taken from the
 * current selection. If nothing is selected the root feature is used instead.
 *
 * The concrete subclasses supply the element type id to determine their
 * cardinality (OR, XOR, AND or a custom cardinality).
 */
public class ACreateGroupNodeHandler extends EMFCreateOperationHandler<CreateNodeOperation> {

    @Inject
    protected EMFNotationModelState modelState;

    @Inject
    protected IdentifiableResolver resolver;

    @Inject
    protected EMFIdGenerator idGenerator;

    @Inject
    protected ActionDispatcher actionDispatcher;

    protected ACreateGroupNodeHandler(final String elementTypeId) {
        super(elementTypeId);
    }

    @Override
    public Optional<Command> createCommand(final CreateNodeOperation operation) {
        return Optional.of(createGroupNodeCommand());
    }

    protected Optional<Feature> getRoot() {
        return modelState.getSemanticModel(FeatureModel.class).flatMap(model -> model.getRoots().stream()
                .findFirst());
    }

    @Override
    public String getLabel() {
        return "";
    }

    protected FeatureModel getFeatureModel() {
        return modelState.getSemanticModel(FeatureModel.class).get();
    }

    protected Command createGroupNodeCommand() {
        Optional<Identifiable> selection = modelState.getProperty("currentSelection", Identifiable.class);

        // A group node can only be added to a feature
        if (selection.isPresent() && !(selection.get() instanceof Feature)) {
            return IdentityCommand.INSTANCE;
        }

        // Without a selection, fall back to the root feature
        Optional<Feature> parentFeature = selection
                .filter(Feature.class::isInstance)
                .map(Feature.class::cast)
                .or(this::getRoot);

        if (parentFeature.isEmpty()) {
            return IdentityCommand.INSTANCE;
        }

        Command command = AddCommand.create(
                modelState.getEditingDomain(),
                parentFeature.get(),
                FeatJARPackage.Literals.FEATURE__GROUP_NODE_LIST,
                createGroupNode().orElseThrow());

        selection.ifPresent(element ->
                actionDispatcher.dispatchAfterNextUpdate(SelectAction.setSelection(List.of(element.getId()))));

        return command;
    }

    private Result<GroupNode> createGroupNode() {

        GroupNode gn = FeatJARFactory.eINSTANCE.createGroupNode();

        idGenerator.getOrCreateId(gn);
        gn.setName(getLabel());
        HandlerUtils.logIdentifiableCreation(gn);

        Result<GroupNodeType> t =
                GroupNodeType.fromValue(getHandledElementTypeIds().get(0));
        if (t.isEmpty()) {
            return Result.empty(t.getProblems());
        }

        GroupNodeType type = t.get();

        switch (type) {
            case OR_NODE:
                gn.setCardinality(CardinalityUtils.createOrCardinality());
                break;
            case XOR_NODE:
                gn.setCardinality(CardinalityUtils.createXorCardinality());
                break;
            case AND_NODE:
                gn.setCardinality(CardinalityUtils.createAndCardinality());
                break;
            case CARDINALITY_NODE:
                //            	There is no need to directly create a cardinality node when you can directly set the
                // bounds.
                break;
            default:
                break;
        }

        return Result.of(gn);
    }
}
