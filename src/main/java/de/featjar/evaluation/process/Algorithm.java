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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Algorithm<R> implements IAlgorithm<R> {

    protected final ArrayList<String> commandElements = new ArrayList<>();

    public void postProcess() throws Exception {}

    @Override
    public void readOutput(String line) throws Exception {}

    public Result<R> parseResults() throws IOException {
        return Result.empty();
    }

    public String getName() {
        return getClass().getName();
    }

    public String getParameterSettings() {
        return "";
    }

    public void preProcess() throws Exception {
        commandElements.clear();
        addCommandElements();
    }

    protected abstract void addCommandElements() throws Exception;

    public void addCommandElement(String parameter) {
        commandElements.add(parameter);
    }

    public List<String> getCommandElements() {
        return commandElements;
    }

    public String getCommand() {
        final StringBuilder commandBuilder = new StringBuilder();
        for (final String commandElement : commandElements) {
            commandBuilder.append(commandElement);
            commandBuilder.append(' ');
        }
        return commandBuilder.toString();
    }

    public String getFullName() {
        return getName() + "_" + getParameterSettings();
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
