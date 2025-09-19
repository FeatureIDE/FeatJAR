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

import java.nio.file.Path;

public abstract class AFeatJARAlgorithm<T> extends AJavaAlgorithm<T> {

    protected String jarName;
    protected String command;
    protected Path input;
    protected Path output;
    protected Path time;

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    protected void setCommand(String command) {
        this.command = command;
    }

    public void setInput(Path input) {
        this.input = input;
    }

    public void setOutput(Path output) {
        this.output = output;
    }

    public void setTime(Path time) {
        this.time = time;
    }

    @Override
    protected void addCommandElements() {
        super.addCommandElements();
        addCommandElement("-jar");
        addCommandElement(String.format("build/libs/%s.jar", jarName));
        addCommandElement("--command");
        addCommandElement(command);
        addCommandElement("--log-info");
        addCommandElement("message");
        addCommandElement("--log-error");
        addCommandElement("error");
        addCommandElement("--input");
        addCommandElement(input.toString());
        addCommandElement("--output");
        addCommandElement(output.toString());
        addCommandElement("--write-time-to-file");
        addCommandElement((time != null ? time : output.resolveSibling("time")).toString());
    }
}
