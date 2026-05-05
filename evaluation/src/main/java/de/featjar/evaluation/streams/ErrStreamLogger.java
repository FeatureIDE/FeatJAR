/*
 * Copyright (C) 2026 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-evaluation.
 *
 * evaluation is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * evaluation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with evaluation. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-evaluation> for further information.
 */
package de.featjar.evaluation.streams;

import de.featjar.base.FeatJAR;

public class ErrStreamLogger implements IOutputReader {

    private String source;
    private boolean errorOccured;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isErrorOccured() {
        return errorOccured;
    }

    @Override
    public void readOutput(String line) throws Exception {
        if (!errorOccured) {
            errorOccured = true;
            if (source != null) {
                FeatJAR.log().error("Error for: %s", source);
            }
        }
        FeatJAR.log().error(line);
    }
}
