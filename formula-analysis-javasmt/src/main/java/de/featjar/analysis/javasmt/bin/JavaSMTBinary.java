/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-javasmt.
 *
 * formula-analysis-javasmt is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-javasmt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-javasmt. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-javasmt> for further information.
 */
package de.featjar.analysis.javasmt.bin;

import de.featjar.base.env.ABinary;
import de.featjar.base.env.HostEnvironment.OperatingSystem;
import java.io.IOException;
import java.lang.reflect.Field;
import org.sosy_lab.common.NativeLibraries;

public class JavaSMTBinary extends ABinary {

    public JavaSMTBinary() throws IOException {
        super();
        Field nativePathField;
        try {
            nativePathField = NativeLibraries.class.getDeclaredField("nativePath");
            nativePathField.setAccessible(true);
            nativePathField.set(null, getDirectory());
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    @Override
    public String getCategory() {
        return "solver";
    }

    @Override
    protected String getName() {
        return "javasmt";
    }

    protected String getOSResourceDirectory(OperatingSystem os) {
        return switch (os) {
            case WINDOWS -> "win";
            case MAC_OS -> "mac";
            case LINUX -> "unix";
            case UNKNOWN -> "unkown";
            default -> throw new IllegalStateException("Unexpected value: " + os);
        };
    }
}
