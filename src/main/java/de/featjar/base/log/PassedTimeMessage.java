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

/**
 * Returns the passed time since creating this message supplier.
 * The output format is: {@code dd hh:mm:ss} or {@code " > 99 days "}.
 *
 * @author Sebastian Krieter
 */
public final class PassedTimeMessage implements IMessage {

    private long startTime;

    /**
     * Creates a new message supplier.
     */
    public PassedTimeMessage() {
        startTime = System.currentTimeMillis() / 1000L;
    }

    public String get() {
        long curTime = System.currentTimeMillis() / 1000L;

        long curTimeDiff = curTime - startTime;
        int day = (int) (curTimeDiff / 86400L);
        if (day > 99) {
            return " > 99 days ";
        } else {
            int sec = (int) (curTimeDiff % 60L);
            int min = (int) ((curTimeDiff / 60L) % 60L);
            int hour = (int) ((curTimeDiff / 3600L) % 24L);
            return String.format("%02d %02d:%02d:%02d", day, hour, min, sec);
        }
    }
}
