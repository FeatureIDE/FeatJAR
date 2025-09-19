/*
 * Copyright (C) 2025 FeatJAR-Development-Team
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
package de.featjar.evaluation.process;

import de.featjar.base.data.Result;
import de.featjar.evaluation.streams.IOutputReader;
import java.io.IOException;
import java.util.List;

public interface IAlgorithm<R> extends IOutputReader {

    void postProcess() throws Exception;

    Result<R> parseResults() throws IOException;

    String getName();

    String getParameterSettings();

    void preProcess() throws Exception;

    List<String> getCommandElements();

    String getCommand();

    String getFullName();
}
