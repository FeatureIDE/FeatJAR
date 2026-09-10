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
import de.featjar.base.env.Process;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class D4Binary extends ABinary {

    @Override
    public String getCategory() {
        return "solver";
    }

    @Override
    protected String getName() {
        return "d4";
    }

    @Override
    public Optional<String> getExecutableName() {
        final OperatingSystem os = HostEnvironment.OPERATING_SYSTEM;
        return switch (os) {
            case WINDOWS -> Optional.of("d4.exe");
            case MAC_OS, LINUX -> Optional.of("d4");
            case UNKNOWN -> Optional.empty();
            default -> throw new IllegalStateException("Unexpected value: " + os);
        };
    }

    @Override
    public Process getProcess(List<String> arguments, Duration timeout) {
        final Optional<Path> executablePath = getExecutablePath();
        if (executablePath.isEmpty()) {
            throw new UnsupportedOperationException("No executable available");
        } else {
            return HostEnvironment.isWindows()
                    ? new Process(getExecutablePath().get(), arguments, timeout)
                    : new Process(
                            getExecutablePath().get(),
                            arguments,
                            Map.of("LD_LIBRARY_PATH", getDirectory().toString()),
                            timeout);
        }
    }
}
