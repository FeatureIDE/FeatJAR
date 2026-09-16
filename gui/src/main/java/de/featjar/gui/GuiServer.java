/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-gui.
 *
 * gui is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * gui is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gui. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE> for further information.
 */
package de.featjar.gui;

import de.featjar.base.FeatJAR;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Starts a GUI for modifying a feature model.
 *
 * Starts the language server as a separate process.
 * The server runs detached so that a crash does not cause FeatJar to crash.
 * Output signals issued by the client are written to the server's standard output.
 *
 * @author Niclas Kleinert
 * @author Sebastian Krieter
 */
public class GuiServer {

    private boolean serverStarted;
    private boolean serverStopped;

    private Runnable startHook;
    private Runnable stopHook;
    private Consumer<String> signalHook;

    public void setStartHook(Runnable startHook) {
        this.startHook = startHook;
    }

    public void setStopHook(Runnable stopHook) {
        this.stopHook = stopHook;
    }

    public void setSignalHook(Consumer<String> signalHook) {
        this.signalHook = signalHook;
    }

    public boolean run(int port) {
        Process server = null;
        List<String> command = Arrays.asList(
                "java",
                "-cp",
                System.getProperty("user.home") + "/.featjar-bin/gui/server/*",
                "de.featjar.gui.FeatureModelWebsocketLauncher",
                "-p " + String.valueOf(port));

        FeatJAR.log().info("Server is starting...");
        ProcessBuilder pb = new ProcessBuilder(command);

        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            server = pb.start();

            final Process processForListeningThread = server;
            Thread listeningThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(processForListeningThread.getInputStream(), StandardCharsets.UTF_8))) {
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        if (processServerSignals(line)) {
                            return;
                        }
                    }
                } catch (final IOException e) {
                    FeatJAR.log().error(e);
                } finally {
                    processServerSignals(FeatureModelWebsocketLauncher.SIGNAL_STOP);
                }
            });
            listeningThread.start();
            waitForStartSignal();

            if (!server.isAlive()) {
                return false;
            }
            FeatJAR.log().info("Server is runnig and listens on port %d", port);
            if (startHook != null) {
                startHook.run();
            }

            Thread listenForUserThread = new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if ("exit".equalsIgnoreCase(line.trim())
                                || FeatureModelWebsocketLauncher.SIGNAL_STOP.equalsIgnoreCase(line)) {
                            processServerSignals(FeatureModelWebsocketLauncher.SIGNAL_STOP);
                            return;
                        } else if ("save".equalsIgnoreCase(line.trim())
                                || FeatureModelWebsocketLauncher.SIGNAL_SAVE.equalsIgnoreCase(line)) {
                            processServerSignals(FeatureModelWebsocketLauncher.SIGNAL_SAVE);
                        }
                    }
                } catch (IOException e) {
                }
            });
            listenForUserThread.start();

            waitForStopSignal();
            return true;
        } catch (IOException e) {
            FeatJAR.log().error(e);
            return false;
        } finally {
            FeatJAR.log().info("Server is shutting down...");
            if (server != null) {
                server.destroy();
            }
            if (stopHook != null) {
                stopHook.run();
            }
        }
    }

    private synchronized boolean processServerSignals(String signal) {
        switch (signal) {
            case FeatureModelWebsocketLauncher.SIGNAL_STOP:
                serverStarted = true;
                serverStopped = true;
                notifyAll();
                return true;
            case FeatureModelWebsocketLauncher.SIGNAL_START:
                serverStarted = true;
                notifyAll();
                return false;
            case FeatureModelWebsocketLauncher.SIGNAL_SAVE:
            default:
                notifyAll();
                if (signalHook != null) {
                    signalHook.accept(signal);
                }
                return false;
        }
    }

    private synchronized void waitForStartSignal() {
        while (!serverStarted) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                FeatJAR.log().error(e);
            }
        }
    }

    private synchronized void waitForStopSignal() {
        while (!serverStopped) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                FeatJAR.log().error(e);
            }
        }
    }
}
