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

import de.featjar.gui.action.handler.ClientMessageHandler;
import de.featjar.gui.action.handler.ExitHandler;
import de.featjar.gui.action.handler.SaveHandler;
import de.featjar.gui.action.handler.SelectionHandler;
import de.featjar.gui.model.FeatureModelGModelFactory;
import de.featjar.gui.model.FeatureModelLayoutEngine;
import de.featjar.gui.model.FeatureModelSourceModelStorage;
import de.featjar.gui.operation.handler.CreateConstraintHandler;
import de.featjar.gui.operation.handler.DeleteIdentifiableNodeHandler;
import de.featjar.gui.operation.handler.LabelEditHandler;
import de.featjar.gui.operation.handler.SetCardinalityFeatureBoundsHandler;
import de.featjar.gui.operation.handler.SetCardinalityGroupNodeBoundsHandler;
import de.featjar.gui.operation.handler.SetFeatureImplementationHandler;
import de.featjar.gui.operation.handler.SetGroupNodeTypeHandler;
import de.featjar.gui.operation.handler.create.feature.CreateMandatoryFeatureNodeHandler;
import de.featjar.gui.operation.handler.create.feature.CreateMultipleFeatureNodeHandler;
import de.featjar.gui.operation.handler.create.feature.CreateOptionalFeatureNodeHandler;
import de.featjar.gui.operation.handler.create.group.CreateAndGroupNodeHandler;
import de.featjar.gui.operation.handler.create.group.CreateCardinalityGroupNodeHandler;
import de.featjar.gui.operation.handler.create.group.CreateOrGroupNodeHandler;
import de.featjar.gui.operation.handler.create.group.CreateXorGroupNodeHandler;
import de.featjar.gui.utils.FeatureModelIdGenerator;
import de.featjar.gui.utils.FeatureModelLabelEditValidator;
import de.featjar.gui.utils.FeatureModelToolPaletteItemProvider;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.di.MultiBinding;
import org.eclipse.glsp.server.diagram.DiagramConfiguration;
import org.eclipse.glsp.server.emf.EMFIdGenerator;
import org.eclipse.glsp.server.emf.EMFSourceModelStorage;
import org.eclipse.glsp.server.emf.notation.EMFNotationDiagramModule;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.features.directediting.LabelEditValidator;
import org.eclipse.glsp.server.features.toolpalette.ToolPaletteItemProvider;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.operations.OperationHandler;

/**
 * Determines which classes (bindings) are used.
 */
public class FeatureModelDiagramModule extends EMFNotationDiagramModule {

    @Override
    protected Class<? extends DiagramConfiguration> bindDiagramConfiguration() {
        return FeatureModelDiagramConfiguration.class;
    }

    @Override
    protected Class<? extends EMFSourceModelStorage> bindSourceModelStorage() {
        return FeatureModelSourceModelStorage.class;
    }

    @Override
    public Class<? extends GModelFactory> bindGModelFactory() {
        return FeatureModelGModelFactory.class;
    }

    @Override
    protected Class<? extends EMFIdGenerator> bindEMFIdGenerator() {
        return FeatureModelIdGenerator.class;
    }

    @Override
    protected Class<? extends ToolPaletteItemProvider> bindToolPaletteItemProvider() {
        return FeatureModelToolPaletteItemProvider.class;
    }

    @Override
    protected Class<? extends LabelEditValidator> bindLabelEditValidator() {
        return FeatureModelLabelEditValidator.class;
    }

    @Override
    protected Class<? extends LayoutEngine> bindLayoutEngine() {
        return FeatureModelLayoutEngine.class;
    }

    @Override
    protected void configureActionHandlers(final MultiBinding<ActionHandler> bindings) {
        super.configureActionHandlers(bindings);
        bindings.add(SelectionHandler.class);
        bindings.add(ClientMessageHandler.class);
        bindings.add(ExitHandler.class);
        bindings.add(SaveHandler.class);
    }

    @Override
    protected void configureOperationHandlers(final MultiBinding<OperationHandler<?>> binding) {
        super.configureOperationHandlers(binding);
        binding.add(CreateMandatoryFeatureNodeHandler.class);
        binding.add(CreateOptionalFeatureNodeHandler.class);
        binding.add(CreateMultipleFeatureNodeHandler.class);

        binding.add(CreateOrGroupNodeHandler.class);
        binding.add(CreateXorGroupNodeHandler.class);
        binding.add(CreateAndGroupNodeHandler.class);
        binding.add(CreateCardinalityGroupNodeHandler.class);

        binding.add(CreateConstraintHandler.class);
        binding.add(DeleteIdentifiableNodeHandler.class);
        binding.add(LabelEditHandler.class);

        binding.add(SetFeatureImplementationHandler.class);
        binding.add(SetGroupNodeTypeHandler.class);

        binding.add(SetCardinalityGroupNodeBoundsHandler.class);
        binding.add(SetCardinalityFeatureBoundsHandler.class);
    }

    @Override
    public String getDiagramType() {
        return "featuremodel-diagram";
    }
}
