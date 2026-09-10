/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-cadical.
 *
 * formula-analysis-cadical is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-cadical is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-cadical. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-cadical> for further information.
 */
package de.featjar.analysis.cadical.bin;

import de.featjar.base.env.ABinary;
import de.featjar.base.env.HostEnvironment;
import java.util.Optional;

public class CadiBackBinary extends ABinary {

    @Override
    public String getCategory() {
        return "solver";
    }

    @Override
    protected String getName() {
        return "cadiback";
    }

    @Override
    public Optional<String> getExecutableName() {
        return HostEnvironment.isWindows() ? Optional.empty() : Optional.of("cadiback");
    }
}
