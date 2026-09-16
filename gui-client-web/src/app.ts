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
import 'reflect-metadata';

import {
    BaseJsonrpcGLSPClient,
    DiagramLoader,
    GLSPActionDispatcher,
    GLSPClient,
    GLSPWebSocketProvider,
    MessageAction,
    StatusAction
} from '@eclipse-glsp/client';
import { Container } from 'inversify';
import { join, resolve } from 'path';
import { MessageConnection } from 'vscode-jsonrpc';
import createContainer from './di.config';
import { ExitAction } from './client-exit-action';
import { SaveAction } from './client-save-action';
import { initializeNodeContextMenu } from './node-context-menu';
import { ClientMessageAction } from './client-message-action';

/**
 * The entry point of the feature model diagram client.
 * Establishes the WebSocket connection, creates the container, and
 * loads the diagram. When reconnecting, the previous container is discarded before a
 * new one is created, because GLSP registers its UI extensions at startup and
 * cannot load the same container twice.
 */

const HOST = GLSP_SERVER_HOST;
const PORT = GLSP_SERVER_PORT;

// see FeatureModelDiagramModule.java
const DIAGRAM_TYPE = 'featuremodel-diagram';
// The server uses 'featuremodel' as the WebSocket endpoint
// see in FeatureModelServerLauncher.java "/featuremodel"
const ENDPOINT_ID = 'featuremodel';
const MODEL_FILE = 'gui_model' + '.' + ENDPOINT_ID;

const loc = window.location.pathname;
const CLIENT_PATH = loc.substring(0, loc.lastIndexOf('/'));
const CLIENT_ABSOLUTE_EMF_FILE_PATH = resolve(join(CLIENT_PATH, '..', 'app', MODEL_FILE));
const CLIENT_ID = 'sprotty';

// The server is listening on /featuremodel
const WEBSOCKET_URL = `ws://${HOST}:${PORT}/${ENDPOINT_ID}`;

let glspClient: GLSPClient;

// The container is created once and reused across reconnects.
let container: Container | undefined;

// Guards against overlapping initialize() runs if there are several reconnect attempts
let initializing = false;

// Ensures the global key bindings are registered only once.
let keyBindingsInstalled = false;

const PING_INTERVAL_MS = 10000;

let pingTimer: ReturnType<typeof setInterval> | undefined;

const wsProvider = new GLSPWebSocketProvider(WEBSOCKET_URL, {
    reconnecting: true,
    // Short delay such that the connection can come back quickly after a loss.
    reconnectDelay: 2 * 1000
});

let featureContextMenuinitialized = false;

wsProvider.listen({ onConnection: initialize, onReconnect: reconnect, logger: console });

/**
 * Sets up the diagram for a new connection.
 *
 * Creates the GLSP client, builds a fresh container and loads the diagram.
 *
 * @param isReconnecting whether it is the initial startup or a reconnect.
 * @param connectionProvider the message connection of the websocket. The transportation channel
 * encapsules the JSON-RPC connection, a new one for every reconnect.
 */
async function initialize(connectionProvider: MessageConnection, isReconnecting = false): Promise<void> {
    if (initializing) {
        console.log('initialize() already running, skipping duplicate call');
        return;
    }
    initializing = true;

    try {
        console.log('Initializing Feature Model GLSP Client...');
        console.log('Diagram Type:', DIAGRAM_TYPE);
        console.log('WebSocket URL:', WEBSOCKET_URL);
        console.log('Source URI:', CLIENT_ABSOLUTE_EMF_FILE_PATH);

        // A fresh client for the new connection. The container gets reassigned if it has been created once.
        glspClient = new BaseJsonrpcGLSPClient({ id: ENDPOINT_ID, connectionProvider });

        if (!container) {
            container = createContainer({
                clientId: CLIENT_ID,
                diagramType: DIAGRAM_TYPE,
                glspClientProvider: async () => glspClient,
                sourceUri: CLIENT_ABSOLUTE_EMF_FILE_PATH
            });
        }

        if (!featureContextMenuinitialized) {
            initializeNodeContextMenu(container.get(GLSPActionDispatcher));
            featureContextMenuinitialized = true;
        }

        const actionDispatcher = container.get(GLSPActionDispatcher);
        const diagramLoader = container.get(DiagramLoader);

        installKeyBindings(actionDispatcher);

        await diagramLoader.load({
            requestModelOptions: { isReconnecting },
            enableNotifications: true
        });

        startKeepalivePing(actionDispatcher);

        if (isReconnecting) {
            const message = `Connection to the ${ENDPOINT_ID} glsp server got closed. Connection was successfully re-established.`;
            const timeout = 5000;
            const severity = 'WARNING';
            actionDispatcher.dispatchAll([
                StatusAction.create(message, { severity, timeout }),
                MessageAction.create(message, { severity })
            ]);
        }
    } finally {
        initializing = false;
    }
}

/**
 * Rebuilds the diagram after the connection has been lost.
 *
 * The keepalive timer is initially paused such that no data is sent to a closed
 * socket. The old container is disposed while the connection is still
 * open.
 *
 * @param connectionProvider the message connection of the restored websocket
 */
async function reconnect(connectionProvider: MessageConnection): Promise<void> {
    stopKeepalivePing();
    console.log('reconnect() called');

    disposeContainer();

    try {
        await glspClient.stop();
    } catch (error) {
        console.warn('Stopping the old GLSP client failed:', error);
    }

    await initialize(connectionProvider, true);
}

/**
 * Restarts the keepalive timer such that exactly one timer is active and always uses
 * the current action dispatcher.
 */
function startKeepalivePing(actionDispatcher: GLSPActionDispatcher): void {
    stopKeepalivePing();
    pingTimer = setInterval(() => {
        actionDispatcher.dispatch(ClientMessageAction.create('Keep connection alive ping'));
    }, PING_INTERVAL_MS);
}

function stopKeepalivePing(): void {
    if (pingTimer) {
        clearInterval(pingTimer);
        pingTimer = undefined;
    }
}

/**
 * Disposes the old container by unbinding all its connections and clearing the DOM.
 */
function disposeContainer(): void {
    if (!container) {
        return;
    }
    try {
        container.unbindAll();
    } catch (error) {
        console.warn('Disposing the previous container failed:', error);
    }
    document.getElementById('sprotty')?.replaceChildren();
    container = undefined;
}

/**
 * Registers the global shortcuts once. The handler reads the dispatcher from the
 * container on each key press, so it stays valid across reconnects.
 */
function installKeyBindings(actionDispatcher: GLSPActionDispatcher): void {
    if (keyBindingsInstalled) {
        return;
    }
    keyBindingsInstalled = true;

    document.addEventListener('keydown', (event: KeyboardEvent) => {
        const dispatcher = container?.get(GLSPActionDispatcher) ?? actionDispatcher;

        if (event.ctrlKey && event.altKey && event.code === 'KeyE') {
            event.preventDefault();
            dispatcher.dispatch(ExitAction.create());
        } else if (event.ctrlKey && event.altKey && event.code === 'KeyS') {
            event.preventDefault();
            dispatcher.dispatch(SaveAction.create());
        }
    });
}
