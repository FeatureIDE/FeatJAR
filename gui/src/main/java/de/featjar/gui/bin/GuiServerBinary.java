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
package de.featjar.gui.bin;

import de.featjar.base.env.ABinary;
import de.featjar.base.env.HostEnvironment.OperatingSystem;

public class GuiServerBinary extends ABinary {

    @Override
    public String getCategory() {
        return "gui";
    }

    @Override
    protected String getName() {
        return "server";
    }

    @Override
    protected String getOSResourceDirectory(OperatingSystem os) {
        return "";
    }
}
