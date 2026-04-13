/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.log;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Progress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Prints a progress message at regular intervals.
 *
 * @author Sebastian Krieter
 */
public final class ProgressThread extends Thread implements IProgressBar, AutoCloseable {

    private static class Tracking {
        private final Progress progress;
        private final List<Supplier<String>> messageSupplier;

        public Tracking(Progress progress, List<Supplier<String>> messageSupplier) {
            this.progress = progress;
            this.messageSupplier = messageSupplier;
        }
    }

    private final LinkedList<Tracking> trackingStack = new LinkedList<>();

    private int refreshRate;

    private boolean running = true;

    /**
     * Creates a new progress thread.
     * @param refreshRate the number of milliseconds between new messages.
     */
    public ProgressThread(int refreshRate) {
        super();
        this.refreshRate = refreshRate;
        start();
    }

    /**
     * Set the number of milliseconds between new messages.
     * @param refreshRate the refreshRate in milliseconds.
     */
    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    @Override
    public void track(Progress progress, List<IMessage> messageSuppliers) {
        synchronized (this) {
            trackingStack.push(
                    new Tracking(progress, messageSuppliers != null ? new ArrayList<>(messageSuppliers) : List.of()));
        }
    }

    @Override
    public void untrack() {
        synchronized (this) {
            trackingStack.pop();
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                try {
                    StringBuilder sb = null;
                    synchronized (this) {
                        Tracking tracking = trackingStack.peek();
                        if (tracking != null) {
                            if (tracking.progress.isFinished()) {
                                untrack();
                            } else {
                                sb = new StringBuilder();
                                for (Supplier<String> m : tracking.messageSupplier) {
                                    sb.append(m.get());
                                    sb.append(" | ");
                                }
                            }
                        }
                    }
                    if (sb != null) {
                        if (sb.length() > 0) {
                            sb.delete(sb.length() - 3, sb.length());
                        }
                        FeatJAR.log().progress(sb.toString());
                    }
                } catch (Exception e) {
                    untrack();
                }
                Thread.sleep(refreshRate);
            }
        } catch (InterruptedException e) {
            FeatJAR.log().error(e);
        }
    }

    /**
     * Stops this progress thread.
     */
    public void shutdown() {
        running = false;
    }

    @Override
    public void close() {
        shutdown();
    }
}
