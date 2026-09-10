/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-ddnnife.
 *
 * formula-analysis-ddnnife is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-ddnnife is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-ddnnife. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/formula-analysis-ddnnife> for further information.
 */
package de.featjar.analysis.ddnnife.bin;

import de.featjar.base.env.ABinary;
import de.featjar.base.env.HostEnvironment;
import de.featjar.base.env.HostEnvironment.OperatingSystem;
import java.util.Optional;

public class DdnnifeBinary extends ABinary {

    @Override
    public String getCategory() {
        return "solver";
    }

    @Override
    protected String getName() {
        return "ddnnife";
    }

    @Override
    public Optional<String> getExecutableName() {
        final OperatingSystem os = HostEnvironment.OPERATING_SYSTEM;
        return switch (os) {
            case WINDOWS -> Optional.of("ddnnife.exe");
            case MAC_OS, LINUX -> Optional.of("ddnnife");
            case UNKNOWN -> Optional.empty();
            default -> throw new IllegalStateException("Unexpected value" + os);
        };
    }
}
