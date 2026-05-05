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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class StreamRedirector implements Runnable {

    private final List<IOutputReader> outputReaderList;
    private InputStream in;

    public StreamRedirector(List<IOutputReader> outputReaderList) {
        this.outputReaderList = outputReaderList;
    }

    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                for (final IOutputReader outputReader : outputReaderList) {
                    try {
                        outputReader.readOutput(line);
                    } catch (final Exception e) {
                    }
                }
            }
        } catch (final IOException e) {
            FeatJAR.log().error(e);
        }
    }
}
