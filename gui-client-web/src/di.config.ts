/********************************************************************************
 * Copyright (c) 2019-2024 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/

import { createWorkflowDiagramContainer } from './dependencies/workflow-diagram-module';
import {
    ConsoleLogger,
    EditMode,
    GEdge,
    IDiagramOptions,
    LogLevel,
    STANDALONE_MODULE_CONFIG,
    TYPES,
    accessibilityModule,
    bindOrRebind,
    configureModelElement,
    createDiagramOptionsModule,
    overrideModelElement,
    toolPaletteModule,
    DefaultTypes,
    GNode,
    GLabel,
    GLabelView,
    editLabelFeature,
    contextMenuModule
} from '@eclipse-glsp/client';
import { Container } from 'inversify';
import { makeLoggerMiddleware } from 'inversify-logger-middleware';
import { FeatureNodeView } from './feature-node-view';
import { FeatureCardinalityEdgeView } from './feature-edge-view';
import { getParameters } from './url-parameters';
import { SessionManagementPanel } from './session-management-panel';
import { FeatureSearchProvider } from './feature-search-provider';

import '../css/diagram.css';
import '../css/command-palette.css';

/**
 * Builds the  container for the diagram.
 *
 * Registers the views, element types, and services that determine how the
 * diagram is rendered and how the user interacts with it.
 *
 * @param options connection and diagram settings passed from the entry point
 * @returns the container used for this connection
 */
export default function createContainer(options: IDiagramOptions): Container {
    const parameters = getParameters();
    if (parameters.readonly) {
        options.editMode = EditMode.READONLY;
    }
    // The tool palette module is replaced by the server-side palette because the entries
    // come from the server instead. The contextMenuModule could have produced a reconnect bug
    // and since it is only used for IDE integration, it is removed.
    const container = createWorkflowDiagramContainer(
        createDiagramOptionsModule(options),
        {
            add: [accessibilityModule],
            remove: [toolPaletteModule, contextMenuModule]
        },
        STANDALONE_MODULE_CONFIG
    );

    // Context for configureModelElement / overrideModelElement
    const ctx = {
        bind: container.bind.bind(container),
        isBound: container.isBound.bind(container)
    };

    // own view for feature nodes which draws curves
    overrideModelElement(ctx, DefaultTypes.NODE, GNode, FeatureNodeView);

    // constraints are the only 'special' type of nodes because they have a box
    configureModelElement(ctx, 'constraint-box', GNode, FeatureNodeView);

    configureModelElement(ctx, 'edge-mandatory', GEdge, FeatureCardinalityEdgeView);
    configureModelElement(ctx, 'edge-optional', GEdge, FeatureCardinalityEdgeView);
    overrideModelElement(ctx, 'edge', GEdge, FeatureCardinalityEdgeView);

    configureModelElement(ctx, 'label-heading', GLabel, GLabelView, {
        enable: [editLabelFeature]
    });

    // Session management panel

    container.bind(SessionManagementPanel).toSelf().inSingletonScope();
    container.bind(TYPES.IUIExtension).toService(SessionManagementPanel);
    container.bind(TYPES.IDiagramStartup).toService(SessionManagementPanel);

    // Command palette search
    container.bind(TYPES.ICommandPaletteActionProvider).to(FeatureSearchProvider).inSingletonScope();

    // Cardinality labels
    configureModelElement(ctx, 'label-edge-cardinality', GLabel, GLabelView);
    configureModelElement(ctx, 'label-node-cardinality', GLabel, GLabelView);

    bindOrRebind(container, TYPES.ILogger).to(ConsoleLogger).inSingletonScope();
    bindOrRebind(container, TYPES.LogLevel).toConstantValue(LogLevel.warn);
    container.bind(TYPES.IMarqueeBehavior).toConstantValue({ entireEdge: true, entireElement: true });

    if (parameters.inversifyLog) {
        configureInversifyLogger(container);
    }
    return container;
}

function configureInversifyLogger(container: Container): void {
    const logOptions = {
        request: {
            bindings: {
                activated: true,
                cache: false,
                constraint: false,
                dynamicValue: false,
                factory: false,
                implementationType: true,
                onActivation: false,
                provider: false,
                scope: true,
                serviceIdentifier: true,
                type: false
            },
            serviceIdentifier: true,
            target: {
                metadata: true,
                name: false,
                serviceIdentifier: true
            }
        },
        time: true
    };

    const logger = makeLoggerMiddleware(logOptions);
    container.applyMiddleware(logger);
}
