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

import de.featjar.base.data.Sets;
import de.featjar.base.env.ABinary;
import de.featjar.base.env.HostEnvironment;
import de.featjar.base.env.Process;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class D4Binary extends ABinary {
    public D4Binary() throws IOException {}

    @Override
    public String getExecutableName() {
        return HostEnvironment.isWindows() ? "d4.exe" : "d4";
    }

    @Override
    public LinkedHashSet<String> getResourceNames() {
        return HostEnvironment.isWindows()
                ? Sets.of(
                        "d4.exe",
                        "libarjun.dll",
                        "libcryptominisat5.dll",
                        "libgcc_s_seh-1.dll",
                        "libglucose.dll",
                        "libgmp-10.dll",
                        "libgmpxx-4.dll",
                        "libhwloc-15.dll",
                        "libmcfgthread-1.dll",
                        "libmtkahypar.dll",
                        "libsbva.dll",
                        "libstdc++-6.dll",
                        "libtbb12.dll",
                        "libtbbmalloc.dll",
                        "libtbbmalloc_proxy.dll")
                : Sets.of(
                        "d4",
                        "libhwloc.def",
                        "libhwloc.dll.a",
                        "libhwloc.la",
                        "libhwloc.so",
                        "libhwloc.so.15",
                        "libhwloc.so.15.8.1",
                        "libmtkahypar.dll.a",
                        "libmtkahypar.so",
                        "libtbb12.dll.a",
                        "libtbb.dll.a",
                        "libtbbmalloc.dll.a",
                        "libtbbmalloc_proxy.dll.a",
                        "libtbbmalloc_proxy.so",
                        "libtbbmalloc_proxy.so.2",
                        "libtbbmalloc_proxy.so.2.11",
                        "libtbbmalloc.so",
                        "libtbbmalloc.so.2",
                        "libtbbmalloc.so.2.11",
                        "libtbb.so",
                        "libtbb.so.12",
                        "libtbb.so.12.11");
    }

    @Override
    public Process getProcess(List<String> arguments, Duration timeout) {
        if (HostEnvironment.isWindows()) {
            return new Process(getExecutablePath(), arguments, timeout);
        } else {
            return new Process(
                    getExecutablePath(), arguments, Map.of("LD_LIBRARY_PATH", BINARY_DIRECTORY.toString()), timeout);
        }
    }
}
