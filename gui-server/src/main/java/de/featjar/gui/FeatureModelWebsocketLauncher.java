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
package de.featjar.gui;

import de.featjar.base.FeatJAR;
import de.featjar.gui.policy.FeatureModelDiagramModule;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.glsp.layout.ElkLayoutEngine;
import org.eclipse.glsp.server.di.ServerModule;
import org.eclipse.glsp.server.launch.DefaultCLIParser;
import org.eclipse.glsp.server.websocket.WebsocketServerLauncher;

/**
 * Starts the graphical language server on a websocket endpoint.
 *
 * Runs as its own process, initiated by {@link FeatureModelGuiCommand}.
 * Standard output is redirected to a log file.
 */
public final class FeatureModelWebsocketLauncher {

    public static final String SIGNAL_START = "server_start";
    public static final String SIGNAL_STOP = "server_stop";
    public static final String SIGNAL_SAVE = "server_save";

    public static final int DEFAULT_PORT = 8081;

    private static final String PROCESS_NAME = "FeatureModelGlspServer";
    private static final String ENDPOINT_PATH = "/featuremodel";

    private static final int MAX_LOG_FILE_SIZE = 1024 * 50; // 50 KiB
    private static final Path LOG_FILE_PATH = Path.of("server_logs.log");

    public static PrintStream out;

    public static void main(String[] args) throws Exception {
        createLogFile();
        out = System.out;
        System.setOut(new PrintStream(LOG_FILE_PATH.toFile()));

        if (!FeatJAR.isInitialized()) {
            FeatJAR.allConfiguration().initialize();
        }

        DefaultCLIParser parser = new DefaultCLIParser(args, PROCESS_NAME);

        String hostname = parser.parseHostname();
        int parsedPort = parser.parsePort();
        int port = parsedPort == 0 ? DEFAULT_PORT : parsedPort;
        ServerModule configureDiagramModule =
                new ServerModule().configureDiagramModule(new FeatureModelDiagramModule());

        ElkLayoutEngine.initialize(new LayeredMetaDataProvider());
        WebsocketServerLauncher launcher = new WebsocketServerLauncher(configureDiagramModule, ENDPOINT_PATH);
        out.println(SIGNAL_START);
        // TODO improve error handling
        launcher.start(hostname, port, parser);
        out.println(SIGNAL_STOP);
    }

    private static void createLogFile() throws IOException {
        if (Files.exists(LOG_FILE_PATH)) {
            if (Files.size(LOG_FILE_PATH) > MAX_LOG_FILE_SIZE) {
                Files.delete(LOG_FILE_PATH);
                Files.createFile(LOG_FILE_PATH);
            }
        } else {
            Files.createFile(LOG_FILE_PATH);
        }
    }
}
