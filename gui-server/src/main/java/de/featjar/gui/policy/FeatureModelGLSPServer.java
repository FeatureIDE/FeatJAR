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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.glsp.server.protocol.DefaultGLSPServer;
import org.eclipse.glsp.server.protocol.InitializeResult;

/**
 * Placeholder class that could be used to send args from the client to the server e.g. the
 * file path of CLIENT_ABSOLUTE_EMF_FILE_PATH from {@link FeatureModelGuiCommand}
 *
 */
public class FeatureModelGLSPServer extends DefaultGLSPServer {
    @Override
    public CompletableFuture<InitializeResult> handleIntializeArgs(
            final InitializeResult result, final Map<String, String> args) {
        CompletableFuture<InitializeResult> completableResult = CompletableFuture.completedFuture(result);
        if (args.isEmpty()) {
            return completableResult;
        }

        return completableResult;
    }
}
